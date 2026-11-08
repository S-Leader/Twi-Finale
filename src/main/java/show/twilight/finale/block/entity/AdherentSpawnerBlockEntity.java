package show.twilight.finale.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;
import twilightforest.init.TFParticleType;

public class AdherentSpawnerBlockEntity
        extends BossSpawnerBlockEntity<AdherentHighPriestEntity> {
    public AdherentSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType) ModBlocks.ADHERENT_SPAWNER.get(), ModEntity.ADHERENT_HIGH_PRIEST.get(), pos, state);
    }

    public boolean anyPlayerInRange() {
        Player closestPlayer = this.getLevel().getNearestPlayer((double) this.getBlockPos().getX() + 0.5, (double) this.getBlockPos().getY() + 0.5, (double) this.getBlockPos().getZ() + 0.5, (double) this.getRange(), false);
        return closestPlayer != null && closestPlayer.getY() > (double) (this.getBlockPos().getY() - 4);
    }

    protected boolean spawnMyBoss(ServerLevelAccessor accessor) {
        AdherentHighPriestEntity myCreature = (AdherentHighPriestEntity) this.makeMyCreature();
        myCreature.moveTo(this.getBlockPos(), accessor.getLevel().random.nextFloat() * 360.0f, 0.0f);
        EventHooks.finalizeMobSpawn(myCreature, accessor, accessor.getCurrentDifficultyAt(this.getBlockPos()), MobSpawnType.SPAWNER, null);
        this.initializeCreature(myCreature);
        return accessor.addFreshEntity((Entity) myCreature);
    }

    public ParticleOptions getSpawnerParticle() {
        return (ParticleOptions) TFParticleType.ANNIHILATE.get();
    }
}

