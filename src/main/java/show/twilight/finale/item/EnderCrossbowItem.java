package show.twilight.finale.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import twilightforest.entity.projectile.TwilightWandBolt;

public class EnderCrossbowItem extends CrossbowItem {
    public EnderCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity entity, ItemStack weapon, ItemStack ammo, boolean critical) {
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            var bolt = new TwilightWandBolt(level, entity);
            return bolt;
        }
        var arrow = super.createProjectile(level, entity, weapon, ammo, critical);
        arrow.getPersistentData().putBoolean("twilightforest:ender", true);
        return arrow;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack material) {
        return material.getItem() instanceof BlockItem block && block.getBlock().defaultBlockState().is(BlockTags.ICE) || super.isValidRepairItem(toRepair, material);
    }
}

