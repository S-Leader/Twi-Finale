package show.twilight.finale.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.twilight.finale.init.ModItem;

@Mixin(value = {ItemInHandRenderer.class})
public class ItemInHandRendererMixin {
    @Inject(method = {"evaluateWhichHandsToRender"}, at = {@At(value = "RETURN")}, cancellable = true)
    private static void evaluateWhichHandsToRender(LocalPlayer player, CallbackInfoReturnable<ItemInHandRenderer.HandRenderSelection> cir) {
        ItemStack itemStack = player.getMainHandItem();
        ItemStack itemStack1 = player.getOffhandItem();
        boolean flag = itemStack.is(ModItem.TRIPLE_CROSSBOW.get()) || itemStack.is(ModItem.SEEKER_CROSSBOW.get()) || itemStack.is(ModItem.ICE_CROSSBOW.get()) || itemStack.is(ModItem.ENDER_CROSSBOW.get());
        boolean flag1 = itemStack1.is(ModItem.TRIPLE_CROSSBOW.get()) || itemStack1.is(ModItem.SEEKER_CROSSBOW.get()) || itemStack1.is(ModItem.ICE_CROSSBOW.get()) || itemStack1.is(ModItem.ENDER_CROSSBOW.get());
        if (flag || flag1) {
            if (player.isUsingItem()) {
                ItemStack itemStack2 = player.getUseItem();
                InteractionHand interactionhand = player.getUsedItemHand();
                if (itemStack2.is(ModItem.TRIPLE_CROSSBOW.get()) || itemStack2.is(ModItem.SEEKER_CROSSBOW.get()) || itemStack2.is(ModItem.ICE_CROSSBOW.get()) || itemStack2.is(ModItem.ENDER_CROSSBOW.get())) {
                    cir.setReturnValue(ItemInHandRenderer.HandRenderSelection.onlyForHand(interactionhand));
                }
            } else {
                cir.setReturnValue(((itemStack.is(ModItem.TRIPLE_CROSSBOW.get()) || itemStack.is(ModItem.SEEKER_CROSSBOW.get()) || itemStack.is(ModItem.ICE_CROSSBOW.get()) || itemStack.is(ModItem.ENDER_CROSSBOW.get())) && CrossbowItem.isCharged(itemStack) ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS));
            }
        }
    }
}

