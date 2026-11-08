package show.twilight.finale.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import show.twilight.finale.block.FloorHadeanTrophyBlock;
import show.twilight.finale.block.WallHadeanTrophyBlock;

public class HadeanTrophyItem
        extends FinalTrophyItem {
    public HadeanTrophyItem(FloorHadeanTrophyBlock floorBlock, WallHadeanTrophyBlock wallBlock, Item.Properties properties) {
        super((Block) floorBlock, (Block) wallBlock, properties);
    }
}

