package show.twilight.finale.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.network.TwilightSwordStatePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maps the Hierophant's FIERY_THRUST pose onto vanilla player bones.
 */
public final class TwilightSwordPlayerAnimation {
    private static final Map<UUID, Motion> MOTIONS = new HashMap<>();
    private static final Pose REST = new Pose(0, 0);

    private record Pose(float weight, float thrust) {
    }

    private static final class Motion {
        final int mode;
        final float started;
        final Pose from;
        long updated;

        Motion(int mode, float started, Pose from, long updated) {
            this.mode = mode;
            this.started = started;
            this.from = from;
            this.updated = updated;
        }

        Pose sample(float age) {
            float blend = smooth((age - started) / (mode == TwilightSwordStatePacket.DASHING ? 4F : 6F));
            float targetWeight = mode == TwilightSwordStatePacket.IDLE ? 0 : 1;
            float targetThrust = switch (mode) {
                case TwilightSwordStatePacket.CHARGING -> 0;
                case TwilightSwordStatePacket.DASHING -> 1;
                default -> from.thrust;
            };
            return new Pose(Mth.lerp(blend, from.weight, targetWeight), Mth.lerp(blend, from.thrust, targetThrust));
        }
    }

    public static void update(AbstractClientPlayer player, int mode) {
        Motion previous = MOTIONS.get(player.getUUID());
        long now = player.level().getGameTime();
        if (previous != null && previous.mode == mode) {
            previous.updated = now;
            return; // Charge synchronization must not restart the pose every five ticks.
        }
        if (previous == null && mode == TwilightSwordStatePacket.IDLE) return;
        Pose from = previous == null ? REST : previous.sample(player.tickCount);
        MOTIONS.put(player.getUUID(), new Motion(mode, player.tickCount, from, now));
    }

    public static void clear() {
        MOTIONS.clear();
    }

    public static void tick(ClientLevel level) {
        long now = level.getGameTime();
        MOTIONS.entrySet().removeIf(entry -> now - entry.getValue().updated > 45
                || entry.getValue().mode == TwilightSwordStatePacket.IDLE && now - entry.getValue().updated > 6);
    }

    private static Pose pose(AbstractClientPlayer player, float age) {
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping()
                || !player.getMainHandItem().is(ModItem.TWILIGHT_SWORD.get())) return REST;
        Motion motion = MOTIONS.get(player.getUUID());
        return motion == null ? REST : motion.sample(age);
    }

    public static boolean isActive(AbstractClientPlayer player, float age) {
        return pose(player, age).weight > 0;
    }

    public static void apply(HumanoidModel<?> model, AbstractClientPlayer player, float age) {
        Pose pose = pose(player, age);
        float weight = pose.weight, thrust = pose.thrust;
        if (weight <= 0) return;

        // These are the same charge/thrust angles used by the Hierophant.
        float lean = Mth.lerp(thrust, 0.1F, 0.7F);
        float armPitch = Mth.lerp(thrust, -1.9F, -Mth.HALF_PI);
        float spread = Mth.lerp(thrust, 0.8F, 0.15F);
        float torsoY = 12 - 12 * Mth.cos(lean), torsoZ = -12 * Mth.sin(lean);
        model.body.xRot = Mth.lerp(weight, model.body.xRot, lean);
        model.body.yRot = Mth.lerp(weight, model.body.yRot, 0);
        model.body.y = Mth.lerp(weight, model.body.y, torsoY);
        model.body.z = Mth.lerp(weight, model.body.z, torsoZ);
        model.head.y = Mth.lerp(weight, model.head.y, torsoY);
        model.head.z = Mth.lerp(weight, model.head.z, torsoZ);
        model.head.xRot += lean * weight;

        // The boss's arms are children of its torso; player arms are siblings.
        // Flatten that parent rotation so the shoulders and sword still follow it.
        poseArm(model.leftArm, lean, armPitch, spread, weight);
        poseArm(model.rightArm, lean, armPitch, -spread, weight);
        model.leftLeg.xRot = Mth.lerp(weight, model.leftLeg.xRot, -0.7F * thrust);
        model.rightLeg.xRot = Mth.lerp(weight, model.rightLeg.xRot, 0.8F * thrust);
        model.leftLeg.yRot = Mth.lerp(weight, model.leftLeg.yRot, 0);
        model.rightLeg.yRot = Mth.lerp(weight, model.rightLeg.yRot, 0);
        model.leftLeg.zRot = Mth.lerp(weight, model.leftLeg.zRot, 0);
        model.rightLeg.zRot = Mth.lerp(weight, model.rightLeg.zRot, 0);
        model.hat.copyFrom(model.head);
        // PlayerModel copies these bones into sleeves, jacket and pants after
        // this hook; vanilla armor also copies the completed humanoid pose.
    }

    private static void poseArm(ModelPart arm, float lean, float pitch, float spread, float weight) {
        Vector3f angles = new Quaternionf().rotationX(lean).rotateY(spread).rotateX(pitch)
                .getEulerAnglesZYX(new Vector3f());
        arm.xRot = Mth.lerp(weight, arm.xRot, angles.x);
        arm.yRot = Mth.lerp(weight, arm.yRot, angles.y);
        arm.zRot = Mth.lerp(weight, arm.zRot, angles.z);
        arm.y = Mth.lerp(weight, arm.y, 12 - 10 * Mth.cos(lean));
        arm.z = Mth.lerp(weight, arm.z, -10 * Mth.sin(lean));
    }

    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack stack, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack item, float partial, float equip, float swing) {
                if (arm != player.getMainArm()) return false;
                Pose pose = pose(player, player.tickCount + partial);
                if (pose.weight <= 0) return false;
                int side = arm == HumanoidArm.RIGHT ? 1 : -1;
                stack.translate(side * 0.56F, -0.52F - equip * 0.6F, -0.72F);
                stack.translate(side * Mth.lerp(pose.thrust, -0.23F, -0.05F) * pose.weight,
                        Mth.lerp(pose.thrust, 0.16F, -0.08F) * pose.weight,
                        Mth.lerp(pose.thrust, 0.18F, -0.55F) * pose.weight);
                stack.mulPose(Axis.YP.rotationDegrees(side * Mth.lerp(pose.thrust, -45, -15) * pose.weight));
                stack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(pose.thrust, -35, -80) * pose.weight));
                stack.mulPose(Axis.ZP.rotationDegrees(side * Mth.lerp(pose.thrust, 20, 10) * pose.weight));
                return true;
            }
        }, ModItem.TWILIGHT_SWORD.get());
    }

    private static float smooth(float value) {
        value = Mth.clamp(value, 0, 1);
        return value * value * (3 - 2 * value);
    }
}
