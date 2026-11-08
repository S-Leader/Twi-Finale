package show.twilight.finale.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class ItemData {
    private ItemData() {
    }

    public static float getFloat(ItemStack stack, String key) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getFloat(key);
    }

    public static void putFloat(ItemStack stack, String key, float value) {
        if (getFloat(stack, key) != value)
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putFloat(key, value));
    }
}
