package show.twilight.finale.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import show.twilight.finale.entity.EngineerAdherentEntity;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;

/** Native one-shot spawner removes its cage after a successful spawn. */
public final class EngineerSpawnerBlockEntity extends BossSpawnerBlockEntity<EngineerAdherentEntity> {
    public EngineerSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ENGINEER_SPAWNER_ENTITY.get(), ModEntity.ENGINEER_ADHERENT.get(), pos, state);
    }

    @Override public ParticleOptions getSpawnerParticle() { return ParticleTypes.GUST; }
}
