package show.twilight.finale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class KnightmetalShovelItem extends HoeItem {
    public KnightmetalShovelItem(Tier material, Item.Properties properties) {
        super(material, properties.attributes(HoeItem.createAttributes(material, 1, -3.0f)));
    }

    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flags) {
        super.appendHoverText(stack, context, list, flags);
        list.add(Component.translatable("item.twilightforest.knightmetal_pickaxe.desc").withStyle(ChatFormatting.GRAY));
    }
}

