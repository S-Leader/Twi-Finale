package show.twilight.finale.item;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;

public class TripleCrossbowItem extends CrossbowItem {
    public TripleCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        boolean wasCharged = isCharged(stack);
        super.releaseUsing(stack, level, entity, remaining);
        if (!wasCharged && isCharged(stack)) {
            List<ItemStack> loaded = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).getItems();
            List<ItemStack> expanded = new ArrayList<>();

            for (ItemStack projectile : loaded) {
                for (int i = 0; i < 3; i++) {
                    ItemStack copy = projectile.copy();
                    if (i > 0) copy.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
                    expanded.add(copy);
                }
            }
            stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(expanded));
        }
    }

    @Override
    protected void shoot(ServerLevel level, LivingEntity entity, InteractionHand hand,
                         ItemStack weapon, List<ItemStack> ammo, float speed, float spread, boolean critical, @Nullable LivingEntity target) {
        for (int i = 0; i < ammo.size(); i++) {
            Projectile projectile = createProjectile(level, entity, weapon, ammo.get(i), critical);
            if (projectile instanceof AbstractArrow arrow && i > 0) arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            int column = i % 3;
            float yaw = column == 0 ? 0 : column == 1 ? -10 : 10;
            float pitch = ammo.size() > 3 ? (i / 3 - 1) * 10 : 0;
            shootProjectile(entity, projectile, i, speed, spread, yaw, target);
            if (pitch != 0) {
                var motion = projectile.getDeltaMovement().xRot(pitch * Mth.DEG_TO_RAD);
                projectile.setDeltaMovement(motion);
            }
            level.addFreshEntity(projectile);
            weapon.hurtAndBreak(getDurabilityUse(ammo.get(i)), entity, LivingEntity.getSlotForHand(hand));
            if (weapon.isEmpty()) break;
        }
    }
}

