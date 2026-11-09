package show.twilight.finale.entity.skill.adherent;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;

import java.util.List;

public class ContinuousSprintSkill extends BaseSkill<AdherentHighPriestEntity> {
    public static final int ID = 6;
    public static final int DT = 5;
    private static final List<Integer> SPRINT_TIMESTAMPS = List.of(20, 30, 40, 50, 60, 70);

    public ContinuousSprintSkill() {
        super(6, 100, 12, 240);
    }

    public void tick(AdherentHighPriestEntity entity) {
    }

    public void effect(AdherentHighPriestEntity entity) {
        entity.getNavigation().stop();
        LivingEntity target = entity.getTarget();
        if (target == null) {
            return;
        }
        int tick = entity.getSkillTick();
        if (SPRINT_TIMESTAMPS.contains(tick)) {
            entity.lookAt(target, 360.0f, 360.0f);
            entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, Mth.nextFloat(entity.getRandom(), 1.0f, 1.0f));
        }
        if (tick == 5) {
            entity.playSound(SoundEvents.ENDERMAN_SCREAM, 1.0f, Mth.nextFloat(entity.getRandom(), 1.0f, 1.0f));
        }
        if (SPRINT_TIMESTAMPS.stream().anyMatch(start -> tick >= start - 5 && tick <= start - 3)) {
            Vec3 look = entity.getLookAngle();
            entity.addDeltaMovement(new Vec3(look.x(), 0.0, look.z()));
        }
        for (int start2 : SPRINT_TIMESTAMPS) {
            if (tick < start2 - 3 || tick >= start2 + 5) continue;
            entity.invulnerableTime = 20;
            List<LivingEntity> targets = entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(1.5, 0.0, 1.5));
            for (LivingEntity rangeTarget : targets) {
                if (rangeTarget == entity || entity.isAlliedTo(rangeTarget) || rangeTarget instanceof Player && rangeTarget.isCrouching() || !entity.canAttack(rangeTarget) || !rangeTarget.hurt(entity.damageSources().mobAttack(entity), 5.5f))
                    continue;
                entity.playSound(SoundEvents.PLAYER_ATTACK_SWEEP);
            }
        }
    }
}

