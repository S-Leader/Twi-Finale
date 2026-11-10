package show.twilight.finale.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import twilightforest.init.TFParticleType;

import java.util.function.Supplier;

public class WallAdherentTrophyBlock extends WallFinalTrophyBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final VoxelShape NORTH = Block.box(4.0, 4.0, 8.0, 12.0, 12.0, 16.0);
    protected static final VoxelShape SOUTH = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 8.0);
    protected static final VoxelShape EAST = Block.box(0.0, 4.0, 4.0, 8.0, 12.0, 12.0);
    protected static final VoxelShape WEST = Block.box(8.0, 4.0, 4.0, 16.0, 12.0, 12.0);

    public WallAdherentTrophyBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<FinalTrophyBlockEntity>> blockEntityType) {
        super(properties, blockEntityType);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = this.defaultBlockState();
        Level blockgetter = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal()) continue;
            blockstate = blockstate.setValue(FACING, direction.getOpposite());
            if (blockgetter.getBlockState(blockpos.relative(direction)).canBeReplaced(context)) continue;
            return blockstate;
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        level.playSound(player, pos, SoundEvents.PHANTOM_AMBIENT, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            for (int i = 0; i < 18; ++i) {
                double x = (double) pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5);
                double y = (double) pos.getY() + 0.5 + (level.getRandom().nextDouble() - 0.5);
                double z = (double) pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5);
                serverLevel.sendParticles((ParticleOptions) TFParticleType.ANNIHILATE.get(), x, y, z, 1, 0.0, 0.05, 0.0, 0.0);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }
}

