package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.world.components.structures.TFStructureComponentOld;

/**
 * Only a chosen graph edge gets a corridor. Unchosen edges remain solid rock.
 */
public final class RevivedBasementCorridor extends TFStructureComponentOld {
    final int floor, a, b;
    private final boolean east;

    RevivedBasementCorridor(BlockPos from, int floor, int a, int b, boolean east) {
        super(FinaleCastlePieces.REVIVED_CORRIDOR.get(), 0, 0, 0, 0);
        this.floor = floor;
        this.a = a;
        this.b = b;
        this.east = east;
        boundingBox = east ? new BoundingBox(from.getX() + 8, from.getY(), from.getZ() - 2, from.getX() + 16, from.getY() + 5, from.getZ() + 2)
                : new BoundingBox(from.getX() - 2, from.getY(), from.getZ() + 8, from.getX() + 2, from.getY() + 5, from.getZ() + 16);
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 2;
    }

    public RevivedBasementCorridor(StructurePieceSerializationContext context, CompoundTag tag) {
        super(FinaleCastlePieces.REVIVED_CORRIDOR.get(), tag);
        floor = tag.getInt("BasementLevel");
        a = tag.getInt("A");
        b = tag.getInt("B");
        east = tag.getBoolean("East");
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 2;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putInt("BasementLevel", floor);
        tag.putInt("A", a);
        tag.putInt("B", b);
        tag.putBoolean("East", east);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
                            BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        for (int x = boundingBox.minX(); x <= boundingBox.maxX(); x++)
            for (int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++)
                for (int y = boundingBox.minY(); y <= boundingBox.maxY(); y++) {
                    BlockPos at = new BlockPos(x, y, z);
                    if (!clip.isInside(at)) continue;
                    int w = east ? z - boundingBox.minZ() : x - boundingBox.minX();
                    boolean wall = w == 0 || w == 4 || y == boundingBox.minY() || y == boundingBox.maxY();
                    var state = wall ? TFBlocks.CASTLE_BRICK.get().defaultBlockState() : AIR;
                    if (y == boundingBox.maxY() && w == 2 && (east ? x - boundingBox.minX() : z - boundingBox.minZ()) == 4)
                        state = Blocks.SEA_LANTERN.defaultBlockState();
                    level.setBlock(at, state, 18);
                }
    }
}
