package show.twilight.finale.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModEntity;
import twilightforest.entity.monster.Adherent;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFItems;

/** Snowball movement/collision, with a knight weapon's item and impact damage. */
public final class HierophantThrownWeapon extends ThrowableItemProjectile {
    public HierophantThrownWeapon(EntityType<? extends HierophantThrownWeapon> type, Level level) {
        super(type, level);
    }

    public HierophantThrownWeapon(Level level, TheTwilightHierophantEntity owner, ItemStack item) {
        super(ModEntity.HIEROPHANT_THROWN_WEAPON.get(), owner, level);
        setItem(item);
    }

    @Override protected Item getDefaultItem() { return TFItems.KNIGHTMETAL_AXE.get(); }

    @Override
    public void tick() {
        // Inherited ThrowableProjectile: gravity 0.03, air drag 0.99, water drag 0.8.
        super.tick();
        if (!level().isClientSide() && tickCount > 200) discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof Adherent) && !(entity instanceof TheTwilightHierophantEntity)
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
}
