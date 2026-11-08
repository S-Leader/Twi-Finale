package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.world.components.structures.TFStructureComponentOld;


public class FinalCastleDungeonExitComponent extends FinalCastleDungeonRoom31Component {
    // Build-time edges: validate the descent before committing the complete layout.
    private FinalCastleDungeonStepsComponent descent;
    private StructurePiece lowerRoom;

    public FinalCastleDungeonExitComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        super(FinaleCastlePieces.TFFCDunEx.get(), nbt);
        this.deco = new StructureTFDecoratorCastle();
    }

    public FinalCastleDungeonExitComponent(int i, int x, int y, int z, Direction direction, int level) {
        super(FinaleCastlePieces.TFFCDunEx.get(), i, x, y, z, direction, level);
    }

    @Override
    public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
        if (parent instanceof TFStructureComponentOld) {
            this.deco = ((TFStructureComponentOld) parent).deco;
        }

        // no need for additional rooms, we're along the outside anyways

        // add stairway down
        Rotation bestDir = this.findStairDirectionTowards(parent.getBoundingBox().minX(), parent.getBoundingBox().minZ());

        BlockPos center = FinaleCastleInteriors.local(this, 15, 0, 15);
        FinalCastleDungeonStepsComponent steps0 = new FinalCastleDungeonStepsComponent(5, center.getX(), center.getY(), center.getZ(), bestDir.rotate(Direction.SOUTH));
        if (!CastleGenerationBounds.allows(list, steps0)) return;
        this.descent = steps0;
        list.addPiece(steps0);
        steps0.addChildren(this, list, rand);

        // another level!?
        if (this.level == 1) {
            this.lowerRoom = steps0.buildLevelUnder(list, rand, this.level + 1);
        } else {
            this.lowerRoom = steps0.buildBossRoomUnder(list, rand);
        }
    }

    boolean hasConnectedDescent() {
        if (descent == null || lowerRoom == null) return false;
        BlockPos top = FinaleCastleInteriors.local(descent, 2, 15, 0);
        if (!boundingBox.isInside(top)) return false;
        BlockPos bottom = FinaleCastleInteriors.local(descent, 2, 0, 15);
        if (level == 1 && lowerRoom instanceof FinalCastleDungeonEntranceComponent entrance) {
            BlockPos ramp = FinaleCastleInteriors.local(entrance, 15, entrance.getBoundingBox().getYSpan(), 10);
            return entrance.level == 2 && bottom.distManhattan(ramp) == 1;
        }
        if (level == 2 && lowerRoom instanceof FinalCastleDungeonForgeRoomComponent) {
            BlockPos entry = FinaleCastleInteriors.local(lowerRoom, 15, 31, 11);
            return bottom.equals(entry);
        }
        return false;
    }

    boolean hasCompleteDescent() {
        return hasConnectedDescent() && (level == 2
                ? lowerRoom instanceof FinalCastleDungeonForgeRoomComponent
                : lowerRoom instanceof FinalCastleDungeonEntranceComponent entrance && entrance.hasExit);
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {

        super.postProcess(world, manager, generator, rand, sbb, chunkPosIn, blockPos);

        // No reappearing door on either lower-level exit; keep the barrier intact.
        final BlockState castleDoor = TFBlocks.PINK_FORCE_FIELD.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.PipeBlock.UP, true).setValue(net.minecraft.world.level.block.PipeBlock.DOWN, true)
                .setValue(net.minecraft.world.level.block.PipeBlock.NORTH, true).setValue(net.minecraft.world.level.block.PipeBlock.SOUTH, true);

        this.generateBox(world, sbb, 7, 0, 16, 7, 3, 18, castleDoor, AIR, false);
        this.generateBox(world, sbb, 7, 4, 16, 7, 4, 18, deco.blockState, deco.blockState, false);
    }

    public Rotation findStairDirectionTowards(int x, int z) {
        Vec3i center = BoundingBoxUtils.getCenter(this.boundingBox);
        // difference
        int dx = center.getX() - x;
        int dz = center.getZ() - z;

        Rotation absoluteDir;
        if (Math.abs(dz) >= Math.abs(dx)) {
            absoluteDir = (dz >= 0) ? Rotation.CLOCKWISE_180 : Rotation.NONE;
        } else {
            absoluteDir = (dx >= 0) ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        }

        return absoluteDir;
    }

    @Override
    protected BlockState getForceFieldColor(RandomSource decoRNG) {
        return TFBlocks.PINK_FORCE_FIELD.get().defaultBlockState();
    }

    @Override
    protected BlockState getRuneColor(BlockState fieldColor) {
        return TFBlocks.PINK_CASTLE_RUNE_BRICK.get().defaultBlockState();
    }
}
