package show.twilight.finale.runtime.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import show.twilight.finale.runtime.animation.VzlingAnimatable;
import show.twilight.finale.runtime.animation.VzlingAnimation;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSkillEntity extends Monster implements ISkillEntity, VzlingAnimatable {
    private static final EntityDataAccessor<Integer> SKILL_TYPE = SynchedEntityData.defineId(AbstractSkillEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKILL_START = SynchedEntityData.defineId(AbstractSkillEntity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Boolean> USING_SKILL = SynchedEntityData.defineId(AbstractSkillEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(AbstractSkillEntity.class, EntityDataSerializers.INT);
    private final List<VzlingAnimation> animations = new ArrayList<>();
    private int skillTick;
    public boolean updatePhase;

    protected AbstractSkillEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(USING_SKILL, false);
        builder.define(SKILL_TYPE, -1);
        builder.define(SKILL_START, 0);
        builder.define(PHASE, 1);
    }

    @Override
    public boolean isUsingSkill() {
        return entityData.get(USING_SKILL);
    }

    @Override
    public int getSkillType() {
        return entityData.get(SKILL_TYPE);
    }

    @Override
    public int getSkillTick() {
        return skillTick;
    }

    public int getPhase() {
        return entityData.get(PHASE);
    }

    public void setPhase(int phase) {
        entityData.set(PHASE, phase);
        updatePhase = false;
    }

    public boolean isSpawned() {
        return true;
    }

    public void beginSkill(int id) {
        entityData.set(SKILL_TYPE, id);
        entityData.set(SKILL_START, tickCount);
        skillTick = 0;
        entityData.set(USING_SKILL, true);
    }

    public void endSkill() {
        entityData.set(USING_SKILL, false);
        skillTick = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (isUsingSkill()) skillTick++;
        if (!level().isClientSide() && isAlive()) getSkillManager().tick();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(USING_SKILL) || key.equals(SKILL_START)) skillTick = 0;
    }

    @Override
    public List<VzlingAnimation> getAnimations() {
        return animations;
    }

    public boolean shouldPlayAnimation(Entity entity, VzlingAnimation animation) {
        return isAlive();
    }

    public void playAnimation(Entity entity, VzlingAnimation animation) {
        if (shouldPlayAnimation(entity, animation)) animation.state.start(tickCount);
    }

    public void playAnimationIfStopped(Entity entity, VzlingAnimation animation) {
        if (shouldPlayAnimation(entity, animation)) animation.state.startIfStopped(tickCount);
    }

    public void stopAnimation(Entity entity, VzlingAnimation animation) {
        animation.state.stop();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FinalePhase", getPhase());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPhase(tag.contains("FinalePhase") ? tag.getInt("FinalePhase") : 1);

        endSkill();
    }
}
