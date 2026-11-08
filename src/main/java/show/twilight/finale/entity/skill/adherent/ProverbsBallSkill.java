package show.twilight.finale.entity.skill.adherent;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.entity.projectile.ProverbsBolt;
import show.twilight.finale.runtime.entity.skill.BaseSkill;

public class ProverbsBallSkill
        extends BaseSkill<AdherentHighPriestEntity> {
    public static final int ID = 0;

    public ProverbsBallSkill() {
        super(0, 70, 128, 240);
    }

    public void tick(AdherentHighPriestEntity entity) {
    }

    public void effect(AdherentHighPriestEntity entity) {
        entity.getNavigation().stop();
        LivingEntity target = entity.getTarget();
        if (entity.getSkillTick() == 1) {
            entity.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.0f, 1.0f);
        }
        if (entity.getSkillTick() == 40) {
            entity.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 1.0f);
        }
        if (entity.getSkillTick() == 40 && target != null) {
            ProverbsBolt natureBolt = new ProverbsBolt(entity.level(), entity);
            entity.playSound(SoundEvents.WITHER_SHOOT, 1.0f, 1.0f / (entity.getRandom().nextFloat() * 0.4f + 0.8f));
            double d0 = target.getY();
            double d1 = target.getX() - entity.getX();
            double d2 = d0 - natureBolt.getY();
            double d3 = target.getZ() - entity.getZ();
            float f = Mth.sqrt((float) ((float) (d1 * d1 + d3 * d3))) * 0.2f;
            natureBolt.shoot(d1, d2 + (double) f, d3, 0.6f, 10 - entity.level().getDifficulty().getId() * 4);
            entity.level().addFreshEntity((Entity) natureBolt);
        }
    }
}

