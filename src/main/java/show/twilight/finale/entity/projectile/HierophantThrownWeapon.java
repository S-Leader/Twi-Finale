package show.twilight.finale.entity.projectile;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModEntity;
import twilightforest.entity.monster.Adherent;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFItems;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Snowball movement/collision, with a knight weapon's item and impact damage.
 */
public final class HierophantThrownWeapon extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> SLAM_AXE = SynchedEntityData.defineId(HierophantThrownWeapon.class, EntityDataSerializers.BOOLEAN);
    private final Set<UUID> pierced = new HashSet<>();
    private boolean impacted;

    public HierophantThrownWeapon(EntityType<? extends HierophantThrownWeapon> type, Level level) {
        super(type, level);
    }

    public HierophantThrownWeapon(Level level, TheTwilightHierophantEntity owner, ItemStack item) {
        super(ModEntity.HIEROPHANT_THROWN_WEAPON.get(), owner, level);
        setItem(item);
    }

    @Override
    protected Item getDefaultItem() {
        return TFItems.KNIGHTMETAL_AXE.get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLAM_AXE, false);
    }

    public void setSlamAxe() {
        entityData.set(SLAM_AXE, true);
    }

    public boolean isSlamAxe() {
        return entityData.get(SLAM_AXE);
    }

    @Override
    public void tick() {
        if (isSlamAxe() && level() instanceof ServerLevel server) {
            if (impacted || !(getOwner() instanceof TheTwilightHierophantEntity boss) || !boss.isAlive()
                    || !boss.insideArena(position()) || !boss.isAreaLoaded(getBoundingBox().expandTowards(getDeltaMovement()).inflate(0.3))) {
                discard();
                return;
            }
            Vec3 from = position(), to = from.add(getDeltaMovement());
            BlockHitResult wall = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 end = wall.getType() == HitResult.Type.MISS ? to : wall.getLocation();
            // Sweep every victim up to the first wall. Disabling vanilla's
            // single entity hit below keeps its block sweep intact, even when
            // several mobs stand directly in front of the landing surface.
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().expandTowards(getDeltaMovement()).inflate(0.3),
                    target -> target.isAlive() && !target.isSpectator() && target != boss
                            && !(target instanceof Adherent) && !boss.isAlliedTo(target) && !pierced.contains(target.getUUID()))) {
                AABB hitbox = victim.getBoundingBox().inflate(0.2);
                if (!hitbox.contains(from) && hitbox.clip(from, end).isEmpty()) continue;
                pierced.add(victim.getUUID());
                victim.hurt(TFDamageTypes.getIndirectEntityDamageSource(server, TFDamageTypes.THROWN_AXE, this, boss),
                        TheTwilightHierophantEntity.AXE_SLAM_DAMAGE);
            }
        }
        // Inherited ThrowableProjectile: gravity 0.03, air drag 0.99, water drag 0.8.
        super.tick();
        if (!level().isClientSide() && tickCount > 200) discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !isSlamAxe() && !(entity instanceof Adherent) && !(entity instanceof TheTwilightHierophantEntity)
                && super.canHitEntity(entity) && (getOwner() == null || !getOwner().isAlliedTo(entity));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!level().isClientSide() && hit.getEntity() instanceof LivingEntity victim) {
            boolean pick = getItem().is(TFItems.KNIGHTMETAL_PICKAXE.get());
            victim.hurt(TFDamageTypes.getIndirectEntityDamageSource(level(), pick
                    ? TFDamageTypes.THROWN_PICKAXE : TFDamageTypes.THROWN_AXE, this, getOwner()), pick ? 3 : 6);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        if (isSlamAxe()) {
            if (hit instanceof BlockHitResult block) {
                if (block.getDirection() != Direction.UP) {
                    // A side/ceiling hit drops the axe down that surface. Only
                    // landing on its top triggers the shock; no wall explosion.
                    Vec3 normal = Vec3.atLowerCornerOf(block.getDirection().getNormal());
                    setPos(block.getLocation().add(normal.scale(0.15)));
                    setDeltaMovement(0, Math.min(-0.2, getDeltaMovement().y), 0);
                    hasImpulse = true;
                    return;
                }
                if (!level().isClientSide() && !impacted) {
                    impacted = true;
                    if (level() instanceof ServerLevel server && getOwner() instanceof TheTwilightHierophantEntity boss && boss.isAlive())
                        boss.onThrownAxeImpact(server, block.getLocation());
                }
                discard();
            }
            return;
        }
        super.onHit(hit);
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 3) {
            for (int i = 0; i < 8; i++)
                level().addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 0, 0, 0);
        } else super.handleEntityEvent(event);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HierophantSlamAxe", isSlamAxe());
        tag.putBoolean("SlamImpacted", impacted);
        tag.putInt("WeaponAge", tickCount);
        ListTag hits = new ListTag();
        for (UUID id : pierced) hits.add(NbtUtils.createUUID(id));
        tag.put("Pierced", hits);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SLAM_AXE, tag.getBoolean("HierophantSlamAxe"));
        impacted = tag.getBoolean("SlamImpacted");
        tickCount = Math.clamp(tag.getInt("WeaponAge"), 0, 201);
        pierced.clear();
        for (Tag raw : tag.getList("Pierced", Tag.TAG_INT_ARRAY)) pierced.add(NbtUtils.loadUUID(raw));
    }
}
