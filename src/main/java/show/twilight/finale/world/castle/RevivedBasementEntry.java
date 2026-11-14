package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.world.components.structures.TFStructureComponentOld;

public final class RevivedBasementEntry extends TFStructureComponentOld {
    private final BlockPos landing;

    RevivedBasementEntry(BlockPos landing) {
        super(FinaleCastlePieces.REVIVED_ENTRY.get(), 0, 0, 0, 0);
        this.landing = landing;
        boundingBox = new BoundingBox(landing.getX() - 3, landing.getY() - 26, landing.getZ() - 28,
                landing.getX() + 3, landing.getY() + 5, landing.getZ() + 3);
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 2;
    }

    public RevivedBasementEntry(StructurePieceSerializationContext context, CompoundTag tag) {
        super(FinaleCastlePieces.REVIVED_ENTRY.get(), tag);
        landing = BlockPos.of(tag.getLong("Entry"));
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 2;
    }

    BlockPos landing() {
        return landing;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putLong("Entry", landing.asLong());
    }

    private void fill(WorldGenLevel level, BoundingBox clip, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++)
                for (int y = y0; y <= y1; y++) {
                    BlockPos at = landing.offset(x, y, z);
                    if (clip.isInside(at)) level.setBlock(at, state, 18);
                }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
                            BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        fill(level, clip, -3, -26, -28, 3, 5, 3, deco.blockState);
        fill(level, clip, -3, 0, -3, 3, 5, 3, AIR);
        for (int step = 0; step < 25; step++) {
            int y = -2 - step, z = -3 - step;
            fill(level, clip, -2, y + 1, z, 2, y + 5, z, AIR);
            fill(level, clip, -2, y, z, 2, y, z, deco.stairState.setValue(StairBlock.FACING, Direction.SOUTH));
        }
        fill(level, clip, -1, -25, -28, 1, -22, -28, AIR);
    }
}
