package show.twilight.finale.runtime.entity.skill;

import net.minecraft.world.entity.monster.Monster;
import show.twilight.finale.runtime.entity.ISkillEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillManager<T extends ISkillEntity> {
    private final Monster mob;
    private final ISkillEntity entity;
    private final List<? extends BaseSkill<?>> skills;
    private final Map<Integer, Integer> cooldowns = new HashMap<>();
    private int pause = 30;

    public SkillManager(Monster entity, List<? extends BaseSkill<?>> skills) {
        this.mob = entity;
        this.entity = (ISkillEntity) entity;
        this.skills = skills;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void tick() {
        cooldowns.replaceAll((id, ticks) -> Math.max(0, ticks - 1));
        if (entity.isUsingSkill()) {
            BaseSkill skill = skills.stream().filter(s -> s.id == entity.getSkillType()).findFirst().orElse(null);
            if (skill == null) {
                entity.endSkill();
                return;
            }
            skill.tick(mob);
            skill.effect(mob);
            if (entity.getSkillTick() >= skill.duration) {
                cooldowns.put(skill.id, skill.cooldown);
                entity.endSkill();
                pause = 20;
            }
            return;
        }
        if (pause > 0) {
            pause--;
            return;
        }
        if (mob.getTarget() == null || !mob.getTarget().isAlive()) return;
        List<Integer> pool = entity.getSkillPool();
        List<? extends BaseSkill<?>> available = skills.stream()
                .filter(s -> pool.contains(s.id) && cooldowns.getOrDefault(s.id, 0) == 0)
                .filter(s -> mob.distanceToSqr(mob.getTarget()) <= s.range * s.range).toList();
        if (!available.isEmpty()) entity.beginSkill(available.get(mob.getRandom().nextInt(available.size())).id);
    }
}
