package show.twilight.finale.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;

import java.util.function.Supplier;

/**
 * Hierophant-only interaction. The existing High Priest trophy is not changed.
 */
public final class HierophantTrophyBlocks {
    private HierophantTrophyBlocks() {
    }

    private static InteractionResult interact(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server)
            server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    18, 0.45, 0.4, 0.45, 0.05);
        // No temporary Phantom sound: this trophy is intentionally silent.
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static final class Floor extends FloorAdherentTrophyBlock {
        public Floor(BlockBehaviour.Properties properties, Supplier<BlockEntityType<FinalTrophyBlockEntity>> type) {
            super(properties, type);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            return interact(level, pos);
        }
    }

    public static final class Wall extends WallAdherentTrophyBlock {
        public Wall(BlockBehaviour.Properties properties, Supplier<BlockEntityType<FinalTrophyBlockEntity>> type) {
            super(properties, type);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            return interact(level, pos);
        }
    }
}
