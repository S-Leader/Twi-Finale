package show.twilight.finale.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;

import java.util.function.Supplier;

public abstract class AbstractFinalTrophyBlock extends BaseEntityBlock {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    public final Supplier<BlockEntityType<FinalTrophyBlockEntity>> blockEntityType;

    protected AbstractFinalTrophyBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<FinalTrophyBlockEntity>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.blockEntityType.get().create(pos, state);
    }
}

