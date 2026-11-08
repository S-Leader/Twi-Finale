package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import twilightforest.client.model.TFModelLayers;

/**
 * Uses the very same four nested cube parts/texture as TF's Roving Cube.
 */
final class HierophantCubeMesh {
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("twilightforest", "textures/entity/cubeofannihilation.png");
    private final ModelPart root;

    HierophantCubeMesh(EntityRendererProvider.Context context) {
        root = context.bakeLayer(TFModelLayers.CUBE_OF_ANNIHILATION);
    }

    void render(PoseStack pose, MultiBufferSource buffers, int light, float age, float alpha) {
        root.getAllParts().forEach(ModelPart::resetPose);
        root.getChild("box_x").xRot = (float) Math.sin(age * 0.15) * 0.06F;
        root.getChild("box_y").yRot = (float) Math.sin(age * 0.15 + 1) * 0.06F;
        root.getChild("box_z").zRot = (float) Math.sin(age * 0.15 + 2) * 0.06F;
        root.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, OverlayTexture.NO_OVERLAY,
                net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, 0.8F, 0.7F, 1));
    }
}
