package show.twilight.finale.item;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import twilightforest.util.TFItemStackUtils;

import show.twilight.finale.capabilities.ForceFieldCapabilityList;

public class ForceFieldWandItem extends FinaleScepterItem {
    public ForceFieldWandItem(Item.Properties properties) {
        super(properties);
    }

    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() == stack.getMaxDamage()) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide()) {
            ForceFieldCapabilityList.get(player).ifPresent(cap -> {
                cap.replenishFields();
                TFItemStackUtils.hurtButDontBreak(stack, 1, (ServerLevel) level, player);
            });
        }
        if (!player.isCreative()) {
            player.getCooldowns().addCooldown(this, 1200);
        }
        return InteractionResultHolder.success(stack);
    }

    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltip, flags);
        tooltip.add(Component.translatable("item.twilightforest.scepter.desc", stack.getMaxDamage() - stack.getDamageValue()).withStyle(ChatFormatting.GRAY));
    }
}

