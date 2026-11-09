package show.twilight.finale.runtime.entity.skill;

import net.minecraft.world.entity.monster.Monster;
import show.twilight.finale.runtime.entity.ISkillEntity;

public abstract class BaseSkill<T extends Monster & ISkillEntity> {
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
