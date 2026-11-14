package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;

public final class FinalCastlePlainTowerPiece extends StructurePiece {
    private final BoundingBox towerBounds;
    private final int roofBaseY;
    private final boolean fillSpire;

    public FinalCastlePlainTowerPiece(BoundingBox tower, BoundingBox roof, int actualRoofTopY) {
        super(FinaleCastlePieces.PLAIN_TOWER.get(), 11, new BoundingBox(
                Math.min(tower.minX(), roof.minX()), tower.minY(), Math.min(tower.minZ(), roof.minZ()),
                Math.max(tower.maxX(), roof.maxX()), Math.max(actualRoofTopY, Math.max(tower.maxY(), roof.maxY())), Math.max(tower.maxZ(), roof.maxZ())));
        this.towerBounds = tower;
        this.roofBaseY = roof.minY();
        this.fillSpire = true;
    }

    public FinalCastlePlainTowerPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(FinaleCastlePieces.PLAIN_TOWER.get(), tag);
        int[] b = tag.getIntArray("TreasureTowerBounds");
        if (b.length != 6) throw new IllegalArgumentException("Missing Finalslayer tower bounds");
        towerBounds = new BoundingBox(b[0], b[1], b[2], b[3], b[4], b[5]);
        roofBaseY = tag.getInt("TreasureRoofBaseY");
        fillSpire = tag.getBoolean("FillTreasureSpire");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putIntArray("TreasureTowerBounds", new int[]{towerBounds.minX(), towerBounds.minY(), towerBounds.minZ(), towerBounds.maxX(), towerBounds.maxY(), towerBounds.maxZ()});
        tag.putInt("TreasureRoofBaseY", roofBaseY);
        tag.putBoolean("FillTreasureSpire", fillSpire);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                            RandomSource random, BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        BoundingBox box = getBoundingBox();
        for (int x = Math.max(box.minX(), clip.minX()); x <= Math.min(box.maxX(), clip.maxX()); x++)
            for (int z = Math.max(box.minZ(), clip.minZ()); z <= Math.min(box.maxZ(), clip.maxZ()); z++) {
                boolean underRoof = false;
                for (int y = Math.min(box.maxY(), clip.maxY()); y >= Math.max(box.minY(), clip.minY()); y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = level.getBlockState(pos);
                    if (y < roofBaseY && !towerBounds.isInside(pos)) continue;
                    if (state.is(TFBlocks.CASTLE_ROOF_TILE)) underRoof = true;

                    if (fillSpire && underRoof && y >= roofBaseY && state.isAir()
                            && x >= towerBounds.minX() && x <= towerBounds.maxX()
                            && z >= towerBounds.minZ() && z <= towerBounds.maxZ())
                        level.setBlock(pos, TFBlocks.CASTLE_BRICK.get().defaultBlockState(), 2);
                    if (state.is(TFBlocks.WORN_CASTLE_BRICK) || state.is(TFBlocks.CRACKED_CASTLE_BRICK) || state.is(TFBlocks.MOSSY_CASTLE_BRICK))
                        level.setBlock(pos, TFBlocks.CASTLE_BRICK.get().defaultBlockState(), 2);
                }
            }
    }
}
