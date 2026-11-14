package show.twilight.finale.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import twilightforest.enchantment.RechargeScepterEffect;
import twilightforest.init.TFEnchantments;

public abstract class FinaleScepterItem extends Item {
    protected FinaleScepterItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return !stack.isEnchanted();
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(TFEnchantments.RENEWAL) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        var enchantments = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        return !enchantments.isEmpty() && enchantments.keySet().stream().allMatch(e -> e.is(TFEnchantments.RENEWAL));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!selected && entity.tickCount % 20 == 0 && level instanceof ServerLevel server && stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(level.holderOrThrow(TFEnchantments.RENEWAL)) > 0)
            RechargeScepterEffect.applyRecharge(server, stack, entity);
    }
}
