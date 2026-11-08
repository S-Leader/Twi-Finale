package show.twilight.finale.entity.skill.adherent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.entity.LoyalWraithEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;
import twilightforest.init.TFParticleType;

public class ExplodeSkill extends BaseSkill<AdherentHighPriestEntity> {
    public static final int ID = 5;

    public ExplodeSkill() {
        super(5, 70, 16, 240);
    }

    public void tick(AdherentHighPriestEntity entity) {
    }

    public void effect(AdherentHighPriestEntity entity) {
        entity.getNavigation().stop();
        ServerLevel serverLevel = (ServerLevel) entity.level();
        if (entity.getSkillTick() == 1) {
            entity.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0f, 1.0f);
        }
        if (entity.getSkillTick() <= 40) {
            for (int i = 0; i < 3; ++i) {
                double px = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 10.0;
                double py = entity.getY() + entity.getRandom().nextDouble() * 5.0;
                double pz = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 10.0;
                double vx = (entity.getX() - px) * 0.2;
                double vy = (entity.getY() + (double) entity.getBbHeight() * 0.5 - py) * 0.2;
                double vz = (entity.getZ() - pz) * 0.2;
                serverLevel.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, vx, vy, vz, 1.0);
            }
        }
        if (entity.getSkillTick() == 1 || entity.getSkillTick() == 21) {
            BlockPos entityPos = entity.blockPosition();
            int radius = 16;
            int height = 16;
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int x = -radius; x <= radius; ++x) {
                for (int y = -height; y <= height; ++y) {
                    for (int z = -radius; z <= radius; ++z) {
                        mutablePos.set(entityPos.getX() + x, entityPos.getY() + y, entityPos.getZ() + z);
                        BlockState state = serverLevel.getBlockState(mutablePos);
                        Block block = state.getBlock();
                        if (!(block instanceof BellBlock bell)) continue;
                        bell.attemptToRing(serverLevel, mutablePos, null);
                    }
                }
            }
        }
        if (entity.getSkillTick() == 20 && !entity.level().isClientSide()) {
            entity.level().getEntitiesOfClass(LoyalWraithEntity.class, entity.getBoundingBox().inflate(64.0)).stream().filter(e -> e.master == entity).forEach(Entity::discard);
        }
        if (entity.getSkillTick() == 40) {
            entity.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.5f, 1.0f);
            if (!entity.level().isClientSide()) {
                double targetZ;
                double targetY;
                int i;
                AABB area = entity.getBoundingBox().inflate(5.0);
                for (LivingEntity rangeTarget : entity.level().getEntitiesOfClass(LivingEntity.class, area, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                    if (rangeTarget == entity || entity.isAlliedTo(rangeTarget)) continue;
                    rangeTarget.hurt(entity.damageSources().mobAttack(entity), 20.0f);
                }
                double centerX = entity.getX();
                double centerY = entity.getY() + (double) entity.getBbHeight() * 0.5;
                double centerZ = entity.getZ();
                for (i = 0; i < 60; ++i) {
                    double targetX = (entity.getRandom().nextDouble() - 0.5) * 6.0;
                    targetY = (entity.getRandom().nextDouble() - 0.5) * 6.0;
                    targetZ = (entity.getRandom().nextDouble() - 0.5) * 6.0;
                    serverLevel.sendParticles((ParticleOptions) ParticleTypes.LARGE_SMOKE, centerX, centerY, centerZ, 0, targetX, targetY, targetZ, 1.0);
                }
                for (i = 0; i < 300; ++i) {
                    double targetX = (entity.getRandom().nextDouble() - 0.5) * 6.0;
                    targetY = (entity.getRandom().nextDouble() - 0.5) * 6.0;
                    targetZ = (entity.getRandom().nextDouble() - 0.5) * 6.0;
                    serverLevel.sendParticles((ParticleOptions) TFParticleType.ANNIHILATE.get(), centerX, centerY, centerZ, 0, targetX, targetY, targetZ, 1.0);
                }
            }
        }
    }
}

