package show.twilight.finale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.capabilities.hierophant.HierophantShield;
import show.twilight.finale.capabilities.hierophant.IHierophantShield;
import show.twilight.finale.client.animation.TheTwilightHierophantAnimation;
import show.twilight.finale.config.HierophantConfig;
import show.twilight.finale.entity.projectile.HierophantFragment;
import show.twilight.finale.entity.projectile.HierophantMeteor;
import show.twilight.finale.entity.projectile.HierophantThrownWeapon;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.init.ModParticle;
import show.twilight.finale.runtime.animation.VzlingAnimatable;
import show.twilight.finale.runtime.animation.VzlingAnimation;
import show.twilight.finale.runtime.entity.ISkillEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;
import show.twilight.finale.runtime.entity.skill.SkillManager;
import show.twilight.finale.world.HierophantArena;
import show.twilight.finale.world.castle.FinalCastleBossGazeboComponent;
import show.twilight.finale.world.castle.FinalCastleDungeonForgeRoomComponent;
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
    public static final int IDLE_ACTION = 0, CHARGE = 1, RETURN_FRAGMENTS = 2, FORMATION = 3, SUMMON = 5, BARRAGE = 6, SMOKE_MOVE = 7;
    public static final int INTRO = -1, SHIELD_BREAK = -2, DEATH = -3;
    public static final int SHIELD_STUN = 8, FAN = 9, ICE_BOMB = 10, WEAPON_VOLLEY = 11, CHUCKLE = 12, AXE_SLAM = 13;
    public static final int SHIELD_CAST = 14, METEORS = 15, FIERY_QUAKE = 16, FIERY_THRUST = 17, REBIRTH = -4;
    public static final int SWORD_PLANT_TICK = 12;
    public static final int FIRST_FIERY_QUAKE_TICK = SWORD_PLANT_TICK + 20, SECOND_FIERY_QUAKE_TICK = SWORD_PLANT_TICK + 60;
    public static final int REBIRTH_DURATION = 140, SHIELD_CAST_DURATION = 200, METEOR_DURATION = 180, FIERY_QUAKE_DURATION = SWORD_PLANT_TICK + 100, FIERY_THRUST_DURATION = 60;
    public static final int INTRO_DURATION = 100, BREAK_DURATION = 40, DEATH_DURATION = 160, BARRAGE_FIRE_TICKS = 100;
    public static final float BARRAGE_SPREAD_DEGREES = 5F;
    public static final float SHIELD_SIZE = 2.6F, SHIELD_MAX_SPIN = 84F;
    public static final double RUSH_INITIAL_SPEED = 1.9, RUSH_DECAY = 0.006, RUSH_WALL_RETENTION = 0.85;
    public static final int SHIELD_SPIN_UP_TICKS = 100, SHIELD_STUN_TICKS = IHierophantShield.DEFAULT_STOP_TICKS;
    public static final int BARRAGE_RAMP_TICKS = 160, BARRAGE_INITIAL_INTERVAL = 12, BARRAGE_MIN_INTERVAL = 2, BARRAGE_FAST_PROJECTILES = 50;
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
    private static final EntityDataAccessor<Float> SPIN_SPEED = data(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> RUSHING = data(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHIELD_RED = data(EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SHIELD_STOP_REMAINING = data(EntityDataSerializers.INT), SHIELD_STOP_DURATION = data(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> AIM = data(EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> REBIRTH_TIME = data(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ARENA_READY = data(EntityDataSerializers.BOOLEAN);

    private static <T> EntityDataAccessor<T> data(EntityDataSerializer<T> serializer) {
        return SynchedEntityData.defineId(TheTwilightHierophantEntity.class, serializer);
    }

    private final ServerBossEvent shieldBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
    private AABB arena;
    private final HierophantShield cubeShield = new HierophantShield(this);
    private boolean shieldCollision, routeShieldDamage, reflectingDamage;
    private boolean wallHitX, wallHitZ, pendingRelocation, moveCirclePlaced, resumeBarrage;
    private Vec3 lockedAim = Vec3.ZERO, moveDestination = Vec3.ZERO, dashDirection = Vec3.ZERO, previousDash = Vec3.ZERO;
    private Vec3 moveOrigin = Vec3.ZERO;
    private double rushSpeed;
    private float shieldAngle, shieldAngleO;
    private int nextAttack = BARRAGE, nextMissileTick = 1, fastMissiles, barrageTicks;
    private int introSyncTick, breakSyncTick, deathSyncTick, attackSyncTick, stopSyncTick;
    private int skillTick, lastPhaseTwoAttack = -1, feedbackTick = -20;
    private int rebirthSyncTick, emptyArenaTicks, shieldShotTick, queuedThirdAttack = METEORS;
    private int shieldBurstRemaining, shieldNextBurstTick;
    private boolean bypassRebirth, thirdPhaseUnlocked;
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
            case CHARGE -> 600;
            case SHIELD_STUN -> SHIELD_STUN_TICKS;
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
        return new BaseSkill<>(id, getActionDuration(id), 64, cooldown) {
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
            float hp = HierophantConfig.SHIELD_HEALTH.get().floatValue();
            cubeShield.restore(hp, hp);
        }
        setDropChance(EquipmentSlot.MAINHAND, 0);
        shieldBar.setVisible(false);
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
        b.define(SPIN_SPEED, 0F);
        b.define(RUSHING, false);
        b.define(SHIELD_STOP_REMAINING, 0);
        b.define(SHIELD_STOP_DURATION, SHIELD_STUN_TICKS);
        b.define(AIM, new Vector3f());
        b.define(REBIRTH_TIME, 0);
        b.define(ARENA_READY, false);
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
        entityData.set(SKILL_TYPE, id);
        skillTick = 0;
        entityData.set(ATTACK_TIME, 0);
        entityData.set(USING_SKILL, true);
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        chargeHits.clear();
        cubeShield.setRushing(false);
        wallHitX = wallHitZ = false;
        previousDash = position();
        setNoGravity(false);
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        lockedAim = getTarget() == null ? position().add(getLookAngle().scale(8)) : getTarget().getEyePosition();
        aimAt(lockedAim);
        faceTarget();
        if (id == CHARGE) {
            rushSpeed = 0;
            cubeShield.setSpinSpeed(0);
            setNoGravity(true);
        }
        if (id == SHIELD_STUN) {
            if (!cubeShield.isStopped()) cubeShield.stop(HierophantConfig.SHIELD_STOP_SECONDS.get() * 20);
            setDeltaMovement(0, Math.min(-0.15, getDeltaMovement().y), 0);
        }
        if (id == BARRAGE) {
            if (!resumeBarrage) {
                fastMissiles = 0;
                barrageTicks = 0;
                nextMissileTick = 1;
            }
            resumeBarrage = false;
        }
        if (id == SMOKE_MOVE) {
            pendingRelocation = false;
            moveOrigin = position();
            moveCirclePlaced = false;
            if (getPhase() == 3) queuedThirdAttack = switch (random.nextInt(3)) {
                case 0 -> METEORS;
                case 1 -> FIERY_QUAKE;
                default -> FIERY_THRUST;
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
        if (getPhase() == 3) setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.FIERY_SWORD.get()));
        if (id == FIERY_THRUST) {
            dashDirection = Vec3.ZERO;
            chargeHits.clear();
        }
        if (id == SHIELD_CAST) paceDestination = position();
        if (id == BARRAGE || id == ICE_BOMB || id == WEAPON_VOLLEY || id == CHUCKLE || id == AXE_SLAM)
            lastPhaseTwoAttack = id;
    }

    @Override
    public void endSkill() {
        entityData.set(USING_SKILL, false);
        skillTick = 0;
        entityData.set(ATTACK_TIME, 0);
        cubeShield.resetMotion();
        setNoGravity(false);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        setItemInHand(InteractionHand.MAIN_HAND, getPhase() == 3 && thirdPhaseUnlocked && (entityData.get(REBIRTH_TIME) == 0 || entityData.get(REBIRTH_TIME) >= 110) && isAlive() ? new ItemStack(TFItems.FIERY_SWORD.get()) : ItemStack.EMPTY);
        setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            ensureArena();
            if (isAlive()) {
                cubeShield.tick();
                if (!hasShield() && getPhase() == 1) onShieldBroken();
            }
            if (getTarget() != null && !validTarget(getTarget())) {
                setTarget(null);
                if (isUsingSkill()) endSkill();
            }
            if (getTarget() == null && isAlive()) setTarget(nearestArenaPlayer());
            if (pendingRelocation && getPhase() == 2 && isSpawned() && !hasShield() && entityData.get(BREAK_TIME) == 0 && getAction() != SMOKE_MOVE && getAction() != FAN && validTarget(getTarget())) {
                resumeBarrage |= isUsingSkill() && getSkillType() == BARRAGE;
                beginSkill(SMOKE_MOVE);
            }
        }
        super.tick();
        if (isRemoved()) return;
        if (isUsingSkill()) skillTick++;
        if (!level().isClientSide() && isAlive() && entityData.get(ARENA_READY) && entityData.get(REBIRTH_TIME) == 0)
            skillManager.tick();
        if (level().isClientSide()) {
            shieldAngleO = shieldAngle;
            shieldAngle += getShieldSpinSpeed();
        }
        if (isAlive()) IDLE.state.startIfStopped(tickCount);
        if (level() instanceof ServerLevel server && isAlive()) {
            if (roofLift != null && !roofLift.ready()) {
                endSkill();
                setDeltaMovement(Vec3.ZERO);
                updateBars();
                return;
            }
            if (entityData.get(REBIRTH_TIME) > 0) {
                tickRebirth(server);
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
            if (hasShield() && isSpawned()) tickShieldCasting(server, ++shieldShotTick);
            entityData.set(ATTACK_TIME, hasShield() && isSpawned() ? shieldShotTick % 60 : isUsingSkill() ? getSkillTick() : 0);
            if (getAction() == SMOKE_MOVE) smoke(server, 10, 0.6);
            if (getPhase() == 3 && tickCount % 3 == 0 && random.nextFloat() < getBlur()) smoke(server, 2, 0.4);
            tickSummoningCircles(server);
            if (!isUsingSkill() && isSpawned() && entityData.get(BREAK_TIME) == 0 && validTarget(getTarget())) {
                faceTarget();
                getNavigation().stop();
            }
            constrainMovement();
            updateBars();
            // Native reset and rewards keep the stationary arena's home.
            // Reopening the encounter reuses the same floor and home.
            if (isSpawned() && nearestArenaPlayer() == null) {
                if (++emptyArenaTicks >= 200 && getRestrictionPoint() != null) {
                    placeSpawner(getRestrictionPoint().pos());
                    discard();
                }
            } else emptyArenaTicks = 0;
        }
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

    public float getShieldSpinSpeed() {
        return cubeShield.getSpinSpeed();
    }

    public float getShieldRotation(float partial) {
        return Mth.lerp(partial, shieldAngleO, shieldAngle);
    }

    public boolean isShieldRushing() {
        return cubeShield.isRushing();
    }

    public IHierophantShield getCubeShield() {
        return cubeShield;
    }

    public int getShieldStopDuration() {
        return cubeShield.getStopDuration();
    }

    /**
     * Network replica only: authoritative state lives in the capability.
     */
    public void syncShieldState() {
        if (level().isClientSide() || cubeShield == null) return;
        entityData.set(SHIELD, cubeShield.getHealth());
        entityData.set(SHIELD_MAX, cubeShield.getMaxHealth());
        entityData.set(SPIN_SPEED, cubeShield.getSpinSpeed());
        entityData.set(RUSHING, cubeShield.isRushing());
        entityData.set(SHIELD_RED, cubeShield.requiresPhysicalDamage());
        entityData.set(SHIELD_STOP_DURATION, cubeShield.getStopDuration());
        entityData.set(SHIELD_STOP_REMAINING, cubeShield.getStopTicksRemaining());
        updateShieldCollision();
    }

    private Player nearestArenaPlayer() {
        if (!(level() instanceof ServerLevel server)) return null;
        return server.players().stream().filter(this::validTarget).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    public int getAction() {
        if (isDeadOrDying()) return DEATH;
        if (!entityData.get(ARENA_READY)) return INTRO;
        if (entityData.get(REBIRTH_TIME) > 0) return REBIRTH;
        if (!isSpawned()) return INTRO;
        if (hasShield()) return SHIELD_CAST;
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
            case REBIRTH -> predictedTime(entityData.get(REBIRTH_TIME), rebirthSyncTick);
            case SHIELD_STUN ->
                    predictedTime(getShieldStopDuration() - cubeShield.getStopTicksRemaining(), stopSyncTick);
            default -> predictedTime(entityData.get(ATTACK_TIME), attackSyncTick);
        };
        return Mth.clamp(time, 0, action == SHIELD_STUN ? getShieldStopDuration() : getActionDuration(action));
    }

    private int predictedTime(int serverTime, int receivedTick) {
        // Metadata need not arrive every tick. Advance locally between updates
        // rather than repeatedly holding/jumping the pose at the packet rate.
        return serverTime + (level().isClientSide() ? Math.max(0, tickCount - receivedTick) : 0);
    }

    public float getBlur() {
        return getPhase() == 3 && entityData.get(REBIRTH_TIME) == 0 ? Mth.clamp(1 - getHealth() / getMaxHealth(), 0, 1) : 0;
    }

    public boolean hasThirdPhaseSword() {
        return getPhase() == 3 && (getAction() != REBIRTH || getActionTime() >= 110);
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
                startThirdPhase();
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
        pendingRelocation = false;
        entityData.set(BREAK_TIME, 0);
        updateShieldCollision();
        // Leave immediately, while the uncurling/recovery animation plays.
        if (validTarget(getTarget())) beginSkill(SMOKE_MOVE);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
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
            if (getShieldSpinSpeed() > 0.01F) return false;
        }
        boolean shielded = hasShield();
        boolean hurt;
        bypassRebirth = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        try {
            hurt = super.hurt(source, amount);
        } finally {
            bypassRebirth = false;
        }
        if (hurt && !shielded && getPhase() == 2 && isAlive() && getAction() != SMOKE_MOVE) pendingRelocation = true;
        return hurt;
    }

    @Override
    protected void blockedByShield(LivingEntity defender) {
        if (!level().isClientSide() && hasShield() && isShieldRushing() && defender instanceof Player) {
            Player player = (Player) defender;
            Item shield = player.getUseItem().getItem();
            player.getCooldowns().addCooldown(shield, 200);
            player.stopUsingItem();
            level().broadcastEntityEvent(player, (byte) 30);
            beginSkill(SHIELD_STUN);
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.3, getZ(), 16, 1, 0.5, 1, 0.1);
            }
        } else super.blockedByShield(defender);
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
        if (key.equals(SKILL_TYPE) || key.equals(USING_SKILL)) skillTick = 0;
        if (level().isClientSide()) {
            if (cubeShield != null && (key.equals(SHIELD) || key.equals(SHIELD_MAX) || key.equals(SPIN_SPEED) || key.equals(RUSHING) || key.equals(SHIELD_RED) || key.equals(SHIELD_STOP_REMAINING) || key.equals(SHIELD_STOP_DURATION))) {
                cubeShield.applySnapshot(entityData.get(SHIELD), entityData.get(SHIELD_MAX), entityData.get(SPIN_SPEED), entityData.get(RUSHING), entityData.get(SHIELD_STOP_REMAINING), entityData.get(SHIELD_STOP_DURATION), entityData.get(SHIELD_RED));
            }
            if (key.equals(SHIELD_STOP_REMAINING) || key.equals(SHIELD_STOP_DURATION)) stopSyncTick = tickCount;
            if (key.equals(INTRO_TIME)) introSyncTick = tickCount;
            if (key.equals(BREAK_TIME)) breakSyncTick = tickCount;
            if (key.equals(DEATH_TIME)) deathSyncTick = tickCount;
            if (key.equals(ATTACK_TIME)) attackSyncTick = tickCount;
            if (key.equals(REBIRTH_TIME)) rebirthSyncTick = tickCount;
        }
        if (key.equals(SHIELD) || key.equals(INTRO_TIME) || key.equals(DEATH_TIME)) updateShieldCollision();
    }

    private void tickAttack(int id, int t) {
        if (!(level() instanceof ServerLevel server) || !validTarget(getTarget())) {
            endSkill();
            return;
        }
        getNavigation().stop();
        faceTarget();
        // All second-phase actions can be interrupted by a close player,
        // except the harmless chuckle and a fan gust already in progress.
        if (getPhase() == 2 && !hasShield() && id != CHUCKLE && id != FAN) {
            Player nearest = nearestArenaPlayer();
            if (nearest != null && distanceToSqr(nearest) < 3.5 * 3.5 && hasLineOfSight(nearest)) {
                setTarget(nearest);
                beginSkill(FAN);
                return;
            }
        }
        if (id != CHARGE && id != SHIELD_CAST && id != SMOKE_MOVE && id != METEORS && id != FIERY_THRUST)
            setDeltaMovement(0, getDeltaMovement().y, 0);
        switch (id) {
            case SHIELD_CAST -> tickShieldCasting(server, t);
            case METEORS -> tickMeteors(server, t);
            case FIERY_QUAKE -> {
                if (t == FIRST_FIERY_QUAKE_TICK) fieryGroundShock(server, 10);
                if (t == SECOND_FIERY_QUAKE_TICK) fieryGroundShock(server, 15);
                if (t >= SWORD_PLANT_TICK && t % 4 == 0)
                    server.sendParticles(ParticleTypes.LAVA, getX(), groundAt(position()).y + 0.1, getZ(), 5, 1.2, 0, 1.2, 0);
                if (t >= FIERY_QUAKE_DURATION - 1) finishAction(SMOKE_MOVE);
            }
            case FIERY_THRUST -> tickFieryThrust(server, t);
            case CHARGE -> tickShieldRush(server, t);
            case SHIELD_STUN -> {
                cubeShield.setSpinSpeed(0);
                setNoGravity(false);
                if (!cubeShield.isStopped()) finishAction(CHARGE);
            }
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
                setNoGravity(t < 30);
                fallDistance = 0;
                if (t < 30) {
                    Vec3 to = moveDestination.subtract(position());
                    if (to.lengthSqr() > 0.4) {
                        // Keep the larger arena's third-phase corner destinations
                        // reachable in the same flight window; phase two is unchanged.
                        double speed = getPhase() == 3 && roofLift != null ? 0.9 * roofLift.radius / 10D : 0.9;
                        Vec3 velocity = to.normalize().scale(Math.min(speed, to.length()));
                        if (insideArena(position().add(velocity)) && isAreaLoaded(getBoundingBox().move(velocity)) && server.noCollision(this, getBoundingBox().move(velocity)))
                            setDeltaMovement(velocity);
                        else setDeltaMovement(Vec3.ZERO);
                    } else setDeltaMovement(Vec3.ZERO);
                } else setDeltaMovement(0, getDeltaMovement().y, 0);
                if (t >= getActionDuration(SMOKE_MOVE) - 1) {
                    resumeBarrage = false;
                    finishAction(getPhase() == 3 ? queuedThirdAttack : choosePhaseTwoAttack());
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
                if (t == AXE_WINDUP_TICKS) axeSlam(server);
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
            // A blue cast starts a three-shot burst, not a three-way spread.
            explosive = isShieldRed();
            if (!explosive) {
                shieldBurstRemaining = 3;
                shieldNextBurstTick = clock;
            }
        }
        boolean normal = shieldBurstRemaining > 0 && clock >= shieldNextBurstTick;
        if (!explosive && !normal) return;
        if (normal) {
            shieldBurstRemaining--;
            shieldNextBurstTick = clock + 4; // 0.2 seconds between successive shots.
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
            server.addFreshEntity(new HierophantFragment(server, this, forward, false));
        }
    }

    private void startThirdPhase() {
        endSkill();
        thirdPhaseUnlocked = true;
        setPhase(3);
        super.setHealth(getMaxHealth()); // No die(), loot roll, advancement or conquered flag at phase two's end.
        entityData.set(REBIRTH_TIME, 1);
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        pendingRelocation = false;
        summoningCircles.clear();
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        if (level() instanceof ServerLevel server) {
            killSummonedAdherents(server);
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItem.GLASS_HEART.get())), getX(), getY() + 1.1, getZ(), 40, 0.45, 0.6, 0.45, 0.15);
            playSound(SoundEvents.GLASS_BREAK, 2, 0.8F);
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
            server.sendParticles(ParticleTypes.HEART, getX() + Math.cos(angle), getY() + 1.1 + Math.sin(angle * 2) * 0.25, getZ() + Math.sin(angle), 1, 0, 0, 0, 0);
        }
        if (t == 110) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.FIERY_SWORD.get()));
            playSound(SoundEvents.WITHER_SPAWN, 2, 1);
        }
        if (t >= REBIRTH_DURATION) {
            entityData.set(REBIRTH_TIME, 0);
            endSkill();
            nextAttack = METEORS;
        } else entityData.set(REBIRTH_TIME, t + 1);
    }

    private void tickMeteors(ServerLevel server, int t) {
        double floor = getRestrictionPoint().pos().getY();
        setNoGravity(t < 130);
        fallDistance = 0;
        if (t < 130) {
            double desired = floor + 3 + Math.sin(t * 0.12) * 0.12;
            setDeltaMovement(0, Mth.clamp(desired - getY(), -0.16, 0.22), 0);
        } else setDeltaMovement(0, Math.max(-0.25, getDeltaMovement().y), 0);
        if (t >= 40 && t <= 100 && (t - 40) % 20 == 0) {
            List<ServerPlayer> players = server.players().stream().filter(this::validTarget).toList();
            if (!players.isEmpty()) {
                Player target = players.get(((t - 40) / 20) % players.size());
                Vec3 at = target.position().add(0, target.getBbHeight() + 3, 0);
                HierophantMeteor meteor = new HierophantMeteor(server, this, at);
                server.addFreshEntity(meteor);
            }
        }
        if (t >= METEOR_DURATION - 1) finishAction(SMOKE_MOVE);
    }

    private void fieryGroundShock(ServerLevel server, int radius) {
        Vec3 centre = groundAt(position());
        playSound(TFSounds.MINOSHROOM_SLAM.get(), 2, 0.8F);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 2, radius), this::canAttackVictim)) {
            if (victim.position().subtract(centre).horizontalDistanceSqr() > radius * radius || !victim.onGround())
                continue;
            if (victim.hurt(TFDamageTypes.getEntityDamageSource(server, TFDamageTypes.SLAM, this), 12)) {
                victim.push(0, 0.55, 0);
                victim.hurtMarked = true;
            }
        }
        ParticlePacket packet = new ParticlePacket();
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius) continue;
                BlockPos floor = BlockPos.containing(centre).offset(x, -1, z);
                if (!server.hasChunkAt(floor) || !insideArena(Vec3.atCenterOf(floor.above()))) continue;
                var state = server.getBlockState(floor);
                if (state.isAir()) continue;
                Vec3 at = Vec3.atBottomCenterOf(floor.above()).add(0, 0.05, 0);
                packet.queueParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), false, at, new Vec3(0, 0.18, 0));
                packet.queueParticle(ParticleTypes.LAVA, false, at, Vec3.ZERO);
                // Break sound/particles only: not a single arena floor block is removed.
                if ((x + z) % 4 == 0)
                    server.playSound(null, floor, state.getSoundType(server, floor, this).getBreakSound(), net.minecraft.sounds.SoundSource.HOSTILE, 0.35F, 0.9F);
            }
        PacketDistributor.sendToPlayersTrackingEntity(this, packet);
    }

    private void tickFieryThrust(ServerLevel server, int t) {
        if (t < 20) {
            lockedAim = getTarget().position();
            setDeltaMovement(0, getDeltaMovement().y, 0);
            return;
        }
        if (t == 20) dashDirection = lockedAim.subtract(position()).multiply(1, 0, 1).normalize();
        if (t >= 20 && t < 36) {
            setYRot((float) Math.toDegrees(Math.atan2(-dashDirection.x, dashDirection.z)));
            setYBodyRot(getYRot());
            setYHeadRot(getYRot());
            Vec3 velocity = dashDirection.scale(1.35);
            if (insideArena(position().add(velocity)) && isAreaLoaded(getBoundingBox().expandTowards(velocity)) && server.noCollision(this, getBoundingBox().move(velocity)))
                setDeltaMovement(velocity.x, getDeltaMovement().y, velocity.z);
            else setDeltaMovement(0, getDeltaMovement().y, 0);
            server.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.5, getZ(), 12, 0.35, 0.35, 0.35, 0.03);
            AABB sweep = getBoundingBox().expandTowards(velocity).inflate(0.4);
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, sweep, this::canAttackVictim)) {
                if (!chargeHits.add(victim.getUUID())) continue;
                if (victim.hurt(damageSources().mobAttack(this), 14)) {
                    victim.igniteForSeconds(5);
                    victim.push(dashDirection.x * 1.6, 0.55, dashDirection.z * 1.6);
                    victim.hurtMarked = true;
                }
            }
        } else setDeltaMovement(0, getDeltaMovement().y, 0);
        if (t >= FIERY_THRUST_DURATION - 1) finishAction(SMOKE_MOVE);
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

    private void axeSlam(ServerLevel server) {
        Vec3 centre = groundAt(position());
        playSound(TFSounds.MINOSHROOM_SLAM.get(), 2, 1);
        gameEvent(GameEvent.HIT_GROUND);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(AXE_SLAM_RADIUS, 2, AXE_SLAM_RADIUS), this::canAttackVictim)) {
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
            if (column.subtract(centre).horizontalDistanceSqr() > AXE_SLAM_RADIUS * AXE_SLAM_RADIUS || !insideArena(new Vec3(column.x, getY(), column.z)))
                continue;
            BlockPos base = BlockPos.containing(column.x, getY() + 2, column.z);
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
                BlockPos spawn = BlockPos.containing(centre.x + Math.cos(angle) * radius, roofLift.home().getY() + 12, centre.z + Math.sin(angle) * radius);
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

    private void tickShieldRush(ServerLevel server, int t) {
        setNoGravity(true);
        fallDistance = 0;
        if (!isShieldRushing()) {
            cubeShield.setSpinSpeed(SHIELD_MAX_SPIN * Mth.clamp(t / (float) SHIELD_SPIN_UP_TICKS, 0, 1));
            double lift = groundAt(position()).y + 0.35 - getY();
            setDeltaMovement(0, Mth.clamp(lift, -0.06, 0.06), 0);
            if (t < SHIELD_SPIN_UP_TICKS) return;
            Player nearest = nearestArenaPlayer();
            if (nearest == null) {
                finishAction(CHARGE);
                return;
            }
            setTarget(nearest);
            lockedAim = nearest.position();
            aimAt(lockedAim);
            dashDirection = lockedAim.subtract(position()).multiply(1, 0, 1).normalize();
            if (dashDirection.lengthSqr() < 1.0E-6) dashDirection = getLookAngle().multiply(1, 0, 1).normalize();
            rushSpeed = RUSH_INITIAL_SPEED;
            previousDash = position();
            cubeShield.setRushing(true);
            wallHitX = wallHitZ = false;
        }
        if (wallHitX || wallHitZ) {
            dashDirection = new Vec3(wallHitX ? -dashDirection.x : dashDirection.x, 0, wallHitZ ? -dashDirection.z : dashDirection.z).normalize();
            rushSpeed *= RUSH_WALL_RETENTION;
            wallHitX = wallHitZ = false;
            smoke(server, 8, 0.8);
        }
        AABB swept = getBoundingBox().minmax(getBoundingBox().move(previousDash.subtract(position()))).inflate(0.1);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, swept, this::canAttackVictim)) {
            if (!chargeHits.add(victim.getUUID())) continue;
            // Vanilla hurt/blockUsingShield invokes blockedByShield only when
            // the directional block really succeeds (including NeoForge hooks).
            float damage = HierophantConfig.CHARGE_DAMAGE.get().floatValue() * (float) (rushSpeed / 1.25);
            boolean hurt = victim.hurt(damageSources().mobAttack(this), damage);
            if (!hasShield() || !isShieldRushing()) return;
            if (hurt) {
                double knockback = Math.min(2.6, rushSpeed * 1.2);
                victim.push(dashDirection.x * knockback, 0.45 + rushSpeed * 0.15, dashDirection.z * knockback);
                victim.hurtMarked = true;
            }
        }
        previousDash = position();
        rushSpeed = Math.max(0, rushSpeed - RUSH_DECAY);
        cubeShield.setSpinSpeed(SHIELD_MAX_SPIN * (float) (rushSpeed / RUSH_INITIAL_SPEED));
        if (rushSpeed < 0.06 || t >= getActionDuration(CHARGE) - 1) {
            finishAction(CHARGE);
            return;
        }
        Vec3 velocity = dashDirection.scale(rushSpeed);
        if (!isAreaLoaded(getBoundingBox().expandTowards(velocity))) {
            finishAction(CHARGE);
            return;
        }
        setDeltaMovement(velocity);
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
        if (getTarget() == null || !(level() instanceof ServerLevel server)) return;
        List<LivingEntity> threats = new ArrayList<>();
        for (Player player : server.players()) if (validTarget(player)) threats.add(player);
        if (!threats.contains(getTarget())) threats.add(getTarget());
        Vec3 nearest = threats.stream().min(Comparator.comparingDouble(e -> e.position().subtract(position()).horizontalDistanceSqr()))
                .orElse(getTarget()).position();
        Vec3 away = position().subtract(nearest).multiply(1, 0, 1);
        double initialDistance = nearestThreatDistanceSqr(position(), threats);
        double bestDistance = initialDistance + 0.25;
        double pathMinimum = Math.pow(Math.max(0, Math.sqrt(initialDistance) - 0.5), 2);
        double radius = roofLift == null ? Math.max(2, HierophantConfig.ARENA_RADIUS.get() - 2) : roofLift.radius - 2;
        double maxTravel = (getPhase() == 3 && roofLift != null ? 0.9 * roofLift.radius / 10D : 0.9) * 29;
        Vec3 centre = getRestrictionPoint() == null ? arena == null ? position() : arena.getCenter() : Vec3.atBottomCenterOf(getRestrictionPoint().pos());
        double awayAngle = away.lengthSqr() < 1.0E-6 ? random.nextDouble() * Math.PI * 2 : Math.atan2(away.z, away.x);
        boolean corner = getPhase() == 3 && queuedThirdAttack == FIERY_THRUST;
        for (int i = 0; i < (corner ? 4 : 49); i++) {
            Vec3 seed;
            if (corner) {
                double angle = Math.PI / 4 + i * Math.PI / 2;
                seed = centre.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            } else if (i == 0) {
                seed = position().add(Math.cos(awayAngle) * maxTravel, 0, Math.sin(awayAngle) * maxTravel);
            } else {
                double angle = awayAngle + (i - 1) * Math.PI * 2 / 32;
                double ring = i <= 32 ? radius : radius * (0.35 + random.nextDouble() * 0.45);
                seed = centre.add(Math.cos(angle) * ring, 0, Math.sin(angle) * ring);
            }
            Vec3 candidate = groundAt(clampToArena(seed));
            Vec3 delta = candidate.subtract(position());
            if (delta.lengthSqr() > maxTravel * maxTravel)
                candidate = groundAt(clampToArena(position().add(delta.normalize().scale(maxTravel))));
            double separation = nearestThreatDistanceSqr(candidate, threats);
            if (separation <= bestDistance || separation < 25 || candidate.subtract(position()).horizontalDistanceSqr() < 16
                    || !canStandAt(candidate)) continue;
            delta = candidate.subtract(position());
            boolean clear = true;
            int steps = Mth.ceil(delta.length() / 0.75);
            for (int stepIndex = 1; stepIndex <= steps; stepIndex++) {
                double f = stepIndex / (double) steps;
                AABB step = getBoundingBox().move(delta.scale(f));
                if (nearestThreatDistanceSqr(position().add(delta.scale(f)), threats) < pathMinimum
                        || !isAreaLoaded(step) || !level().noCollision(this, step)) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                moveDestination = candidate;
                bestDistance = separation;
            }
        }
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
            case EASY, NORMAL -> 3;
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
            if (entity instanceof Adherent minion && minion.isAlive()
                    && minion.getPersistentData().hasUUID("FinaleHierophantOwner")
                    && getUUID().equals(minion.getPersistentData().getUUID("FinaleHierophantOwner"))) owned.add(minion);
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
        if (level() instanceof ServerLevel server) {
            GlobalPos home = getRestrictionPoint();
            if (home == null || !home.dimension().equals(level().dimension())) {
                home = GlobalPos.of(level().dimension(), blockPosition());
                setRestrictionPoint(home);
            }
            if (roofLift == null) roofLift = HierophantArena.get(server).attach(server, this, home.pos());
            if (roofLift != null) {
                arena = roofLift.bounds();
                entityData.set(ARENA_READY, roofLift.ready());
                return;
            }
            // Wait for arena preparation rather than starting an unbounded fight.
            entityData.set(ARENA_READY, false);
            return;
        }
        if (arena != null) return;
        GlobalPos home = getRestrictionPoint();
        if (home == null || !home.dimension().equals(level().dimension())) {
            home = GlobalPos.of(level().dimension(), blockPosition());
            setRestrictionPoint(home);
        }
        int radius = HierophantConfig.ARENA_RADIUS.get();
        BlockPos at = home.pos();
        arena = new AABB(at.getX() - radius, at.getY() - 3, at.getZ() - radius, at.getX() + radius + 1, at.getY() + 8, at.getZ() + radius + 1);
        if (level() instanceof ServerLevel server && server.getChunkSource().getChunkNow(at.getX() >> 4, at.getZ() >> 4) != null) {
            var registry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
            var chunk = server.getChunkSource().getChunkNow(at.getX() >> 4, at.getZ() >> 4);
            var castle = ResourceLocation.fromNamespaceAndPath("twilightforest", "final_castle");
            Set<StructureStart> starts = new HashSet<>();
            for (var entry : chunk.getAllStarts().entrySet())
                if (castle.equals(registry.getKey(entry.getKey()))) starts.add(entry.getValue());
            // Avoid StructureManager's lazy loading of reference-start chunks.
            for (var entry : chunk.getAllReferences().entrySet())
                if (castle.equals(registry.getKey(entry.getKey()))) for (long reference : entry.getValue()) {
                    ChunkPos pos = new ChunkPos(reference);
                    var originChunk = server.getChunkSource().getChunkNow(pos.x, pos.z);
                    if (originChunk != null) {
                        var start = originChunk.getStartForStructure(entry.getKey());
                        if (start != null) starts.add(start);
                    }
                }
            for (var start : starts)
                for (var piece : start.getPieces())
                    if ((piece instanceof FinalCastleDungeonForgeRoomComponent || piece instanceof FinalCastleBossGazeboComponent) && piece.getBoundingBox().isInside(at)) {
                        var box = piece.getBoundingBox();
                        arena = new AABB(box.minX() + 2, Math.max(box.minY() + 1, at.getY() - 3), box.minZ() + 2, box.maxX() - 1, Math.min(box.maxY(), at.getY() + 8), box.maxZ() - 1);
                    }
        }
    }

    public boolean insideArena(Vec3 at) {
        if (arena == null) return true;
        if (!arena.contains(at)) return false;
        if (roofLift == null) return true;
        // Match the actual circular set of floor cells, including their outer edges.
        BlockPos column = BlockPos.containing(at);
        double dx = column.getX() - roofLift.sourceHome.getX(), dz = column.getZ() - roofLift.sourceHome.getZ();
        return dx * dx + dz * dz <= roofLift.radius * roofLift.radius;
    }

    public boolean isAreaLoaded(AABB box) {
        if (!(level() instanceof ServerLevel server)) return true;
        for (int x = Mth.floor(box.minX) >> 4; x <= Mth.floor(box.maxX) >> 4; x++)
            for (int z = Mth.floor(box.minZ) >> 4; z <= Mth.floor(box.maxZ) >> 4; z++)
                if (server.getChunkSource().getChunkNow(x, z) == null) return false;
        return true;
    }

    @Override
    public void move(MoverType type, Vec3 motion) {
        Vec3 requested = motion, before = position();
        boolean rushing = !level().isClientSide() && isShieldRushing() && type == MoverType.SELF;
        if (!level().isClientSide() && arena != null) {
            Vec3 limited = clampToArena(position().add(motion)).subtract(position());
            // Also handles external knockback. Never snap a displaced boss back home.
            motion = new Vec3(Mth.clamp(limited.x, -Math.abs(motion.x) - 0.3, Math.abs(motion.x) + 0.3), Mth.clamp(limited.y, -Math.abs(motion.y) - 0.3, Math.abs(motion.y) + 0.3), Mth.clamp(limited.z, -Math.abs(motion.z) - 0.3, Math.abs(motion.z) + 0.3));
        }
        super.move(type, motion);
        if (rushing) {
            Vec3 travelled = position().subtract(before);
            wallHitX |= Math.abs(requested.x) > 1.0E-6 && Math.abs(travelled.x - requested.x) > 1.0E-4;
            wallHitZ |= Math.abs(requested.z) > 1.0E-6 && Math.abs(travelled.z - requested.z) > 1.0E-4;
        }
    }

    private Vec3 clampToArena(Vec3 at) {
        if (arena == null) return at;
        double margin = getBbWidth() / 2 + 0.2;
        double x = Mth.clamp(at.x, arena.minX + margin, arena.maxX - margin), z = Mth.clamp(at.z, arena.minZ + margin, arena.maxZ - margin);
        if (roofLift != null) {
            double cx = roofLift.sourceHome.getX() + 0.5, cz = roofLift.sourceHome.getZ() + 0.5;
            double dx = x - cx, dz = z - cz, distance = Math.sqrt(dx * dx + dz * dz);
            double radius = roofLift.radius - margin;
            if (distance > radius) {
                x = cx + dx / distance * radius;
                z = cz + dz / distance * radius;
            }
        }
        return new Vec3(x, Mth.clamp(at.y, arena.minY, arena.maxY - getBbHeight()), z);
    }

    private void constrainMovement() {
        if (arena == null || isShieldRushing()) return; // move() records the actual arena/wall collision for ricochets.
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

    private void updateBars() {
        // The visual never fades below 50%, but the HP bar must still reach zero.
        shieldBar.setProgress(Mth.clamp(getShieldHealth() / cubeShield.getMaxHealth(), 0, 1));
        shieldBar.setColor(isShieldRed() ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.BLUE);
        shieldBar.setVisible(entityData.get(ARENA_READY) && hasShield() && entityData.get(INTRO_TIME) >= 20 && isAlive());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        shieldBar.addPlayer(player);
        updateBars();
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        shieldBar.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        shieldBar.removeAllPlayers();
        if (reason == RemovalReason.DISCARDED && level() instanceof ServerLevel server) {
            summoningCircles.clear();
            for (Adherent minion : server.getEntitiesOfClass(Adherent.class, getBoundingBox().inflate(48),
                    e -> e.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(e.getPersistentData().getUUID("FinaleHierophantOwner"))))
                minion.discard();
        }
        super.remove(reason);
    }

    @Override
    public void setRestrictionPoint(GlobalPos pos) {
        super.setRestrictionPoint(pos);
        arena = null;
    }

    @Override
    public int getHomeRadius() {
        return 24;
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
        super.die(source);
        if (!dead) return;
        if (level() instanceof ServerLevel server && getRestrictionPoint() != null)
            HierophantArena.get(server).defeated(getRestrictionPoint().pos());
        endSkill();
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        updateShieldCollision();
        shieldBar.setVisible(false);
        if (level() instanceof ServerLevel server)
            for (var minion : server.getEntitiesOfClass(twilightforest.entity.monster.Adherent.class, getBoundingBox().inflate(48), e -> e.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(e.getPersistentData().getUUID("FinaleHierophantOwner"))))
                minion.discard();
    }

    @Override
    protected void tickDeath() {
        super.tickDeath(); // Native removal, loot deposit and death-time handling.
        if (isRemoved() || !(level() instanceof ServerLevel server)) return;
        entityData.set(DEATH_TIME, deathTime);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        getNavigation().stop();
        if (deathTime == 40) playSound(SoundEvents.ENDERMAN_SCREAM, 3, 0.8F);
        if (deathTime >= 50 && deathTime < 150) {
            smoke(server, 4 + (deathTime - 50) / 10, 0.65);
            server.sendParticles(ParticleTypes.ASH, getX(), getY() + 1, getZ(), 8, 0.6, 0.8, 0.6, 0.04);
        }
    }

    @Override
    public boolean isDeathAnimationFinished() {
        return deathTime >= DEATH_DURATION;
    }

    @Override
    protected void postRemoval(ServerLevel server, RemovalReason reason) {
        boolean showReward = reason == RemovalReason.KILLED && shouldSpawnLoot();
        BlockPos chestBase = showReward ? EntityUtil.bossChestLocation(this) : null;
        super.postRemoval(server, reason); // Native loot roll/inventory/deposit, exactly once.
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
        tag.putInt("FinalePhase", getPhase());
        tag.putInt("HierophantHealthVersion", 2);
        tag.putBoolean("HierophantThirdUnlocked", thirdPhaseUnlocked);
        tag.putInt("HierophantRebirth", entityData.get(REBIRTH_TIME));
        tag.putInt("HierophantShieldClock", shieldShotTick);
        tag.putInt("HierophantShieldBurstRemaining", shieldBurstRemaining);
        tag.putInt("HierophantShieldNextBurst", shieldNextBurstTick);
        tag.putInt("HierophantEmptyArena", emptyArenaTicks);
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
        float previousMax = getMaxHealth(), previousHealth = getHealth();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        // Old saved attributes otherwise override the new 200-HP default.
        // Preserve the remaining percentage, including zero for a dying boss.
        if (!tag.contains("HierophantHealthVersion") && previousMax > 0)
            setHealth(previousHealth / previousMax * getMaxHealth());
        setPhase(tag.contains("FinalePhase") ? tag.getInt("FinalePhase") : 1);
        thirdPhaseUnlocked = tag.getBoolean("HierophantThirdUnlocked") || getPhase() == 3;
        entityData.set(REBIRTH_TIME, Mth.clamp(tag.getInt("HierophantRebirth"), 0, REBIRTH_DURATION));
        shieldShotTick = Math.max(0, tag.getInt("HierophantShieldClock"));
        shieldBurstRemaining = Mth.clamp(tag.getInt("HierophantShieldBurstRemaining"), 0, 3);
        shieldNextBurstTick = Math.max(shieldShotTick, tag.getInt("HierophantShieldNextBurst"));
        emptyArenaTicks = Mth.clamp(tag.getInt("HierophantEmptyArena"), 0, 199);
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
            float max = tag.contains("HierophantShieldMax") ? tag.getFloat("HierophantShieldMax") : HierophantConfig.SHIELD_HEALTH.get().floatValue();
            float hp = tag.contains("HierophantShield") ? tag.getFloat("HierophantShield") : getPhase() >= 2 ? 0 : max;
            float replacement = HierophantConfig.SHIELD_HEALTH.get().floatValue();
            cubeShield.restore(replacement, max > 0 ? hp / max * replacement : 0);
        }
        var bounds = tag.getList("HierophantArena", net.minecraft.nbt.Tag.TAG_DOUBLE);
        if (bounds.size() == 6 && bounds.getDouble(3) > bounds.getDouble(0) && bounds.getDouble(5) > bounds.getDouble(2))
            arena = new AABB(bounds.getDouble(0), bounds.getDouble(1), bounds.getDouble(2), bounds.getDouble(3), bounds.getDouble(4), bounds.getDouble(5));
        summonedAdherents.clear();
        var savedMinions = tag.getList("HierophantAdherents", net.minecraft.nbt.Tag.TAG_COMPOUND);
        if (getPhase() == 2)
            for (int i = 0; i < Math.min(savedMinions.size(), 256); i++) {
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
        nextAttack = getPhase() == 3 ? METEORS : BARRAGE;
        pendingRelocation = false;
        resumeBarrage = false;
        endSkill(); // Cancel casts on reload; native BaseTFBoss restores home and loot.
        if (!hasShield() && getPhase() == 1) setPhase(2);
        entityData.set(DEATH_TIME, deathTime);
        updateShieldCollision();
        setCanPickUpLoot(false);
        if (entityData.get(INTRO_TIME) < INTRO_DURATION && getPhase() == 1) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.FORTIFICATION_SCEPTER.get()));
            setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItem.FORCE_FIELD_SCEPTER.get()));
        }
        if (getPhase() == 3 && entityData.get(REBIRTH_TIME) > 0) {
            setNoGravity(true);
            if (entityData.get(REBIRTH_TIME) < 110) setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}
