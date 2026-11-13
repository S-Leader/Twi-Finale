package show.twilight.finale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import twilightforest.util.TFToolMaterials;

import java.util.List;

public final class TwilightSwordItem extends SwordItem {
    public static final int MIN_CHARGE_TICKS = 10;
    // Riptide III launch strength; movement remains native after this impulse.
    public static final double DASH_SPEED = 3.0;
    public static final int DASH_TICKS = 20;
    public static final double GROUND_LIFT = 1.1999999F;

    public TwilightSwordItem(Properties properties) {
        super(TFToolMaterials.FIERY, properties.attributes(createAttributes(TFToolMaterials.FIERY, 8F - TFToolMaterials.FIERY.getAttackDamageBonus(), -2.4F)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.getOffhandItem().isEmpty())
            return InteractionResultHolder.pass(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // Bow-like holding: becoming fully charged does not release the item.
        // The dash starts only when the player releases the right mouse button.
        return 72000;
    }

    public static void igniteTarget(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level().isClientSide() || target.fireImmune()) return;
        var fireAspect = attacker.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FIRE_ASPECT);
        int level = EnchantmentHelper.getEnchantmentLevel(fireAspect, attacker);
        target.igniteForSeconds(5 + level * 4);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit) igniteTarget(stack, target, attacker);
        return hit;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(getDescriptionId() + ".dash").withStyle(ChatFormatting.GRAY));
    }
}
