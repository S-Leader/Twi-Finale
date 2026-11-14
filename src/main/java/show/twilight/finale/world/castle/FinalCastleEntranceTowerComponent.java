package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import twilightforest.init.TFBlocks;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.world.components.structures.TFStructureComponentOld;

public class FinalCastleEntranceTowerComponent extends FinalCastleMazeTower13Component {

    public FinalCastleEntranceTowerComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        super(FinaleCastlePieces.TFFCEnTo.get(), nbt);
        this.deco = new StructureTFDecoratorCastle();
    }

    public FinalCastleEntranceTowerComponent(int i, int x, int y, int z, Direction direction) {
        super(FinaleCastlePieces.TFFCEnTo.get(), i, x, y, z, 3, 2, TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get().defaultBlockState(), direction);
    }

    @Override
    public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
        if (parent != null && parent instanceof TFStructureComponentOld) {
            this.deco = ((TFStructureComponentOld) parent).deco;
        }

        FinalCastleFoundation13Component foundation = new FinalCastleFoundation13Component(FinaleCastlePieces.TFFCToF13.get(), 4, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(foundation);
        foundation.addChildren(this, list, rand);

        TFStructureComponentOld roof = new FinalCastleRoof13PeakedComponent(4, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(roof);
        roof.addChildren(this, list, rand);

        int ground = 127;
        if (list instanceof StructurePiecesBuilder builder && !builder.pieces.isEmpty()
                && builder.pieces.getFirst() instanceof FinaleCastleMainComponent root) {
            ground = root.groundAt(boundingBox.getCenter().getX(), boundingBox.getCenter().getZ());
        }
        int missingFloors = Math.max(0, Math.floorDiv(this.boundingBox.minY() - ground, 8));

        int bottomFloors = missingFloors / 2;

        int middleFloors = missingFloors - bottomFloors;

        Direction facing = Rotation.CLOCKWISE_90.rotate(this.getOrientation());
        int howFar = 20;
        if (!this.buildSideTower(list, rand, middleFloors + 1, facing, howFar)) {
            facing = Rotation.COUNTERCLOCKWISE_90.rotate(this.getOrientation());
            if (!this.buildSideTower(list, rand, middleFloors + 1, facing, howFar)) {
                facing = Rotation.NONE.rotate(this.getOrientation());
                if (!this.buildSideTower(list, rand, middleFloors + 1, facing, howFar)) {

                }
            }
        }

        FinalCastleEntranceBottomTowerComponent eTower = new FinalCastleEntranceBottomTowerComponent(this.getGenDepth() + 1, this.boundingBox.minX() + 6, this.boundingBox.minY() - (middleFloors * 8), this.boundingBox.minZ() + 6, bottomFloors + 1, bottomFloors, facing.getOpposite());
        list.addPiece(eTower);
        eTower.addChildren(this, list, rand);

        BlockPos opening = this.getValidOpeningCC(rand, facing);
        opening = opening.below(middleFloors * 8);

        BlockPos bc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), 1, facing);
        FinalCastleBridgeComponent bridge = new FinalCastleBridgeComponent(this.getGenDepth() + 1, bc.getX(), bc.getY(), bc.getZ(), howFar - 7, facing);
        list.addPiece(bridge);
        bridge.addChildren(this, list, rand);
    }

    private boolean buildSideTower(StructurePieceAccessor list, RandomSource rand, int middleFloors, Direction facing, int howFar) {
        BlockPos opening = this.getValidOpeningCC(rand, facing);

        BlockPos tc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), howFar, facing);

        FinalCastleEntranceSideTowerComponent eTower = new FinalCastleEntranceSideTowerComponent(this.getGenDepth() + 1, tc.getX(), tc.getY(), tc.getZ(), middleFloors, middleFloors - 1, facing);

        BoundingBox largerBB = BoundingBoxUtils.cloneWithAdjustments(eTower.getBoundingBox(), -6, 0, -6, 6, 0, 6);

        StructurePiece intersect = list.findCollisionPiece(largerBB);

        if (intersect == null) {
            list.addPiece(eTower);
            eTower.addChildren(this, list, rand);

            BlockPos bc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), 1, facing);
            FinalCastleBridgeComponent bridge = new FinalCastleBridgeComponent(this.getGenDepth() + 1, bc.getX(), bc.getY(), bc.getZ(), howFar - 7, facing);
            list.addPiece(bridge);
            bridge.addChildren(this, list, rand);

            addOpening(opening.getX(), opening.getY() + 1, opening.getZ(), facing);

            return true;
        } else {

            return false;
        }

    }

    @Override
    public BlockPos getValidOpeningCC(RandomSource rand, Direction facing) {
        BlockPos opening = super.getValidOpeningCC(rand, facing);
        return new BlockPos(opening.getX(), 0, opening.getZ());
    }

}
