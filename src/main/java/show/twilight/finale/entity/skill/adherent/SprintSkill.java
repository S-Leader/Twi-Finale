package show.twilight.finale.entity.skill.adherent;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;

public class SprintSkill extends BaseSkill<AdherentHighPriestEntity> {
    public static final int ID = 7;

    public SprintSkill() {
        super(7, 60, 12, 120);
    }

    public void tick(AdherentHighPriestEntity entity) {
    }

    public void effect(AdherentHighPriestEntity entity) {
        entity.getNavigation().stop();
        LivingEntity target = entity.getTarget();
        if (target != null) {
            if (entity.getSkillTick() == 7) {
                entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, Mth.nextFloat(entity.getRandom(), 1.0f, 1.0f));
                entity.lookAt(target, 360.0f, 360.0f);
            }
            if (entity.getSkillTick() >= 10 && entity.getSkillTick() < 15) {
                for (LivingEntity rangeTarget : entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.5, 0.0, 1.5))) {
                    Player p;
                    if (rangeTarget == entity || entity.isAlliedTo(rangeTarget) || !entity.canAttack(rangeTarget) || rangeTarget instanceof Player && (p = (Player) rangeTarget).isCrouching() || !rangeTarget.hurt(entity.damageSources().mobAttack(entity), 6.5f))
                        continue;
                    entity.playSound(SoundEvents.PLAYER_ATTACK_SWEEP);
                }
            }
        }
        if (entity.getSkillTick() >= 7 && entity.getSkillTick() <= 8) {
            entity.addDeltaMovement(new Vec3(entity.getLookAngle().x(), 0.0, entity.getLookAngle().z()));
        }
        if (entity.getSkillTick() >= 1 && entity.getSkillTick() <= 15) {
            entity.invulnerableTime = 20;
        }
    }
}

