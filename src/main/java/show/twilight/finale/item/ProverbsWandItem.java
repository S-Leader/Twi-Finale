package show.twilight.finale.item;

import java.util.List;

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
import twilightforest.init.TFSounds;
import twilightforest.util.TFItemStackUtils;

import show.twilight.finale.entity.projectile.ProverbsBolt;

public class ProverbsWandItem
        extends FinaleScepterItem {
    public ProverbsWandItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() == stack.getMaxDamage()) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        player.playSound(TFSounds.TWILIGHT_SCEPTER_USE.get(), 1.0f, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f + 1.0f);
        if (!level.isClientSide()) {
            level.addFreshEntity(new ProverbsBolt(level, player));
            TFItemStackUtils.hurtButDontBreak(stack, 1, (ServerLevel) level, player);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.twilightforest.scepter.desc", new Object[]{stack.getMaxDamage() - stack.getDamageValue()}).withStyle(ChatFormatting.GRAY));
    }
}

