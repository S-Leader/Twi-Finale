package show.twilight.finale.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModEntity;
import twilightforest.entity.boss.HydraMortar;

/**
 * Native Hydra mortar model/texture, but immediate impact and no arena-floor destruction.
 */
public final class HierophantMeteor extends HydraMortar {
    public HierophantMeteor(EntityType<? extends HierophantMeteor> type, Level level) {
        super(type, level);
    }

    public HierophantMeteor(ServerLevel level, TheTwilightHierophantEntity owner, Vec3 position) {
        this(ModEntity.HIEROPHANT_METEOR.get(), level);
        setOwner(owner);
        setPos(position);
        setDeltaMovement(0, -0.22, 0);
        fuse = 80;
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && (!(getOwner() instanceof TheTwilightHierophantEntity boss) || !boss.isAlive() || tickCount > 160)) {
            discard();
            return;
        }
        super.tick();
        if (level().isClientSide()) level().addParticle(ParticleTypes.FLAME, getX(), getY() + 0.4, getZ(), 0, 0.04, 0);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && (getOwner() == null || !getOwner().isAlliedTo(entity));
    }

    @Override
    protected void onHit(HitResult hit) {
        if (level().isClientSide() || isRemoved()) return;
        Vec3 at = hit.getLocation();
        level().explode(this, at.x, at.y, at.z, 2.2F, Level.ExplosionInteraction.NONE);
        ((ServerLevel) level()).sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 35, 0.8, 0.5, 0.8, 0.1);
        discard();
    }
}
