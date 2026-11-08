package show.twilight.finale.item;

import net.minecraft.world.item.Item;
import show.twilight.finale.block.FloorAdherentTrophyBlock;
import show.twilight.finale.block.WallAdherentTrophyBlock;

public class AdherentTrophyItem
        extends FinalTrophyItem {
    public AdherentTrophyItem(FloorAdherentTrophyBlock floorTrophyBlock, WallAdherentTrophyBlock wallTrophyBlock, Item.Properties properties) {
        super(floorTrophyBlock, wallTrophyBlock, properties);
    }
}

