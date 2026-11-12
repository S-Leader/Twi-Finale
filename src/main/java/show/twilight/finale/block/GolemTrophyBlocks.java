package show.twilight.finale.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import twilightforest.init.TFBlocks;

import java.util.function.Supplier;

public final class GolemTrophyBlocks {
    private GolemTrophyBlocks() {
    }

    private static InteractionResult interact(Level level, BlockPos pos, Player player) {
        level.playSound(player, pos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.BLOCKS, 1, 0.6F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, TFBlocks.DEADROCK.get().defaultBlockState()),
                    pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 24, 0.25, 0.2, 0.25, 0.12);
        }
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
