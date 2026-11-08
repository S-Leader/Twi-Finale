package show.twilight.finale.world.castle;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.world.components.structures.TFStructureComponentOld;


public class FinalCastleEntranceSideTowerComponent extends FinalCastleMazeTower13Component {

    public FinalCastleEntranceSideTowerComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        super(FinaleCastlePieces.TFFCEnSiTo.get(), nbt);
        this.deco = new StructureTFDecoratorCastle();
    }

    @SuppressWarnings("this-escape")
    public FinalCastleEntranceSideTowerComponent(int i, int x, int y, int z, int floors, int entranceFloor, Direction direction) {
        super(FinaleCastlePieces.TFFCEnSiTo.get(), i, x, y, z, floors, entranceFloor, TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get().defaultBlockState(), direction);

        addOpening(0, 1, size / 2, Rotation.CLOCKWISE_180);
    }

    @Override
    public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
        if (parent != null && parent instanceof TFStructureComponentOld) {
            this.deco = ((TFStructureComponentOld) parent).deco;
        }

        // add foundation
        FinalCastleFoundation13Component foundation = new FinalCastleFoundation13Component(FinaleCastlePieces.TFFCToF13.get(), 4, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(foundation);
        foundation.addChildren(this, list, rand);

        // add roof
        TFStructureComponentOld roof = new FinalCastleRoof13PeakedComponent(4, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(roof);
        roof.addChildren(this, list, rand);
    }
}
