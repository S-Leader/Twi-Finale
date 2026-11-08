package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.world.components.structures.TFStructureComponentOld;

/**
 * Last, chunk-clipped pass: join the second basement landing to the forge floor.
 */
public final class FinalCastleForgeAccessComponent extends TFStructureComponentOld {
    private final boolean revivedEntrance;

    public FinalCastleForgeAccessComponent(FinalCastleDungeonForgeRoomComponent forge) {
        super(FinaleCastlePieces.FORGE_ACCESS.get(), 11, 0, 0, 0);
        revivedEntrance = forge.hasRevivedEntrance();
        var b = forge.getBoundingBox();
        boundingBox = new BoundingBox(b.minX(), b.minY(), b.minZ(), b.maxX(), b.minY() + (revivedEntrance ? 38 : 35), b.maxZ());
        setOrientation(forge.getOrientation());
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 3;
    }

    public FinalCastleForgeAccessComponent(StructurePieceSerializationContext context, CompoundTag tag) {
        super(FinaleCastlePieces.FORGE_ACCESS.get(), tag);
        revivedEntrance = tag.getBoolean("FinaleRevivedEntrance");
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 3;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putBoolean("FinaleRevivedEntrance", revivedEntrance);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunk, ChunkPos chunkPos, BlockPos pivot) {
        // Existing descent ends just above the forge ceiling. Open that ceiling and
        // provide stairs, instead of leaving a sealed roof or a thirty-block drop.
        if (revivedEntrance) {
            // Replace the second copy's abandoned vertical shaft with a real
            // floor/roof entrance, not a lateral attachment or an extra arena.
            generateBox(level, chunk, 8, 31, 8, 22, 31, 22, deco.blockState, deco.blockState, false);
            generateAirBox(level, chunk, 8, 32, 8, 22, 38, 22);
        }
        generateBox(level, chunk, 13, 30, 11, 17, 30, 14, deco.blockState, deco.blockState, false);
        generateAirBox(level, chunk, 13, 31, 11, 17, revivedEntrance ? 38 : 35, 15);
        // The actual forge template has TWO floors (y=0 and y=15). A single
        // thirty-step flight cuts straight through its mezzanine. Use a real
        // landing on y=15, then a separate return flight to the lowest hall.
        // TFStructureComponentOld rotates block states 180 degrees relative to
        // its local coordinate mapping. Pre-flip stair facing so it rises toward
        // the previous (higher) tread, including the SOUTH-oriented new forge.
        var upper = deco.stairState.setValue(StairBlock.FACING, Direction.SOUTH);
        for (int step = 0; step <= 15; step++) {
            int y = 30 - step, z = 15 + step;
            generateAirBox(level, chunk, 13, y + 1, z, 17, y + 4, z);
            generateBox(level, chunk, 13, y, z, 17, y, z, upper, upper, false);
            generateBox(level, chunk, 13, y - 1, z, 17, y - 1, z, deco.blockState, deco.blockState, false);
        }
        generateBox(level, chunk, 13, 15, 31, 29, 15, 33, deco.blockState, deco.blockState, false);
        generateAirBox(level, chunk, 13, 16, 30, 29, 19, 33);
        var lower = deco.stairState.setValue(StairBlock.FACING, Direction.NORTH);
        for (int step = 0; step < 15; step++) {
            int y = 14 - step, z = 30 - step;
            generateAirBox(level, chunk, 25, y + 1, z, 29, y + 4, z);
            generateBox(level, chunk, 25, y, z, 29, y, z, lower, lower, false);
            if (y > 0) generateBox(level, chunk, 25, y - 1, z, 29, y - 1, z, deco.blockState, deco.blockState, false);
        }
        generateBox(level, chunk, 25, 0, 14, 29, 0, 15, deco.blockState, deco.blockState, false);
        generateAirBox(level, chunk, 25, 1, 14, 29, 4, 15);
    }
}
