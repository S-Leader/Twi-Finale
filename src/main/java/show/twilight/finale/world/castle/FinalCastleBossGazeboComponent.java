package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;
import twilightforest.util.RotationUtil;
import twilightforest.world.components.structures.TFStructureComponentOld;
import twilightforest.world.components.structures.util.StructureTemplateDefinitions;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FinalCastleBossGazeboComponent extends TFStructureComponentOld {

    public static final ResourceLocation GAZEBO_TEMP_POOL = TwilightForestMod.prefix("final_castle/temp/gazebo");

    private static StructureTemplateDefinitions structureTemplateDefinitions;

    @SuppressWarnings("unused")
    public FinalCastleBossGazeboComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        super(FinaleCastlePieces.TFFCBoGaz.get(), nbt);
        this.deco = new StructureTFDecoratorCastle();
    }

    @SuppressWarnings("this-escape")
    public FinalCastleBossGazeboComponent(int i, TFStructureComponentOld keep, int x, int y, int z) {
        super(FinaleCastlePieces.TFFCBoGaz.get(), i, x, y, z);
        this.spawnListIndex = -1; // no monsters

        this.setOrientation(keep.getOrientation());
        this.boundingBox = new BoundingBox(keep.getBoundingBox().minX() + 14, keep.getBoundingBox().maxY() + 2, keep.getBoundingBox().minZ() + 14, keep.getBoundingBox().maxX() - 14, keep.getBoundingBox().maxY() + 13, keep.getBoundingBox().maxZ() - 14);

    }

    @Override
    public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
        this.deco = new StructureTFDecoratorCastle();
        this.deco.blockState = TFBlocks.VIOLET_CASTLE_RUNE_BRICK.get().defaultBlockState();

        this.deco.fenceState = TFBlocks.VIOLET_FORCE_FIELD.get().defaultBlockState();

    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource randomIn, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
        BlockState state = ((Block) TFBlocks.VIOLET_FORCE_FIELD.get()).defaultBlockState();
        for (Rotation rotation : RotationUtil.ROTATIONS) {
            this.fillBlocksRotated(world, sbb, 0, 0, 0, 0, 10, 20, state, rotation);
        }
        this.generateBox(world, sbb, 1, 11, 0, 19, 11, 20, state, state, false);
        this.generateBox(world, sbb, 0, 11, 0, 0, 11, 20, state, state, false);
        this.generateBox(world, sbb, 20, 11, 0, 20, 11, 20, state, state, false);
        BlockState blockState = ((StairBlock) TFBlocks.ENCASED_CASTLE_BRICK_STAIRS.get()).defaultBlockState();
        this.placeBlock(world, (BlockState) ((BlockState) blockState.setValue(StairBlock.FACING, Direction.NORTH)).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 9, 0, 9, sbb);
        this.placeBlock(world, (BlockState) ((BlockState) blockState.setValue(StairBlock.FACING, Direction.NORTH)).setValue(StairBlock.SHAPE, StairsShape.STRAIGHT), 10, 0, 9, sbb);
        this.placeBlock(world, (BlockState) ((BlockState) blockState.setValue(StairBlock.FACING, Direction.EAST)).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 11, 0, 9, sbb);
        this.placeBlock(world, (BlockState) ((BlockState) blockState.setValue(StairBlock.FACING, Direction.WEST)).setValue(StairBlock.SHAPE, StairsShape.STRAIGHT), 9, 0, 10, sbb);
        this.placeBlock(world, (BlockState) ((BlockState) blockState.setValue(StairBlock.FACING, Direction.EAST)).setValue(StairBlock.SHAPE, StairsShape.STRAIGHT), 11, 0, 10, sbb);
        this.placeBlock(world, (BlockState) ((BlockState) blockState.setValue(StairBlock.FACING, Direction.WEST)).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 9, 0, 11, sbb);
        this.placeBlock(world, (BlockState) ((BlockState) blockState.setValue(StairBlock.FACING, Direction.SOUTH)).setValue(StairBlock.SHAPE, StairsShape.STRAIGHT), 10, 0, 11, sbb);
        this.placeBlock(world, (BlockState) ((BlockState) blockState.setValue(StairBlock.FACING, Direction.SOUTH)).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 11, 0, 11, sbb);
        this.placeBlock(world, TFBlocks.CASTLE_BRICK.get().defaultBlockState(), 10, 0, 10, sbb);
        // Native spawning/reset uses this raised home, above a single castle-brick pedestal.
        // The arena itself remains stationary; activation only opens the violet fields.
        this.placeBlock(world, show.twilight.finale.init.ModBlocks.TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get().defaultBlockState(), 10, 1, 10, sbb);

    }

    public static final String INTERACTION_TAG = "final_castle_wip";

}
