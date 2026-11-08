package show.twilight.finale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import show.twilight.finale.capabilities.ForceFieldCapabilityList;
import show.twilight.finale.client.animation.AdherentHighPriestAnimation;
import show.twilight.finale.entity.projectile.AnnihilationBolt;
import show.twilight.finale.entity.projectile.ProverbsBolt;
import show.twilight.finale.entity.skill.adherent.*;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.runtime.animation.VzlingAnimation;
import show.twilight.finale.runtime.entity.BossEntity;
import show.twilight.finale.runtime.entity.ISkillEntity;
import show.twilight.finale.runtime.entity.ai.VzlingPathNavigateGround;
import show.twilight.finale.runtime.entity.goal.EmptyMeleeAttackGoal;
import show.twilight.finale.runtime.entity.goal.LookAtTargetGoal;
import show.twilight.finale.runtime.entity.skill.SkillManager;
import show.twilight.finale.runtime.util.MiscUtil;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.entity.boss.IBossLootBuffer;
import twilightforest.entity.projectile.LichBomb;
import twilightforest.entity.projectile.TFThrowable;
import twilightforest.init.TFAdvancements;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;
import twilightforest.loot.TFLootTables;
import twilightforest.util.entities.EntityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdherentHighPriestEntity extends BossEntity implements EnforcedHomePoint, IBossLootBuffer {
    public final VzlingAnimation MAGIC_BALL = new VzlingAnimation(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_magicball"));
    public final VzlingAnimation IDLE = new VzlingAnimation(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_idle"));
    public final VzlingAnimation SPELL = new VzlingAnimation(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_spell"));
    public final VzlingAnimation SUMMON = new VzlingAnimation(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_summon"));
    public final VzlingAnimation DEATH = new VzlingAnimation(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_death"));
    public final VzlingAnimation SPRINT = new VzlingAnimation(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_sprint"));
    public final VzlingAnimation C_SPRINT = new VzlingAnimation(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_c_sprint"));
    private final NonNullList<ItemStack> dyingInventory = NonNullList.withSize(27, ItemStack.EMPTY);
    private static final EntityDataAccessor<Boolean> IS_CLONE = SynchedEntityData.defineId(AdherentHighPriestEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PROJECTILE_TYPE = SynchedEntityData.defineId(AdherentHighPriestEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<GlobalPos>> HOME_POINT = SynchedEntityData.defineId(AdherentHighPriestEntity.class, EntityDataSerializers.OPTIONAL_GLOBAL_POS);
    private static final EntityDataAccessor<Boolean> HAS_SHIELD = SynchedEntityData.defineId(AdherentHighPriestEntity.class, EntityDataSerializers.BOOLEAN);
    @Nullable
    private AdherentHighPriestEntity master;
    private final ServerBossEvent bossInfo = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_6).setDarkenScreen(true);
    private final List<ServerPlayer> hurtBy = new ArrayList<ServerPlayer>();
    private BlockPos deathChestPos;
    private boolean shieldInitialized = false;
    private final SkillManager<AdherentHighPriestEntity> skillManager = new SkillManager(this, List.of(new ProverbsBallSkill(), new MagicBallSkill(), new FireBallSkill(), new CloneSkill(), new SummonSkill(), new ExplodeSkill(), new ContinuousSprintSkill(), new SprintSkill()));
    private final SkillManager<AdherentHighPriestEntity> cloneSkillManager = new SkillManager(this, List.of());

    public AdherentHighPriestEntity(EntityType<AdherentHighPriestEntity> type, Level world) {
        super(type, world);
        this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0f);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f);
        if (FMLLoader.getDist().isClient()) {
            this.IDLE.setDefinition(AdherentHighPriestAnimation.IDLE);
            this.MAGIC_BALL.setDefinition(AdherentHighPriestAnimation.SPELL1);
            this.SPELL.setDefinition(AdherentHighPriestAnimation.SPELL2);
            this.SUMMON.setDefinition(AdherentHighPriestAnimation.SUMMON);
            this.DEATH.setDefinition(AdherentHighPriestAnimation.DEATH);
            this.C_SPRINT.setDefinition(AdherentHighPriestAnimation.CONTINUOUS_SPRINT);
            this.SPRINT.setDefinition(AdherentHighPriestAnimation.SPRINT);
        }
    }

    public AdherentHighPriestEntity(EntityType<AdherentHighPriestEntity> type, Level world, AdherentHighPriestEntity master) {
        this(type, world);
        this.master = master;
        this.setShadowClone(true);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_CLONE, false);
        builder.define(PROJECTILE_TYPE, 0);
        builder.define(HOME_POINT, Optional.empty());
        builder.define(HAS_SHIELD, true);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        saveHomePointToNbt(tag);
        addDeathItemsSaveData(tag, registryAccess());
        tag.putBoolean("IsShadowClone", this.isShadowClone());
        tag.putInt("ProjectileType", this.getProjectileType());
        tag.putBoolean("ShieldInitialized", this.shieldInitialized);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        loadHomePointFromNbt(tag);
        readDeathItemsSaveData(tag, registryAccess());
        this.setShadowClone(tag.getBoolean("IsShadowClone"));
        this.setProjectileType(tag.getInt("ProjectileType"));
        if (tag.contains("ShieldInitialized")) {
            this.shieldInitialized = tag.getBoolean("ShieldInitialized");
        }
    }

    public int getProjectileType() {
        return this.getEntityData().get(PROJECTILE_TYPE);
    }

    public void setProjectileType(int type) {
        this.getEntityData().set(PROJECTILE_TYPE, type);
    }

    public boolean hasShield() {
        return this.getEntityData().get(HAS_SHIELD);
    }

    public void setHasShield(boolean hasShield) {
        this.getEntityData().set(HAS_SHIELD, hasShield);
    }

    public boolean hurt(DamageSource source, float amount) {
        AdherentHighPriestEntity adherent;
        if (this.isShadowClone() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        Entity entity = source.getEntity();
        if (entity instanceof AdherentHighPriestEntity && (adherent = (AdherentHighPriestEntity) entity).getMaster() == this.getMaster()) {
            return false;
        }
        if (source.is(DamageTypes.LIGHTNING_BOLT)) {
            return false;
        }
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 16.0).add(Attributes.MOVEMENT_SPEED, 0.45).add(Attributes.FOLLOW_RANGE, 1024.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    protected PathNavigation createNavigation(@NotNull Level level) {
        return new VzlingPathNavigateGround(this, level);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EmptyMeleeAttackGoal(this, 1.0, false) {

            public boolean canUse() {
                return !AdherentHighPriestEntity.this.isShadowClone() && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new LookAtTargetGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false));
    }

    public boolean isShadowClone() {
        return this.getEntityData().get(IS_CLONE);
    }

    public void setShadowClone(boolean flag) {
        this.getEntityData().set(IS_CLONE, flag);
    }

    public AdherentHighPriestEntity getMaster() {
        return this.master != null ? this.master : this;
    }

    public int countMyClones() {
        return (int) this.level().getEntitiesOfClass(AdherentHighPriestEntity.class, this.getBoundingBox().inflate(32.0)).stream().filter(e -> e != this && e.isShadowClone() && e.getMaster() == this).count();
    }

    public boolean wantsNewClone() {
        return !this.isShadowClone() && this.countMyClones() < 3;
    }

    public int countMyMinions() {
        return (int) this.level().getEntitiesOfClass(LoyalWraithEntity.class, this.getBoundingBox().inflate(32.0)).stream().filter(e -> e.master == this).count();
    }

    public boolean wantsNewMinion() {
        return !this.isShadowClone() && this.countMyMinions() < 4;
    }

    private void stopAllAnimStates() {
        this.stopAnimation(this, this.MAGIC_BALL);
        this.stopAnimation(this, this.SPELL);
        this.stopAnimation(this, this.SUMMON);
        this.stopAnimation(this, this.C_SPRINT);
        this.stopAnimation(this, this.SPRINT);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (accessor.equals(USING_SKILL) && this.isUsingSkill()) {
            this.stopAllAnimStates();
            switch (this.getSkillType()) {
                case 0:
                case 1:
                case 2: {
                    this.playAnimation(this, this.MAGIC_BALL);
                    break;
                }
                case 3:
                case 4:
                case 5: {
                    this.playAnimation(this, this.SUMMON);
                    break;
                }
                case 6: {
                    this.playAnimation(this, this.C_SPRINT);
                    break;
                }
                case 7: {
                    this.playAnimation(this, this.SPRINT);
                }
            }
        }
        super.onSyncedDataUpdated(accessor);
    }

    public void aiStep() {
        super.aiStep();
        if (this.isDeadOrDying()) {
            return;
        }
        boolean isCasting = this.isUsingSkill() && this.getSkillTick() <= 35;
        boolean isCloneCasting = this.isShadowClone() && this.tickCount <= 35;
        if (isCasting || isCloneCasting) {
            float angle = this.yBodyRot * (float) Math.PI / 180.0f;
            double dy = this.getY() + (double) this.getBbHeight() * 0.94;
            int factor = (80 - (this.isShadowClone() ? this.tickCount : this.getSkillTick())) / 10;
            int particles = factor > 0 ? this.getRandom().nextInt(factor) : 1;
            float sparkle = 1.0f - ((float) (this.isShadowClone() ? this.tickCount : this.getSkillTick()) + 1.0f) / 60.0f;
            sparkle *= sparkle;
            float red = 0.5f * sparkle;
            float grn = 0.5f * sparkle;
            float blu = 0.5f * sparkle;
            if (this.isShadowClone()) {
                if (this.getProjectileType() == 0) {
                    red = 0.99f * sparkle;
                    grn = 0.47f * sparkle;
                    blu = 0.0f * sparkle;
                } else if (this.getProjectileType() == 2) {
                    red = 0.75f * sparkle;
                    grn = 0.0f * sparkle;
                    blu = 0.99f * sparkle;
                } else {
                    red = 0.37f * sparkle;
                    grn = 0.99f * sparkle;
                    blu = 0.89f * sparkle;
                }
            } else if (this.isUsingSkill()) {
                if (this.getSkillType() == 1) {
                    red = 0.37f * sparkle;
                    grn = 0.99f * sparkle;
                    blu = 0.89f * sparkle;
                } else if (this.getSkillType() == 2) {
                    red = 0.99f * sparkle;
                    grn = 0.47f * sparkle;
                    blu = 0.0f * sparkle;
                } else if (this.getSkillType() == 3) {
                    red = 0.0f * sparkle;
                    grn = 0.0f * sparkle;
                    blu = 0.99f * sparkle;
                } else if (this.getSkillType() == 0) {
                    red = 0.75f * sparkle;
                    grn = 0.0f * sparkle;
                    blu = 0.99f * sparkle;
                } else if (this.getSkillType() == 4) {
                    red = 0.5f * sparkle;
                    grn = 0.5f * sparkle;
                    blu = 0.5f * sparkle;
                } else if (this.getSkillType() == 5) {
                    red = sparkle;
                    grn = 0.84f * sparkle;
                    blu = 0.0f * sparkle;
                }
            }
            for (int side = 0; side < 2; ++side) {
                float currentAngle = angle + (float) side * (float) Math.PI;
                double dx = this.getX() + (double) Mth.cos(currentAngle) * 0.65;
                double dz = this.getZ() + (double) Mth.sin(currentAngle) * 0.65;
                for (int j1 = 0; j1 < particles; ++j1) {
                    this.level().addParticle(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, red, grn, blu), dx + this.getRandom().nextGaussian() * 0.025, dy + this.getRandom().nextGaussian() * 0.025, dz + this.getRandom().nextGaussian() * 0.025, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    public void teleportToTarget(@Nullable Entity entity) {
        Vec3 dest = this.findVecInLOSOf(entity);
        double srcX = this.getX();
        double srcY = this.getY();
        double srcZ = this.getZ();
        if (dest != null && entity != null) {
            this.teleportToNoChecks(dest.x(), dest.y(), dest.z());
            this.getLookControl().setLookAt(entity, 100.0f, 100.0f);
            this.yBodyRot = this.getYRot();
            if (!this.getSensing().hasLineOfSight(entity)) {
                this.teleportToNoChecks(srcX, srcY, srcZ);
            }
        }
    }

    @Nullable
    public Vec3 findVecInLOSOf(@Nullable Entity targetEntity) {
        if (targetEntity == null) {
            return null;
        }
        double origX = this.getX();
        double origY = this.getY();
        double origZ = this.getZ();
        for (int i = 0; i < 100; ++i) {
            double tx = targetEntity.getX() + this.getRandom().nextGaussian() * 16.0;
            double ty = targetEntity.getY();
            double tz = targetEntity.getZ() + this.getRandom().nextGaussian() * 16.0;
            boolean destClear = this.randomTeleport(tx, ty, tz, true);
            boolean canSeeTargetAtDest = this.hasLineOfSight(targetEntity);
            this.teleportTo(origX, origY, origZ);
            if (!destClear || !canSeeTargetAtDest) continue;
            return new Vec3(tx, ty, tz);
        }
        return null;
    }

    private void teleportToNoChecks(double destX, double destY, double destZ) {
        double srcX = this.getX();
        double srcY = this.getY();
        double srcZ = this.getZ();
        this.teleportTo(destX, destY, destZ);
        this.makeTeleportTrail(srcX, srcY, srcZ, destX, destY, destZ);
        this.level().playSound(null, srcX, srcY, srcZ, TFSounds.LICH_TELEPORT.get(), this.getSoundSource(), 1.0f, 1.0f);
        this.playSound(TFSounds.LICH_TELEPORT.get(), 1.0f, 1.0f);
        this.gameEvent(GameEvent.TELEPORT);
        this.jumping = false;
    }

    public void makeTeleportTrail(double srcX, double srcY, double srcZ, double destX, double destY, double destZ) {
        int particles = 128;
        for (int i = 0; i < particles; ++i) {
            double trailFactor = (double) i / ((double) particles - 1.0);
            float f = (this.getRandom().nextFloat() - 0.5f) * 0.2f;
            float f1 = (this.getRandom().nextFloat() - 0.5f) * 0.2f;
            float f2 = (this.getRandom().nextFloat() - 0.5f) * 0.2f;
            double tx = srcX + (destX - srcX) * trailFactor + (this.getRandom().nextDouble() - 0.5) * (double) this.getBbWidth() * 2.0;
            double ty = srcY + (destY - srcY) * trailFactor + this.getRandom().nextDouble() * (double) this.getBbHeight();
            double tz = srcZ + (destZ - srcZ) * trailFactor + (this.getRandom().nextDouble() - 0.5) * (double) this.getBbWidth() * 2.0;
            this.level().addParticle(ParticleTypes.EFFECT, tx, ty, tz, f, f1, f2);
        }
    }

    public void removeClonesAndMinions() {
        if (!this.level().isClientSide()) {
            this.level().getEntitiesOfClass(AdherentHighPriestEntity.class, this.getBoundingBox().inflate(64.0)).stream().filter(e -> e.isShadowClone() && e.getMaster() == this).forEach(Entity::discard);
            this.level().getEntitiesOfClass(LoyalWraithEntity.class, this.getBoundingBox().inflate(64.0)).stream().filter(e -> e.master == this).forEach(Entity::discard);
        }
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer) {
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource source, boolean hitByPlayer) {
    }

    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!this.isShadowClone()) {
            this.bossInfo.addPlayer(player);
        }
    }

    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isShadowClone()) {
            return;
        }
        if (this.getPhase() == 2) {
            this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
            this.bossInfo.setColor(BossEvent.BossBarColor.WHITE);
            this.bossInfo.setOverlay(BossEvent.BossBarOverlay.PROGRESS);
        } else {
            this.bossInfo.setColor(BossEvent.BossBarColor.BLUE);
            ForceFieldCapabilityList.get(this).ifPresent(cap -> {
                int shields = cap.getFieldsLeft();
                if (shields > 0) {
                    this.bossInfo.setProgress((float) shields / 6.0f);
                    this.bossInfo.setOverlay(BossEvent.BossBarOverlay.NOTCHED_6);
                } else {
                    this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
                    this.bossInfo.setOverlay(BossEvent.BossBarOverlay.PROGRESS);
                }
            });
        }
    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && !this.isShadowClone()) {
            ForceFieldCapabilityList.get(this).ifPresent(cap -> {
                if (cap.getMaxFields() != 6) {
                    cap.setMaxFields(6);
                }
                if (!this.isRemoved() && !this.shieldInitialized) {
                    cap.replenishFields();
                    this.shieldInitialized = true;
                }
                this.setHasShield(cap.getFieldsLeft() > 0);
            });
            this.updatePhase();
        }
        if (this.tickCount == 1 && !this.level().isClientSide() && !this.isShadowClone()) {
            this.deathChestPos = EntityUtil.bossChestLocation(this);
            if (!this.level().getBlockState(this.deathChestPos).isAir()) {
                this.deathChestPos = this.deathChestPos.above();
            }
        }
        this.playAnimationIfStopped(this, this.IDLE);
        if (this.level().isClientSide() && this.deathTime < 30) {
            this.level().addParticle(TFParticleType.ANNIHILATE.get(), this.getX() + (this.random.nextDouble() - 0.5), this.getY() + 0.5 + this.random.nextDouble() * 0.4, this.getZ() + (this.random.nextDouble() - 0.5), 0.0, 0.03 + this.random.nextDouble() * 0.02, 0.0);
        }
        if (this.isShadowClone()) {
            this.getNavigation().stop();
            if (this.tickCount == 1) {
                this.bossInfo.removeAllPlayers();
            }
            if (this.tickCount == 1 && this.master != null) {
                ForceFieldCapabilityList.get(this.master).ifPresent(mCap -> ForceFieldCapabilityList.get(this).ifPresent(cCap -> {
                    cCap.setMaxFields(mCap.getMaxFields());
                    cCap.setFields(mCap.getFieldsLeft(), false);
                }));
                this.playAnimationIfStopped(this, this.MAGIC_BALL);
                MiscUtil.sendAnimationPacket(this.level(), 0, this.getId(), this.MAGIC_BALL.getId());
            }
            if (!this.level().isClientSide()) {
                LivingEntity target;
                if (this.tickCount == 1 && this.getProjectileType() == 0) {
                    this.setProjectileType(this.getRandom().nextInt(3));
                }
                if (this.tickCount == 3 && (target = this.getTarget()) != null) {
                    this.teleportToTarget(target);
                }
                if (this.tickCount == 40 && (target = this.getTarget()) != null) {
                    TFThrowable projectile = this.getProjectileType() == 1 ? new AnnihilationBolt(this.level(), this) : (this.getProjectileType() == 2 ? new ProverbsBolt(this.level(), this) : new LichBomb(this.level(), this));
                    this.playSound(TFSounds.LICH_SHOOT.get(), 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                    double d1 = target.getX() - this.getX();
                    double d2 = target.getY() - projectile.getY();
                    double d3 = target.getZ() - this.getZ();
                    float f = Mth.sqrt((float) (d1 * d1 + d3 * d3)) * 0.2f;
                    if (projectile instanceof ThrowableProjectile) {
                        ThrowableProjectile throwable = projectile;
                        throwable.shoot(d1, d2 + (double) f, d3, 0.6f, (float) (10 - this.level().getDifficulty().getId() * 4));
                    }
                    this.level().addFreshEntity(projectile);
                }
                if (this.tickCount >= 90) {
                    this.discard();
                }
            }
        }
    }

    public void die(DamageSource cause) {
        super.die(cause);
        Level level = this.level();
        if (level instanceof ServerLevel serverLevel) {
            if (!this.isShadowClone()) {
                this.bossInfo.setProgress(0.0f);
                for (ServerPlayer player : this.hurtBy) {
                    TFAdvancements.HURT_BOSS.get().trigger(player, this);
                }
                this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                IBossLootBuffer.saveDropsIntoBoss(this, TFLootTables.createLootParams(this, true, cause).create(LootContextParamSets.ENTITY), serverLevel);
            }
        }
    }

    protected void tickDeath() {
        Level animation2;
        this.playAnimationIfStopped(this, this.DEATH);
        ++this.deathTime;
        if (this.deathTime == 1) {
            ForceFieldCapabilityList.get(this).ifPresent(cap -> {
                int fields = cap.getFieldsLeft();
                for (int i = 0; i < fields; ++i) {
                    cap.setFields(cap.getFieldsLeft() - 1, true);
                }
            });
            this.removeClonesAndMinions();
            for (VzlingAnimation animation : this.getAnimations()) {
                if (this.shouldPlayAnimation(this, animation)) continue;
                this.stopAnimation(this, animation);
            }
        }
        if ((animation2 = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) animation2;
            int maxDeath = 155;
            if (this.deathTime == 1 || this.deathTime == 15) {
                this.playSound(SoundEvents.PHANTOM_HURT, this.getSoundVolume(), this.getVoicePitch());
            }
            if (this.deathTime == 30) {
                this.playSound(SoundEvents.PHANTOM_DEATH, this.getSoundVolume(), this.getVoicePitch());
            }
            if (this.deathTime >= 50 && this.deathTime <= 95) {
                float angle = (this.yBodyRot + 180.0f) * ((float) Math.PI / 180);
                double mx = Math.cos(angle) * 0.15;
                double mz = Math.sin(angle) * 0.15;
                for (int i = 0; i < 4; ++i) {
                    serverLevel.sendParticles((ParticleOptions) TFParticleType.ANNIHILATE.get(), this.getX() + (this.random.nextDouble() - 0.5), this.getY() + 1.0 + (this.random.nextDouble() - 0.5) * 2.5, this.getZ() + (this.random.nextDouble() - 0.5), 0, mx, 0.02, mz, 0.5);
                }
            }
            if (this.deathTime >= 50 && this.deathChestPos != null) {
                int i;
                boolean flag = this.deathTime >= maxDeath && !this.isRemoved();
                Vec3 start = this.position().add(0.0, 1.0, 0.0);
                Vec3 end = Vec3.atBottomCenterOf(this.deathChestPos).add(0.0, 0.5, 0.0);
                double factor = (double) (this.deathTime - 50) / 105.0;
                double powFactor = Math.pow(factor, 2.0) * 2.0;
                double expand = (Math.cos((factor + 0.5) * Math.PI * 2.0) + 1.0) * 0.5;
                Vec3 pPos = start.add(end.subtract(start).scale(Math.min(factor * 1.25, 1.0)));
                if (this.deathTime >= maxDeath - 3) {
                    for (i = 0; i < 40; ++i) {
                        serverLevel.sendParticles((ParticleOptions) (this.random.nextBoolean() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.POOF), end.x, end.y + (this.random.nextDouble() - 0.5) * 0.075 * (double) i, end.z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
                if (flag) {
                    for (i = 0; i < 16; ++i) {
                        serverLevel.sendParticles((ParticleOptions) ParticleTypes.POOF, start.x + (this.random.nextDouble() - 0.5) * 0.075 * (double) i, start.y + (this.random.nextDouble() - 0.5) * 0.075 * (double) i, start.z + (this.random.nextDouble() - 0.5) * 0.075 * (double) i, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
                for (double i2 = 0.0; i2 < 1.0; i2 += 0.2) {
                    serverLevel.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, pPos.x + Math.sin((powFactor + i2) * Math.PI * 2.0) * expand * 1.25, pPos.y, pPos.z + Math.cos((powFactor + i2) * Math.PI * 2.0) * expand * 1.25, 1, 0.0, 0.0, 0.0, 0.0);
                }
                if (flag) {
                    this.remove(Entity.RemovalReason.KILLED);
                }
            }
        }
    }

    public void remove(Entity.RemovalReason removalReason) {
        Level level;
        if (removalReason.equals(Entity.RemovalReason.KILLED) && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            if (!this.isShadowClone()) {
                BlockPos pos = this.deathChestPos != null ? this.deathChestPos : EntityUtil.bossChestLocation(this);
                IBossLootBuffer.depositDropsIntoChest(this, this.random.nextBoolean() ? TFBlocks.TWILIGHT_OAK_CHEST.get().defaultBlockState() : TFBlocks.CANOPY_CHEST.get().defaultBlockState(), pos, serverLevel);
            }
        }
        super.remove(removalReason);
    }

    public boolean shouldPlayAnimation(Entity entity, VzlingAnimation animation) {
        return animation == this.DEATH || super.shouldPlayAnimation(entity, animation) && this.isSpawned();
    }

    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && !this.isShadowClone()) {
            if (this.isRestrictionPointValid(this.level().dimension()) && this.level().isLoaded(this.getRestrictionPoint().pos())) {
                this.level().setBlockAndUpdate(this.getRestrictionPoint().pos(), ModBlocks.ADHERENT_BOSS_SPAWNER.get().defaultBlockState());
            }
            this.discard();
        } else {
            super.checkDespawn();
        }
    }

    public SkillManager<? extends ISkillEntity> getSkillManager() {
        return this.isShadowClone() ? this.cloneSkillManager : this.skillManager;
    }

    public net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
        return !this.isShadowClone() ? super.getDefaultLootTable() : net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;
    }

    public NonNullList<ItemStack> getItemStacks() {
        return this.dyingInventory;
    }

    public List<Integer> getSkillPool() {
        if (this.isShadowClone()) {
            return List.of();
        }
        if (this.getPhase() == 2) {
            return List.of(7, 6, 5);
        }
        if (!this.hasShield()) {
            if (this.countMyMinions() == 0) {
                return List.of(0, 1, 2, 3, 4);
            }
            return List.of(0, 1, 2, 3, 5);
        }
        return List.of(0, 1, 2, 3);
    }

    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.PHANTOM_HURT;
    }

    public SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    public void updatePhase() {
        if ((this.getHealth() <= this.getMaxHealth() / 2.0f || this.updatePhase) && (this.getPhase() != 2 || this.updatePhase) && this.isSpawned()) {
            this.setPhase(2);
        }
    }

    public void spawn() {
    }

    public boolean hasSpawnProcess() {
        return false;
    }

    @Nullable
    public GlobalPos getRestrictionPoint() {
        return this.getEntityData().get(HOME_POINT).orElse(null);
    }

    public void setRestrictionPoint(@Nullable GlobalPos pos) {
        this.getEntityData().set(HOME_POINT, Optional.ofNullable(pos));
    }

    public int getHomeRadius() {
        return 20;
    }
}

