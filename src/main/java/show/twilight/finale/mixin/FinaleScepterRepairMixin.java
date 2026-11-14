package show.twilight.finale.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.twilight.finale.init.ModItem;
import twilightforest.item.recipe.ScepterRepairRecipe;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ScepterRepairRecipe.class, remap = false)
public abstract class FinaleScepterRepairMixin {
    @Shadow
    @Final
    private Item scepter;
    @Shadow
    @Final
    private List<Ingredient> repairItems;

    @Unique
    private boolean finale$isOurScepter() {
        return scepter == ModItem.PROVERBS_SCEPTER.get() || scepter == ModItem.FORCE_FIELD_SCEPTER.get() || scepter == ModItem.SHADOW_SCEPTER.get() || scepter == ModItem.WRAITH_SCEPTER.get();
    }

    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void finale$exactIngredients(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> result) {
        if (!finale$isOurScepter()) return;
        boolean wand = false;
        var materials = new ArrayList<ItemStack>();
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) continue;
            if (stack.is(scepter)) {
                if (wand || !stack.isDamaged()) {
                    result.setReturnValue(false);
                    return;
                }
                wand = true;
            } else materials.add(stack);
        }
        result.setReturnValue(wand && materials.size() == repairItems.size() && RecipeMatcher.findMatches(materials, repairItems) != null);
    }

    @Inject(method = "canCraftInDimensions", at = @At("HEAD"), cancellable = true)
    private void finale$validGrid(int width, int height, CallbackInfoReturnable<Boolean> result) {
        if (finale$isOurScepter()) {
            result.setReturnValue(width * height >= (scepter == ModItem.WRAITH_SCEPTER.get() ? 3 : 2));
        }
    }
}
