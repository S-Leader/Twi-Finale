package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.world.components.structures.TFStructureComponentOld;

import java.util.ArrayList;

public class FinalCastleWreckedTowerComponent extends FinalCastleDamagedTowerComponent {

    public FinalCastleWreckedTowerComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        super(FinaleCastlePieces.TFFCWrT.get(), nbt);
        this.deco = new StructureTFDecoratorCastle();
    }

    public FinalCastleWreckedTowerComponent(RandomSource rand, int i, int x, int y, int z, Direction direction) {
        super(FinaleCastlePieces.TFFCWrT.get(), rand, i, x, y, z, direction);
    }

    @Override
    public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
        if (parent != null && parent instanceof TFStructureComponentOld) {
            this.deco = ((TFStructureComponentOld) parent).deco;
        }

        FinalCastleFoundation13Component thorns = new FinalCastleFoundation13ComponentThorns(0, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(thorns);
        thorns.addChildren(this, list, rand);

    }

    @Override
    public BlockState getGlyphMeta() {
        return TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get().defaultBlockState();
    }

    @Override
    protected void determineBlockDestroyed(WorldGenLevel world, ArrayList<DestroyArea> areas, int y, int x, int z) {
        boolean isInside = false;

        BlockPos pos = new BlockPos(x, y, z);

        for (DestroyArea dArea : areas) {
            if (dArea != null && dArea.isVecInside(pos)) {
                isInside = true;
            }
        }

        if (!isInside) {
            world.removeBlock(pos, false);
        }
    }

    @Override
    protected ArrayList<DestroyArea> makeInitialDestroyList(RandomSource rand) {
        ArrayList<DestroyArea> areas = new ArrayList<DestroyArea>(2);

        areas.add(DestroyArea.createNonIntersecting(this.getBoundingBox(), rand, this.getBoundingBox().maxY() - 1, areas));
        areas.add(DestroyArea.createNonIntersecting(this.getBoundingBox(), rand, this.getBoundingBox().maxY() - 1, areas));
        areas.add(DestroyArea.createNonIntersecting(this.getBoundingBox(), rand, this.getBoundingBox().maxY() - 1, areas));
        areas.add(DestroyArea.createNonIntersecting(this.getBoundingBox(), rand, this.getBoundingBox().maxY() - 1, areas));
        return areas;
    }
}
