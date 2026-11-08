package show.twilight.finale.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import twilightforest.item.PhantomArmorItem;

public class PhantomLeggingsItem
        extends PhantomArmorItem {
    public PhantomLeggingsItem(net.minecraft.core.Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties.durability(type.getDurability(30)));
    }

    public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return net.minecraft.resources.ResourceLocation.parse("twilightforest_finale:textures/models/armor/phantom_layer_2.png");
    }
}

