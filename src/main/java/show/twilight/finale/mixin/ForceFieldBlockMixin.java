package show.twilight.finale.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.block.ForceFieldBlock;

/**
 * A solid ceiling remains a valid anchor even when three other arms are connected.
 */
@Mixin(ForceFieldBlock.class)
public abstract class ForceFieldBlockMixin {
    @Unique
    private static boolean finale$isCeilingAnchor(BlockState state, BlockGetter level, BlockPos position) {
        return !Block.isExceptionForConnection(state) && state.isFaceSturdy(level, position, Direction.DOWN);
    }

    @Inject(method = "canConnectTo", at = @At("HEAD"), cancellable = true, remap = false)
    private void finale$allowCeilingAnchor(BlockState state, BlockGetter level, BlockPos position, Direction direction, CallbackInfoReturnable<Boolean> result) {
        if (direction != Direction.UP) return;
        BlockPos above = position.above();
        BlockState neighbour = level.getBlockState(above);
        if (!(neighbour.getBlock() instanceof ForceFieldBlock))
            result.setReturnValue(finale$isCeilingAnchor(neighbour, level, above));
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void finale$connectPlacedField(BlockPlaceContext context, CallbackInfoReturnable<BlockState> result) {
        BlockState state = result.getReturnValue();
        BlockPos above = context.getClickedPos().above();
        BlockState neighbour = context.getLevel().getBlockState(above);
        if (state != null && !(neighbour.getBlock() instanceof ForceFieldBlock) && finale$isCeilingAnchor(neighbour, context.getLevel(), above))
            result.setReturnValue(state.setValue(PipeBlock.UP, true));
    }

    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void finale$refreshCeilingAnchor(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level, BlockPos position, BlockPos neighbourPosition, CallbackInfoReturnable<BlockState> result) {
        if (direction == Direction.UP && !(neighbour.getBlock() instanceof ForceFieldBlock))
            result.setReturnValue(result.getReturnValue().setValue(PipeBlock.UP, finale$isCeilingAnchor(neighbour, level, neighbourPosition)));
    }
}
