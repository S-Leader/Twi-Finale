package show.twilight.finale.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IceCrossbowItem extends CrossbowItem {
    public IceCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity entity, ItemStack weapon, ItemStack ammo, boolean critical) {
        if (ammo.is(net.minecraft.world.item.Items.FIREWORK_ROCKET)) {
            var bomb = new twilightforest.entity.projectile.IceBomb(level, entity.position().add(0, entity.getEyeHeight() - 0.15, 0));
            bomb.setOwner(entity);
            return bomb;
        }
        return super.createProjectile(level, entity, weapon, ammo, critical);
    }

    @Override
    public net.minecraft.world.entity.projectile.AbstractArrow customArrow(
            net.minecraft.world.entity.projectile.AbstractArrow arrow, ItemStack ammo, ItemStack weapon) {
        return new twilightforest.entity.projectile.IceArrow(arrow, ammo, weapon);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack material) {
        return material.getItem() instanceof net.minecraft.world.item.BlockItem block
                && block.getBlock().defaultBlockState().is(net.minecraft.tags.BlockTags.ICE)
                || super.isValidRepairItem(toRepair, material);
    }
}

