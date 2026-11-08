package show.twilight.finale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.init.ModParticle;
import twilightforest.entity.ai.control.NoClipMoveControl;
import twilightforest.entity.ai.goal.SimplifiedAttackGoal;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFItems;
import twilightforest.init.TFParticleType;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AnnihilationHandEntity
        extends Monster
        implements Enemy {
    @Nullable
    public CastleGuardianEntity master;
    private UUID masterUUID;

    public AnnihilationHandEntity(EntityType<? extends AnnihilationHandEntity> type, Level level) {
        super(type, level);
        this.moveControl = new NoClipMoveControl((Mob) this);
        this.noPhysics = false;
        this.setNoGravity(true);
        this.master = null;
    }

    public AnnihilationHandEntity(EntityType<? extends AnnihilationHandEntity> type, Level world, CastleGuardianEntity master) {
        this(type, world);
        this.master = master;
        this.masterUUID = master.getUUID();
    }

    public void setMaster(CastleGuardianEntity guardian) {
        this.master = guardian;
        if (guardian != null) {
            this.masterUUID = guardian.getUUID();
        }
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(2, (Goal) new SimplifiedAttackGoal((Mob) this));
        this.goalSelector.addGoal(3, (Goal) new FlyTowardsTargetGoal(this));
        this.goalSelector.addGoal(7, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 1.0));
        this.goalSelector.addGoal(8, (Goal) new LookAroundGoal(this));
        this.goalSelector.addGoal(9, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(3, (Goal) new HurtByTargetGoal((PathfinderMob) this, new Class[0]));
        this.targetSelector.addGoal(4, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.45).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.ARMOR, 0.0).add(Attributes.FOLLOW_RANGE, 5.0);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor levelAccessor, @NotNull DifficultyInstance instance, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {
        Random random = new Random();
        int equipmentSet = random.nextInt(4);
        switch (equipmentSet) {
            case 0: {
                this.setItemSlot(EquipmentSlot.MAINHAND, ((Item) ModItem.KNIGHTMETAL_SHOVEL.get()).getDefaultInstance());
                break;
            }
            case 1: {
                this.setItemSlot(EquipmentSlot.MAINHAND, ((Item) TFItems.KNIGHTMETAL_PICKAXE.get()).getDefaultInstance());
                break;
            }
            case 2: {
                this.setItemSlot(EquipmentSlot.MAINHAND, ((Item) TFItems.KNIGHTMETAL_AXE.get()).getDefaultInstance());
                break;
            }
            case 3: {
                this.setItemSlot(EquipmentSlot.MAINHAND, ((Item) TFItems.KNIGHTMETAL_SWORD.get()).getDefaultInstance());
            }
        }
        return groupData;
    }

    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation((Mob) this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(false);
        return navigation;
    }

    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public void aiStep() {
        this.noPhysics = false;
        this.level().addParticle((ParticleOptions) TFParticleType.ANNIHILATE.get(), this.getX(), this.getY() + (double) (this.getBbHeight() / 2.0f), this.getZ(), 0.0, 0.0, 0.0);
        if (!this.level().isClientSide()) {
            double distanceToMaster;
            ServerLevel serverLevel;
            Entity entity;
            Level level;
            if (this.master == null && this.masterUUID != null && (level = this.level()) instanceof ServerLevel && (entity = (serverLevel = (ServerLevel) level).getEntity(this.masterUUID)) instanceof CastleGuardianEntity) {
                CastleGuardianEntity guardian;
                this.master = guardian = (CastleGuardianEntity) entity;
            }
            if (this.master == null || !this.master.isAlive()) {
                this.discard();
                return;
            }
            if (this.master.usesLaser()) {
                this.discard();
                return;
            }
            LivingEntity masterTarget = this.master.getTarget();
            if (masterTarget != null && masterTarget != this.getTarget()) {
                this.setTarget(masterTarget);
            }
            if ((distanceToMaster = (double) this.distanceTo((Entity) this.master)) > 8.0) {
                Vec3 pullDirection = this.master.position().subtract(this.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(pullDirection.scale(0.05)));
            }
            if (distanceToMaster < 2.0 && distanceToMaster > 0.0) {
                Vec3 pushDirection = this.position().subtract(this.master.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(pushDirection.scale(0.04)));
            }
            List<AnnihilationHandEntity> nearbyMinions = this.level().getEntitiesOfClass(AnnihilationHandEntity.class, this.getBoundingBox().inflate(1.5));
            for (AnnihilationHandEntity minion : nearbyMinions) {
                double distanceToMinion;
                if (minion == this || !((distanceToMinion = (double) this.distanceTo((Entity) minion)) < 1.5) || !(distanceToMinion > 0.0))
                    continue;
                Vec3 pushDirection = this.position().subtract(minion.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(pushDirection.scale(0.04)));
            }
        }
        super.aiStep();
        this.setNoGravity(true);
        if (!this.level().isClientSide() && this.master != null && this.master.isAlive()) {
            Vec3 origin = this.position().add(0.0, (double) (this.getBbHeight() / 2.0f), 0.0);
            Vec3 target = this.master.position().add(0.0, (double) (this.master.getBbHeight() / 2.0f), 0.0);
            double distance = origin.distanceTo(target);
            int count = (int) Math.max(5.0, distance * 7.0);
            for (int i = 0; i <= count; ++i) {
                double t = (double) i / (double) count;
                double x = Mth.lerp((double) t, (double) origin.x, (double) target.x);
                double z = Mth.lerp((double) t, (double) origin.z, (double) target.z);
                double y = Mth.lerp((double) t, (double) origin.y, (double) target.y) + Math.sin(t * Math.PI) * (distance * 0.25);
                Level level = this.level();
                if (!(level instanceof ServerLevel)) continue;
                ServerLevel serverLevel = (ServerLevel) level;
                serverLevel.sendParticles((ParticleOptions) ((SimpleParticleType) ModParticle.ANNIHILATE_HAND.get()), x, y, z, 0, 0.0, 0.0, 0.0, 1.0);
            }
        }
    }

    public boolean doHurtTarget(Entity entity) {
        boolean success = entity.hurt(TFDamageTypes.getEntityDamageSource((Level) this.level(), (ResourceKey) TFDamageTypes.HAUNT, (Entity) this, (EntityType[]) new EntityType[0]), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (success) {
            Vec3 pullVec;
            double distance;
            entity.push(0.0, 0.2, 0.0);
            this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.0f, 1.0f);
            if (this.master != null && this.master.isAlive() && (distance = (pullVec = this.master.position().subtract(this.position())).length()) > 0.1) {
                this.setDeltaMovement(pullVec.normalize().scale(Math.min(distance, 1.5)));
            }
        }
        return success;
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.masterUUID != null) {
            tag.putUUID("MasterUUID", this.masterUUID);
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("MasterUUID")) {
            this.masterUUID = tag.getUUID("MasterUUID");
        }
    }

    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    public boolean onClimbable() {
        return false;
    }

    public boolean isNoGravity() {
        return true;
    }

    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.master == null;
    }

    static class FlyTowardsTargetGoal
            extends Goal {
        private final AnnihilationHandEntity wraith;

        FlyTowardsTargetGoal(AnnihilationHandEntity wraith) {
            this.wraith = wraith;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return this.wraith.getTarget() != null;
        }

        public boolean canContinueToUse() {
            return this.wraith.getTarget() != null && this.wraith.getMoveControl().hasWanted();
        }

        public void start() {
            LivingEntity target = this.wraith.getTarget();
            if (target != null) {
                this.wraith.getMoveControl().setWantedPosition(target.getX(), target.getY() + (double) target.getEyeHeight() * 0.5, target.getZ(), 0.75);
            }
        }

        public void tick() {
            LivingEntity target = this.wraith.getTarget();
            if (target != null) {
                this.wraith.getMoveControl().setWantedPosition(target.getX(), target.getY() + (double) target.getEyeHeight() * 0.5, target.getZ(), 0.75);
            }
        }
    }

    public static class LookAroundGoal
            extends Goal {
        private final AnnihilationHandEntity wraith;

        public LookAroundGoal(AnnihilationHandEntity wraith) {
            this.wraith = wraith;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return true;
        }

        public void tick() {
            if (this.wraith.getTarget() == null) {
                Vec3 delta = this.wraith.getDeltaMovement();
                if (delta.horizontalDistanceSqr() > 0.002) {
                    this.wraith.setYRot(-((float) Mth.atan2((double) delta.x, (double) delta.z)) * 57.295776f);
                    this.wraith.setYBodyRot(this.wraith.getYRot());
                }
            } else {
                LivingEntity target = this.wraith.getTarget();
                if (target.distanceToSqr((Entity) this.wraith) < 4096.0) {
                    double dx = target.getX() - this.wraith.getX();
                    double dz = target.getZ() - this.wraith.getZ();
                    this.wraith.setYRot(-((float) Mth.atan2((double) dx, (double) dz)) * 57.295776f);
                    this.wraith.setYBodyRot(this.wraith.getYRot());
                }
            }
        }
    }
}

