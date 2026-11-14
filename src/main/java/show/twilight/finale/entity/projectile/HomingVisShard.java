package show.twilight.finale.entity.projectile;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.init.ModEntity;
import twilightforest.entity.monster.Adherent;
import twilightforest.init.TFMobEffects;

import java.util.UUID;

public final class HomingVisShard extends Projectile implements ItemSupplier {
    private UUID targetId;
    private int slot, age;

    public HomingVisShard(EntityType<? extends HomingVisShard> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public HomingVisShard(Level level, LivingEntity caster, LivingEntity target, int slot) {
        this(ModEntity.HOMING_VIS_SHARD.get(), level);
        setOwner(caster);
        targetId = target.getUUID();
        this.slot = slot;
        setPos(hover(caster));
    }

    private Vec3 hover(Entity caster) {
        double angle = slot * Math.PI * 2 / 5;
        return caster.position().add(Math.cos(angle) * 0.9, caster.getBbHeight() + 0.85, Math.sin(angle) * 0.9);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (level().isClientSide()) {
            level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFBF77FF), getX(), getY(), getZ(), 0, 0, 0);
            setPos(position().add(getDeltaMovement()));
            return;
        }
        if (age > 160) {
            discard();
            return;
        }
        if (age < 20 + slot * 4 && getOwner() != null && getOwner().isAlive()) {
            setPos(hover(getOwner()));
            return;
        }
        Entity target = targetId == null ? null : ((ServerLevel) level()).getEntity(targetId);
        if (target instanceof LivingEntity living && living.isAlive() && !living.isSpectator()) {
            Vec3 desired = living.getEyePosition().subtract(position()).normalize().scale(0.85);
            Vec3 current = getDeltaMovement();
            setDeltaMovement(current.lengthSqr() < 0.01 ? desired : current.scale(0.84).add(desired.scale(0.16)).normalize().scale(0.85));
        } else if (getDeltaMovement().lengthSqr() < 0.01) {
            discard();
            return;
        }
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            return;
        }
        setPos(position().add(getDeltaMovement()));
        ProjectileUtil.rotateTowardsMovement(this, 0.3F);
        hasImpulse = true;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof Adherent) && super.canHitEntity(entity)
                && (getOwner() == null || !getOwner().isAlliedTo(entity));
    }

    @Override
    protected void onHit(HitResult hit) {
        if (level().isClientSide()) return;
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity victim) {
            if (victim.hurt(damageSources().indirectMagic(this, getOwner()), 6)) {
                switch (random.nextInt(5)) {
                    case 0 -> victim.igniteForSeconds(5);
                    case 1 -> victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    case 2 -> victim.addEffect(new MobEffectInstance(TFMobEffects.FROSTY, 100, 0));
                    case 3 -> victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
                    default ->
                            level().explode(getOwner(), hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 1.5F, Level.ExplosionInteraction.NONE);
                }
            }
        }
        ((ServerLevel) level()).sendParticles(ParticleTypes.WITCH, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 10, 0.2, 0.2, 0.2, 0.02);
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (targetId != null) tag.putUUID("Target", targetId);
        tag.putInt("Slot", slot);
        tag.putInt("Age", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Target")) targetId = tag.getUUID("Target");
        slot = Math.clamp(tag.getInt("Slot"), 0, 4);
        age = tag.getInt("Age");
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.AMETHYST_SHARD);
    }
}
