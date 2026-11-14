package show.twilight.finale.client.event;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.client.animation.TwilightSwordPlayerAnimation;
import show.twilight.finale.item.TwilightSwordItem;
import show.twilight.finale.network.TwilightSwordStatePacket;

import java.util.*;

@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID, value = Dist.CLIENT)
public final class TwilightSwordClientEffects {
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final Map<UUID, History> HISTORIES = new HashMap<>();
    private static final Map<PlayerRenderer, PlayerModel<AbstractClientPlayer>> GHOST_MODELS = new WeakHashMap<>();
    private static ClientLevel lastLevel;

    private record State(int mode, long received) {
    }

    private record Bone(PartPose pose, float xScale, float yScale, float zScale, boolean visible, boolean skipDraw) {
    }

    private record Frame(Vec3 position, float yaw, float scale, double offsetY, float age, List<Bone> bones) {
    }

    private static final class History {
        final List<Frame> frames = new ArrayList<>();
        float lastSample = Float.NEGATIVE_INFINITY;
        long updated;
    }

    public static void apply(TwilightSwordStatePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        resetLevel(mc.level);
        if (!(mc.level.getEntity(packet.entityId()) instanceof AbstractClientPlayer player)
                || !player.getUUID().equals(packet.playerId())) return;
        TwilightSwordPlayerAnimation.update(player, packet.mode());
        State previous = STATES.get(packet.playerId());
        if (packet.mode() == TwilightSwordStatePacket.IDLE) {
            STATES.remove(packet.playerId());
        } else {
            STATES.put(packet.playerId(), new State(packet.mode(), mc.level.getGameTime()));
            if (player == mc.player && packet.mode() == TwilightSwordStatePacket.DASHING
                    && (previous == null || previous.mode != TwilightSwordStatePacket.DASHING) && player.onGround())
                player.move(MoverType.SELF, new Vec3(0, TwilightSwordItem.GROUND_LIFT, 0));
        }
    }

    private static void resetLevel(ClientLevel level) {
        if (lastLevel == level) return;
        lastLevel = level;
        STATES.clear();
        HISTORIES.clear();
        TwilightSwordPlayerAnimation.clear();
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        resetLevel(level);
        if (level == null) return;
        TwilightSwordPlayerAnimation.tick(level);
        long now = level.getGameTime();
        STATES.entrySet().removeIf(entry -> now - entry.getValue().received > 45);
        HISTORIES.entrySet().removeIf(entry -> now - entry.getValue().updated > 12);
    }

    @SubscribeEvent
    public static void render(RenderPlayerEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player) || player.isInvisible() || !player.isAlive())
            return;
        State state = STATES.get(player.getUUID());
        boolean active = state != null && state.mode == TwilightSwordStatePacket.DASHING;
        History history = HISTORIES.get(player.getUUID());
        if (!active && history == null) return;
        if (history == null) {
            history = new History();
            HISTORIES.put(player.getUUID(), history);
        }
        float partial = event.getPartialTick(), now = player.tickCount + partial;
        history.updated = player.level().getGameTime();
        history.frames.removeIf(frame -> now < frame.age || now - frame.age >= 10);
        var source = event.getRenderer().getModel();
        if (active && now - history.lastSample >= 1) {
            List<Bone> bones = parts(source).stream().map(part ->
                    new Bone(part.storePose(), part.xScale, part.yScale, part.zScale, part.visible, part.skipDraw)).toList();
            history.frames.add(new Frame(player.getPosition(partial), Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot),
                    player.getScale(), event.getRenderer().getRenderOffset(player, partial).y, now, bones));
            history.lastSample = now;
        }
        PlayerModel<AbstractClientPlayer> ghost = GHOST_MODELS.computeIfAbsent(event.getRenderer(), renderer -> {
            boolean slim = player.getSkin().model() == PlayerSkin.Model.SLIM;
            return new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim);
        });
        var pose = event.getPoseStack();
        var vertices = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(player.getSkin().texture()));
        double currentOffset = event.getRenderer().getRenderOffset(player, partial).y;
        for (Frame frame : history.frames) {
            float age = now - frame.age;
            if (age <= 0) continue;
            List<ModelPart> bones = parts(ghost);
            for (int i = 0; i < bones.size(); i++) {
                ModelPart bone = bones.get(i);
                Bone saved = frame.bones.get(i);
                bone.loadPose(saved.pose);
                bone.xScale = saved.xScale;
                bone.yScale = saved.yScale;
                bone.zScale = saved.zScale;
                bone.visible = saved.visible;
                bone.skipDraw = saved.skipDraw;
            }
            pose.pushPose();
            try {
                Vec3 offset = frame.position.subtract(player.getPosition(partial));
                pose.translate(offset.x, offset.y + frame.offsetY - currentOffset, offset.z);
                pose.mulPose(Axis.YP.rotationDegrees(180 - frame.yaw));
                pose.scale(-frame.scale, -frame.scale, frame.scale);
                pose.scale(0.9375F, 0.9375F, 0.9375F);
                pose.translate(0, -1.501, 0);
                ghost.young = false;
                ghost.renderToBuffer(pose, vertices, event.getPackedLight(), OverlayTexture.NO_OVERLAY,
                        FastColor.ARGB32.colorFromFloat(0.5F * Math.max(0, 1 - age / 10F), 1, 1, 1));
            } finally {
                pose.popPose();
            }
        }
        if (!active && history.frames.isEmpty()) HISTORIES.remove(player.getUUID());
    }

    private static List<ModelPart> parts(PlayerModel<?> model) {
        return List.of(model.head, model.hat, model.body, model.leftArm, model.rightArm, model.leftLeg, model.rightLeg,
                model.jacket, model.leftSleeve, model.rightSleeve, model.leftPants, model.rightPants);
    }
}
