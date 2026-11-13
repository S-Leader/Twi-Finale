package show.twilight.finale.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import show.twilight.finale.init.ModEntity;
import twilightforest.init.TFParticleType;

public final class EngineerWrench extends ThrowableItemProjectile {
    public EngineerWrench(EntityType<? extends EngineerWrench> type, Level level) {
        super(type, level);
    }

    public EngineerWrench(Level level, LivingEntity owner) {
        super(ModEntity.ENGINEER_WRENCH.get(), owner, level);
    }

    @Override protected Item getDefaultItem() { return Items.GOLDEN_HOE; }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && (getOwner() == null || !getOwner().isAlliedTo(entity));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!level().isClientSide()) hit.getEntity().hurt(damageSources().thrown(this, getOwner()), 6);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(TFParticleType.ANNIHILATE.get(), getX(), getY(), getZ(), 8, 0.15, 0.15, 0.15, 0.04);
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server && tickCount % 2 == 0)
            server.sendParticles(TFParticleType.ANNIHILATE.get(), getX(), getY(), getZ(), 1, 0.03, 0.03, 0.03, 0);
        if (tickCount > 100) discard();
    }
}
