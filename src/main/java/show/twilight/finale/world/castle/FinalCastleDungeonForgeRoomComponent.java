package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.world.components.structures.TFStructureComponentOld;

public class FinalCastleDungeonForgeRoomComponent extends TFStructureComponentOld {
    private final boolean revivedEntrance;

    public FinalCastleDungeonForgeRoomComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        super(FinaleCastlePieces.TFFCDunBoR.get(), nbt);
        revivedEntrance = nbt.getBoolean("FinaleRevivedEntrance");
        this.deco = new StructureTFDecoratorCastle();
    }

    @SuppressWarnings("this-escape")
    public FinalCastleDungeonForgeRoomComponent(int i, int x, int y, int z, Direction direction) {
        this(i, x, y, z, direction, false);
    }

    public FinalCastleDungeonForgeRoomComponent(int i, int x, int y, int z, Direction direction, boolean revivedEntrance) {
        super(FinaleCastlePieces.TFFCDunBoR.get(), i, x, y, z);
        this.revivedEntrance = revivedEntrance;
        this.spawnListIndex = 3;
        this.setOrientation(direction);

        this.boundingBox = switch (direction) {
            case WEST -> new BoundingBox(x - 35, y, z - 15, x + 15, y + 30, z + 35);
            case NORTH -> new BoundingBox(x - 35, y, z - 35, x + 15, y + 30, z + 15);
            case EAST -> new BoundingBox(x - 15, y, z - 35, x + 35, y + 30, z + 15);
            default -> new BoundingBox(x - 15, y, z - 15, x + 35, y + 30, z + 35);
        };
    }

    public boolean hasRevivedEntrance() {
        return revivedEntrance;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putBoolean("FinaleRevivedEntrance", revivedEntrance);
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
        this.generateAirBox(world, sbb, 0, 0, 0, 50, 30, 50);

    }
}
