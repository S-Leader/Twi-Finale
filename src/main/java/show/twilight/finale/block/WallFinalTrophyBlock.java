package show.twilight.finale.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;

import java.util.function.Supplier;

public class WallFinalTrophyBlock
        extends AbstractFinalTrophyBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final VoxelShape NORTH = Block.box((double) 4.0, (double) 4.0, (double) 8.0, (double) 12.0, (double) 12.0, (double) 16.0);
    protected static final VoxelShape SOUTH = Block.box((double) 4.0, (double) 4.0, (double) 0.0, (double) 12.0, (double) 12.0, (double) 8.0);
    protected static final VoxelShape EAST = Block.box((double) 0.0, (double) 4.0, (double) 4.0, (double) 8.0, (double) 12.0, (double) 12.0);
    protected static final VoxelShape WEST = Block.box((double) 8.0, (double) 4.0, (double) 4.0, (double) 16.0, (double) 12.0, (double) 12.0);

    public WallFinalTrophyBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<FinalTrophyBlockEntity>> blockEntityType) {
        super(properties, blockEntityType);
        this.registerDefaultState((BlockState) ((BlockState) this.getStateDefinition().any()).setValue(FACING, Direction.NORTH));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction[] nearestOrderedDirections;
        BlockState blockstate = this.defaultBlockState();
        Level blockgetter = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        for (Direction direction : nearestOrderedDirections = context.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal()) continue;
            blockstate = (BlockState) blockstate.setValue(FACING, direction.getOpposite());
            if (blockgetter.getBlockState(blockpos.relative(direction)).canBeReplaced(context)) continue;
            return blockstate;
        }
        return null;
    }

    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return switch ((Direction) state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_) {
        return Shapes.empty();
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return (BlockState) state.setValue(FACING, rotation.rotate((Direction) state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation((Direction) state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }
}

