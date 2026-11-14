package show.twilight.finale.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import twilightforest.init.TFSounds;
import twilightforest.util.TFItemStackUtils;

import show.twilight.finale.entity.projectile.AnnihilationBolt;

public class ShadowWandItem
        extends FinaleScepterItem {
    public ShadowWandItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() == stack.getMaxDamage()) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int count) {
        if (livingEntity instanceof Player player) {
            int duration = this.getUseDuration(stack, livingEntity) - count;
            if (stack.getDamageValue() >= stack.getMaxDamage()) {
                player.stopUsingItem();
                return;
            }
            if (duration > 20) {
                player.stopUsingItem();
                player.getCooldowns().addCooldown(this, 60);
                return;
            }
            player.playSound(TFSounds.LICH_SHOOT.get(), 1.0f, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f + 1.0f);
            if (!level.isClientSide()) {
                AnnihilationBolt bolt = new AnnihilationBolt(level, player);
                bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
                level.addFreshEntity(bolt);
                if (!player.getAbilities().instabuild && duration % 5 == 0) {
                    TFItemStackUtils.hurtButDontBreak(stack, 1, (ServerLevel) level, player);
                }
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof Player player) {
            player.getCooldowns().addCooldown(this, 60);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.twilightforest.scepter.desc", new Object[]{stack.getMaxDamage() - stack.getDamageValue()}).withStyle(ChatFormatting.GRAY));
    }
}

