package show.twilight.finale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class KnightmetalHoeItem
        extends HoeItem {
    public KnightmetalHoeItem(Tier material, Item.Properties properties) {
        super(material, properties.attributes(HoeItem.createAttributes(material, -2, -2.2f)));
    }

    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flags) {
        super.appendHoverText(stack, context, list, flags);
        list.add((Component) Component.translatable((String) "item.twilightforest.knightmetal_pickaxe.desc").withStyle(ChatFormatting.GRAY));
    }
}

