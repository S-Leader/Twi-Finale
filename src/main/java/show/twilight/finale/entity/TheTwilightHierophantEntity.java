package show.twilight.finale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.capabilities.hierophant.HierophantShield;
import show.twilight.finale.capabilities.hierophant.IHierophantShield;
import show.twilight.finale.client.animation.TheTwilightHierophantAnimation;
import show.twilight.finale.entity.projectile.HierophantFragment;
import show.twilight.finale.entity.projectile.HierophantMeteor;
import show.twilight.finale.entity.projectile.HierophantThrownWeapon;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.init.ModParticle;
import show.twilight.finale.runtime.animation.VzlingAnimatable;
import show.twilight.finale.runtime.animation.VzlingAnimation;
import show.twilight.finale.runtime.entity.ISkillEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;
import show.twilight.finale.runtime.entity.skill.SkillManager;
import show.twilight.finale.world.HierophantArena;
import twilightforest.entity.boss.BaseTFBoss;
import twilightforest.entity.monster.Adherent;
import twilightforest.entity.projectile.IceBomb;
import twilightforest.init.*;
import twilightforest.network.ParticlePacket;
import twilightforest.util.entities.EntityUtil;

import java.util.*;

/**
 * Server-authoritative encounter; does not replace the native Final Boss Spawner.
 */
