package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import show.twilight.finale.client.layer.HierophantArmorLayer;
import show.twilight.finale.client.layer.HierophantOuterLayer;
import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;

@OnlyIn(value = Dist.CLIENT)
public class TheTwilightHierophantRenderer<T extends TheTwilightHierophantEntity>
        extends MobRenderer<T, TheTwilightHierophantModel<T>> {
    private final TheTwilightHierophantModel<T> slimModel;
    private final TheTwilightHierophantModel<T> normalModel;
    private final HierophantCubeMesh cube;
    private boolean ghostPass;

    public TheTwilightHierophantRenderer(EntityRendererProvider.Context context) {
        super(context, new TheTwilightHierophantModel(context.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION), false), 0.5f);
        this.slimModel = new TheTwilightHierophantModel(context.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_SLIM), true);
        this.normalModel = this.model;
        this.cube = new HierophantCubeMesh(context);
        this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
        HierophantArmorLayer.addCustomArmorLayers(this, context, TheTwilightHierophantModel.LAYER_LOCATION_I, TheTwilightHierophantModel.LAYER_LOCATION_O);
        this.addLayer(new HierophantOuterLayer(this, context.getModelSet()));
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull T entity) {
        this.model = this.normalModel;
        Entity entity2 = Minecraft.getInstance().getCameraEntity();
        if (entity2 instanceof AbstractClientPlayer) {
            AbstractClientPlayer clientPlayer = (AbstractClientPlayer) entity2;
            if (clientPlayer.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM) {
                this.model = this.slimModel;
            }
            return clientPlayer.getSkin().texture();
        }
        return DefaultPlayerSkin.getDefaultTexture();
    }

    public void render(@NotNull T entity, float entityYaw, float partialTicks, @NotNull PoseStack stack, @NotNull MultiBufferSource bufferSource, int light) {
        getTextureLocation(entity); // Select the model before setupAnim, not halfway through rendering.
        float age = entity.tickCount + partialTicks;
        int action = entity.getAction(), time = entity.getActionTime();
        float blur = entity.getBlur();
        float alpha = 1 - blur * 0.45F;
        if (action == TheTwilightHierophantEntity.SMOKE_MOVE) alpha = 0.12F;
        if (action == TheTwilightHierophantEntity.DEATH)
            alpha *= net.minecraft.util.Mth.clamp((125 - time - partialTicks) / 55F, 0, 1);
        boolean afterimages = entity.getPhase() == 2 && (blur > 0.03F || action == TheTwilightHierophantEntity.SUMMON || action == TheTwilightHierophantEntity.BARRAGE || action == TheTwilightHierophantEntity.SMOKE_MOVE);
        if (afterimages && alpha > 0) {
            ghostPass = true;
            try {
                for (int i = 4; i >= 1; i--) {
                    stack.pushPose();
                    var trail = entity.getTrailPosition(i * 3).subtract(entity.position());
                    double spread = (0.04 + blur * 0.24) * i;
                    stack.translate(trail.x + Math.sin(age * 0.13 + i * 1.7) * spread, trail.y, trail.z + Math.cos(age * 0.13 + i * 1.7) * spread);
                    float opacity = alpha * (0.05F + blur * 0.035F) * (5 - i);
                    MultiBufferSource faded = type -> new AlphaVertexConsumer(bufferSource.getBuffer(type), opacity);
                    super.render(entity, entityYaw, partialTicks, stack, faded, light);
                    stack.popPose();
                }
            } finally {
                ghostPass = false;
            }
        }
        if (alpha > 0) {
            float opacity = alpha;
            super.render(entity, entityYaw, partialTicks, stack, type -> new AlphaVertexConsumer(bufferSource.getBuffer(type), opacity), light);
        }
        if (entity.hasShield() && action != TheTwilightHierophantEntity.DEATH && (action != TheTwilightHierophantEntity.INTRO || time >= 55)) {
            float grow = action == TheTwilightHierophantEntity.INTRO ? net.minecraft.util.Mth.clamp((time - 55 + partialTicks) / 25F, 0, 1) : 1;
            stack.pushPose();
            stack.translate(0, 1.3, 0);
            if (entity.hurtTime > 0) {
                stack.translate(Math.sin(age * 3.1) * 0.07, 0, Math.cos(age * 2.7) * 0.07);
                stack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) Math.sin(age * 3) * entity.hurtTime * 0.45F));
            }
            stack.scale(2.6F * grow, 2.6F * grow, 2.6F * grow);
            cube.render(stack, bufferSource, light, age, 0.42F * grow * (action == TheTwilightHierophantEntity.SMOKE_MOVE ? 0.1F : 1));
            stack.popPose();
        }
        if (action == TheTwilightHierophantEntity.BEAM && time <= 46) {
            var start = entity.getBeamOrigin().subtract(entity.position());
            var end = entity.getAimPoint().subtract(entity.position());
            boolean firing = time >= 40;
            drawBeam(stack, bufferSource, start, end, firing ? 0.2 : 0.018, firing ? 0xCC400078 : 0x885B178F);
            if (firing) drawBeam(stack, bufferSource, start, end, 0.055, 0xFFAF66D8);
        }
        if (action == TheTwilightHierophantEntity.CHARGE && time < 30)
            drawBeam(stack, bufferSource, new net.minecraft.world.phys.Vec3(0, 0.08, 0), entity.getAimPoint().subtract(entity.position()).multiply(1, 0, 1).add(0, 0.08, 0), 0.06, 0x99AC3BE2);
    }

    @Override
    protected net.minecraft.client.renderer.RenderType getRenderType(T entity, boolean visible, boolean translucent, boolean glowing) {
        return visible || translucent ? net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(entity)) : super.getRenderType(entity, visible, translucent, glowing);
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return !ghostPass && super.shouldShowName(entity);
    }

    @Override
    protected void setupRotations(T entity, PoseStack pose, float age, float yaw, float partial, float scale) {
        // The custom kneeling death pose must not receive vanilla's sideways roll.
        if (entity.isDeadOrDying()) pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180 - yaw));
        else super.setupRotations(entity, pose, age, yaw, partial, scale);
    }

    @Override
    public boolean shouldRender(T entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z) || entity.getAction() == TheTwilightHierophantEntity.BEAM && frustum.isVisible(new net.minecraft.world.phys.AABB(entity.getBeamOrigin(), entity.getAimPoint()).inflate(0.3));
    }

    private static void drawBeam(PoseStack pose, MultiBufferSource buffers, net.minecraft.world.phys.Vec3 start, net.minecraft.world.phys.Vec3 end, double width, int color) {
        var direction = end.subtract(start).normalize();
        var side = direction.cross(new net.minecraft.world.phys.Vec3(0, 1, 0));
        if (side.lengthSqr() < 0.001) side = direction.cross(new net.minecraft.world.phys.Vec3(1, 0, 0));
        side = side.normalize().scale(width);
        var up = direction.cross(side).normalize().scale(width);
        var vertices = buffers.getBuffer(net.minecraft.client.renderer.RenderType.lightning());
        for (var offset : java.util.List.of(side, up))
            for (var point : java.util.List.of(start.subtract(offset), end.subtract(offset), end.add(offset), start.add(offset)))
                vertices.addVertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z).setColor(color);
    }
}

