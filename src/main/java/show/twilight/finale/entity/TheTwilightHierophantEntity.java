package show.twilight.finale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLLoader;
import org.joml.Vector3f;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.client.animation.TheTwilightHierophantAnimation;
import show.twilight.finale.config.HierophantConfig;
import show.twilight.finale.entity.combat.CombatRay;
import show.twilight.finale.entity.projectile.HierophantFragment;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.runtime.animation.VzlingAnimation;
import show.twilight.finale.runtime.entity.BossEntity;
import show.twilight.finale.runtime.entity.ISkillEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;
import show.twilight.finale.runtime.entity.skill.SkillManager;
import show.twilight.finale.world.castle.FinalCastleBossGazeboComponent;
import show.twilight.finale.world.castle.FinalCastleDungeonForgeRoomComponent;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFEntities;
import twilightforest.init.TFParticleType;

import java.util.*;

/**
 * Server-authoritative encounter; does not replace the native Final Boss Spawner.
 */
public class TheTwilightHierophantEntity extends BossEntity implements EnforcedHomePoint {
    public static final int IDLE_ACTION = 0, CHARGE = 1, RETURN_FRAGMENTS = 2, FORMATION = 3, BEAM = 4, SUMMON = 5, BARRAGE = 6, SMOKE_MOVE = 7;
    public static final int INTRO = -1, SHIELD_BREAK = -2, DEATH = -3;
    public static final int INTRO_DURATION = 100, BREAK_DURATION = 40, DEATH_DURATION = 160, BARRAGE_FIRE_TICKS = 100;
    private static final EntityDataAccessor<Integer> INTRO_TIME = data(EntityDataSerializers.INT), BREAK_TIME = data(EntityDataSerializers.INT), DEATH_TIME = data(EntityDataSerializers.INT), ATTACK_TIME = data(EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SHIELD = data(EntityDataSerializers.FLOAT), SHIELD_MAX = data(EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> AIM = data(EntityDataSerializers.VECTOR3), CENTRE = data(EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> GRID_X = data(EntityDataSerializers.BOOLEAN);

    private static <T> EntityDataAccessor<T> data(EntityDataSerializer<T> serializer) {
        return SynchedEntityData.defineId(TheTwilightHierophantEntity.class, serializer);
    }

    private final ServerBossEvent bodyBar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private final ServerBossEvent shieldBar = new ServerBossEvent(Component.translatable("boss.twilightforest_finale.hierophant_shield"), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
    private GlobalPos home;
    private AABB arena;
    private boolean shieldCollision, routeShieldDamage, dashStopped, rewardPending, rewardCreated;
    private Vec3 lockedAim = Vec3.ZERO, moveDestination = Vec3.ZERO, dashDirection = Vec3.ZERO, previousDash = Vec3.ZERO;
    private int lastAttack;
    private int rewardExperience;
    private final Vec3[] trail = new Vec3[16];

    public Vec3 getTrailPosition(int age) {
        Vec3 p = trail[Math.min(trail.length - 1, Math.max(0, age))];
        return p == null ? position() : p;
    }

    private final Set<UUID> chargeHits = new HashSet<>();
    private final List<Vec3> circles = new ArrayList<>();
    private final NonNullList<ItemStack> rewards = NonNullList.withSize(27, ItemStack.EMPTY);
    public final VzlingAnimation IDLE = new VzlingAnimation(this, TwilightFinaleMod.prefix("the_twilight_hierophant"));
    public final SkillManager<TheTwilightHierophantEntity> skillManager = new SkillManager<>(this, List.of(attack(CHARGE, 86, 100), attack(RETURN_FRAGMENTS, 120, 160), attack(FORMATION, 122, 180), attack(BEAM, 96, 100), attack(SUMMON, 120, 240), attack(BARRAGE, 160, 180), attack(SMOKE_MOVE, 40, 80)));

    private static BaseSkill<TheTwilightHierophantEntity> attack(int id, int duration, int cooldown) {
        return new BaseSkill<>(id, duration, 64, cooldown) {
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
        setPersistenceRequired();
        setCanPickUpLoot(false);
        xpReward = 100;
        if (FMLLoader.getDist().isClient()) IDLE.setDefinition(TheTwilightHierophantAnimation.IDLE);
        if (!level.isClientSide()) {
            float hp = HierophantConfig.SHIELD_HEALTH.get().floatValue();
            entityData.set(SHIELD, hp);
            entityData.set(SHIELD_MAX, hp);
        }
        setDropChance(EquipmentSlot.MAINHAND, 0);
        shieldBar.setVisible(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.FOLLOW_RANGE, 48);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(INTRO_TIME, 0);
        b.define(BREAK_TIME, 0);
        b.define(DEATH_TIME, 0);
        b.define(ATTACK_TIME, 0);
        b.define(SHIELD, 100F);
        b.define(SHIELD_MAX, 100F);
        b.define(AIM, new Vector3f());
        b.define(CENTRE, new Vector3f());
        b.define(GRID_X, true);
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
        if (!isSpawned() || entityData.get(BREAK_TIME) > 0 || !validTarget(getTarget())) return List.of();
        if (distanceToSqr(getTarget()) > 64 && tickCount % 3 != 0) return List.of(SMOKE_MOVE);
        List<Integer> pool = new ArrayList<>(hasShield() ? List.of(CHARGE, RETURN_FRAGMENTS, FORMATION) : List.of(BEAM, SUMMON, BARRAGE));
        pool.remove(lastAttack);
        double range = HierophantConfig.BEAM_RANGE.get();
        if (!hasShield() && distanceToSqr(getTarget()) > range * range) pool.remove(BEAM);
        return pool;
    }

    @Override
    public void beginSkill(int id) {
        super.beginSkill(id);
        lastAttack = id;
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        chargeHits.clear();
        dashStopped = false;
        previousDash = position();
        circles.clear();
        lockedAim = getTarget() == null ? position().add(getLookAngle().scale(8)) : getTarget().getEyePosition();
        aimAt(lockedAim);
        if (id == FORMATION) {
            entityData.set(CENTRE, vector(groundAt(clampToArena(lockedAim)).subtract(position())));
            entityData.set(GRID_X, random.nextBoolean());
        }
        if (id == SMOKE_MOVE) chooseMoveDestination();
    }

    @Override
    public void endSkill() {
        super.endSkill();
        entityData.set(ATTACK_TIME, 0);
        setNoGravity(false);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        circles.clear();
    }

    @Override
    public void tick() {
        if (level().isClientSide()) {
            System.arraycopy(trail, 0, trail, 1, trail.length - 1);
            trail[0] = position();
        }
        if (!level().isClientSide()) {
            ensureArena();
            if (getTarget() != null && !validTarget(getTarget())) {
                setTarget(null);
                if (isUsingSkill()) endSkill();
            }
        }
        super.tick();
        playAnimationIfStopped(this, IDLE);
        if (level() instanceof ServerLevel server && isAlive()) {
            int intro = entityData.get(INTRO_TIME);
            if (intro < INTRO_DURATION) {
                entityData.set(INTRO_TIME, intro + 1);
                getNavigation().stop();
                setDeltaMovement(0, getDeltaMovement().y, 0);
                if (intro == 30)
                    setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItem.FORCE_FIELD_SCEPTER.get()));
                if (intro >= 55 && intro < 85) smoke(server, 8, 1.2);
                if (intro == 60) updateShieldCollision();
                if (intro == INTRO_DURATION - 1) setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            if (entityData.get(BREAK_TIME) > 0) {
                entityData.set(BREAK_TIME, entityData.get(BREAK_TIME) - 1);
                smoke(server, 6, 1.2);
            }
            entityData.set(ATTACK_TIME, isUsingSkill() ? getSkillTick() : 0);
            if (getAction() == SMOKE_MOVE) smoke(server, 10, 0.6);
            if (getPhase() == 2 && tickCount % 3 == 0 && random.nextFloat() < getBlur()) smoke(server, 2, 0.4);
            if (!isUsingSkill() && isSpawned() && entityData.get(BREAK_TIME) == 0 && validTarget(getTarget())) {
                getLookControl().setLookAt(getTarget(), 20, 20);
                // PathNavigationRegion can request a large rectangular chunk
                // region. Only use it when that entire region is already FULL.
                double padding = Math.min(128, getAttributeValue(Attributes.FOLLOW_RANGE) + 16);
                AABB pathArea = new AABB(position(), getTarget().position()).inflate(padding, 0, padding);
                if (distanceToSqr(getTarget()) > 9 && getAttributeValue(Attributes.FOLLOW_RANGE) <= 112 && isAreaLoaded(pathArea))
                    getNavigation().moveTo(getTarget(), 0.7);
                else getNavigation().stop();
            }
            constrainMovement();
            updateBars();
        }
    }

    private boolean validTarget(LivingEntity target) {
        return level().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL && target != null && target.isAlive() && !target.isSpectator() && !(target instanceof Player p && p.isCreative()) && (arena == null || arena.inflate(0, 1, 0).contains(target.position()));
    }

    @Override
    public boolean isSpawned() {
        return entityData.get(INTRO_TIME) >= INTRO_DURATION;
    }

    @Override
    public boolean hasSpawnProcess() {
        return true;
    }

    @Override
    public void spawn() {
    }

    @Override
    public void updatePhase() {
    }

    public boolean hasShield() {
        return entityData.get(SHIELD) > 0;
    }

    public float getShieldHealth() {
        return entityData.get(SHIELD);
    }

    public int getAction() {
        if (isDeadOrDying()) return DEATH;
        if (!isSpawned()) return INTRO;
        if (entityData.get(BREAK_TIME) > 0) return SHIELD_BREAK;
        return isUsingSkill() ? getSkillType() : IDLE_ACTION;
    }

    public int getActionTime() {
        return switch (getAction()) {
            case INTRO -> entityData.get(INTRO_TIME);
            case SHIELD_BREAK -> BREAK_DURATION - entityData.get(BREAK_TIME);
            case DEATH -> entityData.get(DEATH_TIME);
            default -> entityData.get(ATTACK_TIME);
        };
    }

    public float getBlur() {
        return getPhase() == 2 ? Mth.clamp(1 - getHealth() / getMaxHealth(), 0, 1) : 0;
    }

    public Vec3 getBeamOrigin() {
        return position().add(0, 1.35, 0);
    }

    public Vec3 getAimPoint() {
        Vector3f a = entityData.get(AIM);
        return getBeamOrigin().add(a.x, a.y, a.z);
    }

    public Vec3 getFormationCentre() {
        Vector3f a = entityData.get(CENTRE);
        return position().add(a.x, a.y, a.z);
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
            super.setHealth(hp);
            return;
        }
        float damage = Math.max(0, getHealth() - hp);
        if (damage <= 0) return;
        entityData.set(SHIELD, Math.max(0, getShieldHealth() - damage));
        if (level() instanceof ServerLevel server) smoke(server, 12, 1.4);
        if (!hasShield()) {
            endSkill();
            setPhase(2);
            entityData.set(BREAK_TIME, BREAK_DURATION);
            updateShieldCollision();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker == this || attacker != null && attacker.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(attacker.getPersistentData().getUUID("FinaleHierophantOwner")))
            return false;
        if (!isSpawned() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return shieldCollision ? EntityDimensions.scalable(2.6F, 2.6F).withEyeHeight(1.65F) : super.getDefaultDimensions(pose);
    }

    private void updateShieldCollision() {
        boolean next = hasShield() && entityData.get(INTRO_TIME) >= 60 && !isDeadOrDying();
        if (next != shieldCollision) {
            shieldCollision = next;
            refreshDimensions();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(SHIELD) || key.equals(INTRO_TIME) || key.equals(DEATH_TIME)) updateShieldCollision();
    }

    private void tickAttack(int id, int t) {
        if (!(level() instanceof ServerLevel server) || !validTarget(getTarget())) {
            endSkill();
            return;
        }
        getNavigation().stop();
        if (id != CHARGE && id != SMOKE_MOVE) setDeltaMovement(0, getDeltaMovement().y, 0);
        switch (id) {
            case CHARGE -> {
                if (t <= 24) {
                    lockedAim = getTarget().position();
                    aimAt(clampToArena(lockedAim));
                }
                if (t == 25) {
                    dashDirection = lockedAim.subtract(position()).multiply(1, 0, 1).normalize();
                    previousDash = position();
                }
                if (t >= 30 && t < 44 && !dashStopped) {
                    if (horizontalCollision || !insideArena(position().add(dashDirection.scale(1.05)))) {
                        dashStopped = true;
                        setDeltaMovement(0, getDeltaMovement().y, 0);
                    } else setDeltaMovement(dashDirection.scale(1.05).add(0, getDeltaMovement().y, 0));
                    AABB swept = getBoundingBox().minmax(getBoundingBox().move(previousDash.subtract(position()))).inflate(0.1);
                    for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, swept, this::canAttackVictim))
                        if (chargeHits.add(victim.getUUID())) {
                            victim.hurt(damageSources().mobAttack(this), HierophantConfig.CHARGE_DAMAGE.get().floatValue());
                            victim.push(dashDirection.x * 0.8, 0.25, dashDirection.z * 0.8);
                        }
                    previousDash = position();
                } else if (t >= 44) setDeltaMovement(0, getDeltaMovement().y, 0);
            }
            case RETURN_FRAGMENTS -> {
                if (t == 30) for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    server.addFreshEntity(new HierophantFragment(server, this, new Vec3(Math.cos(a), 0, Math.sin(a)), true));
                }
            }
            case FORMATION -> formation(server, t);
            case BEAM -> {
                if (t <= 30) {
                    lockedAim = getTarget().getEyePosition();
                    aimAt(CombatRay.trace(this, getBeamOrigin(), beamEnd(lockedAim), 0).end());
                }
                if (t == 40) {
                    var hit = CombatRay.trace(this, getBeamOrigin(), beamEnd(lockedAim), 0.25);
                    aimAt(hit.end());
                    if (hit.victim() != null && canAttackVictim(hit.victim()))
                        hit.victim().hurt(damageSources().indirectMagic(this, this), HierophantConfig.BEAM_DAMAGE.get().floatValue());
                    annihilateBlock(server, hit.end());
                }
            }
            case SUMMON -> {
                if (t == 1) chooseCircles();
                if (t < 70 && t % 3 == 0) for (Vec3 circle : circles) drawCircle(server, circle, t / 15D, 1.1);
                if (t == 70) for (Vec3 circle : circles) {
                    var minion = TFEntities.ADHERENT.get().create(server);
                    if (minion == null) continue;
                    minion.moveTo(circle.x, circle.y, circle.z, random.nextFloat() * 360, 0);
                    minion.finalizeSpawn(server, server.getCurrentDifficultyAt(BlockPos.containing(circle)), MobSpawnType.MOB_SUMMONED, null);
                    if (!server.noCollision(minion) || !insideArena(circle)) continue;
                    minion.getPersistentData().putUUID("FinaleHierophantOwner", getUUID());
                    minion.setTarget(getTarget());
                    minion.setPersistenceRequired();
                    minion.setCanPickUpLoot(false);
                    server.addFreshEntity(minion);
                }
            }
            case BARRAGE -> {
                // Active duration matches native HoverBeamGoal(this,80,100).
                if (t >= 40 && t < 40 + BARRAGE_FIRE_TICKS && (t - 40) % 5 == 0)
                    server.addFreshEntity(new HierophantFragment(server, this, getTarget().getEyePosition().subtract(getBeamOrigin()).normalize(), false));
            }
            case SMOKE_MOVE -> {
                setNoGravity(t < 30);
                fallDistance = 0;
                if (t < 30) {
                    Vec3 to = moveDestination.subtract(position());
                    if (to.lengthSqr() > 0.4) {
                        Vec3 velocity = to.normalize().scale(Math.min(0.9, to.length()));
                        if (insideArena(position().add(velocity)) && isAreaLoaded(getBoundingBox().move(velocity)) && server.noCollision(this, getBoundingBox().move(velocity)))
                            setDeltaMovement(velocity);
                        else setDeltaMovement(Vec3.ZERO);
                    } else setDeltaMovement(Vec3.ZERO);
                } else setDeltaMovement(0, getDeltaMovement().y, 0);
            }
        }
    }

    private Vec3 beamEnd(Vec3 towards) {
        Vec3 start = getBeamOrigin(), direction = towards.subtract(start).normalize(), end = start;
        for (double range = 0.25; range <= HierophantConfig.BEAM_RANGE.get(); range += 0.25) {
            Vec3 point = start.add(direction.scale(range));
            if (!insideArena(point) || !isAreaLoaded(new AABB(start, point).inflate(0.3))) break;
            end = point;
        }
        return end;
    }

    private boolean canAttackVictim(LivingEntity victim) {
        return victim != this && victim.isAlive() && !victim.isSpectator() && !(victim instanceof Player p && p.isCreative()) && !(victim instanceof twilightforest.entity.monster.Adherent) && insideArena(victim.position()) && hasLineOfSight(victim);
    }

    private void formation(ServerLevel server, int t) {
        Vec3 centre = getFormationCentre();
        boolean x = entityData.get(GRID_X);
        for (int row = 0; row < 7; row++) {
            int fire = 38 + row * 8;
            if (t < fire - 18 || t > fire + 3) continue;
            double offset = (row - 3) * 2;
            for (int step = -6; step <= 6; step++) {
                Vec3 cell = groundAt(centre.add(x ? offset : step, 0, x ? step : offset));
                if (!insideArena(cell) || !canStandAt(cell)) continue;
                if (t % 3 == 0 || t == fire)
                    server.sendParticles(t < fire ? ParticleTypes.WITCH : ParticleTypes.DRAGON_BREATH, cell.x, cell.y + 0.08, cell.z, 2, 0.12, t < fire ? 0.02 : 0.5, 0.12, 0.01);
                if (t == fire)
                    for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(cell.add(-0.48, 0, -0.48), cell.add(0.48, 2.5, 0.48)), this::canAttackVictim))
                        victim.hurt(damageSources().indirectMagic(this, this), HierophantConfig.GRID_DAMAGE.get().floatValue());
            }
        }
    }

    private void annihilateBlock(ServerLevel server, Vec3 end) {
        if (!HierophantConfig.BEAM_BREAKS_BLOCKS.get() || !net.neoforged.neoforge.event.EventHooks.canEntityGrief(server, this))
            return;
        Vec3 direction = end.subtract(getBeamOrigin()).normalize();
        BlockPos pos = BlockPos.containing(end.add(direction.scale(0.03)));
        if (!server.hasChunkAt(pos) || !insideArena(Vec3.atCenterOf(pos)) || server.getBlockEntity(pos) != null) return;
        var state = server.getBlockState(pos);
        var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.getDestroySpeed(server, pos) < 0 || id.getPath().contains("violet") || id.getPath().contains("purple"))
            return;
        boolean tf = id.getNamespace().equals("twilightforest") && (id.getPath().contains("deadrock") || id.getPath().contains("castle_brick") || id.getPath().contains("castle_rune_brick") || id.getPath().contains("force_field") || id.getPath().contains("thorns"));
        if (tf || state.getBlock().getExplosionResistance() < 8) {
            server.removeBlock(pos, false);
            server.sendParticles(TFParticleType.ANNIHILATE.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0);
        }
    }

    private void chooseMoveDestination() {
        moveDestination = position();
        if (getTarget() == null) return;
        for (int i = 0; i < 16; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            Vec3 candidate = groundAt(clampToArena(getTarget().position().add(Math.cos(angle) * 5, 0, Math.sin(angle) * 5)));
            if (!canStandAt(candidate)) continue;
            Vec3 delta = candidate.subtract(position());
            boolean clear = true;
            for (double f = 0.1; f <= 1; f += 0.1) {
                AABB step = getBoundingBox().move(delta.scale(f));
                if (!isAreaLoaded(step) || !level().noCollision(this, step)) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                moveDestination = candidate;
                break;
            }
        }
    }

    private void chooseCircles() {
        for (int i = 0; i < 3; i++)
            for (int attempt = 0; attempt < 32; attempt++) {
                double a = i * Math.PI * 2 / 3 + Math.toRadians(getYRot()) + attempt * 0.4;
                double radius = 4 - (attempt % 4) * 0.65;
                Vec3 at = groundAt(clampToArena(position().add(Math.cos(a) * radius, 0, Math.sin(a) * radius)));
                if (canStandAt(at) && circles.stream().noneMatch(p -> p.distanceToSqr(at) < 2.25)) {
                    circles.add(at);
                    break;
                }
            }
    }

    private void ensureArena() {
        if (arena != null) return;
        if (home == null || !home.dimension().equals(level().dimension()))
            home = GlobalPos.of(level().dimension(), blockPosition());
        int radius = HierophantConfig.ARENA_RADIUS.get();
        BlockPos at = home.pos();
        arena = new AABB(at.getX() - radius, at.getY() - 3, at.getZ() - radius, at.getX() + radius + 1, at.getY() + 8, at.getZ() + radius + 1);
        if (level() instanceof ServerLevel server && server.getChunkSource().getChunkNow(at.getX() >> 4, at.getZ() >> 4) != null) {
            var registry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
            var chunk = server.getChunkSource().getChunkNow(at.getX() >> 4, at.getZ() >> 4);
            var castle = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("twilightforest", "final_castle");
            Set<net.minecraft.world.level.levelgen.structure.StructureStart> starts = new HashSet<>();
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
        return arena == null || arena.contains(at);
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
        if (!level().isClientSide() && arena != null) {
            Vec3 limited = clampToArena(position().add(motion)).subtract(position());
            // Also handles external knockback. Never snap a displaced boss back home.
            motion = new Vec3(Mth.clamp(limited.x, -Math.abs(motion.x) - 0.3, Math.abs(motion.x) + 0.3), Mth.clamp(limited.y, -Math.abs(motion.y) - 0.3, Math.abs(motion.y) + 0.3), Mth.clamp(limited.z, -Math.abs(motion.z) - 0.3, Math.abs(motion.z) + 0.3));
        }
        super.move(type, motion);
    }

    private Vec3 clampToArena(Vec3 at) {
        if (arena == null) return at;
        double margin = getBbWidth() / 2 + 0.2;
        return new Vec3(Mth.clamp(at.x, arena.minX + margin, arena.maxX - margin), Mth.clamp(at.y, arena.minY, arena.maxY - getBbHeight()), Mth.clamp(at.z, arena.minZ + margin, arena.maxZ - margin));
    }

    private void constrainMovement() {
        if (arena == null) return;
        Vec3 next = position().add(getDeltaMovement()), clamped = clampToArena(next);
        if (next.distanceToSqr(clamped) > 0.0001) {
            getNavigation().stop();
            Vec3 correction = clamped.subtract(position());
            setDeltaMovement(correction.lengthSqr() > 0.81 ? correction.normalize().scale(0.9) : correction);
        }
    }

    private Vec3 groundAt(Vec3 near) {
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
        bodyBar.setName(getDisplayName());
        bodyBar.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0, 1));
        shieldBar.setProgress(Mth.clamp(getShieldHealth() / entityData.get(SHIELD_MAX), 0, 1));
        shieldBar.setVisible(hasShield() && entityData.get(INTRO_TIME) >= 60 && isAlive());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bodyBar.addPlayer(player);
        shieldBar.addPlayer(player);
        updateBars();
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bodyBar.removePlayer(player);
        shieldBar.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        bodyBar.removeAllPlayers();
        shieldBar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void checkDespawn() {
    } // Never replace a native boss block.

    @Override
    public GlobalPos getRestrictionPoint() {
        return home;
    }

    @Override
    public void setRestrictionPoint(GlobalPos pos) {
        home = pos;
        arena = null;
    }

    @Override
    public int getHomeRadius() {
        return 24;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return entity.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(entity.getPersistentData().getUUID("FinaleHierophantOwner")) || super.isAlliedTo(entity);
    }

    @Override
    protected void dropEquipment() {
    } // The summoning prop is not a fifth reward.

    @Override
    protected void dropAllDeathLoot(ServerLevel server, DamageSource source) {
        if (rewardPending || rewardCreated) return;
        rewardPending = true;
        captureDrops(new ArrayList<>());
        if (server.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            dropFromLootTable(source, lastHurtByPlayerTime > 0);
            dropCustomDeathLoot(server, source, lastHurtByPlayerTime > 0);
            if (lastHurtByPlayerTime > 0)
                rewardExperience = net.neoforged.neoforge.event.EventHooks.getExperienceDrop(this, lastHurtByPlayer, getExperienceReward(server, source.getEntity()));
        }
        Collection<ItemEntity> drops = captureDrops(null);
        if (!net.neoforged.neoforge.common.CommonHooks.onLivingDrops(this, source, drops, lastHurtByPlayerTime > 0)) {
            int slot = 0;
            for (ItemEntity drop : drops) if (slot < rewards.size()) rewards.set(slot++, drop.getItem().copy());
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!dead) return;
        endSkill();
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        updateShieldCollision();
        bodyBar.setProgress(0);
        shieldBar.setVisible(false);
        if (level() instanceof ServerLevel server)
            for (var minion : server.getEntitiesOfClass(twilightforest.entity.monster.Adherent.class, getBoundingBox().inflate(48), e -> e.getPersistentData().hasUUID("FinaleHierophantOwner") && getUUID().equals(e.getPersistentData().getUUID("FinaleHierophantOwner"))))
                minion.discard();
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if (!(level() instanceof ServerLevel server)) return;
        entityData.set(DEATH_TIME, deathTime);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        getNavigation().stop();
        if (deathTime >= 50 && deathTime < 125) smoke(server, 4 + (deathTime - 50) / 10, 0.65);
        if (deathTime >= 115) {
            Vec3 centre = position().add(0, 0.5, 0);
            double radius = (DEATH_DURATION - deathTime) / 15D;
            for (int i = 0; i < 8; i++) {
                double a = deathTime * 0.15 + i * Math.PI / 4;
                Vec3 point = centre.add(Math.cos(a) * radius, (DEATH_DURATION - deathTime) / 35D, Math.sin(a) * radius);
                Vec3 v = centre.subtract(point).scale(0.1);
                server.sendParticles(ParticleTypes.REVERSE_PORTAL, point.x, point.y, point.z, 0, v.x, v.y, v.z, 1);
            }
        }
        if (deathTime >= DEATH_DURATION && !isRemoved()) {
            createReward(server);
            remove(RemovalReason.KILLED);
        }
    }

    private void createReward(ServerLevel server) {
        if (rewardCreated || !rewardPending) return;
        rewardCreated = true;
        rewardPending = false;
        if (!server.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        if (rewards.stream().anyMatch(s -> !s.isEmpty())) {
            BlockPos at = BlockPos.containing(groundAt(position()));
            boolean placed = false;
            for (int dy = 0; dy < 4 && !placed; dy++) {
                BlockPos candidate = at.above(dy);
                if (!server.hasChunkAt(candidate) || !insideArena(Vec3.atCenterOf(candidate)) || !server.getBlockState(candidate).canBeReplaced() || server.getBlockEntity(candidate) != null)
                    continue;
                if (server.setBlockAndUpdate(candidate, TFBlocks.CANOPY_CHEST.get().defaultBlockState()) && server.getBlockEntity(candidate) instanceof ChestBlockEntity chest) {
                    for (int i = 0; i < rewards.size(); i++) chest.setItem(i, rewards.get(i));
                    chest.setChanged();
                    placed = true;
                }
            }
            if (!placed) for (ItemStack stack : rewards) if (!stack.isEmpty()) spawnAtLocation(stack);
            rewards.clear();
        }
        if (rewardExperience > 0) ExperienceOrb.award(server, position(), rewardExperience);
        rewardExperience = 0;
        skipDropExperience();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        saveHomePointToNbt(tag);
        tag.putInt("HierophantIntro", entityData.get(INTRO_TIME));
        tag.putFloat("HierophantShield", getShieldHealth());
        tag.putFloat("HierophantShieldMax", entityData.get(SHIELD_MAX));
        tag.putInt("HierophantBreakTime", entityData.get(BREAK_TIME));
        tag.putBoolean("HierophantRewardPending", rewardPending);
        tag.putBoolean("HierophantRewardCreated", rewardCreated);
        tag.putInt("HierophantRewardExperience", rewardExperience);
        CompoundTag loot = new CompoundTag();
        ContainerHelper.saveAllItems(loot, rewards, registryAccess());
        tag.put("HierophantRewards", loot);
        if (arena != null)
            tag.put("HierophantArena", newDoubleList(arena.minX, arena.minY, arena.minZ, arena.maxX, arena.maxY, arena.maxZ));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        loadHomePointFromNbt(tag);
        if (tag.contains("HierophantIntro"))
            entityData.set(INTRO_TIME, Mth.clamp(tag.getInt("HierophantIntro"), 0, INTRO_DURATION));
        entityData.set(BREAK_TIME, Mth.clamp(tag.getInt("HierophantBreakTime"), 0, BREAK_DURATION));
        float max = tag.contains("HierophantShieldMax") ? tag.getFloat("HierophantShieldMax") : HierophantConfig.SHIELD_HEALTH.get().floatValue();
        entityData.set(SHIELD_MAX, Math.max(1, max));
        entityData.set(SHIELD, tag.contains("HierophantShield") ? Mth.clamp(tag.getFloat("HierophantShield"), 0, Math.max(1, max)) : getPhase() == 2 ? 0 : max);
        rewardPending = tag.getBoolean("HierophantRewardPending");
        rewardCreated = tag.getBoolean("HierophantRewardCreated");
        ContainerHelper.loadAllItems(tag.getCompound("HierophantRewards"), rewards, registryAccess());
        rewardExperience = Math.max(0, tag.getInt("HierophantRewardExperience"));
        var bounds = tag.getList("HierophantArena", net.minecraft.nbt.Tag.TAG_DOUBLE);
        if (bounds.size() == 6 && bounds.getDouble(3) > bounds.getDouble(0) && bounds.getDouble(5) > bounds.getDouble(2))
            arena = new AABB(bounds.getDouble(0), bounds.getDouble(1), bounds.getDouble(2), bounds.getDouble(3), bounds.getDouble(4), bounds.getDouble(5));
        if (!hasShield()) setPhase(2);
        entityData.set(DEATH_TIME, deathTime);
        updateShieldCollision();
        setCanPickUpLoot(false);
        if (entityData.get(INTRO_TIME) >= 30 && !isSpawned())
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItem.FORCE_FIELD_SCEPTER.get()));
    }
}
