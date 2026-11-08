package show.twilight.finale.runtime.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import show.twilight.finale.runtime.entity.AbstractSkillEntity;

/**
 * Chase only; actual hits and casts are implemented by the existing skill effects.
 */
public class EmptyMeleeAttackGoal extends MeleeAttackGoal {
    private final AbstractSkillEntity entity;

    public EmptyMeleeAttackGoal(AbstractSkillEntity entity, double speed, boolean follow) {
        super(entity, speed, follow);
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        return !entity.isUsingSkill() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !entity.isUsingSkill() && super.canContinueToUse();
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
    }
}
