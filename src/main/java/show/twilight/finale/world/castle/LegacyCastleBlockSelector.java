package show.twilight.finale.world.castle;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import twilightforest.init.TFBlocks;

final class LegacyCastleBlockSelector extends StructurePiece.BlockSelector {
    @Override
    public void next(RandomSource random, int x, int y, int z, boolean wall) {
        if (!wall) next = Blocks.AIR.defaultBlockState();
        else {
            float value = random.nextFloat();
            next = (value < 0.1F ? TFBlocks.WORN_CASTLE_BRICK : value < 0.2F ? TFBlocks.CRACKED_CASTLE_BRICK : TFBlocks.CASTLE_BRICK).get().defaultBlockState();
        }
    }
}
