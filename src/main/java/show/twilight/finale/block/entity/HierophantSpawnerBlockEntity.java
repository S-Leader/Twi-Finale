package show.twilight.finale.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;
import twilightforest.init.TFParticleType;

/**
 * Legacy registry ID retained for saved tower rooms; now summons one purple adherent.
 * The native final-boss block, not this old room block, summons the Hierophant.
 */
public final class HierophantSpawnerBlockEntity extends BossSpawnerBlockEntity<TheTwilightHierophantEntity> {
    public HierophantSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.HIEROPHANT_SPAWNER.get(), ModEntity.THE_TWILIGHT_HIEROPHANT.get(), pos, state);
    }

    @Override
    protected boolean spawnMyBoss(net.minecraft.world.level.ServerLevelAccessor level) {
        var adherent = twilightforest.init.TFEntities.ADHERENT.get().create(level.getLevel());
        if (adherent == null) return false;
        BlockPos origin = getBlockPos();
        BlockPos spawn = level.getBlockState(origin.below()).getCollisionShape(level, origin.below()).isEmpty() ? origin.below() : origin;
        adherent.moveTo(spawn, level.getRandom().nextFloat() * 360, 0);
        net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(adherent, level, level.getCurrentDifficultyAt(spawn), net.minecraft.world.entity.MobSpawnType.SPAWNER, null);
        ((show.twilight.finale.entity.combat.ColoredAdherent) adherent).finale$setColor(show.twilight.finale.entity.combat.ColoredAdherent.PURPLE);
        adherent.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(50);
        adherent.setHealth(50);
        adherent.setPersistenceRequired();
        adherent.setCanPickUpLoot(false);
        return level.addFreshEntity(adherent);
    }

    @Override
    public ParticleOptions getSpawnerParticle() {
        return TFParticleType.ANNIHILATE.get();
    }
}
