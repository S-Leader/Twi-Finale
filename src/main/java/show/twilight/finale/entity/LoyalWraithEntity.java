package show.twilight.finale.entity;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.ai.control.NoClipMoveControl;
import twilightforest.entity.ai.goal.SimplifiedAttackGoal;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFSounds;

import show.twilight.finale.init.ModEntity;

public class LoyalWraithEntity
        extends TamableAnimal
        implements Enemy {
    @Nullable
    public AdherentHighPriestEntity master;

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public LoyalWraithEntity(EntityType<? extends LoyalWraithEntity> type, Level level) {
        super(type, level);
        this.moveControl = new NoClipMoveControl((Mob) this);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.master = null;
    }

    public LoyalWraithEntity(Level world, AdherentHighPriestEntity master) {
        this(ModEntity.LOYAL_WRAITH.get(), world);
        this.master = master;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(2, (Goal) new SimplifiedAttackGoal((Mob) this));
        this.goalSelector.addGoal(3, (Goal) new FlyTowardsTargetGoal(this));
        this.goalSelector.addGoal(5, (Goal) new FollowOwnerGoal(this, 1.2, 5.0f, 2.0f) {

            public boolean canUse() {
                return LoyalWraithEntity.this.master == null && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 1.0));
        this.goalSelector.addGoal(8, (Goal) new LookAroundGoal(this));
        this.goalSelector.addGoal(9, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new OwnerHurtByTargetGoal((TamableAnimal) this));
        this.targetSelector.addGoal(2, (Goal) new OwnerHurtTargetGoal((TamableAnimal) this));
        this.targetSelector.addGoal(3, (Goal) new HurtByTargetGoal((PathfinderMob) this, new Class[0]));
        this.targetSelector.addGoal(4, (Goal) new NearestAttackableTargetGoal<Player>((Mob) this, Player.class, true) {

            public boolean canUse() {
                return LoyalWraithEntity.this.master != null && super.canUse();
            }
        });
        this.targetSelector.addGoal(5, (Goal) new NearestAttackableTargetGoal<Monster>((Mob) this, Monster.class, true) {

            public boolean canUse() {
                return LoyalWraithEntity.this.master == null && super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.ARMOR, 0.0).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation((Mob) this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR)) {
            return false;
        }
        if (this.master != null && source.getEntity() == this.master) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
            return false;
        }
        return super.hurt(source, amount);
    }

    public void aiStep() {
        LivingEntity masterTarget;
        this.noPhysics = true;
        if (!this.level().isClientSide() && this.master != null && this.master.isAlive() && (masterTarget = this.master.getTarget()) != null && masterTarget != this.getTarget()) {
            this.setTarget(masterTarget);
        }
        if (!this.isTame()) {
            if (this.master == null) {
                this.findNewMaster();
            }
            if (this.master == null || !this.master.isAlive()) {
                this.discard();
            }
        }
        if (!this.level().isClientSide() && this.master == null && this.getEffect(MobEffects.DAMAGE_BOOST) == null && this.tickCount % 20 == 0) {
            this.hurt(TFDamageTypes.getDamageSource((Level) this.level(), (ResourceKey) TFDamageTypes.EXPIRED, (EntityType[]) new EntityType[0]), 2.0f);
        }
        super.aiStep();
        this.setNoGravity(true);
        if (!this.level().isClientSide() && this.master != null && this.master.isAlive() && this.master.getSkillType() == 5 && this.master.getSkillTick() == 20) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            int particles = 48;
            Vec3 startPos = this.position().add(0.0, (double) this.getBbHeight() * 0.5, 0.0);
            Vec3 endPos = this.master.position().add(0.0, (double) this.master.getBbHeight() * 0.5, 0.0);
            for (int i = 0; i < particles; ++i) {
                double trailFactor = (double) i / ((double) particles - 1.0);
                double red = 1.0;
                double green = 0.84;
                double blue = 0.0;
                double tx = startPos.x() + (endPos.x() - startPos.x()) * trailFactor + this.level().getRandom().nextGaussian() * 0.01;
                double ty = startPos.y() + (endPos.y() - startPos.y()) * trailFactor + this.level().getRandom().nextGaussian() * 0.01;
                double tz = startPos.z() + (endPos.z() - startPos.z()) * trailFactor + this.level().getRandom().nextGaussian() * 0.01;
                serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) red, (float) green, (float) blue), tx, ty, tz, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private void findNewMaster() {
        List<AdherentHighPriestEntity> nearbyMasters = this.level().getEntitiesOfClass(AdherentHighPriestEntity.class, this.getBoundingBox().inflate(32.0, 16.0, 32.0));
        for (AdherentHighPriestEntity potentialMaster : nearbyMasters) {
            if (!potentialMaster.isAlive() || potentialMaster.isShadowClone()) continue;
            this.master = potentialMaster;
            this.setTarget(this.master.getTarget());
            break;
        }
    }

    public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand hand) {
        if (this.master != null) {
            return InteractionResult.PASS;
        }
        if (this.isTame() && this.getOwner() == player && player.getItemInHand(hand).is(Items.BONE)) {
            this.removeEffect(MobEffects.DAMAGE_BOOST);
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));
            this.heal(2.0f);
            this.playSound((SoundEvent) TFSounds.WRAITH_AMBIENT.get(), this.getSoundVolume(), this.getVoicePitch());
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
            return InteractionResult.sidedSuccess((boolean) this.level().isClientSide());
        }
        return super.interactAt(player, vec3, hand);
    }

    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        TamableAnimal tamable;
        AbstractHorse horse;
        if (this.master != null) {
            return true;
        }
        if (target instanceof Creeper || target instanceof Ghast) {
            return false;
        }
        if (target instanceof LoyalWraithEntity) {
            LoyalWraithEntity wraith = (LoyalWraithEntity) target;
            return !wraith.isTame() || wraith.getOwner() != owner;
        }
        if (target instanceof Player) {
            Player playerOwner;
            Player player = (Player) target;
            if (owner instanceof Player && !(playerOwner = (Player) owner).canHarmPlayer(player)) {
                return false;
            }
        }
        if (target instanceof AbstractHorse && (horse = (AbstractHorse) target).isTamed()) {
            return false;
        }
        return !(target instanceof TamableAnimal) || !(tamable = (TamableAnimal) target).isTame();
    }

    public boolean doHurtTarget(Entity entity) {
        boolean success = entity.hurt(TFDamageTypes.getEntityDamageSource((Level) this.level(), (ResourceKey) TFDamageTypes.HAUNT, (Entity) this, (EntityType[]) new EntityType[0]), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (success) {
            entity.push(0.0, 0.2, 0.0);
        }
        return success;
    }

    public boolean isBaby() {
        return false;
    }

    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    public boolean onClimbable() {
        return false;
    }

    public Animal getBreedOffspring(ServerLevel level, AgeableMob animal) {
        return null;
    }

    protected SoundEvent getAmbientSound() {
        return (SoundEvent) TFSounds.WRAITH_AMBIENT.get();
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return (SoundEvent) TFSounds.WRAITH_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return (SoundEvent) TFSounds.WRAITH_DEATH.get();
    }

    public boolean isNoGravity() {
        return true;
    }

    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTame() && this.master == null;
    }

    static class FlyTowardsTargetGoal
            extends Goal {
        private final LoyalWraithEntity wraith;

        FlyTowardsTargetGoal(LoyalWraithEntity wraith) {
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
                this.wraith.getMoveControl().setWantedPosition(target.getX(), target.getY() + (double) target.getEyeHeight() * 0.5, target.getZ(), 0.5);
            }
        }

        public void tick() {
            LivingEntity target = this.wraith.getTarget();
            if (target != null) {
                this.wraith.getMoveControl().setWantedPosition(target.getX(), target.getY() + (double) target.getEyeHeight() * 0.5, target.getZ(), 0.5);
            }
        }
    }

    public static class LookAroundGoal
            extends Goal {
        private final LoyalWraithEntity wraith;

        public LookAroundGoal(LoyalWraithEntity wraith) {
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

