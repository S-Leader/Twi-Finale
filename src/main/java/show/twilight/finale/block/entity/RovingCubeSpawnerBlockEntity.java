package show.twilight.finale.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import show.twilight.finale.entity.RovingCubeBoss;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;
import twilightforest.init.TFParticleType;

/**
 * Actual native one-shot BossSpawnerBlockEntity, not a vanilla recurring cage.
 */
public final class RovingCubeSpawnerBlockEntity extends BossSpawnerBlockEntity<RovingCubeBoss> {
    public RovingCubeSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ROVING_CUBE_SPAWNER.get(), ModEntity.ROVING_CUBE.get(), pos, state);
    }

    @Override
    public ParticleOptions getSpawnerParticle() {
        return TFParticleType.ANNIHILATE.get();
    }
}
