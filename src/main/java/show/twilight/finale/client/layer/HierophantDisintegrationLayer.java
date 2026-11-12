package show.twilight.finale.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;

/**
 * Expanding dragon-style rays; the skin dissolves in its own textured pass.
 */
public final class HierophantDisintegrationLayer<T extends TheTwilightHierophantEntity> extends RenderLayer<T, TheTwilightHierophantModel<T>> {
    public HierophantDisintegrationLayer(RenderLayerParent<T, TheTwilightHierophantModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T boss, float swing, float amount, float partial, float age, float yaw, float pitch) {
        if (boss.getPhase() != 4 || !boss.isDeadOrDying()) return;
        float progress = Mth.clamp((boss.getActionTime() + partial) / TheTwilightHierophantEntity.DEATH_DURATION, 0, 1);
        rays(pose, buffers.getBuffer(RenderType.dragonRays()), progress);
        rays(pose, buffers.getBuffer(RenderType.dragonRaysDepth()), progress);
    }

    private static void rays(PoseStack pose, VertexConsumer vertex, float progress) {
        pose.pushPose();
        pose.translate(0, -0.7, 0);
        RandomSource random = RandomSource.create(1739);
        int count = (int) (progress * progress * 26), centre = FastColor.ARGB32.colorFromFloat(1 - Mth.clamp((progress - 0.85F) / 0.15F, 0, 1), 1, 0.9F, 1);
        for (int i = 0; i < count; i++) {
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360 + progress * 90));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360));
            float height = 1 + random.nextFloat() * 5 + progress * 3, width = 0.15F + random.nextFloat() * 0.4F;
            float[][] ends = {{-width, height, -width}, {width, height, -width}, {0, height, width}};
            for (int j = 0; j < 3; j++) {
                float[] a = ends[j], b = ends[(j + 1) % 3];
                vertex.addVertex(pose.last(), 0, 0, 0).setColor(centre);
                vertex.addVertex(pose.last(), a[0], a[1], a[2]).setColor(0x00B960FF);
                vertex.addVertex(pose.last(), b[0], b[1], b[2]).setColor(0x00B960FF);
            }
            pose.popPose();
        }
        pose.popPose();
    }
}
