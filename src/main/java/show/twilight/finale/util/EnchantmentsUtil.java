package show.twilight.finale.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public final class EnchantmentsUtil {
    private EnchantmentsUtil() {
    }

    public static int level(ItemStack stack, ResourceKey<Enchantment> key, Level level) {
        return EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), stack);
    }

    public static boolean hasStored(ItemStack stack, ResourceKey<Enchantment> key) {
        return stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).keySet().stream().anyMatch(holder -> holder.is(key));
    }
}
