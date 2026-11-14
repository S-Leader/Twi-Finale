package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import twilightforest.init.TFBlocks;
import twilightforest.world.components.structures.TFStructureComponentOld;

public class FinalCastleEntranceBottomTowerComponent extends FinalCastleMazeTower13Component {

    public FinalCastleEntranceBottomTowerComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        super(FinaleCastlePieces.TFFCEnBoTo.get(), nbt);
        this.deco = new StructureTFDecoratorCastle();
    }

    public FinalCastleEntranceBottomTowerComponent(int i, int x, int y, int z, int floors, int entranceFloor, Direction direction) {
        super(FinaleCastlePieces.TFFCEnBoTo.get(), i, x, y, z, floors, entranceFloor, TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get().defaultBlockState(), direction);

    }

    @Override
    public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
        if (parent != null && parent instanceof TFStructureComponentOld) {
            this.deco = ((TFStructureComponentOld) parent).deco;
        }

        addStairs(list, rand, this.getGenDepth() + 1, this.size - 1, 1, size / 2, Rotation.NONE);
        addStairs(list, rand, this.getGenDepth() + 1, 0, 1, size / 2, Rotation.CLOCKWISE_180);
        addStairs(list, rand, this.getGenDepth() + 1, this.size / 2, 1, 0, Rotation.COUNTERCLOCKWISE_90);
        addStairs(list, rand, this.getGenDepth() + 1, this.size / 2, 1, this.size - 1, Rotation.CLOCKWISE_90);
    }

    private boolean addStairs(StructurePieceAccessor list, RandomSource rand, int index, int x, int y, int z, Rotation rotation) {

        this.addOpening(x, y, z, rotation);

        Direction direction = getStructureRelativeRotation(rotation);
        BlockPos dx = offsetTowerCCoords(x, y, z, 0, direction);

        FinalCastleEntranceStairsComponent stairs = new FinalCastleEntranceStairsComponent(index, dx.getX(), dx.getY(), dx.getZ(), direction);

        list.addPiece(stairs);
        if (list instanceof StructurePiecesBuilder start) {
            stairs.addChildren(start.pieces.get(0), list, rand);
        }
        return true;
    }

    @Override
    protected boolean hasAccessibleRoof() {
        return false;
    }
}
