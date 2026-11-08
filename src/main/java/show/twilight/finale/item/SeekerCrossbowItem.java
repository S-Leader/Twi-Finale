package show.twilight.finale.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SeekerCrossbowItem extends CrossbowItem {
    public SeekerCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity entity, ItemStack weapon, ItemStack ammo, boolean critical) {
        if (ammo.is(net.minecraft.world.item.Items.FIREWORK_ROCKET))
            return new show.twilight.finale.entity.SeekerFireworkRocketEntity(level, ammo, entity, entity.getX(), entity.getEyeY() - 0.15, entity.getZ(), true);
        return super.createProjectile(level, entity, weapon, ammo, critical);
    }

    @Override
    public net.minecraft.world.entity.projectile.AbstractArrow customArrow(
            net.minecraft.world.entity.projectile.AbstractArrow arrow, ItemStack ammo, ItemStack weapon) {
        return new twilightforest.entity.projectile.SeekerArrow(arrow, ammo, weapon);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack material) {
        return material.getItem() instanceof net.minecraft.world.item.BlockItem block
                && block.getBlock().defaultBlockState().is(net.minecraft.tags.BlockTags.ICE)
                || super.isValidRepairItem(toRepair, material);
    }
}

