package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import show.twilight.finale.block.FloorFinalTrophyBlock;
import show.twilight.finale.block.WallFinalTrophyBlock;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;

public interface RenderWithoutEntity {
    default void render(FinalTrophyBlockEntity trophy, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int overlay) {
        if (trophy == null) {
            this.render(poseStack, bufferSource, packedLight, overlay);
            return;
        }
        BlockState state = trophy.getBlockState();
        boolean isWall = state.getBlock() instanceof WallFinalTrophyBlock;
        float rotation = RotationSegment.convertToDegrees(isWall ? RotationSegment.convertToSegment(state.getValue(WallFinalTrophyBlock.FACING).getOpposite()) : state.getValue(FloorFinalTrophyBlock.ROTATION));
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - rotation));
        poseStack.translate(-0.5, 0.0, -0.5);
        if (isWall) {
            poseStack.translate(0.0, 0.25, -0.25);
        }
        this.render(poseStack, bufferSource, packedLight, overlay);
        poseStack.popPose();
    }

    void render(PoseStack var1, MultiBufferSource var2, int var3, int var4);
}

