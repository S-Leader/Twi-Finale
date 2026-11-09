package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.client.model.CastleGuardianModel;
import show.twilight.finale.entity.CastleGuardianEntity;
import show.twilight.finale.runtime.client.layer.GlowLayer;

import javax.annotation.Nonnull;

public class CastleGuardianRenderer extends MobRenderer<CastleGuardianEntity, CastleGuardianModel<CastleGuardianEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/castle_guardian.png");

    public CastleGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new CastleGuardianModel(context.bakeLayer(CastleGuardianModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new GlowLayer(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/castle_guardian_glow.png")));
    }

    @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull CastleGuardianEntity entity) {
        return TEXTURE;
    }

    protected void scale(CastleGuardianEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
    }

    @Override
    protected void setupRotations(CastleGuardianEntity entity, PoseStack pose, float age, float yaw, float partial, float scale) {
        if (!entity.isAttached()) {
            super.setupRotations(entity, pose, age, yaw, partial, scale);
            return;
        }
        pose.translate(0, 1, 0);
        pose.mulPose(entity.getAttachFace().getOpposite().getRotation());
        pose.translate(0, -1, 0);
        pose.mulPose(Axis.YP.rotationDegrees(180));
    }

    @Override
    public boolean shouldRender(CastleGuardianEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z) || entity.getLaserPhase() > 0 && frustum.isVisible(new AABB(entity.getBeamOrigin(), entity.getBeamEnd()).inflate(0.3));
    }

    @Override
    public void render(CastleGuardianEntity entity, float yaw, float partial, PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        super.render(entity, yaw, partial, pose, buffers, light);
        if (entity.getLaserPhase() == 0) return;
        var start = entity.getBeamOrigin().subtract(entity.position());
        var end = entity.getBeamEnd().subtract(entity.position());
        var direction = end.subtract(start).normalize();
        var side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 0.001) side = direction.cross(new Vec3(1, 0, 0));
        double width = entity.getLaserPhase() == 3 ? 0.16 : 0.012 + 0.018 * entity.getChargeProgress();
        side = side.normalize().scale(width);
        var up = direction.cross(side).normalize().scale(width);
        var vertices = buffers.getBuffer(net.minecraft.client.renderer.RenderType.lightning());
        int color = entity.getLaserPhase() == 3 ? 0xFF8EEBFF : 0xDF309CFF;
        beamQuad(pose, vertices, start, end, side, color);
        beamQuad(pose, vertices, start, end, up, color);
    }

    private static void beamQuad(PoseStack pose, VertexConsumer vertices, Vec3 start, Vec3 end, Vec3 side, int color) {
        for (var point : new Vec3[]{start.subtract(side), end.subtract(side), end.add(side), start.add(side)})
            vertices.addVertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z).setColor(color);
    }
}

