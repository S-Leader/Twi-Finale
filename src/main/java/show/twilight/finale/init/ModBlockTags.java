package show.twilight.finale.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import show.twilight.finale.TwilightFinaleMod;

public final class ModBlockTags {
    public static final TagKey<Block> ANNIHILATION_CUBE_BREAKABLE = TagKey.create(Registries.BLOCK,
            TwilightFinaleMod.prefix("annihilation_cube_breakable"));

    private ModBlockTags() {
    }
}
