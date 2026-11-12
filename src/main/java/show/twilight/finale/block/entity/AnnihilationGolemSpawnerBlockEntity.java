package show.twilight.finale.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import show.twilight.finale.entity.AnnihilationGolemEntity;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;
import twilightforest.init.TFParticleType;

public final class AnnihilationGolemSpawnerBlockEntity extends BossSpawnerBlockEntity<AnnihilationGolemEntity> {
    public AnnihilationGolemSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ANNIHILATION_GOLEM_SPAWNER.get(), ModEntity.ANNIHILATION_GOLEM.get(), pos, state);
    }

    @Override
    public ParticleOptions getSpawnerParticle() {
        return TFParticleType.HUGE_SMOKE.get();
    }
}
