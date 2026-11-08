package show.twilight.finale.runtime.entity.skill;

import show.twilight.finale.runtime.entity.AbstractSkillEntity;

public abstract class BaseSkill<T extends AbstractSkillEntity> {
    public final int id, duration, cooldown;
    public final double range;

    protected BaseSkill(int id, int duration, double range, int cooldown) {
        this.id = id;
        this.duration = duration;
        this.range = range;
        this.cooldown = cooldown;
    }

    public abstract void tick(T entity);

    public abstract void effect(T entity);
}
