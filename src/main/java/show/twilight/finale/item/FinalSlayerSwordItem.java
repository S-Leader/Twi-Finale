package show.twilight.finale.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.data.tags.BlockTagGenerator;

import javax.annotation.Nonnull;

public class FinalSlayerSwordItem
        extends SwordItem {
    public FinalSlayerSwordItem(Tier material, Item.Properties properties) {
        super(material, properties.attributes(SwordItem.createAttributes(material, 3, -2.0f)));
    }

    public float getDestroySpeed(@Nonnull ItemStack stack, BlockState state) {
        float destroySpeed = super.getDestroySpeed(stack, state);
        return state.is(BlockTagGenerator.MAZEBREAKER_ACCELERATED) ? destroySpeed * 128.0f : destroySpeed;
    }

    public boolean isValidRepairItem(ItemStack stack, ItemStack otherStack) {
        return false;
    }
}

