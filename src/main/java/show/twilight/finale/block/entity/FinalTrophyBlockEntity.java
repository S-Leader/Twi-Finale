package show.twilight.finale.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import show.twilight.finale.compat.hadean.ModHadeanBlockCompat;
import show.twilight.finale.init.ModBlocks;

public class FinalTrophyBlockEntity
        extends BlockEntity {
    public static FinalTrophyBlockEntity annihilationGolem(BlockPos pos, BlockState state) {
        return new FinalTrophyBlockEntity(ModBlocks.ANNIHILATION_GOLEM_TROPHY_ENTITY.get(), pos, state);
    }

    public static FinalTrophyBlockEntity hierophant(BlockPos pos, BlockState state) {
        return new FinalTrophyBlockEntity(ModBlocks.HIEROPHANT_TROPHY_ENTITY.get(), pos, state);
    }

    public static FinalTrophyBlockEntity adherent(BlockPos pos, BlockState state) {
        return new FinalTrophyBlockEntity((BlockEntityType<FinalTrophyBlockEntity>) ((BlockEntityType) ModBlocks.ADHERENT_TROPHY_ENTITY.get()), pos, state);
    }

    public static FinalTrophyBlockEntity enforcer(BlockPos pos, BlockState state) {
        return new FinalTrophyBlockEntity((BlockEntityType<FinalTrophyBlockEntity>) ((BlockEntityType) ModHadeanBlockCompat.HADEAN_ENFORCER_TROPHY_ENTITY.get()), pos, state);
    }

    public static FinalTrophyBlockEntity blaster(BlockPos pos, BlockState state) {
        return new FinalTrophyBlockEntity((BlockEntityType<FinalTrophyBlockEntity>) ((BlockEntityType) ModHadeanBlockCompat.HADEAN_BLASTER_TROPHY_ENTITY.get()), pos, state);
    }

    public static FinalTrophyBlockEntity smasher(BlockPos pos, BlockState state) {
        return new FinalTrophyBlockEntity((BlockEntityType<FinalTrophyBlockEntity>) ((BlockEntityType) ModHadeanBlockCompat.HADEAN_SMASHER_TROPHY_ENTITY.get()), pos, state);
    }

    public static FinalTrophyBlockEntity duckenforcer(BlockPos pos, BlockState state) {
        return new FinalTrophyBlockEntity((BlockEntityType<FinalTrophyBlockEntity>) ((BlockEntityType) ModHadeanBlockCompat.DUCK_ENFORCER_TROPHY_ENTITY.get()), pos, state);
    }

    public FinalTrophyBlockEntity(BlockEntityType<FinalTrophyBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}

