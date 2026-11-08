package show.twilight.finale.item;

import net.minecraft.world.item.Item;
import show.twilight.finale.block.FloorDuckTrophyBlock;
import show.twilight.finale.block.WallDuckTrophyBlock;

public class DuckTrophyItem
        extends FinalTrophyItem {
    public DuckTrophyItem(FloorDuckTrophyBlock floorBlock, WallDuckTrophyBlock wallBlock, Item.Properties properties) {
        super(floorBlock, wallBlock, properties);
    }
}