public class TheTwilightHierophantEntity extends BaseTFBoss implements ISkillEntity, VzlingAnimatable {
    public static final int IDLE_ACTION = 0, RETURN_FRAGMENTS = 2, FORMATION = 3, SUMMON = 5, BARRAGE = 6, SMOKE_MOVE = 7;
    public static final int INTRO = -1, SHIELD_BREAK = -2, DEATH = -3;
    public static final int FAN = 9, ICE_BOMB = 10, WEAPON_VOLLEY = 11, CHUCKLE = 12, AXE_SLAM = 13;
    public static final int SHIELD_CAST = 14, METEORS = 15, FIERY_QUAKE = 16, FIERY_THRUST = 17, REBIRTH = -4;
    public static final int SWORD_RISE = 0, SWORD_HOLD = 1, SWORD_DIVE = 2, SWORD_SMOKE = 3, SWORD_PLANTED = 4, SWORD_FINISH = 5;
    public static final int SWORD_IMPACT_HOLD_TICKS = 4;
    public static final int SWORD_COMBO_HITS = 5, SWORD_RISE_TICKS = 20, SWORD_HOLD_TICKS = 6, SWORD_SMOKE_TICKS = SWORD_IMPACT_HOLD_TICKS + 6, SWORD_BLAST_DELAY = 20;
    public static final int REBIRTH_DURATION = 140, SHIELD_CAST_DURATION = 200, METEOR_DURATION = 160, FIERY_QUAKE_DURATION = 360, FIERY_THRUST_DURATION = 60;
    public static final int METEOR_WINDUP_TICKS = 24, METEOR_RECOVERY_TICKS = 32;
    public static final int INTRO_DURATION = 100, BREAK_DURATION = 40, DEATH_DURATION = 200, TRANSFORM_DURATION = 30, BARRAGE_FIRE_TICKS = 100;
    public static final float BARRAGE_SPREAD_DEGREES = 5F;
    public static final float SHIELD_SIZE = 2.6F;
    public static final int BARRAGE_RAMP_TICKS = 160, BARRAGE_INITIAL_INTERVAL = 12, BARRAGE_MIN_INTERVAL = 2, BARRAGE_FAST_PROJECTILES = 50;
    public static final int SHIELD_BURST_SHOTS = 5, SHIELD_BURST_INTERVAL = 4;
    public static final double FIERY_THRUST_SPEED = 1.35 * 3, FIERY_THRUST_DISTANCE = 1.35 * 16 * 2;
    private static final AttributeModifier THRUST_STEP_HEIGHT = new AttributeModifier(TwilightFinaleMod.prefix("fiery_thrust_step"), 0.5, AttributeModifier.Operation.ADD_VALUE);
    private static final double PHASE_TWO_ORBIT_DISTANCE = 15;
    public static final int SUMMON_DELAY_TICKS = 60, FAN_DURATION = 24;
    public static final String ICE_BOMB_NO_SNOW_TAG = "FinaleHierophantIceBomb";
    public static final int ICE_THROW_TICK = 30, ICE_BOMB_DURATION = 54, WEAPON_THROW_COUNT = 30, AXE_WINDUP_TICKS = 40, CHUCKLE_DURATION = 40;
    public static final int AXE_SLAM_DURATION = 64, WEAPON_INITIAL_INTERVAL = 10, WEAPON_MIN_INTERVAL = 3;
    public static final float WEAPON_SPEED = 1.6F, AXE_SLAM_RADIUS = 7, AXE_SLAM_DAMAGE = 20;
    private static final EntityDataAccessor<Integer> SKILL_TYPE = data(EntityDataSerializers.INT), PHASE = data(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> USING_SKILL = data(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> WEAPON_COUNT = data(EntityDataSerializers.INT), WEAPON_LAST_THROW = data(EntityDataSerializers.INT), WEAPON_NEXT_THROW = data(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INTRO_TIME = data(EntityDataSerializers.INT), BREAK_TIME = data(EntityDataSerializers.INT), DEATH_TIME = data(EntityDataSerializers.INT), ATTACK_TIME = data(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SHIELD = data(EntityDataSerializers.FLOAT), SHIELD_MAX = data(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SHIELD_RED = data(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> AIM = data(EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> REBIRTH_TIME = data(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ARENA_READY = data(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_COMBAT_TARGET = data(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SWORD_STAGE = data(EntityDataSerializers.INT), SWORD_STAGE_TIME = data(EntityDataSerializers.INT), SWORD_HIT_INDEX = data(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SWORD_IMPACT_Y = data(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SMOKE_PHASING = data(EntityDataSerializers.BOOLEAN);

    private static <T> EntityDataAccessor<T> data(EntityDataSerializer<T> serializer) {
        return SynchedEntityData.defineId(TheTwilightHierophantEntity.class, serializer);
    }

    private AABB arena;
    private final HierophantShield cubeShield = new HierophantShield(this);
    private boolean shieldCollision, routeShieldDamage, reflectingDamage;
    private boolean moveCirclePlaced, resumeBarrage;
    private Vec3 lockedAim = Vec3.ZERO, moveDestination = Vec3.ZERO, dashDirection = Vec3.ZERO;
    private Vec3 moveOrigin = Vec3.ZERO;
    private Vec3 lastSafeSmokeLanding;
    private int nextSmokeRecoveryTick;
    private boolean checkLoadedSmokePosition = true;
    private double fieryThrustTravelled;
    private Vec3 previousThrustPosition = Vec3.ZERO;
    private int thrustStalledTicks;
    private int phaseTwoOrbitDirection;
    private Vec3 swordLanding = Vec3.ZERO;
    private double swordJumpSpeed, swordDiveStartY;
    private int swordStageSyncTick;
    private int nextAttack = BARRAGE, nextMissileTick = 1, fastMissiles, barrageTicks;
    private int introSyncTick, breakSyncTick, deathSyncTick, attackSyncTick;
    private int skillTick, lastPhaseTwoAttack = -1, feedbackTick = -20;
    private int rebirthSyncTick, shieldShotTick, queuedThirdAttack = METEORS;
    private int shieldBurstRemaining, shieldNextBurstTick;
    private boolean bypassRebirth, thirdPhaseUnlocked;
    private int monstrosityBridgeStage;
    private UUID encounterMonstrosity;
    private BlockPos trapChestPosition;
    private Vec3 finalDeathOrigin;
    private BlockPos finalRewardPosition;
    private int finalDeathExperience, finalDeathExperiencePaid;
    private HierophantArena.Lift roofLift;
    private Vec3 paceDestination = Vec3.ZERO;
    private final List<VzlingAnimation> animations = new ArrayList<>();
    private final Set<UUID> chargeHits = new HashSet<>();
    private final Set<UUID> summonedAdherents = new LinkedHashSet<>();

    private static final class SummoningCircle {
        final Vec3 position;
        int remaining;

        SummoningCircle(Vec3 position, int remaining) {
            this.position = position;
            this.remaining = remaining;
        }
    }

    private final List<SummoningCircle> summoningCircles = new ArrayList<>();
    public final VzlingAnimation IDLE = new VzlingAnimation(this, TwilightFinaleMod.prefix("the_twilight_hierophant"));
    public final SkillManager<TheTwilightHierophantEntity> skillManager = new SkillManager<>(this, List.of(attack(SHIELD_CAST, 0), attack(BARRAGE, 0), attack(SMOKE_MOVE, 0), attack(FAN, 0), attack(ICE_BOMB, 0), attack(WEAPON_VOLLEY, 0), attack(CHUCKLE, 0), attack(AXE_SLAM, 0), attack(METEORS, 0), attack(FIERY_QUAKE, 0), attack(FIERY_THRUST, 0)));

    public static int getActionDuration(int action) {
        return switch (action) {
            case INTRO -> INTRO_DURATION;
            case SHIELD_BREAK -> BREAK_DURATION;
            case DEATH -> DEATH_DURATION;
            case REBIRTH -> REBIRTH_DURATION;
            case SHIELD_CAST -> SHIELD_CAST_DURATION;
            case METEORS -> METEOR_DURATION;
            case FIERY_QUAKE -> FIERY_QUAKE_DURATION;
            case FIERY_THRUST -> FIERY_THRUST_DURATION;
            case FAN -> FAN_DURATION;
            case RETURN_FRAGMENTS, SUMMON -> 120;
            case FORMATION -> 122;
            case BARRAGE ->
                    BARRAGE_RAMP_TICKS + BARRAGE_FAST_PROJECTILES * BARRAGE_MIN_INTERVAL + BARRAGE_INITIAL_INTERVAL;
            case SMOKE_MOVE -> 40;
            case ICE_BOMB -> ICE_BOMB_DURATION;
            case WEAPON_VOLLEY -> WEAPON_THROW_COUNT * WEAPON_INITIAL_INTERVAL + 20;
            case CHUCKLE -> CHUCKLE_DURATION;
            case AXE_SLAM -> AXE_SLAM_DURATION;
            default -> 0;
        };
    }

    private static BaseSkill<TheTwilightHierophantEntity> attack(int id, int cooldown) {
        // The actual roof's diagonal is wider than the old circular arena.
        // validTarget still restricts combat to that roof, not this upper limit.
        return new BaseSkill<>(id, getActionDuration(id), 128, cooldown) {
            @Override
            public void tick(TheTwilightHierophantEntity boss) {
                boss.tickAttack(id, boss.getSkillTick());
            }

            @Override
            public void effect(TheTwilightHierophantEntity boss) {
            }
        };
    }

    public TheTwilightHierophantEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setCanPickUpLoot(false);
        xpReward = 100;
        if (FMLLoader.getDist().isClient()) IDLE.setDefinition(TheTwilightHierophantAnimation.IDLE);
        if (!level.isClientSide()) {
            float hp = HierophantShield.CAPACITY;
            cubeShield.restore(hp, hp);
        }
        setDropChance(EquipmentSlot.MAINHAND, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.FOLLOW_RANGE, 48);
    }

    @Override
    public ResourceKey<Structure> getHomeStructure() {
        return TFStructures.FINAL_CASTLE;
    }

    @Override
    public Block getDeathContainer(RandomSource random) {
        return TFBlocks.CANOPY_CHEST.get();
    }

    @Override
    public Block getBossSpawner() {
        return ModBlocks.TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get();
    }

    @Override
    public int getBossBarColor() {
        return 0x9933CC;
    }

    @Override
    public List<VzlingAnimation> getAnimations() {
        return animations;
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
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(SKILL_TYPE, -1);
        b.define(USING_SKILL, false);
        b.define(PHASE, 1);
        b.define(WEAPON_COUNT, 0);
        b.define(WEAPON_LAST_THROW, 0);
        b.define(WEAPON_NEXT_THROW, WEAPON_INITIAL_INTERVAL);
        b.define(INTRO_TIME, 0);
        b.define(BREAK_TIME, 0);
        b.define(DEATH_TIME, 0);
        b.define(ATTACK_TIME, 0);
        b.define(SHIELD, 200F);
        b.define(SHIELD_MAX, 200F);
        b.define(SHIELD_RED, false);
        b.define(AIM, new Vector3f());
        b.define(REBIRTH_TIME, 0);
        b.define(ARENA_READY, false);
        b.define(HAS_COMBAT_TARGET, false);
        b.define(SWORD_STAGE, SWORD_RISE);
        b.define(SWORD_STAGE_TIME, 0);
        b.define(SWORD_HIT_INDEX, 0);
        b.define(SWORD_IMPACT_Y, 0F);
        b.define(SMOKE_PHASING, false);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SkillManager<? extends ISkillEntity> getSkillManager() {
        return skillManager;
    }

    @Override
    public List<Integer> getSkillPool() {
        if (!isSpawned() || !entityData.get(ARENA_READY) || entityData.get(REBIRTH_TIME) > 0 || entityData.get(BREAK_TIME) > 0 || !validTarget(getTarget()))
            return List.of();
        return hasShield() ? List.of() : List.of(nextAttack);
    }

    @Override
    public void beginSkill(int id) {
        // A fan interruption/phase change must not make the boss solid inside
        // a wall or below the roof while a smoke move is still in progress.
        if (id != SMOKE_MOVE && isSmokePhasing() && !finishSmokeTravel()) return;
        entityData.set(SKILL_TYPE, id);
        skillTick = 0;
        entityData.set(ATTACK_TIME, 0);
        entityData.set(USING_SKILL, true);
        setThrustStepHeight(id == FIERY_THRUST);
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        chargeHits.clear();
        setNoGravity(false);
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        lockedAim = getTarget() == null ? position().add(getLookAngle().scale(8)) : getTarget().getEyePosition();
        aimAt(lockedAim);
        faceTarget();
        if (id == BARRAGE) {
            if (!resumeBarrage) {
                fastMissiles = 0;
                barrageTicks = 0;
                nextMissileTick = 1;
            }
            resumeBarrage = false;
        }
        if (id == SMOKE_MOVE) {
            moveOrigin = position();
            moveCirclePlaced = false;
            nextSmokeRecoveryTick = 0;
            Vec3 safeOrigin = smokeGroundAt(moveOrigin);
            if (safeOrigin != null) lastSafeSmokeLanding = safeOrigin;
            entityData.set(SMOKE_PHASING, true);
            // This must happen before the next super.tick(), not a tick later:
            // noPhysics would otherwise carry an old downward velocity through the floor.
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
            if (getPhase() == 4) queuedThirdAttack = switch (random.nextInt(8)) {
                case 6 -> FIERY_QUAKE;
                case 7 -> FIERY_THRUST;
                default -> METEORS; // Six of eight rolls: meteors : dives : thrust = 6 : 1 : 1.
            };
            chooseMoveDestination();
        }
        if (id == FAN) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.PEACOCK_FEATHER_FAN.get()));
        }
        if (id == ICE_BOMB) setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.ICE_BOMB.get()));
        if (id == WEAPON_VOLLEY) {
            entityData.set(WEAPON_COUNT, 0);
            entityData.set(WEAPON_LAST_THROW, 0);
            entityData.set(WEAPON_NEXT_THROW, WEAPON_INITIAL_INTERVAL);
            prepareKnightWeapon(InteractionHand.MAIN_HAND);
        }
        if (id == CHUCKLE) playSound(SoundEvents.WITCH_CELEBRATE, 1, 0.9F);
        if (id == AXE_SLAM) setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.DIAMOND_MINOTAUR_AXE.get()));
        if (getPhase() == 4) setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItem.TWILIGHT_SWORD.get()));
        if (id == FIERY_QUAKE && level() instanceof ServerLevel server) {
            entityData.set(SWORD_HIT_INDEX, 0);
            startSwordJump(server);
        }
        if (id == FIERY_THRUST) {
            // Lock the ray when charging starts, not when it is released.
            dashDirection = lockedAim.subtract(position()).multiply(1, 0, 1).normalize();
            if (dashDirection.lengthSqr() < 1.0E-6) dashDirection = getLookAngle().multiply(1, 0, 1).normalize();
            if (dashDirection.lengthSqr() < 1.0E-6) dashDirection = new Vec3(1, 0, 0);
            faceThrustDirection();
            fieryThrustTravelled = 0;
            previousThrustPosition = position();
            thrustStalledTicks = 0;
            chargeHits.clear();
        }
        if (id == SHIELD_CAST) paceDestination = position();
        if (id == BARRAGE || id == ICE_BOMB || id == WEAPON_VOLLEY || id == CHUCKLE || id == AXE_SLAM)
            lastPhaseTwoAttack = id;
    }

    @Override
    public void endSkill() {
        if (isSmokePhasing() && !finishSmokeTravel()) return;
        entityData.set(SMOKE_PHASING, false);
        setThrustStepHeight(false);
        entityData.set(USING_SKILL, false);
        skillTick = 0;
        entityData.set(ATTACK_TIME, 0);
        cubeShield.resetMotion();
        setNoGravity(false);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        setItemInHand(InteractionHand.MAIN_HAND, getPhase() == 4 && thirdPhaseUnlocked && (entityData.get(REBIRTH_TIME) == 0 || entityData.get(REBIRTH_TIME) >= 110) && isAlive() ? new ItemStack(ModItem.TWILIGHT_SWORD.get()) : ItemStack.EMPTY);
        setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        if (getPhase() == 3) {
            super.tick();
            getNavigation().stop();
            setDeltaMovement(Vec3.ZERO);
            if (level() instanceof ServerLevel server) tickMonstrosityBridge(server);
            return;
        }
        if (!level().isClientSide()) {
            ensureArena();
            if (checkLoadedSmokePosition && isAlive() && isSpawned() && getPhase() >= 2 && entityData.get(ARENA_READY) && !isSmokePhasing() && tickCount >= nextSmokeRecoveryTick) {
                Vec3 safe = smokeExitGround();
                if (safe == null) {
                    safe = findSmokeRecoveryLanding();
                    if (safe != null) {
                        moveTo(safe.x, safe.y, safe.z, getYRot(), getXRot());
                        setDeltaMovement(Vec3.ZERO);
                        fallDistance = 0;
                    } else nextSmokeRecoveryTick = tickCount + 20;
                }
                if (safe != null) {
                    lastSafeSmokeLanding = safe;
                    checkLoadedSmokePosition = false;
                }
            }
            refreshCombatTarget();
            if (isAlive()) {
                if (entityData.get(HAS_COMBAT_TARGET)) cubeShield.tick();
                if (!hasShield() && getPhase() == 1) onShieldBroken();
            }
        }
        super.tick();
        if (isRemoved()) return;
        // Native target goals run inside super.tick(); reject creative/spectator
        // targets again before advancing any custom encounter behaviour.
        if (!level().isClientSide() && isAlive()) refreshCombatTarget();
        if (isUsingSkill()) skillTick++;
        if (!level().isClientSide() && isAlive() && entityData.get(ARENA_READY) && entityData.get(HAS_COMBAT_TARGET) && entityData.get(REBIRTH_TIME) == 0)
            skillManager.tick();
        if (isAlive()) IDLE.state.startIfStopped(tickCount);
        if (level() instanceof ServerLevel server && isAlive()) {
            if (roofLift != null && !roofLift.ready()) {
                endSkill();
                setDeltaMovement(Vec3.ZERO);
                updateBars();
                return;
            }
            if (entityData.get(REBIRTH_TIME) > 0) {
                if (entityData.get(HAS_COMBAT_TARGET)) tickRebirth(server);
                updateBars();
                return;
            }
            int intro = entityData.get(INTRO_TIME);
            if (intro < INTRO_DURATION) {
                entityData.set(INTRO_TIME, intro + 1);
                getNavigation().stop();
                setDeltaMovement(0, getDeltaMovement().y, 0);
                if (intro == 1) {
                    setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.FORTIFICATION_SCEPTER.get()));
                    setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItem.FORCE_FIELD_SCEPTER.get()));
                }
                if (intro >= 55 && intro < 85) smoke(server, 8, 1.2);
                if (intro == 60) updateShieldCollision();
                if (intro == INTRO_DURATION - 1) {
                    setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                }
            }
            if (entityData.get(BREAK_TIME) > 0) {
                entityData.set(BREAK_TIME, entityData.get(BREAK_TIME) - 1);
                smoke(server, 6, 1.2);
            }
            if (!entityData.get(HAS_COMBAT_TARGET)) {
                entityData.set(ATTACK_TIME, 0);
                updateBars();
                return;
            }
            if (hasShield() && isSpawned()) tickShieldCasting(server, ++shieldShotTick);
            entityData.set(ATTACK_TIME, hasShield() && isSpawned() ? shieldShotTick % 60 : isUsingSkill() ? getSkillTick() : 0);
            if (getAction() == SMOKE_MOVE) smoke(server, 10, 0.6);
            if (getPhase() == 4 && tickCount % 3 == 0 && random.nextFloat() < getBlur()) smoke(server, 2, 0.4);
            tickSummoningCircles(server);
            if (!isUsingSkill() && isSpawned() && entityData.get(BREAK_TIME) == 0 && validTarget(getTarget())) {
                faceTarget();
                getNavigation().stop();
            }
            constrainMovement();
            updateBars();
        }
    }

    private void refreshCombatTarget() {
        if (!isAlive()) return;
        if (!validTarget(getTarget())) setTarget(nearestArenaPlayer());
        boolean active = validTarget(getTarget());
        entityData.set(HAS_COMBAT_TARGET, active);
        if (active) return;
        if (isUsingSkill()) endSkill();
        resumeBarrage = false;
        getNavigation().stop();
        setNoGravity(isSmokePhasing());
        setDeltaMovement(isSmokePhasing() ? Vec3.ZERO : new Vec3(0, getDeltaMovement().y, 0));
        // Wait in place like the native Lich. BaseTFBoss still owns peaceful
        // despawning, persistence, death and loot; no empty-arena respawn loop.
    }

    @Override
    protected float tickHeadTurn(float yaw, float movement) {
        if (isUsingSkill() || hasShield() && isSpawned()) {
            // Keep the client body aligned too; the idle body controller can
            // otherwise leave a stationary caster facing its previous heading.
            setYBodyRot(getYRot());
            setYHeadRot(getYRot());
            return movement;
        }
        return super.tickHeadTurn(yaw, movement);
    }

    private void faceTarget() {
        LivingEntity target = getTarget();
        if (target == null) return;
        lookAt(target, 360F, 360F);
        setYBodyRot(getYRot());
        setYHeadRot(getYRot());
    }

    private boolean validTarget(LivingEntity target) {
        return level().getDifficulty() != Difficulty.PEACEFUL && target != null && target.isAlive() && !target.isSpectator() && !(target instanceof Player p && p.isCreative()) && insideArena(target.position());
    }

    public boolean isSpawned() {
        return entityData.get(ARENA_READY) && entityData.get(INTRO_TIME) >= INTRO_DURATION;
    }

    public boolean hasShield() {
        return cubeShield.isActive();
    }

    public float getShieldHealth() {
        return cubeShield.getHealth();
    }

    public float getShieldOpacity() {
        return cubeShield.getOpacity();
    }

    public boolean isShieldRed() {
        return cubeShield.requiresPhysicalDamage();
    }

    public IHierophantShield getCubeShield() {
        return cubeShield;
    }

    /**
     * Network replica only: authoritative state lives in the capability.
     */
    public void syncShieldState() {
        if (level().isClientSide() || cubeShield == null) return;
        entityData.set(SHIELD, cubeShield.getHealth());
        entityData.set(SHIELD_MAX, cubeShield.getMaxHealth());
        entityData.set(SHIELD_RED, cubeShield.requiresPhysicalDamage());
        updateShieldCollision();
    }

    private Player nearestArenaPlayer() {
        if (!(level() instanceof ServerLevel server)) return null;
        return server.players().stream().filter(this::validTarget).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    public int getAction() {
        if (getPhase() == 3) return DEATH;
        if (isDeadOrDying()) return DEATH;
        if (!entityData.get(ARENA_READY)) return INTRO;
        if (entityData.get(REBIRTH_TIME) > 0) return REBIRTH;
        if (!isSpawned()) return INTRO;
        if (hasShield()) return entityData.get(HAS_COMBAT_TARGET) ? SHIELD_CAST : IDLE_ACTION;
        if (entityData.get(BREAK_TIME) > 0) return SHIELD_BREAK;
        return isUsingSkill() ? getSkillType() : IDLE_ACTION;
    }

    public int getActionTime() {
        int action = getAction();
        if (action == INTRO && !entityData.get(ARENA_READY)) return 0;
        int time = switch (action) {
            case INTRO -> predictedTime(entityData.get(INTRO_TIME), introSyncTick);
            case SHIELD_BREAK -> predictedTime(BREAK_DURATION - entityData.get(BREAK_TIME), breakSyncTick);
            case DEATH -> predictedTime(entityData.get(DEATH_TIME), deathSyncTick);
            case REBIRTH ->
                    entityData.get(HAS_COMBAT_TARGET) ? predictedTime(entityData.get(REBIRTH_TIME), rebirthSyncTick) : entityData.get(REBIRTH_TIME);
            default -> predictedTime(entityData.get(ATTACK_TIME), attackSyncTick);
        };
        return Mth.clamp(time, 0, getActionDuration(action));
    }

    private int predictedTime(int serverTime, int receivedTick) {
        // Metadata need not arrive every tick. Advance locally between updates
        // rather than repeatedly holding/jumping the pose at the packet rate.
        return serverTime + (level().isClientSide() ? Math.max(0, tickCount - receivedTick) : 0);
    }

    public float getBlur() {
        return getPhase() == 4 && entityData.get(REBIRTH_TIME) == 0 ? Mth.clamp(1 - getHealth() / getMaxHealth(), 0, 1) : 0;
    }

    public boolean hasThirdPhaseSword() {
        return getPhase() == 4 && (getAction() != REBIRTH || getActionTime() >= 110);
    }

    public Vec3 getBeamOrigin() {
        return position().add(0, 1.35, 0);
    }

    private static Vector3f vector(Vec3 p) {
        return new Vector3f((float) p.x, (float) p.y, (float) p.z);
    }

    private void aimAt(Vec3 end) {
        entityData.set(AIM, vector(end.subtract(getBeamOrigin())));
    }

    // Retain the vanilla/NeoForge damage pipeline and route its final health
    // subtraction into the shield. A breaking hit never overflows into body HP.
    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        routeShieldDamage = hasShield() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        try {
            super.actuallyHurt(source, amount);
        } finally {
            routeShieldDamage = false;
        }
    }

    @Override
    public void setHealth(float hp) {
        if (!routeShieldDamage) {
            if (!level().isClientSide() && hp <= 0 && getPhase() == 2 && !bypassRebirth && !thirdPhaseUnlocked && getHealth() > 0) {
                startMonstrosityPhase();
                return;
            }
            super.setHealth(hp);
            return;
        }
        float damage = Math.max(0, getHealth() - hp);
        if (damage <= 0) return;
        if (cubeShield.absorbDamage(damage) <= 0) return;
        if (level() instanceof ServerLevel server) smoke(server, 12, 1.4);
        if (!hasShield()) onShieldBroken();
    }

    private void onShieldBroken() {
        shieldBurstRemaining = 0;
        endSkill();
        setPhase(2);
        nextAttack = SMOKE_MOVE;
        entityData.set(BREAK_TIME, 0);
        updateShieldCollision();
        // Leave immediately, while the uncurling/recovery animation plays.
        if (validTarget(getTarget())) beginSkill(SMOKE_MOVE);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (monstrosityBridgeStage > 0 && getPhase() == 3 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
            return false;
        Entity attacker = source.getEntity();
        if (attacker == this || attacker instanceof Adherent || attacker != null && attacker.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(attacker.getPersistentData().getUUID("FinaleHierophantOwner")))
            return false;
        if ((!isSpawned() || entityData.get(REBIRTH_TIME) > 0 || !entityData.get(ARENA_READY)) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
            return false;
        if (hasShield() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            boolean magic = source.is(Tags.DamageTypes.IS_MAGIC) || source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
            if (magic == isShieldRed()) {
                if (attacker instanceof LivingEntity living && !reflectingDamage && !source.is(DamageTypes.THORNS) && Float.isFinite(amount) && amount > 0) {
                    reflectingDamage = true;
                    try {
                        living.hurt(damageSources().thorns(this), amount);
                    } finally {
                        reflectingDamage = false;
                    }
                }
                if (attacker instanceof ServerPlayer player && tickCount - feedbackTick >= 20) {
                    player.displayClientMessage(Component.translatable(isShieldRed() ? "message.twilightforest_finale.hierophant_requires_physical" : "message.twilightforest_finale.hierophant_requires_magic"), true);
                    feedbackTick = tickCount;
                }
                return false;
            }
        }
        boolean hurt;
        bypassRebirth = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        try {
            hurt = super.hurt(source, amount);
        } finally {
            bypassRebirth = false;
        }
        if (hurt && attacker instanceof Player && getPhase() == 4 && isAlive() && getAction() == METEORS)
            beginSkill(SMOKE_MOVE);
        return hurt;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return shieldCollision ? EntityDimensions.scalable(SHIELD_SIZE, SHIELD_SIZE).withEyeHeight(1.65F) : super.getDefaultDimensions(pose);
    }

    private void updateShieldCollision() {
        if (cubeShield == null) return; // Synced-data callbacks can run during superclass construction.
        boolean next = hasShield() && entityData.get(INTRO_TIME) >= 60 && !isDeadOrDying();
        if (next != shieldCollision) {
            shieldCollision = next;
            refreshDimensions();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(SKILL_TYPE) || key.equals(USING_SKILL)) {
            skillTick = 0;
            swordStageSyncTick = tickCount;
        }
        if (level().isClientSide()) {
            if (cubeShield != null && (key.equals(SHIELD) || key.equals(SHIELD_MAX) || key.equals(SHIELD_RED))) {
                cubeShield.applySnapshot(entityData.get(SHIELD), entityData.get(SHIELD_MAX), entityData.get(SHIELD_RED));
            }
            if (key.equals(INTRO_TIME)) introSyncTick = tickCount;
            if (key.equals(BREAK_TIME)) breakSyncTick = tickCount;
            if (key.equals(DEATH_TIME)) deathSyncTick = tickCount;
            if (key.equals(ATTACK_TIME)) attackSyncTick = tickCount;
            if (key.equals(REBIRTH_TIME)) rebirthSyncTick = tickCount;
            if (key.equals(SWORD_STAGE) || key.equals(SWORD_STAGE_TIME)) swordStageSyncTick = tickCount;
        }
        if (key.equals(SHIELD) || key.equals(INTRO_TIME) || key.equals(DEATH_TIME)) updateShieldCollision();
    }

    private void tickAttack(int id, int t) {
        if (!(level() instanceof ServerLevel server) || !validTarget(getTarget())) {
            endSkill();
            return;
        }
        getNavigation().stop();
        if (id == FIERY_THRUST) faceThrustDirection();
        else faceTarget();
        // All second-phase actions can be interrupted by a close player,
        // except the harmless chuckle and a fan gust already in progress.
        if (getPhase() == 2 && !hasShield() && id != CHUCKLE && id != FAN && !isSmokePhasing()) {
            Player nearest = nearestArenaPlayer();
            if (nearest != null && distanceToSqr(nearest) < 3.5 * 3.5 && hasLineOfSight(nearest)) {
                setTarget(nearest);
                beginSkill(FAN);
                return;
            }
        }
        if (id != SHIELD_CAST && id != SMOKE_MOVE && id != METEORS && id != FIERY_THRUST && id != FIERY_QUAKE)
            setDeltaMovement(0, getDeltaMovement().y, 0);
        switch (id) {
            case SHIELD_CAST -> tickShieldCasting(server, t);
            case METEORS -> tickMeteors(server, t);
            case FIERY_QUAKE -> tickSwordCombo(server);
            case FIERY_THRUST -> tickFieryThrust(server, t);
            case BARRAGE -> {
                int firingTime = ++barrageTicks;
                if (firingTime >= nextMissileTick) {
                    Vec3 forward = getTarget().getEyePosition().subtract(getBeamOrigin()).normalize();
                    server.addFreshEntity(new HierophantFragment(server, this, scatterDirection(forward), false));
                    float ramp = Mth.clamp(firingTime / (float) BARRAGE_RAMP_TICKS, 0, 1);
                    int interval = Mth.ceil(Mth.lerp(ramp, BARRAGE_INITIAL_INTERVAL, BARRAGE_MIN_INTERVAL));
                    nextMissileTick = firingTime + interval;
                    if (firingTime >= BARRAGE_RAMP_TICKS && ++fastMissiles >= BARRAGE_FAST_PROJECTILES) {
                        resumeBarrage = false;
                        finishAction(SMOKE_MOVE);
                    }
                }
            }
            case SMOKE_MOVE -> {
                if (getPhase() == 2 && !moveCirclePlaced && position().subtract(moveOrigin).horizontalDistanceSqr() > 0.04) {
                    moveCirclePlaced = true;
                    pruneSummonedAdherents(server);
                    if (summonedAdherents.size() + summoningCircles.size() < getSummonedAdherentLimit())
                        summoningCircles.add(new SummoningCircle(groundAt(moveOrigin), SUMMON_DELAY_TICKS));
                }
                fallDistance = 0;
                if (isSmokePhasing() && t >= 30) finishSmokeTravel();
                setNoGravity(isSmokePhasing());
                if (isSmokePhasing()) {
                    // Smoke crosses walls at a level height, then solidifies
                    // over a checked, unobstructed descent to the roof.
                    Vec3 to = new Vec3(moveDestination.x, Math.max(moveOrigin.y, moveDestination.y), moveDestination.z).subtract(position());
                    if (to.lengthSqr() > 1.0E-6) {
                        // Keep the larger arena's third-phase corner destinations
                        // reachable in the same flight window; phase two is unchanged.
                        double speed = smokeMoveSpeed();
                        Vec3 velocity = to.normalize().scale(Math.min(speed, to.length()));
                        if (insideArena(position().add(velocity)) && isAreaLoaded(getBoundingBox().expandTowards(velocity)))
                            setDeltaMovement(velocity);
                        else setDeltaMovement(Vec3.ZERO);
                    } else setDeltaMovement(Vec3.ZERO);
                } else setDeltaMovement(0, getDeltaMovement().y, 0);
                if (t >= getActionDuration(SMOKE_MOVE) - 1 && !isSmokePhasing()) {
                    resumeBarrage = false;
                    finishAction(getPhase() == 4 ? queuedThirdAttack : choosePhaseTwoAttack());
                }
            }
            case FAN -> {
                if (t == 8) blowPlayersAway(server);
                if (t >= FAN_DURATION - 1) finishAction(SMOKE_MOVE);
            }
            case ICE_BOMB -> {
                if (t == ICE_THROW_TICK) {
                    IceBomb bomb = new IceBomb(TFEntities.THROWN_ICE.get(), server, this);
                    bomb.getPersistentData().putBoolean(ICE_BOMB_NO_SNOW_TAG, true);
                    Vec3 start = getBeamOrigin().add(getLookAngle().scale(0.5)).add(0, 0.45, 0);
                    bomb.setPos(start);
                    Vec3 aim = getTarget().getEyePosition().subtract(start);
                    bomb.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.12, aim.z, 1.25F, 1);
                    server.addFreshEntity(bomb);
                    playSound(TFSounds.ICE_BOMB_FIRED.get(), 1, 0.8F);
                    setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                }
                if (t >= ICE_BOMB_DURATION - 1) finishAction(SMOKE_MOVE);
            }
            case WEAPON_VOLLEY -> tickWeaponVolley(server, t);
            case CHUCKLE -> {
                if (t >= CHUCKLE_DURATION - 1) finishAction(SMOKE_MOVE);
            }
            case AXE_SLAM -> {
                if (t == AXE_WINDUP_TICKS) throwSlamAxe(server);
                if (t >= AXE_SLAM_DURATION - 1) finishAction(SMOKE_MOVE);
            }
        }
    }

    private int choosePhaseTwoAttack() {
        int options = getHealth() > getMaxHealth() / 2 && lastPhaseTwoAttack != CHUCKLE ? 5 : 4;
        return switch (random.nextInt(options)) {
            case 0 -> BARRAGE;
            case 1 -> ICE_BOMB;
            case 2 -> WEAPON_VOLLEY;
            case 3 -> AXE_SLAM;
            default -> CHUCKLE;
        };
    }

    private void tickShieldCasting(ServerLevel server, int clock) {
        cubeShield.resetMotion();
        setNoGravity(false);
        // Slow, continuous pacing rather than a charge or a curled pose.
        if (clock % 80 == 1 || position().subtract(paceDestination).horizontalDistanceSqr() < 0.4) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            GlobalPos home = getRestrictionPoint();
            Vec3 candidate = groundAt(Vec3.atBottomCenterOf(home.pos()).add(Math.cos(angle) * 6, 0, Math.sin(angle) * 6));
            paceDestination = canStandAt(candidate) ? candidate : position();
        }
        Vec3 pace = paceDestination.subtract(position()).multiply(1, 0, 1);
        Vec3 velocity = pace.lengthSqr() < 0.1 ? Vec3.ZERO : pace.normalize().scale(0.075);
        if (server.noCollision(this, getBoundingBox().move(velocity)))
            setDeltaMovement(velocity.x, getDeltaMovement().y, velocity.z);
        else paceDestination = position();
        faceTarget();
        boolean explosive = false;
        if (clock % 60 == 30) {
            // A blue cast starts five successive shots, never simultaneous spread.
            explosive = isShieldRed();
            if (!explosive) {
                shieldBurstRemaining = SHIELD_BURST_SHOTS;
                shieldNextBurstTick = clock;
            }
        }
        boolean normal = shieldBurstRemaining > 0 && clock >= shieldNextBurstTick;
        if (!explosive && !normal) return;
        if (normal) {
            shieldBurstRemaining--;
            shieldNextBurstTick = clock + SHIELD_BURST_INTERVAL; // 0.2 seconds between successive shots.
        }
        Player nearest = nearestArenaPlayer();
        if (nearest == null) return;
        setTarget(nearest);
        faceTarget();
        Vec3 forward = nearest.getEyePosition().subtract(getBeamOrigin()).normalize();
        if (explosive) {
            HierophantFragment fragment = new HierophantFragment(server, this, forward, false);
            fragment.setExplosive();
            server.addFreshEntity(fragment);
        } else {
            HierophantFragment fragment = new HierophantFragment(server, this, forward, false);
            fragment.setBypassesCooldown();
            server.addFreshEntity(fragment);
        }
    }

    private void startMonstrosityPhase() {
        endSkill();
        super.setHealth(1);
        setPhase(3);
        monstrosityBridgeStage = 1;
        entityData.set(DEATH_TIME, 1);
        summoningCircles.clear();
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        setNoAi(true);
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        getBossBar().setVisible(false);
        if (level() instanceof ServerLevel server) killSummonedAdherents(server);
    }

    private void tickMonstrosityBridge(ServerLevel server) {
        getBossBar().setVisible(false);
        if (monstrosityBridgeStage == 2) {
            // Upgrade old saves: transfer the previously hidden body into its loaded custodian.
            if (encounterMonstrosity != null && server.getEntity(encounterMonstrosity) instanceof CastleMonstrosityEntity beast && beast.storeHierophant(this))
                return;
            if (trapChestPosition != null && server.hasChunkAt(trapChestPosition)
                    && server.getBlockEntity(trapChestPosition) instanceof show.twilight.finale.block.entity.TwilightTrapChestBlockEntity chest)
                chest.storeHierophant(this);
            return;
        }
        if (monstrosityBridgeStage != 1) return;
        int t = entityData.get(DEATH_TIME) + 1;
        entityData.set(DEATH_TIME, Math.min(t, TRANSFORM_DURATION));
        if (t < TRANSFORM_DURATION) return;
        BlockPos pos = blockPosition();
        for (int i = 0; i < 16 && !server.getBlockState(pos).canBeReplaced(); i++) pos = pos.above();
        if (!server.getBlockState(pos).canBeReplaced()) return;
        // Intermediate encounter block: never routed through boss loot/chest configuration.
        if (!server.setBlock(pos, ModBlocks.TWILIGHT_TRAP_CHEST.get().defaultBlockState().setValue(net.minecraft.world.level.block.ChestBlock.FACING, getDirection()), 3))
            return;
        if (server.getBlockEntity(pos) instanceof show.twilight.finale.block.entity.TwilightTrapChestBlockEntity chest) {
            encounterMonstrosity = chest.getMonstrosityUUID();
            trapChestPosition = pos.immutable();
            monstrosityBridgeStage = 2;
            smoke(server, 90, 0.65);
            server.sendParticles(TFParticleType.ANNIHILATE.get(), getX(), getY() + 0.7, getZ(), 45, 0.5, 0.45, 0.5, 0.04);
            playSound(TFSounds.BOSS_CHEST_APPEAR.get(), 2, 1);
            chest.storeHierophant(this);
        }
    }

    public boolean isWaitingForMonstrosity(UUID beast) {
        return getPhase() == 3 && monstrosityBridgeStage == 2 && beast.equals(encounterMonstrosity);
    }

    public static boolean restoreAfterMonstrosity(ServerLevel server, UUID ownerId, UUID beast, CompoundTag data, Vec3 at) {
        Entity existing = server.getEntity(ownerId);
        if (existing instanceof TheTwilightHierophantEntity owner) {
            if (owner.getPhase() == 4 && beast.equals(owner.encounterMonstrosity)) return true;
            return owner.resumeAfterMonstrosity(beast, at);
        }
        if (existing != null || data == null) return false;
        TheTwilightHierophantEntity owner = ModEntity.THE_TWILIGHT_HIEROPHANT.get().create(server);
        if (owner == null) return false;
        owner.load(data.copy());
        // Restore the original UUID, arena and encounter state, and heal before spawning.
        if (!ownerId.equals(owner.getUUID()) || !owner.resumeAfterMonstrosity(beast, at)) return false;
        return server.addFreshEntity(owner);
    }

    public boolean resumeAfterMonstrosity(UUID beast, Vec3 at) {
        if (!isWaitingForMonstrosity(beast)) return false;
        monstrosityBridgeStage = 0;
        setInvisible(false);
        noPhysics = false;
        setNoAi(false);
        // Rise from the fallen giant; use the existing glass-heart resurrection and final attacks.
        moveTo(at.x, at.y, at.z, getYRot(), 0);
        getBossBar().setVisible(true);
        startFinalPhase();
        return true;
    }

    private void startFinalPhase() {
        endSkill();
        thirdPhaseUnlocked = true;
        setPhase(4);
        super.setHealth(getMaxHealth()); // No die(), loot roll, advancement or conquered flag at phase two's end.
        entityData.set(REBIRTH_TIME, 1);
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        summoningCircles.clear();
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        if (level() instanceof ServerLevel server) {
            killSummonedAdherents(server);
            pushPlayersForRebirth(server);
            server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1.1, getZ(), 80, 0.45, 0.6, 0.45, 0.15);
            playSound(SoundEvents.GLASS_BREAK, 2, 0.8F);
        }
    }

    private void pushPlayersForRebirth(ServerLevel server) {
        for (Player player : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(6, 4, 6), this::validTarget)) {
            Vec3 away = player.position().subtract(position()).multiply(1, 0, 1);
            if (away.lengthSqr() > 36) continue;
            if (away.lengthSqr() < 1.0E-6) away = new Vec3(1, 0, 0);
            away = away.normalize();
            player.push(away.x * 1.8, 0.65, away.z * 1.8);
            player.hurtMarked = true;
        }
    }

    private void tickRebirth(ServerLevel server) {
        int t = entityData.get(REBIRTH_TIME);
        getNavigation().stop();
        faceTarget();
        setNoGravity(true);
        fallDistance = 0;
        double baseY = getRestrictionPoint() == null ? getY() : getRestrictionPoint().pos().getY();
        double desired = baseY + 2.5 * Mth.clamp(t / 70D, 0, 1);
        setDeltaMovement(0, Mth.clamp(desired - getY(), -0.06, 0.06), 0);
        if (t < 100 && t % 2 == 0) {
            double angle = t * 0.15;
            server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX() + Math.cos(angle), getY() + 1.1 + Math.sin(angle * 2) * 0.25, getZ() + Math.sin(angle), 3, 0.06, 0.06, 0.06, 0.025);
        }
        if (t == 110) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItem.TWILIGHT_SWORD.get()));
            playSound(SoundEvents.WITHER_SPAWN, 2, 1);
        }
        if (t >= REBIRTH_DURATION) {
            entityData.set(REBIRTH_TIME, 0);
            endSkill();
            nextAttack = METEORS;
        } else entityData.set(REBIRTH_TIME, t + 1);
    }

    public int getMeteorCount() {
        return switch (level().getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY -> 8;
            case NORMAL -> 12;
            case HARD -> 16;
        };
    }

    public int getMeteorInterval() {
        return switch (level().getDifficulty()) {
            case PEACEFUL, EASY -> 10;
            case NORMAL -> 8;
            case HARD -> 6;
        };
    }

    public int getMeteorRainEndTick() {
        return METEOR_WINDUP_TICKS + Math.max(0, getMeteorCount() - 1) * getMeteorInterval();
    }

    private void tickMeteors(ServerLevel server, int t) {
        Player close = server.players().stream().filter(this::validTarget).filter(player -> player.position().subtract(position()).horizontalDistanceSqr() < 3.5 * 3.5 && Math.abs(player.getY() - getY()) < 6).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (close != null) {
            setTarget(close);
            beginSkill(SMOKE_MOVE);
            return;
        }
        int rainEnd = getMeteorRainEndTick();
        double floor = groundAt(position()).y;
        setNoGravity(t < rainEnd + 14);
        fallDistance = 0;
        if (t < rainEnd + 14) {
            // A quick lift, then a restrained hover with the sword held overhead.
            double desired = floor + 4.5 + Math.sin(t * 0.16) * 0.1;
            Vec3 motion = new Vec3(0, Mth.clamp(desired - getY(), -0.3, 0.55), 0);
            setDeltaMovement(isAreaLoaded(getBoundingBox().expandTowards(motion)) ? motion : Vec3.ZERO);
        } else setDeltaMovement(0, Math.max(-0.55, getDeltaMovement().y), 0);
        if (t >= 12 && t <= rainEnd && t % 2 == 0) {
            float yaw = getYRot() * Mth.DEG_TO_RAD;
            double side = getMainArm() == HumanoidArm.LEFT ? 0.55 : -0.55;
            server.sendParticles(ParticleTypes.FLAME, getX() + Mth.cos(yaw) * side, getY() + getBbHeight() + 0.9, getZ() + Mth.sin(yaw) * side, 4, 0.09, 0.2, 0.09, 0.025);
        }
        if (t >= METEOR_WINDUP_TICKS && t <= rainEnd && (t - METEOR_WINDUP_TICKS) % getMeteorInterval() == 0) {
            List<ServerPlayer> players = server.players().stream().filter(this::validTarget).toList();
            if (!players.isEmpty()) {
                int shot = (t - METEOR_WINDUP_TICKS) / getMeteorInterval();
                Player target = players.get(shot % players.size());
                // Each new meteor locks the current position, but a formed
                // meteor never homes or moves its warning underneath a dodger.
                Vec3 landing = meteorGroundAt(target.position());
                if (landing != null) {
                    HierophantMeteor meteor = new HierophantMeteor(server, this, landing);
                    if (insideArena(meteor.position()) && isAreaLoaded(meteor.getBoundingBox()) && server.noCollision(meteor)) {
                        server.addFreshEntity(meteor);
                        playSound(SoundEvents.FIRECHARGE_USE, 0.9F, 0.8F + random.nextFloat() * 0.2F);
                    }
                }
            }
        }
        if (t >= rainEnd + METEOR_RECOVERY_TICKS) finishAction(SMOKE_MOVE);
    }

    public int getSwordComboStage() {
        return entityData.get(SWORD_STAGE);
    }

    public int getSwordComboIndex() {
        return entityData.get(SWORD_HIT_INDEX);
    }

    public int getSwordComboTime() {
        return predictedTime(entityData.get(SWORD_STAGE_TIME), swordStageSyncTick);
    }

    public boolean isSwordComboSmoke() {
        return getAction() == FIERY_QUAKE && (getSwordComboStage() == SWORD_SMOKE || getSwordComboIndex() > 0 && getSwordComboStage() == SWORD_RISE);
    }

    public boolean isSwordComboGrounded() {
        return getAction() == FIERY_QUAKE && switch (getSwordComboStage()) {
            case SWORD_SMOKE, SWORD_PLANTED, SWORD_FINISH -> true;
            default -> false;
        };
    }

    public double getSwordImpactY() {
        return entityData.get(SWORD_IMPACT_Y);
    }

    private void setSwordStage(int stage) {
        entityData.set(SWORD_STAGE, stage);
        entityData.set(SWORD_STAGE_TIME, 0);
    }

    private void startSwordJump(ServerLevel server) {
        Vec3 target = groundAt(clampToArena(getTarget() == null ? position() : getTarget().position()));
        swordLanding = groundAt(position());
        for (int i = 0; i < 17; i++) {
            double angle = i * Math.PI / 4, distance = i == 0 ? 0 : i <= 8 ? 1.5 : 3;
            Vec3 candidate = groundAt(clampToArena(target.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance)));
            if (canStandAt(candidate)) {
                swordLanding = candidate;
                break;
            }
        }
        swordJumpSpeed = Math.max(2.4, swordLanding.subtract(position()).horizontalDistance() / (SWORD_RISE_TICKS - 4D + SWORD_HOLD_TICKS));
        setSwordStage(SWORD_RISE);
        setNoGravity(true);
        fallDistance = 0;
        // Only the first strike visibly jumps. Later rises are smoke travel,
        // hidden until the next airborne hold/downward strike.
        if (getSwordComboIndex() == 0) playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1, 0.7F);
    }

    private void tickSwordCombo(ServerLevel server) {
        int stage = getSwordComboStage(), time = entityData.get(SWORD_STAGE_TIME);
        entityData.set(SWORD_STAGE_TIME, time + 1);
        fallDistance = 0;
        switch (stage) {
            case SWORD_RISE, SWORD_HOLD -> {
                setNoGravity(true);
                Vec3 horizontal = swordLanding.subtract(position()).multiply(1, 0, 1);
                Vec3 motion = stage == SWORD_RISE && time < 4 ? Vec3.ZERO : horizontal.normalize().scale(Math.min(swordJumpSpeed, horizontal.length()));
                double height = swordLanding.y + 5;
                Vec3 velocity = new Vec3(motion.x, Mth.clamp(height - getY(), -0.35, 0.65), motion.z);
                if (isAreaLoaded(getBoundingBox().expandTowards(velocity))) setDeltaMovement(velocity);
                else {
                    beginSkill(SMOKE_MOVE);
                    return;
                }
                if (getSwordComboIndex() > 0 && stage == SWORD_RISE) smoke(server, 8, 0.45);
                if (time % 3 == 0) for (int i = 0; i < 16; i++) {
                    double angle = i * Math.PI / 8;
                    server.sendParticles(ParticleTypes.FLAME, swordLanding.x + Math.cos(angle) * 1.4, swordLanding.y + 0.08, swordLanding.z + Math.sin(angle) * 1.4, 1, 0, 0, 0, 0);
                }
                if (stage == SWORD_RISE && time + 1 >= SWORD_RISE_TICKS) {
                    if (getSwordComboIndex() > 0) smoke(server, 20, 0.6);
                    setSwordStage(SWORD_HOLD);
                } else if (stage == SWORD_HOLD && time + 1 >= SWORD_HOLD_TICKS) {
                    swordDiveStartY = getY();
                    setSwordStage(SWORD_DIVE);
                    setDeltaMovement(velocity.x, -0.2, velocity.z);
                    playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.6F, 0.6F);
                }
            }
            case SWORD_DIVE -> {
                setNoGravity(true);
                if (time > 0 && onGround() && verticalCollisionBelow && swordDiveStartY - getY() > 0.05) {
                    setDeltaMovement(Vec3.ZERO);
                    setNoGravity(false);
                    // Position interpolation must not leave the rendered boss
                    // airborne after the authoritative impact starts dissolving.
                    entityData.set(SWORD_IMPACT_Y, (float) getY());
                    swordDiveImpact(server);
                    if (getSwordComboIndex() == SWORD_COMBO_HITS - 1) setSwordStage(SWORD_PLANTED);
                    else setSwordStage(SWORD_SMOKE);
                } else if (time >= 40) {
                    // No phantom impacts over a missing/destroyed floor.
                    beginSkill(SMOKE_MOVE);
                } else {
                    setDeltaMovement(0, time == 0 ? -1.2 : -2.8, 0);
                    server.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.6, getZ(), 6, 0.3, 0.5, 0.3, 0.01);
                }
            }
            case SWORD_SMOKE -> {
                setNoGravity(false);
                setDeltaMovement(0, getDeltaMovement().y, 0);
                // First show the actual grounded strike, then dissolve. Never
                // turn the model upright or start rising while it is visible.
                if (time == SWORD_IMPACT_HOLD_TICKS) smoke(server, 32, 0.8);
                if (time >= SWORD_IMPACT_HOLD_TICKS) smoke(server, 8, 0.65);
                if (time + 1 >= SWORD_SMOKE_TICKS) {
                    entityData.set(SWORD_HIT_INDEX, getSwordComboIndex() + 1);
                    startSwordJump(server);
                }
            }
            case SWORD_PLANTED -> {
                setNoGravity(false);
                setDeltaMovement(0, getDeltaMovement().y, 0);
                if (time % 3 == 0)
                    server.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.1, getZ(), 4, 0.35, 0, 0.35, 0);
                if (time + 1 >= SWORD_BLAST_DELAY) {
                    swordFinisherBlast(server);
                    setSwordStage(SWORD_FINISH);
                }
            }
            case SWORD_FINISH -> {
                setNoGravity(false);
                setDeltaMovement(0, getDeltaMovement().y, 0);
                if (time + 1 >= 12) beginSkill(SMOKE_MOVE);
            }
        }
    }

    private boolean canAttackFrom(LivingEntity victim, Vec3 origin) {
        return victim != this && victim.isAlive() && !victim.isSpectator() && !(victim instanceof Player player && player.isCreative()) && !isAlliedTo(victim) && insideArena(victim.position()) && level().clip(new ClipContext(origin, victim.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private void swordDiveImpact(ServerLevel server) {
        Vec3 centre = position();
        playSound(TFSounds.MINOSHROOM_SLAM.get(), 1.7F, getSwordComboIndex() == 4 ? 0.65F : 1);
        gameEvent(GameEvent.HIT_GROUND);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3, 2, 3), entity -> canAttackFrom(entity, centre.add(0, 0.4, 0)))) {
            if (victim.position().subtract(centre).horizontalDistanceSqr() > 9) continue;
            // Preserve the former sword shock's 12 damage, now as a close landing hit.
            if (victim.hurt(TFDamageTypes.getEntityDamageSource(server, TFDamageTypes.SLAM, this), 12)) {
                victim.push(0, 0.35, 0);
                victim.hurtMarked = true;
            }
        }
        BlockPos floor = blockPosition().below();
        if (server.hasChunkAt(floor)) {
            var state = server.getBlockState(floor);
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), getX(), getY() + 0.1, getZ(), 35, 1.1, 0.15, 1.1, 0.15);
        }
    }

    private void swordFinisherBlast(ServerLevel server) {
        Vec3 centre = position();
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 2, 0.8F);
        gameEvent(GameEvent.EXPLODE);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(10, 3, 10), entity -> canAttackFrom(entity, centre.add(0, 0.5, 0)))) {
            if (victim.position().subtract(centre).horizontalDistanceSqr() > 100) continue;
            if (victim.hurt(TFDamageTypes.getEntityDamageSource(server, TFDamageTypes.HYDRA_FIRE, this), 12)) {
                victim.igniteForSeconds(5);
                Vec3 away = victim.position().subtract(centre).multiply(1, 0, 1).normalize();
                victim.push(away.x * 0.8, 0.4, away.z * 0.8);
                victim.hurtMarked = true;
            }
        }
        ParticlePacket fire = new ParticlePacket();
        for (int ring = 1; ring <= 5; ring++)
            for (int i = 0; i < 40; i++) {
                double angle = i * Math.PI / 20, radius = ring * 2;
                Vec3 at = centre.add(Math.cos(angle) * radius, 0.15, Math.sin(angle) * radius);
                if (!insideArena(at) || !server.hasChunkAt(BlockPos.containing(at))) continue;
                fire.queueParticle(ParticleTypes.FLAME, false, at, new Vec3(Math.cos(angle) * 0.2, 0.15, Math.sin(angle) * 0.2));
            }
        PacketDistributor.sendToPlayersTrackingEntity(this, fire);
        server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.6, getZ(), 1, 0, 0, 0, 0);
        // Visual/fire damage only: do not destroy or ignite the actual castle floor.
    }

    private void tickFieryThrust(ServerLevel server, int t) {
        if (t < 20) {
            setDeltaMovement(0, getDeltaMovement().y, 0);
            return;
        }
        if (t == 20) {
            previousThrustPosition = position();
        } else {
            Vec3 travelled = position().subtract(previousThrustPosition);
            fieryThrustTravelled += travelled.horizontalDistance();
            if (travelled.horizontalDistance() < 0.05 || horizontalCollision && travelled.horizontalDistance() < FIERY_THRUST_SPEED * 0.85)
                thrustStalledTicks++;
            else thrustStalledTicks = 0;
            // Damage follows the movement that really happened. Vanilla clips
            // the swept motion against walls and can step over the pedestal.
            AABB previousBox = getBoundingBox().move(previousThrustPosition.subtract(position()));
            AABB sweep = getBoundingBox().minmax(previousBox).inflate(0.4);
            Vec3 from = previousBox.getCenter(), to = getBoundingBox().getCenter();
            if (travelled.horizontalDistanceSqr() > 1.0E-6)
                for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, sweep, this::canAttackVictim)) {
                    AABB hitbox = victim.getBoundingBox().inflate(getBbWidth() / 2 + 0.4, getBbHeight() / 2 + 0.4, getBbWidth() / 2 + 0.4);
                    if (!hitbox.contains(from) && hitbox.clip(from, to).isEmpty() || !chargeHits.add(victim.getUUID()))
                        continue;
                    if (victim.hurt(damageSources().mobAttack(this), 14)) {
                        victim.igniteForSeconds(5);
                        victim.push(dashDirection.x * 1.6, 0.55, dashDirection.z * 1.6);
                        victim.hurtMarked = true;
                    }
                }
            previousThrustPosition = position();
        }
        if (fieryThrustTravelled >= FIERY_THRUST_DISTANCE - 0.02 || thrustStalledTicks >= 2 || t >= FIERY_THRUST_DURATION - 1) {
            beginSkill(SMOKE_MOVE); // No long stationary thrust pose after the dash.
            return;
        }
        double distance = Math.min(FIERY_THRUST_SPEED, FIERY_THRUST_DISTANCE - fieryThrustTravelled);
        if (arena != null) {
            double margin = getBbWidth() / 2 + 0.2;
            if (Math.abs(dashDirection.x) > 1.0E-6)
                distance = Math.min(distance, ((dashDirection.x > 0 ? arena.maxX - margin : arena.minX + margin) - getX()) / dashDirection.x);
            if (Math.abs(dashDirection.z) > 1.0E-6)
                distance = Math.min(distance, ((dashDirection.z > 0 ? arena.maxZ - margin : arena.minZ + margin) - getZ()) / dashDirection.z);
        }
        Vec3 velocity = dashDirection.scale(Math.max(0, distance));
        if (distance <= 0.02 || !isAreaLoaded(getBoundingBox().expandTowards(velocity).inflate(0, 1.1, 0))) {
            beginSkill(SMOKE_MOVE);
            return;
        }
        faceThrustDirection();
        // Keep charging along the locked ray beyond the player. Do not stop on
        // proximity, a fixed 16-tick window, or a preflight collision with stairs.
        setDeltaMovement(velocity.x, getDeltaMovement().y, velocity.z);
        server.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.5, getZ(), 12, 0.35, 0.35, 0.35, 0.03);
    }

    private void faceThrustDirection() {
        setYRot((float) Math.toDegrees(Math.atan2(-dashDirection.x, dashDirection.z)));
        setYBodyRot(getYRot());
        setYHeadRot(getYRot());
        setXRot(0);
    }

    private void setThrustStepHeight(boolean active) {
        var step = getAttribute(Attributes.STEP_HEIGHT);
        if (step == null) return;
        if (active) {
            if (!step.hasModifier(THRUST_STEP_HEIGHT.id())) step.addTransientModifier(THRUST_STEP_HEIGHT);
        } else step.removeModifier(THRUST_STEP_HEIGHT.id());
    }

    public int getWeaponCount() {
        return entityData.get(WEAPON_COUNT);
    }

    public float getWeaponThrowProgress(float time) {
        return Mth.clamp((time - entityData.get(WEAPON_LAST_THROW)) / Math.max(1F, entityData.get(WEAPON_NEXT_THROW) - entityData.get(WEAPON_LAST_THROW)), 0, 1);
    }

    private void prepareKnightWeapon(InteractionHand hand) {
        Item item = switch (random.nextInt(3)) {
            case 0 -> TFItems.KNIGHTMETAL_PICKAXE.get();
            case 1 -> TFItems.KNIGHTMETAL_SWORD.get();
            default -> TFItems.KNIGHTMETAL_AXE.get();
        };
        setItemInHand(hand, new ItemStack(item));
    }

    private void tickWeaponVolley(ServerLevel server, int t) {
        int count = getWeaponCount();
        if (count < WEAPON_THROW_COUNT && t >= entityData.get(WEAPON_NEXT_THROW)) {
            InteractionHand hand = count % 2 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack item = getItemInHand(hand).copy();
            throwKnightWeapon(server, item, hand);
            setItemInHand(hand, ItemStack.EMPTY);
            count++;
            entityData.set(WEAPON_COUNT, count);
            entityData.set(WEAPON_LAST_THROW, t);
            if (count == WEAPON_THROW_COUNT) {
                entityData.set(WEAPON_NEXT_THROW, t + 8); // Follow through, without a finisher.
            } else {
                int interval = Mth.ceil(Mth.lerp(count / (float) (WEAPON_THROW_COUNT - 1), WEAPON_INITIAL_INTERVAL, WEAPON_MIN_INTERVAL));
                entityData.set(WEAPON_NEXT_THROW, t + interval);
                prepareKnightWeapon(count % 2 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            }
        }
        if (count >= WEAPON_THROW_COUNT && t >= entityData.get(WEAPON_NEXT_THROW)) finishAction(SMOKE_MOVE);
    }

    private void throwKnightWeapon(ServerLevel server, ItemStack item, InteractionHand hand) {
        boolean pick = item.is(TFItems.KNIGHTMETAL_PICKAXE.get());
        HierophantThrownWeapon weapon = new HierophantThrownWeapon(server, this, item);
        float side = (getMainArm() == HumanoidArm.RIGHT ? 1 : -1) * (hand == InteractionHand.MAIN_HAND ? 1 : -1);
        Vec3 offset = getLookAngle().cross(new Vec3(0, 1, 0)).normalize().scale(side * 0.45);
        Vec3 start = getBeamOrigin().add(offset);
        weapon.setPos(start);
        Vec3 aim = getTarget().getEyePosition().subtract(start);
        weapon.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.08, aim.z, WEAPON_SPEED, 1);
        server.addFreshEntity(weapon);
        playSound(pick ? TFSounds.KNIGHT_PHANTOM_THROW_PICK.get() : TFSounds.KNIGHT_PHANTOM_THROW_AXE.get(), 1, 0.8F);
        gameEvent(GameEvent.PROJECTILE_SHOOT);
    }

    private void throwSlamAxe(ServerLevel server) {
        HierophantThrownWeapon axe = new HierophantThrownWeapon(server, this, new ItemStack(TFItems.DIAMOND_MINOTAUR_AXE.get()));
        axe.setSlamAxe();
        Vec3 start = getBeamOrigin().add(getLookAngle().scale(0.6)).add(0, 0.5, 0);
        axe.setPos(start);
        Vec3 aim = groundAt(getTarget().position()).add(0, 0.25, 0).subtract(start);
        double ticks = Mth.clamp(aim.horizontalDistance() / 2, 6, 32);
        double dragSum = (1 - Math.pow(0.99, ticks)) / 0.01;
        // Ballistic aim for the existing snowball drag/gravity, not homing.
        Vec3 velocity = new Vec3(aim.x / dragSum, (aim.y + 3 * (ticks - dragSum)) / dragSum, aim.z / dragSum);
        axe.shoot(velocity.x, velocity.y, velocity.z, (float) velocity.length(), 0);
        server.addFreshEntity(axe);
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        playSound(TFSounds.KNIGHT_PHANTOM_THROW_AXE.get(), 1.5F, 0.65F);
        gameEvent(GameEvent.PROJECTILE_SHOOT);
    }

    public void onThrownAxeImpact(ServerLevel server, Vec3 centre) {
        if (level() != server || !isAlive() || !insideArena(centre)) return;
        server.playSound(null, centre.x, centre.y, centre.z, TFSounds.MINOSHROOM_SLAM.get(), getSoundSource(), 2, 1);
        server.gameEvent(GameEvent.HIT_GROUND, centre, GameEvent.Context.of(this));
        AABB area = new AABB(centre.add(-AXE_SLAM_RADIUS, -2, -AXE_SLAM_RADIUS), centre.add(AXE_SLAM_RADIUS, 3, AXE_SLAM_RADIUS));
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area, entity -> canAttackFrom(entity, centre.add(0, 0.4, 0)))) {
            Vec3 away = victim.position().subtract(centre).multiply(1, 0, 1);
            // A ground shock: players can jump it, or leave the seven-block disk.
            if (away.lengthSqr() > AXE_SLAM_RADIUS * AXE_SLAM_RADIUS || !victim.onGround()) continue;
            if (victim.hurt(twilightforest.init.TFDamageTypes.getEntityDamageSource(server, twilightforest.init.TFDamageTypes.SLAM, this), AXE_SLAM_DAMAGE)) {
                Vec3 push = away.normalize().scale(0.8);
                victim.push(push.x, 0.6, push.z);
                victim.hurtMarked = true;
            }
        }
        ParticlePacket dust = new ParticlePacket();
        for (int i = 0; i < 84; i++) {
            double angle = i * Math.PI * 2 / 28;
            double radius = 2.3 + (i / 28) * 2.3;
            Vec3 point = centre.add(Math.cos(angle) * radius, 0.12, Math.sin(angle) * radius);
            dust.queueParticle(new BlockParticleOption(ParticleTypes.BLOCK, TFBlocks.CASTLE_BRICK.get().defaultBlockState()), false, point, new Vec3(Math.cos(angle) * 0.12, 0.12, Math.sin(angle) * 0.12));
        }
        PacketDistributor.sendToPlayersTrackingEntity(this, dust);
        dropCeilingBricks(server, centre);
    }

    private void dropCeilingBricks(ServerLevel server, Vec3 centre) {
        Set<BlockPos> used = new HashSet<>();
        for (int attempt = 0; attempt < 64 && used.size() < 12; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = Math.sqrt(random.nextDouble()) * AXE_SLAM_RADIUS;
            Vec3 column = attempt == 0 && getTarget() != null ? getTarget().position() : centre.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            if (column.subtract(centre).horizontalDistanceSqr() > AXE_SLAM_RADIUS * AXE_SLAM_RADIUS || !insideArena(new Vec3(column.x, centre.y, column.z)))
                continue;
            BlockPos base = BlockPos.containing(column.x, centre.y + 2, column.z);
            if (!server.hasChunkAt(base)) continue;
            for (int dy = 2; dy <= 24 && base.getY() + dy < server.getMaxBuildHeight(); dy++) {
                BlockPos ceiling = base.above(dy), spawn = ceiling.below();
                if (server.getBlockState(ceiling).getCollisionShape(server, ceiling).isEmpty() || !server.getBlockState(spawn).isAir() || !server.getBlockState(spawn.below()).isAir())
                    continue;
                if (used.add(spawn)) {
                    spawnCeilingBrick(server, spawn);
                }
                break;
            }
        }
        if (used.isEmpty() && roofLift != null && roofLift.ready()) {
            // The roof arena is open after its force-field removal. Keep
            // phase two's existing falling-brick attack instead of silently losing it.
            for (int attempt = 0; attempt < 48 && used.size() < 12; attempt++) {
                double angle = random.nextDouble() * Math.PI * 2, radius = Math.sqrt(random.nextDouble()) * AXE_SLAM_RADIUS;
                BlockPos spawn = BlockPos.containing(centre.x + Math.cos(angle) * radius, centre.y + 12, centre.z + Math.sin(angle) * radius);
                if (!insideArena(Vec3.atCenterOf(spawn)) || !server.hasChunkAt(spawn) || !server.getBlockState(spawn).isAir() || !used.add(spawn))
                    continue;
                spawnCeilingBrick(server, spawn);
            }
        }
    }

    private void spawnCeilingBrick(ServerLevel server, BlockPos spawn) {
        // Virtual debris in AIR: no real ceiling is removed, and nothing is placed/dropped on landing.
        FallingBlockEntity brick = FallingBlockEntity.fall(server, spawn, TFBlocks.CASTLE_BRICK.get().defaultBlockState());
        brick.disableDrop();
        brick.dropItem = false;
        brick.setHurtsEntities(2, 20);
        brick.getPersistentData().putBoolean("FinaleCeilingDebris", true);
        brick.getPersistentData().putUUID("FinaleHierophantOwner", getUUID());
    }

    private void finishAction(int next) {
        nextAttack = next;
        endSkill();
    }

    private void blowPlayersAway(ServerLevel server) {
        playSound(TFSounds.FAN_WHOOSH.get(), 1.4F, 1);
        Vec3 facing = getTarget().position().subtract(position()).multiply(1, 0, 1).normalize();
        if (facing.lengthSqr() < 1.0E-6) facing = getLookAngle().multiply(1, 0, 1).normalize();
        for (Player player : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(5, 2, 5), this::validTarget)) {
            Vec3 away = player.position().subtract(position()).multiply(1, 0, 1).normalize();
            if (away.lengthSqr() < 1.0E-6) away = facing;
            if (!hasLineOfSight(player) || facing.dot(away) < 0.25) continue;
            player.push(away.x * 1.8, 0.65, away.z * 1.8);
            player.hurtMarked = true;
        }
        for (int i = 0; i < 18; i++) {
            Vec3 wind = scatterDirection(facing).scale(0.8);
            server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.3, getZ(), 0, wind.x, 0.1 + wind.y, wind.z, 1);
        }
    }

    private Vec3 scatterDirection(Vec3 forward) {
        if (forward.lengthSqr() < 1.0E-6) forward = getLookAngle();
        Vec3 reference = Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = forward.cross(reference).normalize(), up = right.cross(forward).normalize();
        // Uniform solid-angle sampling; every shot stays inside the 5-degree
        // half-angle cone, unlike Gaussian inaccuracy with unbounded outliers.
        double cosine = Mth.lerp(random.nextDouble(), 1D, Math.cos(Math.toRadians(BARRAGE_SPREAD_DEGREES)));
        double sine = Math.sqrt(Math.max(0, 1 - cosine * cosine)), around = random.nextDouble() * Math.PI * 2;
        return forward.scale(cosine).add(right.scale(Math.cos(around) * sine)).add(up.scale(Math.sin(around) * sine)).normalize();
    }

    private boolean canAttackVictim(LivingEntity victim) {
        return victim != this && victim.isAlive() && !victim.isSpectator() && !(victim instanceof Player p && p.isCreative()) && !(victim instanceof twilightforest.entity.monster.Adherent) && insideArena(victim.position()) && hasLineOfSight(victim);
    }

    private void chooseMoveDestination() {
        moveDestination = position();
        if (getTarget() == null || !(level() instanceof ServerLevel server) || arena == null) return;
        List<LivingEntity> threats = new ArrayList<>();
        for (Player player : server.players()) if (validTarget(player)) threats.add(player);
        if (!threats.contains(getTarget())) threats.add(getTarget());
        Vec3 nearest = threats.stream().min(Comparator.comparingDouble(e -> e.position().subtract(position()).horizontalDistanceSqr())).orElse(getTarget()).position();
        Vec3 away = position().subtract(nearest).multiply(1, 0, 1);
        double initialDistance = nearestThreatDistanceSqr(position(), threats);
        double bestDistance = initialDistance + 0.25;
        double pathMinimum = Math.pow(Math.max(0, Math.sqrt(initialDistance) - 0.5), 2);
        boolean orbitTarget = getPhase() == 2;
        Vec3 orbitCentre = getTarget().position();
        double preferredOrbitAngle = 0, bestOrbitScore = Double.POSITIVE_INFINITY;
        if (orbitTarget) {
            if (phaseTwoOrbitDirection == 0) phaseTwoOrbitDirection = random.nextBoolean() ? 1 : -1;
            preferredOrbitAngle = Math.atan2(getZ() - orbitCentre.z, getX() - orbitCentre.x) + phaseTwoOrbitDirection * Math.PI / 3;
            // Approaching the 15-block ring is allowed; do not demand that each
            // move increases the separation and thereby drive the boss to an edge.
            pathMinimum = Math.pow(Math.min(Math.sqrt(initialDistance), 10), 2);
        }
        double margin = Math.max(getBbWidth() / 2 + 0.5, 2.5);
        double minX = arena.minX + margin, maxX = arena.maxX - margin;
        double minZ = arena.minZ + margin, maxZ = arena.maxZ - margin;
        double maxTravel = smokeMoveSpeed() * 29;
        double awayAngle = away.lengthSqr() < 1.0E-6 ? random.nextDouble() * Math.PI * 2 : Math.atan2(away.z, away.x);
        boolean corner = getPhase() == 4 && queuedThirdAttack == FIERY_THRUST;
        for (int i = 0; i < (corner ? 4 : 49); i++) {
            Vec3 seed;
            if (orbitTarget) {
                // Prefer the next point around the current player, not the arena.
                // Search both sides of that point when walls or other players
                // make it unsafe. Re-centre on the target on every relocation.
                double offset = i == 0 ? 0 : ((i + 1) / 2) * (i % 2 == 1 ? 1 : -1) * Math.PI / 24;
                double angle = preferredOrbitAngle + offset;
                seed = new Vec3(orbitCentre.x + Math.cos(angle) * PHASE_TWO_ORBIT_DISTANCE, getY(), orbitCentre.z + Math.sin(angle) * PHASE_TWO_ORBIT_DISTANCE);
            } else if (corner) {
                seed = new Vec3((i & 1) == 0 ? minX : maxX, getY(), (i & 2) == 0 ? minZ : maxZ);
            } else if (i == 0) {
                seed = position().add(Math.cos(awayAngle) * maxTravel, 0, Math.sin(awayAngle) * maxTravel);
            } else if (i <= 32) {
                int edge = (i - 1) / 8;
                double along = ((i - 1) % 8) / 7D;
                seed = switch (edge) {
                    case 0 -> new Vec3(Mth.lerp(along, minX, maxX), getY(), minZ);
                    case 1 -> new Vec3(maxX, getY(), Mth.lerp(along, minZ, maxZ));
                    case 2 -> new Vec3(Mth.lerp(along, maxX, minX), getY(), maxZ);
                    default -> new Vec3(minX, getY(), Mth.lerp(along, maxZ, minZ));
                };
            } else {
                seed = new Vec3(Mth.lerp(random.nextDouble(), minX, maxX), getY(), Mth.lerp(random.nextDouble(), minZ, maxZ));
            }
            Vec3 candidate = smokeGroundAt(clampToArena(seed));
            if (candidate == null) continue;
            Vec3 delta = candidate.subtract(position());
            if (delta.lengthSqr() > maxTravel * maxTravel)
                candidate = smokeGroundAt(clampToArena(position().add(delta.normalize().scale(maxTravel))));
            if (candidate == null) continue;
            double separation = nearestThreatDistanceSqr(candidate, threats);
            double orbitScore = 0;
            if (orbitTarget) {
                Vec3 relative = candidate.subtract(orbitCentre);
                double angle = Math.atan2(relative.z, relative.x) - preferredOrbitAngle;
                double angleError = Math.abs(Math.atan2(Math.sin(angle), Math.cos(angle)));
                orbitScore = Math.abs(relative.horizontalDistance() - PHASE_TWO_ORBIT_DISTANCE) * 8 + angleError * 2;
            }
            if ((orbitTarget ? orbitScore >= bestOrbitScore : separation <= bestDistance) || separation < 25 || candidate.subtract(position()).horizontalDistanceSqr() < 16 || !canStandAt(candidate))
                continue;
            Vec3 flight = new Vec3(candidate.x, Math.max(getY(), candidate.y), candidate.z);
            AABB arrival = getBoundingBox().move(flight.subtract(position()));
            // Walls along the route do not matter, but the point where smoke
            // becomes solid and its subsequent descent must remain clear.
            if (!isAreaLoaded(arrival) || !level().noCollision(this, arrival)) continue;
            delta = flight.subtract(position());
            boolean clear = true;
            int steps = Math.max(1, Mth.ceil(delta.length() / 0.75));
            for (int stepIndex = 1; stepIndex <= steps; stepIndex++) {
                Vec3 point = position().add(delta.scale(stepIndex / (double) steps));
                AABB step = getBoundingBox().move(point.subtract(position()));
                if (nearestThreatDistanceSqr(point, threats) < pathMinimum || !isAreaLoaded(step)) {
                    clear = false;
                    break;
                }
            }
            int descent = Mth.ceil((flight.y - candidate.y) / 0.75);
            for (int stepIndex = 1; clear && stepIndex <= descent; stepIndex++) {
                Vec3 point = flight.lerp(candidate, stepIndex / (double) descent);
                AABB step = getBoundingBox().move(point.subtract(position()));
                if (!isAreaLoaded(step) || !level().noCollision(this, step)) clear = false;
            }
            if (clear) {
                moveDestination = candidate;
                if (orbitTarget) bestOrbitScore = orbitScore;
                else bestDistance = separation;
            }
        }
    }

    private double smokeMoveSpeed() {
        return getPhase() == 4 && arena != null ? Math.max(1.8, Math.hypot(arena.getXsize(), arena.getZsize()) / 29D) : 0.9;
    }

    private static double nearestThreatDistanceSqr(Vec3 at, List<LivingEntity> threats) {
        double distance = Double.POSITIVE_INFINITY;
        for (LivingEntity threat : threats)
            distance = Math.min(distance, at.subtract(threat.position()).horizontalDistanceSqr());
        return distance;
    }

    public int getSummonedAdherentLimit() {
        return switch (level().getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY -> 2;
            case NORMAL -> 3;
            case HARD -> 5;
        };
    }

    private void pruneSummonedAdherents(ServerLevel server) {
        summonedAdherents.removeIf(id -> {
            Entity entity = server.getEntity(id);
            // Missing entities may be in unloaded chunks: keep their reserved
            // slot so unloading a minion cannot bypass the simultaneous cap.
            return entity != null && (!(entity instanceof Adherent) || !entity.isAlive() || entity.isRemoved());
        });
    }

    public boolean trackSummonedAdherent(Adherent minion) {
        if (getPhase() != 2 || !isAlive() || !(level() instanceof ServerLevel server)) return false;
        pruneSummonedAdherents(server);
        int limit = getSummonedAdherentLimit();
        if (summonedAdherents.contains(minion.getUUID())) {
            int index = 0;
            for (UUID id : summonedAdherents) {
                if (id.equals(minion.getUUID())) return index < limit;
                index++;
            }
        }
        if (summonedAdherents.size() >= limit) return false;
        summonedAdherents.add(minion.getUUID());
        return true;
    }

    public void forgetSummonedAdherent(UUID id) {
        summonedAdherents.remove(id);
    }

    private void killSummonedAdherents(ServerLevel server) {
        summoningCircles.clear();
        List<Adherent> owned = new ArrayList<>();
        // Include all loaded owned minions, not only those still within 48 blocks.
        for (Entity entity : server.getAllEntities())
            if (entity instanceof Adherent minion && minion.isAlive() && minion.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(minion.getPersistentData().getUUID("FinaleHierophantOwner")))
                owned.add(minion);
        for (Adherent minion : owned) minion.kill();
        summonedAdherents.clear();
    }

    private void tickSummoningCircles(ServerLevel server) {
        if (getPhase() != 2 || !isAlive()) {
            summoningCircles.clear();
            return;
        }
        var iterator = summoningCircles.iterator();
        while (iterator.hasNext()) {
            SummoningCircle circle = iterator.next();
            if (!insideArena(circle.position)) {
                iterator.remove();
                continue;
            }
            if (!server.hasChunkAt(BlockPos.containing(circle.position))) continue;
            if (tickCount % 3 == 0)
                drawCircle(server, circle.position, (SUMMON_DELAY_TICKS - circle.remaining) / 15D, 1.1);
            if (--circle.remaining > 0) continue;
            if (spawnCircleAdherent(server, circle.position) || circle.remaining < -40) iterator.remove();
        }
    }

    private boolean spawnCircleAdherent(ServerLevel server, Vec3 origin) {
        pruneSummonedAdherents(server);
        if (getPhase() != 2 || !isAlive() || summonedAdherents.size() >= getSummonedAdherentLimit()) return true;
        var minion = TFEntities.ADHERENT.get().create(server);
        if (minion == null) return true;
        for (int i = 0; i < 9; i++) {
            Vec3 at = groundAt(origin.add(i == 0 ? 0 : Math.cos(i * Math.PI / 4) * 1.5, 0, i == 0 ? 0 : Math.sin(i * Math.PI / 4) * 1.5));
            if (!insideArena(at) || !server.hasChunkAt(BlockPos.containing(at))) continue;
            minion.moveTo(at.x, at.y, at.z, random.nextFloat() * 360, 0);
            if (!isAreaLoaded(minion.getBoundingBox()) || !server.noCollision(minion) || !server.getBlockState(BlockPos.containing(at).below()).isFaceSturdy(server, BlockPos.containing(at).below(), Direction.UP) || !server.getFluidState(BlockPos.containing(at)).isEmpty())
                continue;
            minion.finalizeSpawn(server, server.getCurrentDifficultyAt(BlockPos.containing(at)), MobSpawnType.MOB_SUMMONED, null);
            minion.getPersistentData().putUUID("FinaleHierophantOwner", getUUID());
            LivingEntity initialTarget = getTarget() != null && getTarget().isAlive() ? getTarget() : nearestArenaPlayer();
            minion.setTarget(initialTarget);
            if (initialTarget != null) minion.getLookControl().setLookAt(initialTarget, 360, 360);
            minion.setPersistenceRequired();
            minion.setCanPickUpLoot(false);
            if (!trackSummonedAdherent(minion)) return true;
            if (server.addFreshEntity(minion)) {
                // Reapply after join events, before the first AI tick.
                minion.setTarget(initialTarget);
                return true;
            }
            forgetSummonedAdherent(minion.getUUID());
            return false;
        }
        return false;
    }

    private void ensureArena() {
        if (!(level() instanceof ServerLevel server)) return;
        GlobalPos home = getRestrictionPoint();
        if (home == null || !home.dimension().equals(level().dimension())) {
            home = GlobalPos.of(level().dimension(), blockPosition());
            setRestrictionPoint(home);
        }
        HierophantArena saved = HierophantArena.get(server);
        if (roofLift == null) roofLift = saved.attach(server, this, home.pos());
        if (roofLift != null) {
            if (tickCount % 20 == 0) saved.refreshBounds(server, roofLift);
            arena = roofLift.bounds();
            entityData.set(ARENA_READY, roofLift.ready());
        } else entityData.set(ARENA_READY, false);
    }

    public boolean insideArena(Vec3 at) {
        // Like the Lich, keep combat active on the surrounding walls too.
        // The two-block roof trim/parapet belongs to this encounter. Movement
        // still uses the original, walkable rectangle in clampToArena().
        return arena == null || arena.inflate(2, 0, 2).contains(at);
    }

    public boolean isAreaLoaded(AABB box) {
        if (!(level() instanceof ServerLevel server)) return true;
        for (int x = Mth.floor(box.minX) >> 4; x <= Mth.floor(box.maxX) >> 4; x++)
            for (int z = Mth.floor(box.minZ) >> 4; z <= Mth.floor(box.maxZ) >> 4; z++)
                if (server.getChunkSource().getChunkNow(x, z) == null) return false;
        return true;
    }

    public boolean isSmokePhasing() {
        return isAlive() && entityData.get(SMOKE_PHASING);
    }

    @Override
    public boolean isInWall() {
        return !isSmokePhasing() && super.isInWall();
    }

    @Override
    public void move(MoverType type, Vec3 motion) {
        if (!level().isClientSide() && arena != null) {
            if (isSmokePhasing()) {
                // Walls may be crossed, but the roof's walkable floor may not.
                motion = new Vec3(motion.x, Math.max(motion.y, arena.minY + 2 - getY()), motion.z);
            }
            Vec3 limited = clampToArena(position().add(motion)).subtract(position());
            // Also handles external knockback. Never snap a displaced boss back home.
            motion = new Vec3(Mth.clamp(limited.x, -Math.abs(motion.x) - 0.3, Math.abs(motion.x) + 0.3), Mth.clamp(limited.y, -Math.abs(motion.y) - 0.3, Math.abs(motion.y) + 0.3), Mth.clamp(limited.z, -Math.abs(motion.z) - 0.3, Math.abs(motion.z) + 0.3));
        }
        // Bypass collision only for smoke travel, not for normal attacks,
        // charging or landing. Restore even when movement exits exceptionally.
        boolean wasNoPhysics = noPhysics;
        if (isSmokePhasing()) noPhysics = true;
        try {
            super.move(type, motion);
        } finally {
            noPhysics = wasNoPhysics;
        }
    }

    private Vec3 clampToArena(Vec3 at) {
        if (arena == null) return at;
        double margin = getBbWidth() / 2 + 0.2;
        return new Vec3(Mth.clamp(at.x, arena.minX + margin, arena.maxX - margin), Mth.clamp(at.y, arena.minY, arena.maxY - getBbHeight()), Mth.clamp(at.z, arena.minZ + margin, arena.maxZ - margin));
    }

    private void constrainMovement() {
        if (arena == null || getAction() == FIERY_THRUST) return;
        Vec3 next = position().add(getDeltaMovement()), clamped = clampToArena(next);
        if (next.distanceToSqr(clamped) > 0.0001) {
            getNavigation().stop();
            Vec3 correction = clamped.subtract(position());
            setDeltaMovement(correction.lengthSqr() > 0.81 ? correction.normalize().scale(0.9) : correction);
        }
    }

    private Vec3 groundAt(Vec3 near) {
        GlobalPos home = getRestrictionPoint();
        BlockPos base = BlockPos.containing(near.x, home == null ? getY() : home.pos().getY(), near.z);
        if (!level().hasChunkAt(base)) return near;
        for (int dy = 3; dy >= -4; dy--) {
            BlockPos feet = base.offset(0, dy, 0);
            if (level().getBlockState(feet.below()).isFaceSturdy(level(), feet.below(), Direction.UP) && level().getBlockState(feet).getCollisionShape(level(), feet).isEmpty() && level().getFluidState(feet).isEmpty())
                return Vec3.atBottomCenterOf(feet);
        }
        return near;
    }

    /**
     * Smoke landing searches never descend into rooms beneath the main roof.
     */
    private Vec3 smokeGroundAt(Vec3 near) {
        if (!(level() instanceof ServerLevel) || arena == null) return null;
        int floor = Mth.ceil(arena.minY + 2);
        for (int y = floor + 3; y >= floor; y--) {
            Vec3 candidate = new Vec3(near.x, y, near.z);
            if (isSafeSmokeLanding(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSafeSmokeLanding(Vec3 at) {
        if (!(level() instanceof ServerLevel server) || arena == null || at.y < arena.minY + 2 - 1.0E-5 || at.y > arena.minY + 5 + 1.0E-5)
            return false;
        AABB body = getBoundingBox().move(at.subtract(position())).deflate(1.0E-5);
        // Combat targeting includes parapets; materialization must use the
        // actual main-roof rectangle, not that enlarged targeting volume.
        if (body.minX < arena.minX + 0.2 || body.maxX > arena.maxX - 0.2 || body.minZ < arena.minZ + 0.2 || body.maxZ > arena.maxZ - 0.2 || !isAreaLoaded(body) || !server.noCollision(this, body))
            return false;
        for (int x = Mth.floor(body.minX); x <= Mth.floor(body.maxX); x++) {
            for (int z = Mth.floor(body.minZ); z <= Mth.floor(body.maxZ); z++) {
                BlockPos feet = BlockPos.containing(x, at.y, z);
                if (!server.getBlockState(feet.below()).isFaceSturdy(server, feet.below(), Direction.UP) || !server.getFluidState(feet).isEmpty())
                    return false;
                // Heightmaps work even in dimensions without skylight. A tower
                // room has an opaque roof above it and is not an outdoor landing.
                if (server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) > at.y + 1.0E-5) return false;
            }
        }
        return true;
    }

    private Vec3 smokeExitGround() {
        if (!(level() instanceof ServerLevel server)) return null;
        Vec3 landing = smokeGroundAt(position());
        AABB descent = landing == null ? null : getBoundingBox().expandTowards(0, landing.y - getY(), 0).deflate(1.0E-5);
        return landing != null && getY() >= landing.y - 1.0E-5 && isAreaLoaded(descent) && server.noCollision(this, descent) ? landing : null;
    }

    private boolean finishSmokeTravel() {
        if (!isSmokePhasing()) return true;
        if (!(level() instanceof ServerLevel server)) return false;
        Vec3 landing = smokeExitGround();
        if (landing == null) {
            if (tickCount < nextSmokeRecoveryTick) return false;
            nextSmokeRecoveryTick = tickCount + 10;
            landing = findSmokeRecoveryLanding();
            if (landing == null) {
                // If the entire loaded roof was removed/obstructed, do not
                // force materialization in a wall or fall into the rooms below.
                setNoGravity(true);
                setDeltaMovement(Vec3.ZERO);
                return false;
            }
            smoke(server, 12, 0.5);
            moveTo(landing.x, landing.y, landing.z, getYRot(), getXRot());
            smoke(server, 12, 0.5);
        }
        lastSafeSmokeLanding = landing;
        fallDistance = 0;
        setDeltaMovement(Vec3.ZERO);
        entityData.set(SMOKE_PHASING, false);
        setNoGravity(false);
        return true;
    }

    private Vec3 findSmokeRecoveryLanding() {
        if (lastSafeSmokeLanding != null && isSafeSmokeLanding(lastSafeSmokeLanding)) return lastSafeSmokeLanding;
        if (isSafeSmokeLanding(moveDestination)) return moveDestination;
        Vec3 origin = smokeGroundAt(moveOrigin);
        if (origin != null) return origin;
        if (arena == null) return null;
        Vec3 best = null;
        double nearest = Double.POSITIVE_INFINITY;
        double margin = getBbWidth() / 2 + 0.2;
        for (int x = Mth.ceil(arena.minX + margin); x + 0.5 <= arena.maxX - margin; x++) {
            for (int z = Mth.ceil(arena.minZ + margin); z + 0.5 <= arena.maxZ - margin; z++) {
                Vec3 at = new Vec3(x + 0.5, arena.minY + 2, z + 0.5);
                double distance = at.subtract(position()).horizontalDistanceSqr();
                if (distance >= nearest) continue;
                Vec3 safe = smokeGroundAt(at);
                if (safe != null) {
                    best = safe;
                    nearest = distance;
                }
            }
        }
        return best;
    }

    private Vec3 meteorGroundAt(Vec3 target) {
        if (!insideArena(target) || !level().hasChunkAt(BlockPos.containing(target))) return null;
        double bottom = arena == null ? target.y - 8 : arena.minY;
        // Fences/walls are collision surfaces, not sturdy full-block floors.
        // Do not clamp an elevated target's meteor back onto the inner roof.
        var hit = level().clip(new ClipContext(target.add(0, 2, 0), new Vec3(target.x, bottom, target.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : null;
    }

    private boolean canStandAt(Vec3 at) {
        BlockPos pos = BlockPos.containing(at);
        return insideArena(at) && level().hasChunkAt(pos) && level().getBlockState(pos.below()).isFaceSturdy(level(), pos.below(), Direction.UP) && isAreaLoaded(getBoundingBox().move(at.subtract(position()))) && level().noCollision(this, getBoundingBox().move(at.subtract(position()))) && level().getFluidState(pos).isEmpty();
    }

    private void smoke(ServerLevel server, int count, double spread) {
        server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.2, getZ(), count, spread, 0.6, spread, 0.015);
    }

    private static void drawCircle(ServerLevel server, Vec3 at, double turn, double radius) {
        for (int i = 0; i < 20; i++) {
            double a = i * Math.PI / 10 + turn;
            server.sendParticles(ParticleTypes.WITCH, at.x + Math.cos(a) * radius, at.y + 0.08, at.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
            if (i % 4 == 0)
                server.sendParticles(ParticleTypes.ENCHANT, at.x, at.y + 1, at.z, 0, Math.cos(a) * radius, -0.9, Math.sin(a) * radius, 1);
        }
        // Pentagram inside the ring, sampled over time to keep packet counts bounded.
        for (int i = 0; i < 5; i++) {
            double a = i * Math.PI * 2 / 5, b = (i + 2) * Math.PI * 2 / 5;
            double f = (turn * 0.4) % 1;
            double x = Mth.lerp(f, Math.cos(a), Math.cos(b)) * radius, z = Mth.lerp(f, Math.sin(a), Math.sin(b)) * radius;
            server.sendParticles(ParticleTypes.WITCH, at.x + x, at.y + 0.08, at.z + z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    protected void tickBossBar() {
        updateBars();
    }

    private void updateBars() {
        // Like the native Lich: the same bar shows shield HP, then body HP.
        boolean shielded = getPhase() == 1 && hasShield();
        var bar = getBossBar();
        float progress = shielded ? getShieldHealth() / cubeShield.getMaxHealth() : getHealth() / getMaxHealth();
        bar.setProgress(Mth.clamp(progress, 0, 1));
        int color = shielded ? (isShieldRed() ? 0xFF3333 : 0x3366FF) : getBossBarColor();
        var overlay = shielded ? BossEvent.BossBarOverlay.NOTCHED_10 : BossEvent.BossBarOverlay.PROGRESS;
        if (bar.getBarColor() != color || bar.getOverlay() != overlay) bar.updateStyle(color, overlay, true);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        updateBars();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.DISCARDED && level() instanceof ServerLevel server) {
            summoningCircles.clear();
            for (Adherent minion : server.getEntitiesOfClass(Adherent.class, getBoundingBox().inflate(48), e -> e.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(e.getPersistentData().getUUID("FinaleHierophantOwner"))))
                minion.discard();
        }
        super.remove(reason);
    }

    @Override
    public void setRestrictionPoint(GlobalPos pos) {
        super.setRestrictionPoint(pos);
        arena = null;
        roofLift = null;
    }

    @Override
    public int getHomeRadius() {
        // Cover every roof corner for native callers that need a scalar radius.
        // The actual home/target boundary remains the roof rectangle below.
        return arena == null ? 40 : Mth.ceil(Math.sqrt(arena.getXsize() * arena.getXsize() + arena.getYsize() * arena.getYsize() + arena.getZsize() * arena.getZsize()));
    }

    @Override
    public boolean isMobWithinHomeArea(Entity entity) {
        return !isRestrictionPointValid(entity.level().dimension()) || insideArena(entity.position());
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return entity instanceof Adherent || entity.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(entity.getPersistentData().getUUID("FinaleHierophantOwner")) || super.isAlliedTo(entity);
    }

    @Override
    protected void dropEquipment() {
    } // The summoning prop is not a fifth reward.

    @Override
    public void die(DamageSource source) {
        if (dead) return;
        if (getPhase() == 4) {
            finalDeathOrigin = position();
            finalRewardPosition = EntityUtil.bossChestLocation(this);
            if (!isRestrictionPointValid(level().dimension())) {
                var ground = level().clip(new ClipContext(position().add(0, 0.5, 0), position().add(0, -32, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (ground.getType() == HitResult.Type.BLOCK) finalRewardPosition = ground.getBlockPos().above();
            }
        }
        super.die(source);
        if (!dead) return;
        if (level() instanceof ServerLevel server && getRestrictionPoint() != null)
            HierophantArena.get(server).defeated(getRestrictionPoint().pos());
        endSkill();
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        updateShieldCollision();
        if (getPhase() == 4) {
            setNoGravity(true);
            noPhysics = true;
            setDeltaMovement(Vec3.ZERO);
            playSound(SoundEvents.ENDER_DRAGON_DEATH, 3, 1.2F);
        }
        if (level() instanceof ServerLevel server)
            for (var minion : server.getEntitiesOfClass(twilightforest.entity.monster.Adherent.class, getBoundingBox().inflate(48), e -> e.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(e.getPersistentData().getUUID("FinaleHierophantOwner"))))
                minion.discard();
    }

    @Override
    protected void tickDeath() {
        if (getPhase() != 4) {
            super.tickDeath();
            return;
        }
        deathTime++;
        entityData.set(DEATH_TIME, deathTime);
        setNoGravity(true);
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        getNavigation().stop();
        if (!(level() instanceof ServerLevel server)) return;
        if (finalDeathOrigin == null) finalDeathOrigin = position().add(0, -(deathTime - 1) * 0.04, 0);
        setPos(finalDeathOrigin.add(0, Math.min(deathTime, DEATH_DURATION) * 0.04, 0));
        fallDistance = 0;
        if (deathTime % 3 == 0) {
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1, getZ(), 3 + deathTime / 25, 0.55, 0.9, 0.55, 0.03);
            server.sendParticles(ParticleTypes.ASH, getX(), getY() + 1, getZ(), 3 + deathTime / 20, 0.6, 0.9, 0.6, 0.02);
        }
        if (deathTime >= 180 && deathTime % 3 == 0)
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 1, 0.7, 0.8, 0.7, 0);
        if (deathTime >= 150 && deathTime % 5 == 0) {
            int desired = Mth.floor(finalDeathExperience * Mth.clamp((deathTime - 145) / 55F, 0, 1));
            int amount = Math.max(0, desired - finalDeathExperiencePaid);
            if (amount > 0 && server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) {
                ExperienceOrb.award(server, position(), amount);
                finalDeathExperiencePaid += amount;
            }
        }
        if (deathTime >= DEATH_DURATION) {
            remove(RemovalReason.KILLED); // Native boss inventory deposit still happens exactly once.
            gameEvent(GameEvent.ENTITY_DIE);
        }
    }

    @Override
    protected void dropExperience(Entity attacker) {
        if (getPhase() != 4) {
            super.dropExperience(attacker);
            return;
        }
        // Defer the normal 100 XP until the disintegration, instead of paying both here and later.
        if (level() instanceof ServerLevel server && !wasExperienceConsumed() && server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))
            finalDeathExperience = Math.max(0, net.neoforged.neoforge.event.EventHooks.getExperienceDrop(this, lastHurtByPlayer, getExperienceReward(server, attacker)));
    }

    @Override
    public boolean isDeathAnimationFinished() {
        return deathTime >= DEATH_DURATION;
    }

    @Override
    protected void postRemoval(ServerLevel server, RemovalReason reason) {
        boolean showReward = reason == RemovalReason.KILLED && shouldSpawnLoot();
        BlockPos chestBase = showReward ? finalRewardPosition != null ? finalRewardPosition : EntityUtil.bossChestLocation(this) : null;
        if (showReward && finalRewardPosition != null) {
            // Keep the native inventory/deposit rules, but pin the destination before the ascent.
            // Loading the dying boss may attach an arena home at its elevated position.
            twilightforest.entity.boss.IBossLootBuffer.depositDropsIntoChest(this,
                    getDeathContainer(random).defaultBlockState().setValue(net.minecraft.world.level.block.ChestBlock.FACING, getDirection()), finalRewardPosition, server);
        } else super.postRemoval(server, reason);
        if (!showReward) return;

        // Native deposit may move the chest upwards when its first position is
        // obstructed. Locate the deposited inventory; never invent a second chest.
        BlockPos chest = null;
        if (server.hasChunkAt(chestBase)) {
            for (int y = chestBase.getY(); y < server.getMaxBuildHeight(); y++) {
                BlockPos candidate = new BlockPos(chestBase.getX(), y, chestBase.getZ());
                if (server.getBlockEntity(candidate) instanceof Container container && containsNativeReward(container)) {
                    chest = candidate;
                    break;
                }
            }
        }
        if (chest == null) {
            server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 0.8, getZ(), 40, 0.5, 0.7, 0.5, 0.1);
            return;
        }
        Vec3 start = position().add(0, 0.9, 0), end = Vec3.atCenterOf(chest).add(0, 0.3, 0);
        ParticlePacket trail = new ParticlePacket();
        // Spawn one travelling cluster at the body, never the entire route at
        // once. The packet preserves this destination offset exactly; clients
        // animate the spiral even after the native boss entity has been removed.
        for (int i = 0; i < 48; i++)
            trail.queueParticle(ModParticle.HIEROPHANT_REWARD.get(), false, start, end.subtract(start));
        PacketDistributor.sendToPlayersTrackingEntity(this, trail);
    }

    private boolean containsNativeReward(Container container) {
        if (container.getContainerSize() < getItemStacks().size()) return false;
        boolean any = false;
        for (int i = 0; i < getItemStacks().size(); i++) {
            ItemStack expected = getItem(i), actual = container.getItem(i);
            if (expected.isEmpty()) {
                if (!actual.isEmpty()) return false;
            } else {
                any = true;
                if (!ItemStack.isSameItemSameComponents(expected, actual) || expected.getCount() != actual.getCount())
                    return false;
            }
        }
        return any;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (finalDeathOrigin != null)
            tag.put("FinalDeathOrigin", newDoubleList(finalDeathOrigin.x, finalDeathOrigin.y, finalDeathOrigin.z));
        if (finalRewardPosition != null) tag.putLong("FinalRewardPosition", finalRewardPosition.asLong());
        tag.putInt("FinalDeathExperience", finalDeathExperience);
        tag.putInt("FinalDeathExperiencePaid", finalDeathExperiencePaid);
        tag.putInt("HierophantEncounterVersion", 2);
        tag.putInt("MonstrosityBridgeStage", monstrosityBridgeStage);
        tag.putInt("MonstrosityDeathTime", entityData.get(DEATH_TIME));
        if (encounterMonstrosity != null) tag.putUUID("EncounterMonstrosity", encounterMonstrosity);
        if (trapChestPosition != null) tag.putLong("TrapChestPosition", trapChestPosition.asLong());
        tag.putInt("FinalePhase", getPhase());
        tag.putInt("HierophantHealthVersion", 2);
        tag.putBoolean("HierophantThirdUnlocked", thirdPhaseUnlocked);
        tag.putInt("HierophantRebirth", entityData.get(REBIRTH_TIME));
        tag.putInt("HierophantShieldClock", shieldShotTick);
        tag.putInt("HierophantShieldBurstRemaining", shieldBurstRemaining);
        tag.putInt("HierophantShieldNextBurst", shieldNextBurstTick);
        tag.putInt("HierophantLastAttack", lastPhaseTwoAttack);
        tag.putInt("HierophantIntro", entityData.get(INTRO_TIME));
        tag.put("HierophantShieldCapability", cubeShield.serializeNBT());
        tag.putInt("HierophantBreakTime", entityData.get(BREAK_TIME));
        ListTag savedMinions = new ListTag();
        for (UUID id : summonedAdherents) {
            CompoundTag saved = new CompoundTag();
            saved.putUUID("UUID", id);
            savedMinions.add(saved);
        }
        tag.put("HierophantAdherents", savedMinions);
        ListTag savedCircles = new ListTag();
        for (SummoningCircle circle : summoningCircles) {
            CompoundTag saved = new CompoundTag();
            saved.put("Pos", newDoubleList(circle.position.x, circle.position.y, circle.position.z));
            saved.putInt("Delay", Math.max(0, circle.remaining));
            savedCircles.add(saved);
        }
        tag.put("HierophantSummonCircles", savedCircles);
        if (arena != null)
            tag.put("HierophantArena", newDoubleList(arena.minX, arena.minY, arena.minZ, arena.maxX, arena.maxY, arena.maxZ));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        var deathOrigin = tag.getList("FinalDeathOrigin", net.minecraft.nbt.Tag.TAG_DOUBLE);
        if (deathOrigin.size() == 3)
            finalDeathOrigin = new Vec3(deathOrigin.getDouble(0), deathOrigin.getDouble(1), deathOrigin.getDouble(2));
        if (tag.contains("FinalRewardPosition")) finalRewardPosition = BlockPos.of(tag.getLong("FinalRewardPosition"));
        finalDeathExperience = Math.max(0, tag.getInt("FinalDeathExperience"));
        finalDeathExperiencePaid = Mth.clamp(tag.getInt("FinalDeathExperiencePaid"), 0, finalDeathExperience);
        float previousMax = getMaxHealth(), previousHealth = getHealth();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        // Old saved attributes otherwise override the new 200-HP default.
        // Preserve the remaining percentage, including zero for a dying boss.
        if (!tag.contains("HierophantHealthVersion") && previousMax > 0)
            setHealth(previousHealth / previousMax * getMaxHealth());
        setPhase(tag.contains("FinalePhase") ? tag.getInt("FinalePhase") : 1);
        if (getPhase() == 3 && !tag.contains("HierophantEncounterVersion")) setPhase(4);
        monstrosityBridgeStage = Mth.clamp(tag.getInt("MonstrosityBridgeStage"), 0, 2);
        if (tag.hasUUID("EncounterMonstrosity")) encounterMonstrosity = tag.getUUID("EncounterMonstrosity");
        if (tag.contains("TrapChestPosition")) trapChestPosition = BlockPos.of(tag.getLong("TrapChestPosition"));
        thirdPhaseUnlocked = tag.getBoolean("HierophantThirdUnlocked") || getPhase() == 4;
        entityData.set(REBIRTH_TIME, Mth.clamp(tag.getInt("HierophantRebirth"), 0, REBIRTH_DURATION));
        shieldShotTick = Math.max(0, tag.getInt("HierophantShieldClock"));
        shieldBurstRemaining = Mth.clamp(tag.getInt("HierophantShieldBurstRemaining"), 0, SHIELD_BURST_SHOTS);
        shieldNextBurstTick = Math.max(shieldShotTick, tag.getInt("HierophantShieldNextBurst"));
        lastPhaseTwoAttack = tag.contains("HierophantLastAttack") ? tag.getInt("HierophantLastAttack") : -1;
        // Migrate the old custom loot inventory once; native BaseTFBoss owns it now.
        if (!tag.contains("Items") && tag.contains("HierophantRewards"))
            ContainerHelper.loadAllItems(tag.getCompound("HierophantRewards"), getItemStacks(), registryAccess());
        if (tag.contains("HierophantIntro"))
            entityData.set(INTRO_TIME, Mth.clamp(tag.getInt("HierophantIntro"), 0, INTRO_DURATION));
        entityData.set(BREAK_TIME, Mth.clamp(tag.getInt("HierophantBreakTime"), 0, BREAK_DURATION));
        if (tag.contains("HierophantShieldCapability", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            cubeShield.deserializeNBT(tag.getCompound("HierophantShieldCapability"));
        } else {
            // Preserve partially depleted shields from pre-capability saves.
            float max = tag.contains("HierophantShieldMax") ? tag.getFloat("HierophantShieldMax") : 100.0F;
            float hp = tag.contains("HierophantShield") ? tag.getFloat("HierophantShield") : getPhase() >= 2 ? 0 : max;
            float replacement = HierophantShield.CAPACITY;
            cubeShield.restore(replacement, max > 0 ? hp / max * replacement : 0);
        }
        var bounds = tag.getList("HierophantArena", net.minecraft.nbt.Tag.TAG_DOUBLE);
        if (bounds.size() == 6 && bounds.getDouble(3) > bounds.getDouble(0) && bounds.getDouble(5) > bounds.getDouble(2))
            arena = new AABB(bounds.getDouble(0), bounds.getDouble(1), bounds.getDouble(2), bounds.getDouble(3), bounds.getDouble(4), bounds.getDouble(5));
        summonedAdherents.clear();
        var savedMinions = tag.getList("HierophantAdherents", net.minecraft.nbt.Tag.TAG_COMPOUND);
        if (getPhase() == 2) for (int i = 0; i < Math.min(savedMinions.size(), 256); i++) {
            CompoundTag saved = savedMinions.getCompound(i);
            if (saved.hasUUID("UUID")) summonedAdherents.add(saved.getUUID("UUID"));
        }
        summoningCircles.clear();
        var savedCircles = tag.getList("HierophantSummonCircles", net.minecraft.nbt.Tag.TAG_COMPOUND);
        int availableCircles = getPhase() == 2 ? Math.max(0, getSummonedAdherentLimit() - summonedAdherents.size()) : 0;
        for (int i = 0; i < Math.min(savedCircles.size(), availableCircles); i++) {
            CompoundTag saved = savedCircles.getCompound(i);
            var pos = saved.getList("Pos", net.minecraft.nbt.Tag.TAG_DOUBLE);
            if (pos.size() != 3) continue;
            Vec3 at = new Vec3(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
            if (Double.isFinite(at.x) && Double.isFinite(at.y) && Double.isFinite(at.z) && insideArena(at))
                summoningCircles.add(new SummoningCircle(at, Mth.clamp(saved.getInt("Delay"), 0, SUMMON_DELAY_TICKS)));
        }
        nextAttack = getPhase() == 4 ? METEORS : BARRAGE;
        resumeBarrage = false;
        endSkill(); // Cancel casts on reload; native BaseTFBoss restores home and loot.
        if (!hasShield() && getPhase() == 1) setPhase(2);
        entityData.set(DEATH_TIME, deathTime);
        updateShieldCollision();
        setCanPickUpLoot(false);
        if (getPhase() == 3 && monstrosityBridgeStage > 0) {
            entityData.set(DEATH_TIME, Math.clamp(tag.getInt("MonstrosityDeathTime"), 1, DEATH_DURATION));
            setNoAi(true);
            setNoGravity(true);
            setInvisible(monstrosityBridgeStage == 2);
            noPhysics = monstrosityBridgeStage == 2;
        }
        if (entityData.get(INTRO_TIME) < INTRO_DURATION && getPhase() == 1) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.FORTIFICATION_SCEPTER.get()));
            setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItem.FORCE_FIELD_SCEPTER.get()));
        }
        if (getPhase() == 4 && entityData.get(REBIRTH_TIME) > 0) {
            setNoGravity(true);
            if (entityData.get(REBIRTH_TIME) < 110) setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}
