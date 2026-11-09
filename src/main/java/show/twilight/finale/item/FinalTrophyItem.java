package show.twilight.finale.item;

import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import show.twilight.finale.init.ModWithoutLevelRenderer;

import java.util.function.Consumer;

public abstract class FinalTrophyItem extends StandingAndWallBlockItem implements CurioItem {
    protected FinalTrophyItem(Block floorBlock, Block wallBlock, Item.Properties properties) {
        super(floorBlock, wallBlock, properties, Direction.DOWN);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        EquipmentSlot equipmentslot = EquipmentSlot.HEAD;
        ItemStack itemstack1 = player.getItemBySlot(equipmentslot);
        if (itemstack1.isEmpty()) {
            player.setItemSlot(equipmentslot, itemstack.split(1));
            if (!level.isClientSide()) {
                player.awardStat(Stats.ITEM_USED.get(this));
            }
            return InteractionResultHolder.sidedSuccess(itemstack, (boolean) level.isClientSide());
        }
        return InteractionResultHolder.fail(itemstack);
    }

    @Nullable
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ModWithoutLevelRenderer.ITEM_EXTENSION);
    }
}

