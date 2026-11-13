package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import show.twilight.finale.entity.projectile.EngineerWrench;

public final class EngineerWrenchRenderer extends EntityRenderer<EngineerWrench> {
    private final ItemRenderer items;

    public EngineerWrenchRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
    }

    @Override
    public void render(EngineerWrench wrench, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(yaw - 90));
        pose.mulPose(Axis.ZP.rotationDegrees((wrench.tickCount + partial) * 32));
        pose.scale(1.1F, 1.1F, 1.1F);
        items.renderStatic(wrench.getItem(), ItemDisplayContext.GROUND, 15728880, OverlayTexture.NO_OVERLAY, pose, buffers, wrench.level(), wrench.getId());
        pose.popPose();
        super.render(wrench, yaw, partial, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(EngineerWrench wrench) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}
