package show.twilight.finale.runtime.entity;

import show.twilight.finale.runtime.entity.skill.SkillManager;

import java.util.List;

public interface ISkillEntity {
    SkillManager<? extends ISkillEntity> getSkillManager();

    List<Integer> getSkillPool();

    int getSkillTick();

    int getSkillType();

    boolean isUsingSkill();

    void beginSkill(int id);

    void endSkill();
}
