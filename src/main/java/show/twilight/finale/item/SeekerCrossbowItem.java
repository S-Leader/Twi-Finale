package show.twilight.finale.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import twilightforest.entity.projectile.SeekerArrow;

import show.twilight.finale.entity.SeekerFireworkRocketEntity;

public class SeekerCrossbowItem extends CrossbowItem {
    public SeekerCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity entity, ItemStack weapon, ItemStack ammo, boolean critical) {
        if (ammo.is(Items.FIREWORK_ROCKET))
            return new SeekerFireworkRocketEntity(level, ammo, entity, entity.getX(), entity.getEyeY() - 0.15, entity.getZ(), true);
        return super.createProjectile(level, entity, weapon, ammo, critical);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack ammo, ItemStack weapon) {
        return new SeekerArrow(arrow, ammo, weapon);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack material) {
        return material.getItem() instanceof BlockItem block && block.getBlock().defaultBlockState().is(BlockTags.ICE) || super.isValidRepairItem(toRepair, material);
    }
}

