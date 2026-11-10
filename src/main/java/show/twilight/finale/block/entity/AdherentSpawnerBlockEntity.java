package show.twilight.finale.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;
import twilightforest.init.TFParticleType;

public class AdherentSpawnerBlockEntity extends BossSpawnerBlockEntity<AdherentHighPriestEntity> {
    public AdherentSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ADHERENT_SPAWNER.get(), ModEntity.ADHERENT_HIGH_PRIEST.get(), pos, state);
    }

    protected boolean spawnMyBoss(ServerLevelAccessor accessor) {
        AdherentHighPriestEntity myCreature = this.makeMyCreature();
        myCreature.moveTo(this.getBlockPos(), accessor.getLevel().random.nextFloat() * 360.0f, 0.0f);
        EventHooks.finalizeMobSpawn(myCreature, accessor, accessor.getCurrentDifficultyAt(this.getBlockPos()), MobSpawnType.SPAWNER, null);
        this.initializeCreature(myCreature);
        return accessor.addFreshEntity(myCreature);
    }

    public ParticleOptions getSpawnerParticle() {
        return TFParticleType.ANNIHILATE.get();
    }
}

