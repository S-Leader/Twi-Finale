package show.twilight.finale.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;
import twilightforest.init.TFParticleType;

public final class HierophantSpawnerBlockEntity extends BossSpawnerBlockEntity<TheTwilightHierophantEntity> {
    public HierophantSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TWILIGHT_HIEROPHANT_SPAWNER.get(), ModEntity.THE_TWILIGHT_HIEROPHANT.get(), pos, state);
    }

    @Override
    public ParticleOptions getSpawnerParticle() {
        return TFParticleType.ANNIHILATE.get();
    }
}
