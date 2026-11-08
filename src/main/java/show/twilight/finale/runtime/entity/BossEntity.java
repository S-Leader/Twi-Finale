package show.twilight.finale.runtime.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class BossEntity extends AbstractSkillEntity {
    protected BossEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public abstract void updatePhase();

    public abstract void spawn();

    public abstract boolean hasSpawnProcess();

    public int getDamageTime() {
        return 20;
    }

    public float getDamageMap() {
        return 20;
    }
}
