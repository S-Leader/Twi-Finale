package show.twilight.finale.entity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import twilightforest.config.TFConfig;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.entity.boss.IBossLootBuffer;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;
import twilightforest.loot.TFLootTables;
import twilightforest.util.entities.EntityUtil;

import show.twilight.finale.runtime.entity.BossEntity;
import show.twilight.finale.runtime.entity.skill.BaseSkill;
import show.twilight.finale.runtime.entity.skill.SkillManager;

public final class AnnihilationGolemEntity extends BossEntity implements EnforcedHomePoint, IBossLootBuffer {
    public static final int CHASE = 0, SWING = 1, SMOKE = 2, SLAM = 3, DYING = 4;
    public static final int SWING_HIT = 8, SWING_DURATION = 20;
    public static final int SMOKE_START = 32, SMOKE_END = 92, SMOKE_DURATION = 108;
    public static final int SLAM_HIT = 32, SLAM_DURATION = 66;
    public static final int GLASS_BREAK = 40, DEATH_DURATION = 90;
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(AnnihilationGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTION_START = SynchedEntityData.defineId(AnnihilationGolemEntity.class, EntityDataSerializers.LONG);
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.twilightforest_finale.annihilation_golem"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private final NonNullList<ItemStack> dyingInventory = NonNullList.withSize(IBossLootBuffer.CONTAINER_SIZE, ItemStack.EMPTY);
    private final SkillManager<AnnihilationGolemEntity> skillManager = new SkillManager<>(this, List.of(
            attack(SWING, SWING_DURATION, 4.8), attack(SMOKE, SMOKE_DURATION, 16), attack(SLAM, SLAM_DURATION, 7)));
    private GlobalPos home;
    private BlockPos deathChestPos;
    private int cooldown = 30, smokeCooldown, slamCooldown, lastAttack;
    private DamageSource deferredDeathSource;
    private boolean lootPrepared, lootDropped;

    private static BaseSkill<AnnihilationGolemEntity> attack(int id, int duration, double range) {
        return new BaseSkill<>(id, duration, range, 0) {
            @Override
            public void tick(AnnihilationGolemEntity golem) {
                golem.holdAttackPose();
            }

            @Override
            public void effect(AnnihilationGolemEntity golem) {
                if (golem.level() instanceof ServerLevel server) golem.tickAttack(server, id, golem.getSkillTick());
            }
        };
    }

    public AnnihilationGolemEntity(EntityType<? extends AnnihilationGolemEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 400).add(Attributes.ATTACK_DAMAGE, 16)
                .add(Attributes.ARMOR, 10).add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.STEP_HEIGHT, 1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, CHASE);
        builder.define(ACTION_START, 0L);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, this::canFight));
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
                yBodyRot = getYRot();
                yHeadRot = getYRot();
            }
        };
    }

    public int getAction() {
        return entityData.get(ACTION);
    }

    public int getActionTime() {
        return (int) Math.max(0, level().getGameTime() - entityData.get(ACTION_START));
    }

    @Override
    public boolean isUsingSkill() {
        return getAction() == SWING || getAction() == SMOKE || getAction() == SLAM;
    }

    @Override
    public int getSkillType() {
        return getAction();
    }

    @Override
    public int getSkillTick() {
        return getActionTime();
    }

    @Override
    public void beginSkill(int id) {
        if (level().isClientSide() || !isAlive() || isUsingSkill() || id < SWING || id > SLAM) return;
        super.beginSkill(id);
        setAction(id);
        lastAttack = id;
        if (id == SMOKE) smokeCooldown = 200;
        else if (id == SLAM) slamCooldown = 180;
        else playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.6F, 0.7F);
    }

    @Override
    public void endSkill() {
        int previous = getAction();
        super.endSkill();
        if (isAlive() && previous != DYING) {
            setAction(CHASE);
            cooldown = previous == SWING ? 18 : 35;
        }
    }

    public boolean isEmittingSmoke() {
        return getAction() == SMOKE && getActionTime() >= SMOKE_START && getActionTime() < SMOKE_END;
    }

    public boolean isCoreBroken() {
        return getAction() == DYING && getActionTime() >= GLASS_BREAK;
    }

    private void setAction(int action) {
        entityData.set(ACTION, action);
        entityData.set(ACTION_START, level().getGameTime());
        getNavigation().stop();
        setSpeed(0);
        setXxa(0);
        setZza(0);
        setDeltaMovement(0, getDeltaMovement().y, 0);
    }

    private boolean canFight(LivingEntity target) {
        return target != null && target != this && target.isAlive() && !target.isSpectator() && !isAlliedTo(target)
                && !(target instanceof Player player && player.getAbilities().instabuild) && isMobWithinHomeArea(target);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel) || !isAlive()) return;
        updatePhase();
        bossBar.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0, 1));
        if (smokeCooldown > 0) smokeCooldown--;
        if (slamCooldown > 0) slamCooldown--;
        if (!canFight(getTarget())) setTarget(null);
        int action = getAction();
        if (action == CHASE) {
            if (cooldown > 0) cooldown--;
            if (getTarget() == null) {
                if (isRestrictionPointValid(level().dimension()) && distanceToSqr(Vec3.atCenterOf(home.pos())) > 36) {
                    if (getNavigation().isDone() || tickCount % 20 == 0)
                        getNavigation().moveTo(home.pos().getX() + 0.5, home.pos().getY(), home.pos().getZ() + 0.5, 1);
                } else getNavigation().stop();
                return;
            }
            double distance = position().subtract(getTarget().position()).horizontalDistance();
            faceTarget(8);
            if (distance > 3.8) {
                if (tickCount % 10 == 0) getNavigation().moveTo(getTarget(), 1);
            } else {
                getNavigation().stop();
                setDeltaMovement(0, getDeltaMovement().y, 0);
            }
            return;
        }
    }

    private void holdAttackPose() {
        getNavigation().stop();
        setSpeed(0);
        setXxa(0);
        setZza(0);
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (getTarget() != null && (getAction() == SMOKE || getActionTime() < 6))
            faceTarget(getAction() == SMOKE ? 2 : 8);
    }

    private void tickAttack(ServerLevel server, int action, int t) {
        switch (action) {
            case SWING -> {
                if (t == SWING_HIT) swingAttack(server);
            }
            case SMOKE -> {
                if (t == 27) playSound(SoundEvents.LEVER_CLICK, 2, 0.65F);
                if (t >= SMOKE_START && t < SMOKE_END && getTarget() != null) emitSmoke(server);
            }
            case SLAM -> {
                if (t == SLAM_HIT) slam(server);
                if (t >= SLAM_HIT + 6 && t <= SLAM_HIT + 18 && t % 6 == 2) dropCeilingBricks(server);
            }
            default -> {
            }
        }
    }

    private void faceTarget(float speed) {
        Vec3 delta = getTarget().position().subtract(position());
        if (delta.horizontalDistanceSqr() < 0.04) return;
        float wanted = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90;
        setYRot(Mth.wrapDegrees(Mth.approachDegrees(getYRot(), wanted, speed)));
        yBodyRot = yHeadRot = getYRot();
    }

    private boolean isFacingTarget(float tolerance) {
        Vec3 delta = getTarget().position().subtract(position());
        float wanted = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90;
        return delta.horizontalDistanceSqr() < 0.04 || Math.abs(Mth.wrapDegrees(wanted - getYRot())) <= tolerance;
    }

    private Vec3 forward() {
        return Vec3.directionFromRotation(0, getYRot());
    }

    private void swingAttack(ServerLevel server) {
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5, 1, 3.5), this::canFight)) {
            Vec3 delta = victim.position().subtract(position()).multiply(1, 0, 1);
            if (delta.horizontalDistanceSqr() > 4.8 * 4.8 || delta.normalize().dot(forward()) < 0.1 || !getSensing().hasLineOfSight(victim))
                continue;
            if (victim.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                victim.push(forward().x * 0.7, 0.35, forward().z * 0.7);
                victim.hurtMarked = true;
            }
        }
    }

    public Vec3 smokeOrigin() {

        Vec3 forward = forward(), left = new Vec3(forward.z, 0, -forward.x);
        return position().add(left.scale(1.076931)).add(forward.scale(1.639051)).add(0, 0.639404, 0);
    }

    private void emitSmoke(ServerLevel server) {
        Vec3 origin = smokeOrigin(), horizontal = forward();
        double pitch = Mth.clamp((getTarget().getEyeY() - origin.y) / Math.max(1, origin.distanceTo(getTarget().getEyePosition())), -0.25, 0.4);
        Vec3 direction = horizontal.add(0, pitch, 0).normalize();
        Vec3 end = server.clip(new ClipContext(origin, origin.add(direction.scale(16)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getLocation();
        Vec3 ray = end.subtract(origin);
        for (int i = 0; i < 9; i++) {
            Vec3 at = origin.add(ray.scale(i / 9D));
            double spread = 0.16 + i * 0.065;
            server.sendParticles(TFParticleType.HUGE_SMOKE.get(), at.x, at.y, at.z, 1, spread, spread, spread, 0.015);
        }
        if (getActionTime() % 10 == 2) {
            playSound(SoundEvents.FIRE_EXTINGUISH, 1.6F, 0.55F);
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(origin, end).inflate(0.9), this::canFight)) {
                if (victim.getBoundingBox().inflate(0.75).contains(origin) || victim.getBoundingBox().inflate(0.75).clip(origin, end).isPresent()) {
                    victim.hurt(damageSources().indirectMagic(this, this), 4);
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1), this);
                    victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1), this);
                }
            }
        }
    }

    private void slam(ServerLevel server) {
        Vec3 at = position().add(forward().scale(1.8));
        playSound(TFSounds.MINOSHROOM_SLAM.get(), 3, 0.7F);
        for (int i = 0; i < 70; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI, radius = Math.sqrt(random.nextDouble()) * 6;
            BlockPos floor = groundAt(server, at.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
            var state = server.getBlockState(floor);
            if (!state.isAir())
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), floor.getX() + 0.5, floor.getY() + 1.05, floor.getZ() + 0.5, 2, 0.2, 0.08, 0.2, 0.12);
        }
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(6, 3, 6), this::canFight)) {
            if (victim.position().subtract(at).horizontalDistanceSqr() > 36) continue;
            if (victim.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                Vec3 push = victim.position().subtract(at).multiply(1, 0, 1).normalize();
                victim.push(push.x * 0.7, 0.6, push.z * 0.7);
                victim.hurtMarked = true;
            }
        }
    }

    private BlockPos groundAt(ServerLevel server, Vec3 point) {
        var hit = server.clip(new ClipContext(point.add(0, 2, 0), point.add(0, -5, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : BlockPos.containing(point).below();
    }

    private void dropCeilingBricks(ServerLevel server) {
        if (!server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        for (int i = 0; i < 4; i++) {
            Vec3 around = i == 0 && getTarget() != null ? getTarget().position() : position().add((random.nextDouble() - .5) * 12, 0, (random.nextDouble() - .5) * 12);
            BlockPos base = BlockPos.containing(around.x, getY() + getBbHeight() + 1, around.z);
            for (int dy = 0; dy <= 20; dy++) {
                BlockPos pos = base.above(dy);
                if (!server.hasChunkAt(pos)) break;
                var state = server.getBlockState(pos);
                if (state.getCollisionShape(server, pos).isEmpty()) continue;
                var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                if (id.getNamespace().equals("twilightforest") && id.getPath().contains("castle_brick") && server.getBlockEntity(pos) == null
                        && state.canEntityDestroy(server, pos, this) && EventHooks.onEntityDestroyBlock(this, pos, state)) {
                    server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + .5, pos.getY() - .1, pos.getZ() + .5, 12, .4, .1, .4, .05);
                    FallingBlockEntity brick = FallingBlockEntity.fall(server, pos, state);
                    brick.setHurtsEntities(2, 16);
                    brick.dropItem = false;
                    brick.disableDrop();
                }
                break;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && (source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.getDirectEntity() instanceof Projectile
                || source.getEntity() instanceof LivingEntity && source.getDirectEntity() != source.getEntity()))
            return false;
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (dead) return;
        super.die(source);
        if (!dead) return;
        deathChestPos = EntityUtil.bossChestLocation(this);
        super.endSkill();
        setAction(DYING);
        bossBar.setVisible(false);
        playSound(SoundEvents.RAVAGER_ROAR, 3, 0.6F);
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel server, DamageSource source) {
        deferredDeathSource = source;
        if (lootPrepared) return;
        lootPrepared = true;
        if (shouldDropLoot() && server.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {

            var params = TFLootTables.createLootParams(this, lastHurtByPlayerTime > 0, source).create(LootContextParamSets.ENTITY);
            var table = server.getServer().reloadableRegistries().getLootTable(getLootTable());
            fill(this, params, table);
        }
    }

    @Override
    protected void dropFromLootTable(DamageSource source, boolean killedByPlayer) {
        if (!lootPrepared && dyingInventory.stream().allMatch(ItemStack::isEmpty)) {
            if (!(level() instanceof ServerLevel server)) return;

            lootPrepared = true;
            var params = TFLootTables.createLootParams(this, killedByPlayer, source).create(LootContextParamSets.ENTITY);
            fill(this, params, server.getServer().reloadableRegistries().getLootTable(getLootTable()));
        }
        if (dyingInventory.stream().allMatch(ItemStack::isEmpty)) return;
        if (TFConfig.bossDropChests && level() instanceof ServerLevel server) {
            BlockPos chest = deathChestPos != null ? deathChestPos : EntityUtil.bossChestLocation(this);
            IBossLootBuffer.depositDropsIntoChest(this, TFBlocks.TWILIGHT_OAK_CHEST.get().defaultBlockState(), chest, server);
        } else {
            for (ItemStack stack : dyingInventory)
                if (!stack.isEmpty()) spawnAtLocation(stack.copy());
        }
        dyingInventory.clear();
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (!(level() instanceof ServerLevel server)) return;
        int time = getActionTime();
        if (time == GLASS_BREAK) {
            Vec3 at = position().add(forward().scale(-0.26)).add(0, 2.5, 0);
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState()), at.x, at.y, at.z, 65, .5, .5, .5, .15);
            playSound(SoundEvents.GLASS_BREAK, 3, 0.7F);
        }
        if (time == 72) playSound(TFSounds.MINOSHROOM_SLAM.get(), 2.5F, 0.6F);
        if (time < DEATH_DURATION) return;
        if (!lootDropped) {
            lootDropped = true;
            super.dropAllDeathLoot(server, deferredDeathSource != null ? deferredDeathSource : damageSources().generic());
            dyingInventory.clear();
        }
        remove(RemovalReason.KILLED);
    }

    @Override
    public boolean isAlwaysExperienceDropper() {
        return level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public GlobalPos getRestrictionPoint() {
        return home;
    }

    @Override
    public void setRestrictionPoint(GlobalPos point) {
        home = point;
    }

    @Override
    public int getHomeRadius() {
        return 28;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        bossBar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        saveHomePointToNbt(tag);
        tag.putInt("GolemAction", getAction());
        tag.putInt("GolemTime", getActionTime());
        tag.putInt("AttackCooldown", cooldown);
        tag.putInt("SmokeCooldown", smokeCooldown);
        tag.putInt("SlamCooldown", slamCooldown);
        tag.putInt("LastGolemAttack", lastAttack);
        tag.putBoolean("GolemLootDropped", lootDropped);
        tag.putBoolean("GolemLootPrepared", lootPrepared);
        if (deathChestPos != null) tag.putLong("GolemDeathChestPos", deathChestPos.asLong());
        CompoundTag inventory = new CompoundTag();
        addDeathItemsSaveData(inventory, registryAccess());
        tag.put("GolemLootBuffer", inventory);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        loadHomePointFromNbt(tag);
        entityData.set(ACTION, Mth.clamp(tag.getInt("GolemAction"), CHASE, DYING));
        entityData.set(ACTION_START, level().getGameTime() - Math.max(0, tag.getInt("GolemTime")));
        cooldown = tag.contains("AttackCooldown") ? Math.max(0, tag.getInt("AttackCooldown")) : 30;
        smokeCooldown = Math.max(0, tag.getInt("SmokeCooldown"));
        slamCooldown = Math.max(0, tag.getInt("SlamCooldown"));
        lastAttack = Mth.clamp(tag.getInt("LastGolemAttack"), CHASE, SLAM);
        lootDropped = tag.getBoolean("GolemLootDropped");
        lootPrepared = tag.getBoolean("GolemLootPrepared");
        deathChestPos = tag.contains("GolemDeathChestPos") ? BlockPos.of(tag.getLong("GolemDeathChestPos")) : null;
        dyingInventory.clear();
        if (tag.contains("GolemLootBuffer", Tag.TAG_COMPOUND))
            readDeathItemsSaveData(tag.getCompound("GolemLootBuffer"), registryAccess());
        updatePhase();
        if (getHealth() <= 0) {
            dead = true;
            entityData.set(ACTION, DYING);
            bossBar.setVisible(false);
        } else if (getAction() == DYING) {
            setAction(CHASE);
        } else if (isUsingSkill()) {

            super.beginSkill(getAction());
        }
    }

    @Override
    public void updatePhase() {

        if (!level().isClientSide() && (getPhase() != 1 || updatePhase)) setPhase(1);
    }

    @Override
    public void spawn() {
        if (level().isClientSide() || !isAlive()) return;
        super.endSkill();
        setAction(CHASE);
        cooldown = 30;
        updatePhase();
        bossBar.setName(getDisplayName());
        bossBar.setVisible(true);
    }

    @Override
    public boolean hasSpawnProcess() {
        return false;
    }

    @Override
    public SkillManager<AnnihilationGolemEntity> getSkillManager() {
        return skillManager;
    }

    @Override
    public List<Integer> getSkillPool() {
        LivingEntity target = getTarget();
        if (isNoAi() || !isAlive() || getAction() != CHASE || cooldown > 0 || !canFight(target)
                || !getSensing().hasLineOfSight(target) || !isFacingTarget(12)) return List.of();
        double distance = position().subtract(target.position()).horizontalDistance();
        if (distance <= 7 && slamCooldown == 0
                && (lastAttack != SLAM && random.nextInt(3) == 0 || distance < 3 && lastAttack == SMOKE))
            return List.of(SLAM);
        if (distance <= 16 && smokeCooldown == 0
                && (distance > 4.8 || lastAttack == SWING && random.nextBoolean())) return List.of(SMOKE);
        if (distance <= 4.8) return List.of(SWING);
        return List.of();
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return dyingInventory;
    }
}
