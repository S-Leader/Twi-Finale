package show.twilight.finale.entity.skill.adherent;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.entity.LoyalWraithEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;

public class SummonSkill
        extends BaseSkill<AdherentHighPriestEntity> {
    public static final int ID = 4;

    public SummonSkill() {
        super(4, 70, 128, 240);
    }

    public void tick(AdherentHighPriestEntity entity) {
        if (!entity.level().isClientSide() && entity.getSkillTick() < 40) {
            float red = 0.5f;
            float grn = 0.5f;
            float blu = 0.5f;
            float angle = entity.yBodyRot * (float) Math.PI / 180.0f;
            double dy = entity.getY() + (double) entity.getBbHeight() * 0.94;
            for (int side = 0; side < 2; ++side) {
                float currentAngle = angle + (float) side * (float) Math.PI;
                double dx = entity.getX() + (double) Mth.cos((float) currentAngle) * 0.65;
                double dz = entity.getZ() + (double) Mth.sin((float) currentAngle) * 0.65;
                ((net.minecraft.server.level.ServerLevel) entity.level()).sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, red, grn, blu), dx, dy, dz, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    public void effect(AdherentHighPriestEntity entity) {
        entity.getNavigation().stop();
        LivingEntity target = entity.getTarget();
        if (entity.getSkillTick() == 1) {
            entity.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0f, 1.0f);
        }
        if (entity.getSkillTick() == 40) {
            entity.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 1.0f);
            if (!entity.level().isClientSide() && !entity.isShadowClone() && entity.wantsNewMinion()) {
                for (int i = 0; i < 3; ++i) {
                    LoyalWraithEntity minion = new LoyalWraithEntity(entity.level(), entity);
                    double spawnX = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 4.0;
                    double spawnY = entity.getY() + 1.0;
                    double spawnZ = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 4.0;
                    minion.setPos(spawnX, spawnY, spawnZ);
                    if (target != null) {
                        minion.setTarget(target);
                    }
                    entity.level().addFreshEntity((Entity) minion);
                }
            }
        }
    }
}

