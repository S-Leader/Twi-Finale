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
import twilightforest.entity.projectile.IceArrow;
import twilightforest.entity.projectile.IceBomb;

public class IceCrossbowItem extends CrossbowItem {
    public IceCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity entity, ItemStack weapon, ItemStack ammo, boolean critical) {
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            var bomb = new IceBomb(level, entity.position().add(0, entity.getEyeHeight() - 0.15, 0));
            bomb.setOwner(entity);
            return bomb;
        }
        return super.createProjectile(level, entity, weapon, ammo, critical);
    }

    @Override
    public AbstractArrow customArrow(
            AbstractArrow arrow, ItemStack ammo, ItemStack weapon) {
        return new IceArrow(arrow, ammo, weapon);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack material) {
        return material.getItem() instanceof BlockItem block
                && block.getBlock().defaultBlockState().is(BlockTags.ICE)
                || super.isValidRepairItem(toRepair, material);
    }
}

