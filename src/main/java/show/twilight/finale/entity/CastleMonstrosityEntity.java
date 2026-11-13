package show.twilight.finale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.entity.combat.ColoredAdherent;
import show.twilight.finale.entity.combat.MonstrosityMoveControl;
import show.twilight.finale.entity.projectile.MonstrosityBomb;
import show.twilight.finale.init.ModDamageTypes;
import twilightforest.entity.monster.Adherent;
import twilightforest.init.TFEntities;
import twilightforest.init.TFItems;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;

import java.util.*;

/**
 * Server owns attacks, charge accounting and encounter continuation.
 */
public class CastleMonstrosityEntity extends Monster {
    public static final int CHASE = 0, SMASH = 1, SPIT = 2, SUMMON = 3, TRAP = 4, FIRE = 5,
            BROKEN = 6, RECOVER = 7, DYING = 8, ACTIVATE = 9, RETRACT_TRAP = 10, RETRACT_FIRE = 11, TRIPLE_SMASH = 12;
    public static final int COMBO_HIT_1 = 33, COMBO_HIT_2 = 55, COMBO_HIT_3 = 77, COMBO_DURATION = 106;
    public static final int TRAP_PULL_START = 33, TRAP_PULL_END = 93, TRAP_HIT = 108;
    // Hydra's FLAMING state lasts 100 ticks and deals 19 fire damage, with 3 seconds of fire.
    public static final int FIRE_START = 33, FIRE_DURATION = 100, BROKEN_DURATION = 100, DEATH_DURATION = 140;
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(CastleMonstrosityEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTION_START = SynchedEntityData.defineId(CastleMonstrosityEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> CHARGE = SynchedEntityData.defineId(CastleMonstrosityEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CAPTURED = SynchedEntityData.defineId(CastleMonstrosityEntity.class, EntityDataSerializers.BOOLEAN);
    private static final TagKey<Item> MAZEBREAKER_LIKE = TagKey.create(Registries.ITEM, TwilightFinaleMod.prefix("mazebreaker-like"));
    public static final String SUMMONER_TAG = "FinaleMonstrosityOwner";
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.twilightforest_finale.castle_monstrosity"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private UUID hierophant;
    private CompoundTag hierophantData;
    private final Set<UUID> summons = new HashSet<>();
    private int cooldown = 35, lastAttack = -1, chargeSoundClock;
    private boolean resumed;

    public CastleMonstrosityEntity(EntityType<? extends CastleMonstrosityEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        moveControl = new MonstrosityMoveControl(this);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 400).add(Attributes.ATTACK_DAMAGE, 16)
                .add(Attributes.ARMOR, 10).add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 64).add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(ACTION, CHASE);
        b.define(ACTION_START, 0L);
        b.define(CHARGE, 0);
        b.define(CAPTURED, false);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, this::canAttackTarget));
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
                setYBodyRot(getYRot());
                setYHeadRot(getYRot());
            }
        };
    }

    @Override
    protected float tickHeadTurn(float yaw, float movement) {
        setYBodyRot(getYRot());
        setYHeadRot(getYRot());
        return movement;
    }

    @Override
    public void travel(Vec3 input) {
        if (getAction() != CHASE || !isAlive()) {
            setDeltaMovement(0, getDeltaMovement().y, 0);
            setSpeed(0);
            setXxa(0);
            setZza(0);
            super.travel(Vec3.ZERO);
        } else super.travel(input);
    }

    public void bindHierophant(UUID owner) {
        hierophant = owner;
    }

    public void bindHierophant(UUID owner, CompoundTag data) {
        hierophant = owner;
        hierophantData = data == null ? null : data.copy();
    }

    public boolean storeHierophant(TheTwilightHierophantEntity owner) {
        if (hierophant == null || !hierophant.equals(owner.getUUID()) || !owner.isWaitingForMonstrosity(getUUID()))
            return false;
        hierophantData = owner.saveWithoutId(new CompoundTag());
        owner.discard();
        return true;
    }

    private void captureLegacyOwner(ServerLevel server) {
        if (hierophantData == null && hierophant != null && server.getEntity(hierophant) instanceof TheTwilightHierophantEntity owner)
            storeHierophant(owner);
    }

    public void activate() {
        setAction(ACTIVATE);
    }

    public int getAction() {
        return entityData.get(ACTION);
    }

    public int getActionTime() {
        return (int) Math.max(0, level().getGameTime() - entityData.get(ACTION_START));
    }

    public int getTrapCharge() {
        return entityData.get(CHARGE);
    }

    public boolean hasCapturedTarget() {
        return entityData.get(CAPTURED);
    }

    public boolean isPreparingAttack() {
        return getAction() == CHASE && getTarget() != null && (cooldown <= 0 || getTrapCharge() >= 3);
    }

    public void setAction(int action) {
        entityData.set(ACTION, action);
        entityData.set(ACTION_START, level().getGameTime());
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (action == TRAP) {
            entityData.set(CHARGE, 0);
            entityData.set(CAPTURED, false);
        }
    }

    private boolean canAttackTarget(LivingEntity e) {
        return e != null && e != this && e.isAlive() && !e.isSpectator() && !(e instanceof Player p && p.getAbilities().instabuild) && !isAlliedTo(e);
    }

    public boolean canDamageWithSkill(LivingEntity e) {
        // Adherents are allies for targeting, but can be caught in the giant's area attacks.
        return (e instanceof Adherent && e.isAlive() && !e.isSpectator()) || canAttackTarget(e);
    }

    @Override
    public void setTarget(LivingEntity target) {
        super.setTarget(target != null && isAlliedTo(target) ? null : target);
    }

    @Override
    public boolean isAlliedTo(Entity e) {
        return e instanceof CastleMonstrosityEntity || e instanceof TheTwilightHierophantEntity || e instanceof Adherent
                || e.getPersistentData().hasUUID(SUMMONER_TAG) && getUUID().equals(e.getPersistentData().getUUID(SUMMONER_TAG)) || super.isAlliedTo(e);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)) return;
        captureLegacyOwner(server);
        if (!isAlive()) return;
        bossBar.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0, 1));
        if (!canAttackTarget(getTarget())) setTarget(null);
        int action = getAction(), t = getActionTime();
        if (getTrapCharge() > 0 && ++chargeSoundClock >= 10 && action != TRAP) {
            chargeSoundClock = 0;
            playChargeSound();
        }
        if (action == CHASE) {
            if (getTarget() == null) {
                getNavigation().stop();
                setDeltaMovement(0, getDeltaMovement().y, 0);
                return;
            }
            if (getTrapCharge() >= 3) {
                getNavigation().stop();
                if (isFacingTarget(8)) setAction(TRAP);
                return;
            }
            if (cooldown <= 0) getNavigation().stop();
            if (tickCount % 5 == 0) breakObstacles(server);
            // Both successful paths and wall-breaking fallback use the same forward move controller.
            if (cooldown > 0 && tickCount % 10 == 0) getNavigation().moveTo(getTarget(), 1);
            if (cooldown > 0 && getNavigation().isDone()) {
                getMoveControl().setWantedPosition(getTarget().getX(), getTarget().getY(), getTarget().getZ(), 1);
            }
            if (--cooldown <= 0) {
                // A path waypoint can be beside/behind the player. Finish aiming at the
                // actual target before choosing an attack that plants the feet.
                if (!isFacingTarget(8)) return;
                int next;
                if (distanceToSqr(getTarget()) < 81 && lastAttack != TRIPLE_SMASH) {
                    if (lastAttack == SMASH) next = TRIPLE_SMASH;
                    else next = random.nextBoolean() ? SMASH : TRIPLE_SMASH;
                } else {
                    List<Integer> choices = new ArrayList<>(List.of(SPIT, SUMMON, FIRE));
                    choices.remove(Integer.valueOf(lastAttack));
                    if (summons.size() >= 9) choices.remove(Integer.valueOf(SUMMON));
                    next = choices.get(random.nextInt(choices.size()));
                }
                lastAttack = next;
                setAction(next);
            }
            return;
        }
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        boolean smashWindup = action == SMASH && t <= 28 || action == TRIPLE_SMASH && (t <= 28 || t >= 38 && t <= 50 || t >= 60 && t <= 72);
        if (smashWindup || action == SUMMON && t < 50) faceTarget(8);
        else if (action != SMASH && action != TRIPLE_SMASH && action != SUMMON && action != BROKEN && action != RECOVER && action != DYING)
            faceTarget(action == FIRE && t >= FIRE_START ? 1.5F : 8);
        switch (action) {
            case ACTIVATE -> {
                if (t >= 65) finishAttack();
            }
            case SMASH -> {
                if (t == 33) slam(server, position().add(forward().scale(3)), 6, 16);
                if (t >= 66) finishAttack();
            }
            case TRIPLE_SMASH -> {
                // Each impact has its own wind-up. Lock the feet/body for the whole combo.
                if (t == COMBO_HIT_1 || t == COMBO_HIT_2 || t == COMBO_HIT_3)
                    slam(server, position().add(forward().scale(3)), 6, 16);
                if (t >= COMBO_DURATION) finishAttack();
            }
            case SPIT -> {
                if (t == 20 && getTarget() != null) {
                    Vec3 mouth = position().add(0, 3.9, 0).add(forward().scale(1.5));
                    MonstrosityBomb bomb = new MonstrosityBomb(server, this, mouth, getTarget().getEyePosition().subtract(mouth));
                    server.addFreshEntity(bomb);
                    playSound(SoundEvents.GHAST_SHOOT, 3, 0.7F);
                }
                if (t >= 37) finishAttack();
            }
            case SUMMON -> {
                if (t >= 30 && t <= 58 && t % 3 == 0) drawSummoningCircles(server, t);
                if (t == 58) {
                    slam(server, position().add(forward().scale(2)), 4, 16);
                    summonAdherents(server);
                }
                if (t >= 93) finishAttack();
            }
            case TRAP -> tickTrap(server, t);
            case FIRE -> {
                if (getTarget() == null || t >= FIRE_START + FIRE_DURATION) setAction(RETRACT_FIRE);
                else if (t >= FIRE_START) flame(server);
            }
            case BROKEN -> {
                if (t >= 20 + BROKEN_DURATION) setAction(RECOVER);
            }
            case RECOVER -> {
                if (t >= 20) {
                    finishAttack();
                }
            }
            case RETRACT_TRAP, RETRACT_FIRE -> {
                if (t >= 31) finishAttack();
            }
            default -> {
            }
        }
    }

    private void finishAttack() {
        setAction(CHASE);
        cooldown = 35 + random.nextInt(30);
    }

    private Vec3 forward() {
        return Vec3.directionFromRotation(0, getYRot());
    }

    public Vec3 devicePosition(boolean right) {
        Vec3 f = forward(), side = new Vec3(f.z, 0, -f.x).scale(right ? -0.85 : 0.85);
        return position().add(f.scale(2.9)).add(side).add(0, 1.6, 0);
    }

    private boolean isFacingTarget(float tolerance) {
        if (getTarget() == null) return false;
        Vec3 d = getTarget().position().subtract(position());
        if (d.horizontalDistanceSqr() < 0.04) return true;
        float wanted = (float) (Mth.atan2(d.z, d.x) * 180 / Math.PI) - 90;
        return Math.abs(Mth.wrapDegrees(wanted - getYRot())) <= tolerance;
    }

    private void faceTarget(float maxTurn) {
        if (getTarget() == null) return;
        Vec3 d = getTarget().position().subtract(position());
        if (d.horizontalDistanceSqr() < 0.04) return;
        float wanted = (float) (Mth.atan2(d.z, d.x) * 180 / Math.PI) - 90;
        setYRot(Mth.wrapDegrees(Mth.approachDegrees(getYRot(), wanted, maxTurn)));
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    private void tickTrap(ServerLevel server, int t) {
        if (t == TRAP_PULL_START) playSound(TFSounds.GHAST_TRAP_ON.get(), 3, 1);
        if (t >= TRAP_PULL_START && t < TRAP_PULL_END) {
            Vec3 origin = devicePosition(true), direction = forward();
            twilightforest.network.ParticlePacket particles = new twilightforest.network.ParticlePacket();
            // The native particle takes a destination and its full displacement, not a velocity.
            // TF's packet bypasses vanilla's Gaussian scatter and preserves those parameters.
            for (int i = 0; i < 4; i++) {
                double angle = (t * 0.3 + i * Math.PI / 2);
                Vec3 offset = direction.scale(2.5).add(Math.cos(angle) * 1.2, Math.sin(angle) * 1.2, 0);
                particles.queueParticle(TFParticleType.GHAST_TRAP.get(), true, origin, offset);
            }
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(20, 10, 20), this::canAttackTarget)) {
                Vec3 delta = victim.getBoundingBox().getCenter().subtract(origin);
                double distance = delta.length();
                if (distance > 20 || distance > 2 && delta.normalize().dot(direction) < 0.55 || !getSensing().hasLineOfSight(victim))
                    continue;
                Vec3 pull = origin.subtract(victim.getBoundingBox().getCenter());
                if (distance < 3.4) entityData.set(CAPTURED, true);
                Vec3 velocity = pull.normalize().scale(Math.min(0.85, distance * 0.15));
                victim.setDeltaMovement(victim.getDeltaMovement().scale(0.35).add(velocity));
                victim.hurtMarked = true;
                if (t % 2 == 0) for (int i = 0; i < 5; i++)
                    particles.queueParticle(TFParticleType.GHAST_TRAP.get(), true, origin, delta.add((random.nextDouble() - 0.5) * 0.8, (random.nextDouble() - 0.5) * 0.8, (random.nextDouble() - 0.5) * 0.8));
            }
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(this, particles);
        }
        if (t == TRAP_PULL_END && !hasCapturedTarget()) {
            setAction(RETRACT_TRAP);
            return;
        }
        if (t == TRAP_HIT) slam(server, devicePosition(true), 5, 16);
        if (t >= TRAP_HIT + 18) setAction(RETRACT_TRAP);
    }

    private void flame(ServerLevel server) {
        Vec3 origin = devicePosition(false), direction = getTarget().getEyePosition().subtract(origin).normalize();
        // Limit turning by body orientation, so targets can evade the stream.
        Vec3 horizontal = forward();
        direction = new Vec3(horizontal.x, Mth.clamp(direction.y, -0.3, 0.3), horizontal.z).normalize();
        Vec3 end = server.clip(new ClipContext(origin, origin.add(direction.scale(20)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getLocation();
        Vec3 ray = end.subtract(origin);
        for (int i = 0; i < 10; i++) {
            Vec3 p = origin.add(ray.scale(i / 10D));
            server.sendParticles(TFParticleType.LARGE_FLAME.get(), p.x, p.y, p.z, 2, 0.15 + i * 0.035, 0.15, 0.15 + i * 0.035, 0.015);
        }
        if (tickCount % 10 == 0) playSound(SoundEvents.BLAZE_SHOOT, 2, 0.55F);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(origin, end).inflate(1.2), this::canDamageWithSkill)) {
            if (victim.getBoundingBox().inflate(1).clip(origin, end).isEmpty() && !victim.getBoundingBox().inflate(1).contains(origin))
                continue;
            if (victim.hurt(new DamageSource(server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.MONSTROSITY_FLAME), this), 19))
                victim.igniteForSeconds(3);
        }
    }

    public void interruptFlame() {
        if (!(level() instanceof ServerLevel server) || getAction() != FIRE || getActionTime() < FIRE_START) return;
        setAction(BROKEN);
        playSound(SoundEvents.IRON_GOLEM_DAMAGE, 3, 0.5F);
        Vec3 at = devicePosition(false);
        server.sendParticles(ParticleTypes.CLOUD, at.x, at.y, at.z, 35, 0.6, 0.6, 0.6, 0.1);
    }

    private void slam(ServerLevel server, Vec3 at, double radius, float damage) {
        playSound(TFSounds.MINOSHROOM_SLAM.get(), 3, 0.75F);
        // Sample the real floor, like Minoshroom. These are debris particles, not block destruction.
        for (int i = 0; i < 80; i++) {
            double angle = random.nextDouble() * Math.PI * 2, distance = Math.sqrt(random.nextDouble()) * radius;
            BlockPos ground = groundAt(server, at.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance));
            var state = server.getBlockState(ground);
            if (!state.isAir())
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), ground.getX() + random.nextDouble(), ground.getY() + 1.05, ground.getZ() + random.nextDouble(), 2, 0.1, 0.08, 0.1, 0.15);
        }
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius, 3, radius), this::canDamageWithSkill)) {
            if (victim.position().subtract(at).horizontalDistanceSqr() > radius * radius) continue;
            if (victim.hurt(damageSources().mobAttack(this), damage)) {
                Vec3 d = victim.position().subtract(at).multiply(1, 0, 1).normalize();
                victim.push(d.x * 0.9, 0.55, d.z * 0.9);
                victim.hurtMarked = true;
            }
        }
    }

    private BlockPos groundAt(ServerLevel server, Vec3 at) {
        var hit = server.clip(new ClipContext(at.add(0, 2, 0), at.add(0, -6, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : BlockPos.containing(at).below();
    }

    private Vec3 summoningPosition(int index) {
        double angle = getYRot() * Math.PI / 180 + index * Math.PI * 2 / 3;
        return position().add(Math.cos(angle) * 5, 0, Math.sin(angle) * 5);
    }

    private void drawSummoningCircles(ServerLevel server, int time) {
        for (int i = 0; i < 3; i++) {
            Vec3 at = summoningPosition(i);
            double y = groundAt(server, at).getY() + 1.08;
            for (int j = 0; j < 16; j++) {
                double a = j * Math.PI / 8;
                server.sendParticles(ParticleTypes.WITCH, at.x + Math.cos(a) * 1.35, y, at.z + Math.sin(a) * 1.35, 1, 0, 0, 0, 0);
            }
            for (int j = 0; j < 5; j++) {
                double a = j * Math.PI * 2 / 5, b = (j + 2) * Math.PI * 2 / 5, f = (time % 15) / 15D;
                server.sendParticles(ParticleTypes.ENCHANT, at.x + Mth.lerp(f, Math.cos(a), Math.cos(b)) * 1.1, y + 0.03, at.z + Mth.lerp(f, Math.sin(a), Math.sin(b)) * 1.1, 1, 0, 0.02, 0, 0);
            }
        }
    }

    private void breakObstacles(ServerLevel server) {
        Vec3 ahead = forward().scale(0.9);
        AABB box = getBoundingBox().move(ahead).deflate(0.15);
        int destroyed = 0;
        for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, getY() + 0.15, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            var state = server.getBlockState(p);
            if (!server.hasChunkAt(p) || state.isAir() || state.getCollisionShape(server, p).isEmpty() || state.getDestroySpeed(server, p) < 0 || state.getBlock().getExplosionResistance() >= 1200)
                continue;
            BlockEntity be = server.getBlockEntity(p);
            if (be != null || !state.canEntityDestroy(server, p, this) || !net.neoforged.neoforge.event.EventHooks.onEntityDestroyBlock(this, p, state))
                continue;
            server.destroyBlock(p, false, this);
            if (++destroyed >= 64) break;
        }
    }

    private void summonAdherents(ServerLevel server) {
        summons.removeIf(id -> server.getEntity(id) instanceof LivingEntity e && !e.isAlive());
        for (int i = 0; i < 3 && summons.size() < 9; i++) {
            BlockPos p = groundAt(server, summoningPosition(i)).above();
            Adherent minion = TFEntities.ADHERENT.get().create(server);
            if (minion == null) continue;
            minion.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, random.nextFloat() * 360, 0);
            if (!server.noCollision(minion)) continue;
            minion.getPersistentData().putUUID(SUMMONER_TAG, getUUID());
            minion.setPersistenceRequired();
            minion.setTarget(getTarget());
            if (minion instanceof ColoredAdherent colored) colored.finale$setColor(random.nextInt(4));
            if (server.addFreshEntity(minion)) summons.add(minion.getUUID());
        }
    }

    public void summonedAdherentDied(Adherent minion, DamageSource source) {
        if (!(level() instanceof ServerLevel server) || !isAlive() || !summons.remove(minion.getUUID())) return;
        if (source.getEntity() instanceof CastleMonstrosityEntity || source.getDirectEntity() instanceof CastleMonstrosityEntity)
            return;
        smokeTrail(server, minion.getBoundingBox().getCenter(), devicePosition(true), TFParticleType.HUGE_SMOKE.get());
        entityData.set(CHARGE, Math.min(3, getTrapCharge() + 1));
        playChargeSound();
        chargeSoundClock = 0;
        if (getTrapCharge() >= 3 && getAction() == CHASE && canAttackTarget(getTarget())) setAction(TRAP);
    }

    private void playChargeSound() {
        int charge = getTrapCharge();
        // Match the three native inactive-trap notification levels; WARMUP is the activation loop.
        if (charge > 0)
            playSound(TFSounds.GHAST_TRAP_AMBIENT.get(), charge == 1 ? 1 : charge == 2 ? 1.2F : 1.5F, charge == 1 ? 1 : charge == 2 ? 0.8F : 2);
    }

    public static void smokeTrail(ServerLevel server, Vec3 from, Vec3 to, net.minecraft.core.particles.SimpleParticleType particle) {
        int steps = Math.min(32, Math.max(4, (int) from.distanceTo(to) * 2));
        for (int i = 0; i <= steps; i++) {
            Vec3 at = from.lerp(to, i / (double) steps), velocity = to.subtract(at).scale(0.08);
            server.sendParticles(particle, at.x, at.y, at.z, 0, velocity.x, velocity.y, velocity.z, 1);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) &&
                (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC) || source.getDirectEntity() instanceof Projectile || source.getEntity() instanceof LivingEntity && source.getDirectEntity() != source.getEntity()))
            return false;
        if (source.getEntity() != null && isAlliedTo(source.getEntity())) return false;
        boolean hit = super.hurt(source, amount);
        if (hit && !level().isClientSide() && source.getDirectEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (!weapon.is(TFItems.MAZEBREAKER_PICKAXE.get()) && !weapon.is(MAZEBREAKER_LIKE))
                weapon.hurtAndBreak(15, attacker, EquipmentSlot.MAINHAND);
        }
        return hit;
    }

    @Override
    public void die(DamageSource source) {
        if (dead) return;
        setAction(DYING);
        super.die(source);
        bossBar.setVisible(false);
        if (level() instanceof ServerLevel server)
            for (UUID id : summons) {
                Entity minion = server.getEntity(id);
                if (minion != null) minion.discard();
            }
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        if (!(level() instanceof ServerLevel server)) return;
        int t = getActionTime();
        if (t % 7 == 0) {
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 2, getZ(), 5, 2.5, 2, 2.5, 0);
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 2, 0.6F + random.nextFloat() * 0.4F);
        }
        server.sendParticles(TFParticleType.HUGE_SMOKE.get(), getX(), getY() + 2, getZ(), 4 + t / 6, 2.5, 2, 2.5, 0.03);
        server.sendParticles(TFParticleType.ANNIHILATE.get(), getX(), getY() + 2, getZ(), 5 + t / 10, 2, 2, 2, 0.04);
        if (t < DEATH_DURATION) return;
        // The continuation lives in this entity's NBT, not an invisible one-HP boss.
        if (hierophant != null && !resumed) {
            captureLegacyOwner(server);
            if (!TheTwilightHierophantEntity.restoreAfterMonstrosity(server, hierophant, getUUID(), hierophantData, position()))
                return;
            resumed = true;
        }
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 2, getZ(), 8, 3, 2, 3, 0);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 6, 0.5F);
        remove(RemovalReason.KILLED);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer p) {
        super.startSeenByPlayer(p);
        bossBar.addPlayer(p);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        bossBar.removePlayer(p);
    }

    @Override
    public void remove(RemovalReason reason) {
        bossBar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (hierophant != null) tag.putUUID("Hierophant", hierophant);
        if (hierophantData != null) tag.put("HierophantContinuation", hierophantData.copy());
        tag.putInt("MonstrosityAction", getAction());
        tag.putInt("MonstrosityTime", getActionTime());
        tag.putInt("TrapCharge", getTrapCharge());
        tag.putBoolean("TrapCaptured", hasCapturedTarget());
        tag.putInt("AttackCooldown", cooldown);
        tag.putInt("LastAttack", lastAttack);
        tag.putBoolean("Resumed", resumed);
        ListTag ids = new ListTag();
        for (UUID id : summons) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("UUID", id);
            ids.add(entry);
        }
        tag.put("MonstrositySummons", ids);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Hierophant")) hierophant = tag.getUUID("Hierophant");
        hierophantData = tag.contains("HierophantContinuation", 10) ? tag.getCompound("HierophantContinuation").copy() : null;
        entityData.set(ACTION, Mth.clamp(tag.getInt("MonstrosityAction"), 0, TRIPLE_SMASH));
        entityData.set(ACTION_START, level().getGameTime() - Math.max(0, tag.getInt("MonstrosityTime")));
        entityData.set(CHARGE, Mth.clamp(tag.getInt("TrapCharge"), 0, 3));
        entityData.set(CAPTURED, tag.getBoolean("TrapCaptured"));
        cooldown = tag.getInt("AttackCooldown");
        lastAttack = tag.getInt("LastAttack");
        resumed = tag.getBoolean("Resumed");
        summons.clear();
        ListTag ids = tag.getList("MonstrositySummons", 10);
        for (int i = 0; i < Math.min(9, ids.size()); i++)
            if (ids.getCompound(i).hasUUID("UUID")) summons.add(ids.getCompound(i).getUUID("UUID"));
        if (getHealth() <= 0) {
            dead = true;
            bossBar.setVisible(false);
        }
    }
}
