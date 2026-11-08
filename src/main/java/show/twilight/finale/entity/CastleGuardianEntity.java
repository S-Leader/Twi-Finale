package show.twilight.finale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import show.twilight.finale.entity.combat.CombatRay;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Surface-mounted guardian. Damage is a single server ray, never a visual-only projectile.
 */
public class CastleGuardianEntity extends Monster {
    private static final EntityDataAccessor<Direction> ATTACH_FACE = SynchedEntityData.defineId(CastleGuardianEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Boolean> ATTACHED = SynchedEntityData.defineId(CastleGuardianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LASER_PHASE = SynchedEntityData.defineId(CastleGuardianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHARGE = SynchedEntityData.defineId(CastleGuardianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> BEAM = SynchedEntityData.defineId(CastleGuardianEntity.class, EntityDataSerializers.VECTOR3);
    private BlockPos anchor;
    private int searchDelay, cooldown = 40, attackTicks;
    private Vec3 lockedEnd;
    private final List<UUID> legacyMinions = new ArrayList<>();

    public CastleGuardianEntity(EntityType<? extends CastleGuardianEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100).add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.ATTACK_DAMAGE, 18).add(Attributes.FOLLOW_RANGE, 48);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACH_FACE, Direction.DOWN);
        builder.define(ATTACHED, false);
        builder.define(LASER_PHASE, 0);
        builder.define(CHARGE, 0);
        builder.define(BEAM, new Vector3f());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 48));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public Direction getAttachFace() {
        return entityData.get(ATTACH_FACE);
    }

    public boolean isAttached() {
        return entityData.get(ATTACHED);
    }

    public int getLaserPhase() {
        return entityData.get(LASER_PHASE);
    }

    public float getChargeProgress() {
        return Math.min(1, entityData.get(CHARGE) / 60F);
    }

    public Vec3 getBeamOrigin() {
        return position().add(0, 1, 0).add(Vec3.atLowerCornerOf(getAttachFace().getOpposite().getNormal()).scale(0.7));
    }

    public Vec3 getBeamEnd() {
        Vector3f offset = entityData.get(BEAM);
        return getBeamOrigin().add(offset.x, offset.y, offset.z);
    }

    private void aimAt(Vec3 end) {
        Vec3 offset = end.subtract(getBeamOrigin());
        entityData.set(BEAM, new Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (tickCount % 20 == 0) clearMinions();
        if (anchor != null && !isSupport(anchor, getAttachFace())) {
            anchor = null;
            entityData.set(ATTACHED, false);
            setNoGravity(false);
            resetAttack();
        }
        if (anchor == null && --searchDelay <= 0) {
            searchDelay = 20;
            findAttachment();
        }
        if (anchor != null) {
            Vec3 center = mountCenter(anchor, getAttachFace());
            setPos(center.x, center.y - 1, center.z);
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
            fallDistance = 0;
            getNavigation().stop();
            yBodyRot = 0;
        }
        if (cooldown > 0) cooldown--;
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || target.isSpectator() || distanceToSqr(target) > 48 * 48 || !isAttached()) {
            resetAttack();
            return;
        }
        int phase = getLaserPhase();
        if (phase == 0) {
            if (cooldown > 0 || !getSensing().hasLineOfSight(target)) return;
            attackTicks = 0;
            entityData.set(LASER_PHASE, 1);
            playSound(show.twilight.finale.init.ModSounds.GUARDIAN_TARGETING.get(), 1, 1);
        }
        attackTicks++;
        if (attackTicks <= 60) {
            if (!getSensing().hasLineOfSight(target)) {
                resetAttack();
                cooldown = 20;
                return;
            }
            entityData.set(CHARGE, attackTicks);
            lockedEnd = target.getEyePosition();
            aimAt(CombatRay.trace(this, getBeamOrigin(), lockedEnd, 0).end());
        } else if (attackTicks <= 70) {
            entityData.set(LASER_PHASE, 2);
            aimAt(CombatRay.trace(this, getBeamOrigin(), lockedEnd, 0).end());
        } else if (attackTicks == 71) {
            var hit = CombatRay.trace(this, getBeamOrigin(), lockedEnd, 0.25);
            aimAt(hit.end());
            entityData.set(LASER_PHASE, 3);
            if (hit.victim() != null)
                hit.victim().hurt(damageSources().indirectMagic(this, this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
            playSound(SoundEvents.BEACON_POWER_SELECT, 1.5F, 1.8F);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, hit.end().x, hit.end().y, hit.end().z, 16, 0.2, 0.2, 0.2, 0.1);
        } else if (attackTicks >= 78) {
            resetAttack();
            cooldown = 70;
        }
    }

    private void resetAttack() {
        attackTicks = 0;
        entityData.set(LASER_PHASE, 0);
        entityData.set(CHARGE, 0);
    }

    private boolean isSupport(BlockPos support, Direction face) {
        return level().hasChunkAt(support) && level().getBlockState(support).isFaceSturdy(level(), support, face.getOpposite());
    }

    private static Vec3 mountCenter(BlockPos support, Direction face) {
        return Vec3.atCenterOf(support).add(Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(1.5));
    }

    private void findAttachment() {
        Vec3 best = null;
        BlockPos bestAnchor = null;
        Direction bestFace = Direction.DOWN;
        double distance = Double.MAX_VALUE;
        for (BlockPos support : BlockPos.betweenClosed(blockPosition().offset(-3, -3, -3), blockPosition().offset(3, 3, 3))) {
            for (Direction face : Direction.values()) {
                if (!isSupport(support, face)) continue;
                Vec3 center = mountCenter(support, face);
                double candidate = center.add(0, -1, 0).distanceToSqr(position());
                if (candidate >= distance || !level().noCollision(this, new AABB(center.x - 0.99, center.y - 0.99, center.z - 0.99, center.x + 0.99, center.y + 0.99, center.z + 0.99)))
                    continue;
                distance = candidate;
                best = center;
                bestAnchor = support.immutable();
                bestFace = face;
            }
        }
        if (best != null) {
            anchor = bestAnchor;
            entityData.set(ATTACH_FACE, bestFace);
            entityData.set(ATTACHED, true);
        }
    }

    // Saved pre-port.6 tentacles are removed when they load, without deleting the entity type from saves.
    public boolean usesLaser() {
        return true;
    }

    public boolean hasMinions() {
        return false;
    }

    public void clearMinions() {
        if (level() instanceof ServerLevel server) legacyMinions.removeIf(id -> {
            Entity entity = server.getEntity(id);
            if (entity == null) return false;
            if (entity instanceof AnnihilationHandEntity) entity.discard();
            return true;
        });
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (anchor != null) {
            tag.putLong("FinaleAnchor", anchor.asLong());
            tag.putInt("FinaleAttachFace", getAttachFace().get3DDataValue());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("FinaleAnchor")) {
            anchor = BlockPos.of(tag.getLong("FinaleAnchor"));
            entityData.set(ATTACH_FACE, Direction.from3DDataValue(tag.getInt("FinaleAttachFace")));
            entityData.set(ATTACHED, true);
        }
        CompoundTag old = tag.getCompound("MinionsData");
        for (int i = 0; i < Math.min(64, old.getInt("Count")); i++)
            if (old.hasUUID("Minion_" + i)) legacyMinions.add(old.getUUID("Minion_" + i));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}

