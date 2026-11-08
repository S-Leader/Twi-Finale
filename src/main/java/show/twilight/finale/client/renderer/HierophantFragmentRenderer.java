package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import show.twilight.finale.entity.projectile.HierophantFragment;

public final class HierophantFragmentRenderer extends EntityRenderer<HierophantFragment> {
    private final HierophantCubeMesh cube;

    public HierophantFragmentRenderer(EntityRendererProvider.Context context) {
        super(context);
        cube = new HierophantCubeMesh(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HierophantFragment fragment) {
        return HierophantCubeMesh.TEXTURE;
    }

    @Override
    public void render(HierophantFragment fragment, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, 0.15, 0);
        pose.scale(0.3F, 0.3F, 0.3F);
        pose.mulPose(Axis.YP.rotationDegrees((fragment.tickCount + partial) * 8));
        cube.render(pose, buffers, 15728880, fragment.tickCount + partial, 0.9F);
        pose.popPose();
    }
}
