package show.twilight.finale.compat.hadean;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HadeanRenderCompat {
    public static boolean isEnforcerTrophy(ItemStack stack) {
        return stack.is((Item) ModHadeanBlockCompat.HADEAN_ENFORCER_TROPHY.get());
    }

    public static boolean isBlasterTrophy(ItemStack stack) {
        return stack.is((Item) ModHadeanBlockCompat.HADEAN_BLASTER_TROPHY.get());
    }

    public static boolean isSmasherTrophy(ItemStack stack) {
        return stack.is((Item) ModHadeanBlockCompat.HADEAN_SMASHER_TROPHY.get());
    }

    public static boolean isDuckEnforcerTrophy(ItemStack stack) {
        return stack.is((Item) ModHadeanBlockCompat.DUCK_ENFORCER_TROPHY.get());
    }
}

