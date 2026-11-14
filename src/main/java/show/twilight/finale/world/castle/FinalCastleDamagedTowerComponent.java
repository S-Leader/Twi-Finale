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
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import twilightforest.init.TFBlocks;
import twilightforest.world.components.structures.TFStructureComponentOld;

import java.util.ArrayList;

public class FinalCastleDamagedTowerComponent extends FinalCastleMazeTower13Component {

    public FinalCastleDamagedTowerComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        this(FinaleCastlePieces.TFFCDamT.get(), nbt);
    }

    public FinalCastleDamagedTowerComponent(StructurePieceType piece, CompoundTag nbt) {
        super(piece, nbt);
        this.deco = new StructureTFDecoratorCastle();
    }

    public FinalCastleDamagedTowerComponent(StructurePieceType piece, RandomSource rand, int i, int x, int y, int z, Direction direction) {
        super(piece, rand, i, x, y, z, TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get().defaultBlockState(), direction);
    }

    @Override
    public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
        if (parent != null && parent instanceof TFStructureComponentOld) {
            this.deco = ((TFStructureComponentOld) parent).deco;
        }

        FinalCastleFoundation13Component foundation = new FinalCastleFoundation13Component(FinaleCastlePieces.TFFCToF13.get(), 0, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(foundation);
        foundation.addChildren(this, list, rand);

        FinalCastleFoundation13Component thorns = new FinalCastleFoundation13ComponentThorns(0, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(thorns);
        thorns.addChildren(this, list, rand);

        this.buildNonCriticalTowers(list, rand);
    }

    @Override
    protected FinalCastleMazeTower13Component makeNewDamagedTower(RandomSource rand, Direction facing, BlockPos tc) {
        return new FinalCastleWreckedTowerComponent(rand, this.getGenDepth() + 1, tc.getX(), tc.getY(), tc.getZ(), facing);
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
        super.postProcess(world, manager, generator, rand, sbb, chunkPosIn, blockPos);
        RandomSource decoRNG = RandomSource.create(world.getSeed() + (this.boundingBox.minX() * 321534781L) ^ (this.boundingBox.minZ() * 756839L));

        this.destroyTower(world, decoRNG, sbb);
    }

    public void destroyTower(WorldGenLevel world, RandomSource rand, BoundingBox sbb) {

        ArrayList<DestroyArea> areas = makeInitialDestroyList(rand);

        boolean hitDeadRock = false;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = this.boundingBox.maxY(); !hitDeadRock && y > 64; y--) {
            for (int x = this.boundingBox.minX() - 2; x <= this.boundingBox.maxX() + 2; x++) {
                for (int z = this.boundingBox.minZ() - 2; z <= this.boundingBox.maxZ() + 2; z++) {
                    pos.set(x, y, z);
                    if (sbb.isInside(pos)) {
                        if (world.getBlockState(pos).getBlock() == TFBlocks.DEADROCK.get()) {
                            hitDeadRock = true;
                        }
                        determineBlockDestroyed(world, areas, y, x, z);
                    }
                }
            }

            DestroyArea removeArea = null;

            for (DestroyArea dArea : areas) {
                if (dArea == null || dArea.isEntirelyAbove(y)) {
                    removeArea = dArea;
                }
            }

            if (removeArea != null) {
                areas.remove(removeArea);
                areas.add(DestroyArea.createNonIntersecting(this.getBoundingBox(), rand, y, areas));

            }
        }
    }

    protected ArrayList<DestroyArea> makeInitialDestroyList(RandomSource rand) {
        ArrayList<DestroyArea> areas = new ArrayList<DestroyArea>(2);

        areas.add(DestroyArea.createNonIntersecting(this.getBoundingBox(), rand, this.getBoundingBox().maxY() - 1, areas));
        areas.add(DestroyArea.createNonIntersecting(this.getBoundingBox(), rand, this.getBoundingBox().maxY() - 1, areas));
        areas.add(DestroyArea.createNonIntersecting(this.getBoundingBox(), rand, this.getBoundingBox().maxY() - 1, areas));
        return areas;
    }

    protected void determineBlockDestroyed(WorldGenLevel world, ArrayList<DestroyArea> areas, int y, int x, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        for (DestroyArea dArea : areas) {
            if (dArea != null && dArea.isVecInside(pos)) {
                world.removeBlock(pos, false);
            }
        }
    }
}
