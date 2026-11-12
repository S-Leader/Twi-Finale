package show.twilight.finale.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;

import java.util.function.Supplier;

public final class GolemTrophyBlocks {
    private GolemTrophyBlocks() {
    }

    private static InteractionResult interact(Level level, BlockPos pos, Player player) {
        level.playSound(player, pos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.BLOCKS, 1, 0.6F);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static final class Floor extends FloorAdherentTrophyBlock {
        public Floor(BlockBehaviour.Properties properties, Supplier<BlockEntityType<FinalTrophyBlockEntity>> type) {
            super(properties, type);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            return interact(level, pos, player);
        }
    }

    public static final class Wall extends WallAdherentTrophyBlock {
        public Wall(BlockBehaviour.Properties properties, Supplier<BlockEntityType<FinalTrophyBlockEntity>> type) {
            super(properties, type);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            return interact(level, pos, player);
        }
    }
}
