package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.CubeOfAnnihilationModel;

/**
 * Native Roving Cube geometry with separate shield and projectile transforms.
 * Shield growth must never be confused with a projectile's age/rotation.
 */
final class HierophantCubeMesh {
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("twilightforest", "textures/entity/cubeofannihilation.png");
    private static final float FRAGMENT_SIZE = 0.3F;
    private final CubeOfAnnihilationModel model;

    HierophantCubeMesh(EntityRendererProvider.Context context) {
        model = new CubeOfAnnihilationModel(context.bakeLayer(TFModelLayers.CUBE_OF_ANNIHILATION));
    }

    void renderShield(PoseStack pose, MultiBufferSource buffers, int light, float entityScale, float growth, float rotation, float opacity, boolean red) {
        float size = TheTwilightHierophantEntity.SHIELD_SIZE * entityScale;
        // Entity positions are at the bottom of the collision box, not its centre.
        renderCentred(pose, buffers, light, size / 2, size * Mth.clamp(growth, 0, 1), rotation, opacity, red);
    }

    void renderFragment(PoseStack pose, MultiBufferSource buffers, int light, float rotation) {
        // Match the 0.3-block projectile hitbox; time only changes rotation.
        renderCentred(pose, buffers, light, FRAGMENT_SIZE / 2, FRAGMENT_SIZE, rotation, 1, false);
    }

    private void renderCentred(PoseStack pose, MultiBufferSource buffers, int light, float centreY, float size, float rotation, float opacity, boolean red) {
        if (size <= 0 || opacity <= 0) return;
        pose.pushPose();
        pose.translate(0, centreY, 0);
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.scale(size, size, size);
        model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, OverlayTexture.NO_OVERLAY, net.minecraft.util.FastColor.ARGB32.colorFromFloat(opacity, 1, red ? 0.15F : 1, red ? 0.15F : 1));
        pose.popPose();
    }
}
