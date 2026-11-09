package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import show.twilight.finale.entity.projectile.HierophantMeteor;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.HydraMortarModel;

/**
 * Use the native Hydra mesh/texture, without its stationary fuse's low-alpha flash.
 */
public final class HierophantMeteorRenderer extends EntityRenderer<HierophantMeteor> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("twilightforest", "textures/entity/hydramortar.png");
    private final HydraMortarModel model;

    public HierophantMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new HydraMortarModel(context.bakeLayer(TFModelLayers.HYDRA_MORTAR));
    }

    @Override
    public ResourceLocation getTextureLocation(HierophantMeteor entity) {
        return TEXTURE;
    }

    @Override
    public void render(HierophantMeteor entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), 15728880, OverlayTexture.NO_OVERLAY, -1);
    }
}
