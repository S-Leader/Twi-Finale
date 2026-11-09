package show.twilight.finale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber(modid = "twilightforest")
public class FieryHoeItem
        extends HoeItem {
    public FieryHoeItem(Tier toolMaterial, Item.Properties properties) {
        super(toolMaterial, properties.attributes(HoeItem.createAttributes(toolMaterial, -2, -2.2f)));
    }

    public boolean isPrimaryItemFor(ItemStack stack, net.minecraft.core.Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.FIRE_ASPECT) && super.isPrimaryItemFor(stack, enchantment);
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return !show.twilight.finale.util.EnchantmentsUtil.hasStored(book, Enchantments.FIRE_ASPECT) && super.isBookEnchantable(stack, book);
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && !target.level().isClientSide() && !target.fireImmune()) {
            target.igniteForSeconds(15);
        } else {
            for (int var1 = 0; var1 < 20; ++var1) {
                double px = target.getX() + (double) (target.level().getRandom().nextFloat() * target.getBbWidth() * 2.0f) - (double) target.getBbWidth();
                double py = target.getY() + (double) (target.level().getRandom().nextFloat() * target.getBbHeight());
                double pz = target.getZ() + (double) (target.level().getRandom().nextFloat() * target.getBbWidth() * 2.0f) - (double) target.getBbWidth();
                target.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0.02, 0.02, 0.02);
            }
        }
        return result;
    }

    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.twilightforest.fiery_sword.desc").withStyle(ChatFormatting.GRAY));
    }
}

