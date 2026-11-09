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
import show.twilight.finale.entity.projectile.HierophantThrownWeapon;

/** Keep a spinning weapon visual; only its movement is snowball-like. */
public final class HierophantThrownWeaponRenderer extends EntityRenderer<HierophantThrownWeapon> {
    private final ItemRenderer items;

    public HierophantThrownWeaponRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
    }

    @Override
    public void render(HierophantThrownWeapon weapon, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(yaw - 90));
        pose.mulPose(Axis.ZP.rotationDegrees((weapon.tickCount + partial) * 30));
        pose.scale(1.25F, 1.25F, 1.25F);
        items.renderStatic(weapon.getItem(), ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
                pose, buffers, weapon.level(), weapon.getId());
        pose.popPose();
        super.render(weapon, yaw, partial, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(HierophantThrownWeapon weapon) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}
