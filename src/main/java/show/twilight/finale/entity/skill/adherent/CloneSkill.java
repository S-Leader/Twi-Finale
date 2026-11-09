package show.twilight.finale.entity.skill.adherent;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;

public class CloneSkill extends BaseSkill<AdherentHighPriestEntity> {
    public static final int ID = 3;

    public CloneSkill() {
        super(3, 70, 128, 240);
    }

    public void tick(AdherentHighPriestEntity entity) {
        AdherentHighPriestEntity master;
        if (entity.isShadowClone() && ((master = entity.getMaster()) == null || !master.isAlive())) {
            entity.discard();
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
        }
        if (!entity.isShadowClone()) {
            if (entity.getSkillTick() == 37 && entity.wantsNewClone()) {
                for (int i = 0; i < 2; ++i) {
                    AdherentHighPriestEntity clone = new AdherentHighPriestEntity((EntityType<AdherentHighPriestEntity>) entity.getType(), entity.level(), entity);
                    clone.setPos(entity.getX(), entity.getY(), entity.getZ());
                    if (target != null) {
                        clone.setTarget(target);
                    }
                    entity.level().addFreshEntity(clone);
                }
            }
            if (entity.getSkillTick() == 60 && target != null) {
                entity.teleportToTarget(target);
            }
        }
    }
}

