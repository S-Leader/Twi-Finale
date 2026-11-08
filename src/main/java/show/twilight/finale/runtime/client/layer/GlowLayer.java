package show.twilight.finale.runtime.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class GlowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final RenderType renderType;

    public GlowLayer(RenderLayerParent<T, M> renderer, ResourceLocation texture) {
        super(renderer);
        renderType = RenderType.eyes(texture);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float swing, float amount, float partial, float age, float yaw, float pitch) {
        getParentModel().renderToBuffer(pose, buffers.getBuffer(renderType), 15728640, OverlayTexture.NO_OVERLAY, -1);
    }
}
