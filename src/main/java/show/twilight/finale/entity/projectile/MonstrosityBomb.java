package show.twilight.finale.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFParticleType;

import show.twilight.finale.entity.CastleMonstrosityEntity;
import show.twilight.finale.init.ModEntity;

public class MonstrosityBomb extends ThrowableProjectile {
    public MonstrosityBomb(EntityType<? extends MonstrosityBomb> type, Level level) {
        super(type, level);
    }

    public MonstrosityBomb(ServerLevel level, CastleMonstrosityEntity owner, Vec3 start, Vec3 direction) {
        this(ModEntity.MONSTROSITY_BOMB.get(), level);
        setOwner(owner);
        setPos(start);
        shoot(direction.x, direction.y, direction.z, 0.95F, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.008;
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && (getOwner() instanceof CastleMonstrosityEntity beast && e instanceof LivingEntity living
                ? beast.canDamageWithSkill(living) : getOwner() == null || !getOwner().isAlliedTo(e));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide())
            level().addParticle(TFParticleType.ANNIHILATE.get(), getX(), getY(), getZ(), 0, 0, 0);
        else if (tickCount > 100 || getOwner() != null && !getOwner().isAlive()) discard();
    }

    @Override
    protected void onHit(HitResult result) {
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        Vec3 at = result.getLocation();
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, 0, 0, 0, 0);
        server.sendParticles(TFParticleType.ANNIHILATE.get(), at.x, at.y, at.z, 70, 1.6, 1.6, 1.6, 0.12);
        server.sendParticles(TFParticleType.HUGE_SMOKE.get(), at.x, at.y, at.z, 25, 1.6, 1.6, 1.6, 0.04);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 3, 0.65F);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(4))) {
            if (victim.distanceToSqr(at) > 16) continue;
            if (getOwner() instanceof CastleMonstrosityEntity beast ? !beast.canDamageWithSkill(victim) : getOwner() != null && getOwner().isAlliedTo(victim))
                continue;
            victim.hurt(damageSources().explosion(this, getOwner()), 16);
        }
        discard();
    }
}
