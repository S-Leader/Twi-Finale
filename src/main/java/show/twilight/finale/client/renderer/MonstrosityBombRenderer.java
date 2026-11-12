package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import show.twilight.finale.entity.projectile.MonstrosityBomb;

public class MonstrosityBombRenderer extends EntityRenderer<MonstrosityBomb> {
    private final HierophantCubeMesh cube;

    public MonstrosityBombRenderer(EntityRendererProvider.Context c) {
        super(c);
        cube = new HierophantCubeMesh(c);
    }

    @Override
    public ResourceLocation getTextureLocation(MonstrosityBomb bomb) {
        return HierophantCubeMesh.TEXTURE;
    }

    @Override
    public void render(MonstrosityBomb bomb, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.scale(2.6F, 2.6F, 2.6F);
        cube.renderFragment(pose, buffers, 15728880, (bomb.tickCount + partial) * 10);
        pose.popPose();
    }
}
