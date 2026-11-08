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

/**
 * Three walkable flights; the lower entrance is in the next reserved bay, not inside this shaft.
 */
public final class RevivedBasementDescent extends TFStructureComponentOld {
    private final BlockPos top;

    public RevivedBasementDescent(BlockPos top) {
        super(FinaleCastlePieces.REVIVED_DESCENT.get(), 0, 0, 0, 0);
        this.top = top;
        boundingBox = new BoundingBox(top.getX() - 7, top.getY() - 48, top.getZ() - 11,
                top.getX() + 7, top.getY() + 4, top.getZ() + 16);
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 2;
    }

    public RevivedBasementDescent(StructurePieceSerializationContext context, CompoundTag tag) {
        super(FinaleCastlePieces.REVIVED_DESCENT.get(), tag);
        top = BlockPos.of(tag.getLong("Top"));
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 2;
    }

    BlockPos top() {
        return top;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putLong("Top", top.asLong());
    }

    private void fill(WorldGenLevel level, BoundingBox clip, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++)
                for (int y = y0; y <= y1; y++) {
                    BlockPos at = top.offset(x - 12, y, z - 12);
                    if (clip.isInside(at)) level.setBlock(at, state, 18);
                }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
                            BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        fill(level, clip, 5, -48, 1, 19, -1, 24, deco.blockState);
        // The top platform stays INSIDE the exit room, so no missing north-side
        // maze edge can leave the stairhead inaccessible behind a wall.
        fill(level, clip, 6, 0, 5, 13, 0, 5, deco.blockState);
        fill(level, clip, 6, 1, 5, 13, 4, 5, AIR);
        fill(level, clip, 11, 1, 5, 13, 4, 12, AIR);
        for (int flight = 0; flight < 3; flight++)
            for (int step = 0; step < 16; step++) {
                int y = -flight * 16 - 1 - step, z = flight == 1 ? 21 - step : 6 + step, x = flight == 1 ? 14 : 6;
                fill(level, clip, x, y + 1, z, x + 4, y + 4, z, AIR);
                fill(level, clip, x, y, z, x + 4, y, z, deco.stairState.setValue(StairBlock.FACING, flight == 1 ? Direction.SOUTH : Direction.NORTH));
            }
        fill(level, clip, 6, -15, 22, 18, -12, 24, AIR);
        fill(level, clip, 6, -31, 3, 18, -28, 5, AIR);
        fill(level, clip, 6, -47, 22, 18, -44, 24, AIR);
        fill(level, clip, 10, -48, 22, 14, -43, 28, deco.blockState);
        fill(level, clip, 11, -47, 22, 13, -44, 28, AIR);
        // Reopen the side of the bottom landing after adding the narrow corridor walls.
        fill(level, clip, 6, -47, 22, 18, -44, 24, AIR);
    }
}
