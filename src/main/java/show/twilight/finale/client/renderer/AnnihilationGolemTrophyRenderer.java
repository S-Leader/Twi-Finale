package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import show.twilight.finale.client.model.BlockbenchBossModel;

public final class AnnihilationGolemTrophyRenderer implements BlockEntityRenderer<FinalTrophyBlockEntity>, RenderWithoutEntity {
    private final BlockbenchBossModel model = new BlockbenchBossModel(AnnihilationGolemRenderer.MODEL);

    @Override
    public void render(FinalTrophyBlockEntity trophy, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        RenderWithoutEntity.super.render(trophy, pose, buffers, light, overlay);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0.25, 0.6875);
        pose.scale(0.5F, 0.5F, 0.5F);
        model.renderContents("head", pose, buffers.getBuffer(RenderType.entityCutoutNoCull(AnnihilationGolemRenderer.TEXTURE)), light, 0xFFFFFFFF);
        model.renderContents("head", pose, buffers.getBuffer(RenderType.eyes(AnnihilationGolemRenderer.EYES)), 15728880, 0xFFFFFFFF);
        pose.popPose();
    }
}
