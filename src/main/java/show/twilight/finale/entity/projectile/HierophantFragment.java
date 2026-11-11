package show.twilight.finale.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModEntity;
import twilightforest.init.TFParticleType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Colliding cube projectile. Returning fragments retrace their original ray,
 * not a homing path to the caster's new position.
 */
public final class HierophantFragment extends Projectile {
    private static final ResourceKey<DamageType> SHIELD_MISSILE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "hierophant_shield_missile"));
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(HierophantFragment.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> EXPLOSIVE = SynchedEntityData.defineId(HierophantFragment.class, EntityDataSerializers.BOOLEAN);
    private Vec3 origin = Vec3.ZERO, direction = Vec3.ZERO;
    private int age, stage, stageTicks;
    private boolean bypassesCooldown;
    private final Set<UUID> hitThisLeg = new HashSet<>();

    public HierophantFragment(EntityType<? extends HierophantFragment> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public HierophantFragment(ServerLevel level, TheTwilightHierophantEntity owner, Vec3 direction, boolean returns) {
        this(ModEntity.HIEROPHANT_FRAGMENT.get(), level);
        setOwner(owner);
        this.direction = direction.normalize();
        entityData.set(RETURNING, returns);
        origin = owner.getBeamOrigin();
        setPos(origin.add(this.direction.scale(owner.getBbWidth() / 2 + 0.3)));
        setDeltaMovement(this.direction.scale(returns ? 0.36 : 0.75));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(RETURNING, false);
        b.define(EXPLOSIVE, false);
    }

    public boolean returns() {
        return entityData.get(RETURNING);
    }

    public void setExplosive() {
        entityData.set(EXPLOSIVE, true);
    }

    public void setBypassesCooldown() {
        bypassesCooldown = true;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        stageTicks++;
        if (level().isClientSide()) {
            setPos(position().add(getDeltaMovement()));
            if (age % 2 == 0) level().addParticle(TFParticleType.ANNIHILATE.get(), getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (!(getOwner() instanceof TheTwilightHierophantEntity boss) || !boss.isAlive() || !boss.insideArena(position())
                || returns() && !boss.hasShield() || age > (returns() ? 100 : 40)) {
            discard();
            return;
        }
        if (returns()) {
            if (stage == 0 && stageTicks >= 22) pause();
            if (stage == 1) {
                if (stageTicks >= 16) {
                    stage = 2;
                    stageTicks = 0;
                    hitThisLeg.clear();
                    setDeltaMovement(direction.scale(-0.36));
                    hasImpulse = true;
                } else return;
            }
            if (stage == 2 && position().distanceToSqr(origin) < 0.5) {
                discard();
                return;
            }
        }
        // Swept block and entity traces, so high-speed fragments cannot tunnel.
        Vec3 start = position(), end = start.add(getDeltaMovement());
        if (!boss.insideArena(end) || !boss.isAreaLoaded(new AABB(start, end).inflate(0.3))) {
            discard();
            return;
        }
        BlockHitResult wall = level().clip(new net.minecraft.world.level.ClipContext(start, end, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this));
        Vec3 limited = wall.getType() == HitResult.Type.MISS ? end : wall.getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level(), this, start, limited, getBoundingBox().expandTowards(getDeltaMovement()).inflate(0.2), this::canHitEntity);
        if (hit != null && hit.getEntity() instanceof LivingEntity victim) {
            hitThisLeg.add(victim.getUUID());
            if (!entityData.get(EXPLOSIVE)) {
                // Use vanilla's per-source cooldown tag, without resetting the
                // victim's invulnerability timer for unrelated incoming attacks.
                DamageSource source = bypassesCooldown
                        ? new DamageSource(registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SHIELD_MISSILE_DAMAGE), this, boss)
                        : damageSources().indirectMagic(this, boss);
                victim.hurt(source, 3.0F);
            }
            if (!returns()) {
                burst(hit.getLocation());
                discard();
                return;
            }
        }
        if (wall.getType() != HitResult.Type.MISS) {
            setPos(wall.getLocation().subtract(getDeltaMovement().normalize().scale(0.02)));
            if (returns() && stage == 0) {
                pause();
                return;
            }
            burst(wall.getLocation());
            discard();
            return;
        }
        setPos(end);
        hasImpulse = true;
    }

    private void pause() {
        stage = 1;
        stageTicks = 0;
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;
    }

    private void burst(Vec3 at) {
        if (entityData.get(EXPLOSIVE)) {
            level().explode(this, damageSources().explosion(this, getOwner()), null, at.x, at.y, at.z, 2, false,
                    Level.ExplosionInteraction.NONE, TFParticleType.ANNIHILATE.get(), TFParticleType.ANNIHILATE.get(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE);
            ((ServerLevel) level()).sendParticles(TFParticleType.ANNIHILATE.get(), at.x, at.y, at.z, 48, 0.7, 0.7, 0.7, 0.12);
            return;
        }
        ((ServerLevel) level()).sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 6, 0.12, 0.12, 0.12, 0.01);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof twilightforest.entity.monster.Adherent)
                && !hitThisLeg.contains(entity.getUUID()) && super.canHitEntity(entity);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Returns", returns());
        tag.putBoolean("Explosive", entityData.get(EXPLOSIVE));
        tag.putBoolean("BypassesCooldown", bypassesCooldown);
        tag.putInt("Age", age);
        tag.putInt("Stage", stage);
        tag.putInt("StageTicks", stageTicks);
        tag.put("Origin", newDoubleList(origin.x, origin.y, origin.z));
        tag.put("Direction", newDoubleList(direction.x, direction.y, direction.z));
        var hits = new net.minecraft.nbt.ListTag();
        for (UUID id : hitThisLeg) hits.add(net.minecraft.nbt.NbtUtils.createUUID(id));
        tag.put("HitThisLeg", hits);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(RETURNING, tag.getBoolean("Returns"));
        entityData.set(EXPLOSIVE, tag.getBoolean("Explosive"));
        bypassesCooldown = tag.getBoolean("BypassesCooldown");
        age = tag.getInt("Age");
        stage = tag.getInt("Stage");
        stageTicks = tag.getInt("StageTicks");
        var o = tag.getList("Origin", net.minecraft.nbt.Tag.TAG_DOUBLE);
        if (o.size() == 3) origin = new Vec3(o.getDouble(0), o.getDouble(1), o.getDouble(2));
        var d = tag.getList("Direction", net.minecraft.nbt.Tag.TAG_DOUBLE);
        if (d.size() == 3) direction = new Vec3(d.getDouble(0), d.getDouble(1), d.getDouble(2));
        for (var raw : tag.getList("HitThisLeg", net.minecraft.nbt.Tag.TAG_INT_ARRAY))
            hitThisLeg.add(net.minecraft.nbt.NbtUtils.loadUUID(raw));
    }
}
