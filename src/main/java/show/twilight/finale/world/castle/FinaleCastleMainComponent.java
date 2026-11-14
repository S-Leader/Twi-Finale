package show.twilight.finale.world.castle;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import twilightforest.init.TFBlocks;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.util.RotationUtil;
import twilightforest.world.components.structures.TFStructureComponentOld;

import show.twilight.finale.TwilightFinaleMod;

public class FinaleCastleMainComponent extends TFStructureComponentOld {
    private List<StructurePiece> preparedChildren;
    private Structure.GenerationContext terrainContext;

    boolean allowsDungeonPiece(BoundingBox box) {
        if (terrainContext == null) return true;
        return CastleGenerationBounds.inReferenceWindow(terrainContext.chunkPos(), box)
                && box.minY() - 8 >= terrainContext.heightAccessor().getMinBuildHeight()
                && box.maxY() + 8 < terrainContext.heightAccessor().getMaxBuildHeight();
    }

    int groundAt(int x, int z) {
        if (terrainContext == null) return 127;
        return terrainContext.chunkGenerator().getFirstOccupiedHeight(x, z,
                Heightmap.Types.OCEAN_FLOOR_WG,
                terrainContext.heightAccessor(), terrainContext.randomState());
    }

    public boolean prepareLayout(Structure.GenerationContext context, RandomSource random) {
        terrainContext = context;

        int surface = boundingBox.minY();
        for (int dx = -20; dx <= 20; dx += 10)
            for (int dz = -20; dz <= 20; dz += 10) {
                surface = Math.max(surface, groundAt(boundingBox.getCenter().getX() + dx, boundingBox.getCenter().getZ() + dz) + 1);
            }
        move(0, surface - boundingBox.minY(), 0);
        for (int attempt = 0; attempt < 6; attempt++) {
            StructurePiecesBuilder trial = new StructurePiecesBuilder();
            trial.addPiece(this);
            buildLegacyChildren(this, trial, random);
            long entrances = trial.pieces.stream().filter(p -> p instanceof FinalCastleEntranceTowerComponent).count();
            long bells = trial.pieces.stream().filter(p -> p instanceof FinalCastleBellTower21Component).count();
            long forges = trial.pieces.stream().filter(p -> p instanceof FinalCastleDungeonForgeRoomComponent).count();
            long exits = trial.pieces.stream().filter(p -> p instanceof FinalCastleDungeonExitComponent).count();
            if (entrances != 1 || bells != 1 || forges != 1 || exits != 2) continue;
            if (!hasCompleteDungeon(trial.pieces)) continue;

            if (trial.pieces.stream().filter(p -> p instanceof FinalCastleDungeonRoom31Component
                            || p instanceof FinalCastleDungeonStepsComponent || p instanceof FinalCastleDungeonForgeRoomComponent)
                    .anyMatch(p -> !allowsDungeonPiece(p.getBoundingBox()))) continue;
            FinaleCastleInteriors.append(trial, context.structureTemplateManager(), context.seed());
            BoundingBox bounds = trial.getBoundingBox();
            if (bounds.minY() - 8 < context.heightAccessor().getMinBuildHeight()
                    || bounds.maxY() + 8 >= context.heightAccessor().getMaxBuildHeight()) continue;
            preparedChildren = List.copyOf(trial.pieces.subList(1, trial.pieces.size()));
            terrainContext = null;
            return true;
        }
        terrainContext = null;
        TwilightFinaleMod.LOGGER.warn("Skipped incomplete, out-of-reference-range or height-incompatible castle layout at {}, {} (main floor Y={})", boundingBox.minX(), boundingBox.minZ(), boundingBox.minY());
        return false;
    }

    private static boolean hasCompleteDungeon(List<StructurePiece> pieces) {
        for (int level = 1; level <= 2; level++) {
            final int floor = level;
            if (pieces.stream().filter(p -> p instanceof FinalCastleDungeonEntranceComponent room && room.level == floor).count() != 1)
                return false;
            var exits = pieces.stream().filter(p -> p instanceof FinalCastleDungeonExitComponent room && room.level == floor)
                    .map(p -> (FinalCastleDungeonExitComponent) p).toList();
            if (exits.size() != 1 || !exits.getFirst().hasConnectedDescent()) return false;
            if (pieces.stream().filter(p -> p.getClass() == FinalCastleDungeonRoom31Component.class
                    && ((FinalCastleDungeonRoom31Component) p).level == floor).count() < (level == 1 ? 2 : 3))
                return false;
        }
        return true;
    }

    public FinaleCastleMainComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
        super(FinaleCastlePieces.TFFCMain.get(), nbt);
        this.deco = new StructureTFDecoratorCastle();
    }

    @SuppressWarnings("this-escape")
    public FinaleCastleMainComponent(int i, int x, int y, int z) {
        super(FinaleCastlePieces.TFFCMain.get(), i, x, y, z);
        this.setOrientation(Direction.SOUTH);
        this.spawnListIndex = 1;

        this.boundingBox = BoundingBoxUtils.getComponentToAddBoundingBox(x, y, z, -24, 120, -24, 48, 40, 48, Direction.SOUTH, false);

        this.deco = new StructureTFDecoratorCastle();
    }

    @Override
    public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
        if (preparedChildren == null) return;
        for (StructurePiece child : preparedChildren) list.addPiece(child);
    }

    private void buildLegacyChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {

        FinalCastleFoundation48Component foundation = new FinalCastleFoundation48Component(4, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(foundation);
        foundation.addChildren(this, list, rand);

        TFStructureComponentOld roof = new FinaleCastleRoof48CrenellatedComponent(4, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(roof);
        roof.addChildren(this, list, rand);

        TFStructureComponentOld gazebo = new FinalCastleBossGazeboComponent(5, this, getLocatorPosition().getX(), getLocatorPosition().getY(), getLocatorPosition().getZ());
        list.addPiece(gazebo);
        gazebo.addChildren(this, list, rand);

        FinalCastleStairTowerComponent tower0 = new FinalCastleStairTowerComponent(3, boundingBox.minX(), boundingBox.minY() + 3, boundingBox.minZ(), Direction.NORTH);
        list.addPiece(tower0);
        tower0.addChildren(this, list, rand);

        FinalCastleLargeTowerComponent tower1 = new FinalCastleLargeTowerComponent(3, boundingBox.maxX(), boundingBox.minY() + 3, boundingBox.minZ(), Direction.EAST);
        list.addPiece(tower1);
        tower1.addChildren(this, list, rand);

        FinalCastleStairTowerComponent tower2 = new FinalCastleStairTowerComponent(3, boundingBox.minX(), boundingBox.minY() + 3, boundingBox.maxZ(), Direction.WEST);
        list.addPiece(tower2);
        tower2.addChildren(this, list, rand);

        FinalCastleStairTowerComponent tower3 = new FinalCastleStairTowerComponent(3, boundingBox.maxX(), boundingBox.minY() + 3, boundingBox.maxZ(), Direction.SOUTH);
        list.addPiece(tower3);
        tower3.addChildren(this, list, rand);

        BlockPos dest = new BlockPos(boundingBox.minX() - 4, boundingBox.maxY(), boundingBox.minZ() - 24);
        buildTowerMaze(list, rand, 48, 0, 24, 60, Direction.SOUTH, TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get().defaultBlockState(), dest);

        dest = new BlockPos(boundingBox.maxX() + 4, boundingBox.minY(), boundingBox.maxZ() + 24);
        buildTowerMaze(list, rand, 0, 30, 24, 60, Direction.NORTH, TFBlocks.BLUE_CASTLE_RUNE_BRICK.get().defaultBlockState(), dest);

        FinalCastleDungeonStepsComponent steps0 = new FinalCastleDungeonStepsComponent(5, boundingBox.minX() + 18, boundingBox.minY() + 1, boundingBox.minZ() + 18, Direction.SOUTH);
        list.addPiece(steps0);
        steps0.addChildren(this, list, rand);

        FinalCastleDungeonStepsComponent steps1 = steps0.buildMoreStepsTowards(list, rand, Rotation.COUNTERCLOCKWISE_90);
        FinalCastleDungeonStepsComponent steps2 = steps1.buildMoreStepsTowards(list, rand, Rotation.COUNTERCLOCKWISE_90);
        FinalCastleDungeonStepsComponent steps3 = steps2.buildMoreStepsTowards(list, rand, Rotation.COUNTERCLOCKWISE_90);

        steps3.buildLevelUnder(list, rand, 1);

        BlockPos mc = this.offsetTowerCCoords(48, 23, 25, 1, Direction.SOUTH);
        FinalCastleMuralComponent mural0 = new FinalCastleMuralComponent(7, mc.getX(), mc.getY(), mc.getZ(), 35, 30, Direction.SOUTH);
        list.addPiece(mural0);
        mural0.addChildren(this, list, rand);

        BlockPos mc1 = this.offsetTowerCCoords(48, 33, 24, -1, Direction.SOUTH);
        FinalCastleMuralComponent mural1 = new FinalCastleMuralComponent(7, mc1.getX(), mc1.getY(), mc1.getZ(), 19, 12, Direction.NORTH);
        list.addPiece(mural1);
        mural1.addChildren(this, list, rand);

    }

    private void buildTowerMaze(StructurePieceAccessor list, RandomSource rand, int x, int y, int z, int howFar, Direction direction, BlockState type, BlockPos dest) {
        if (list instanceof StructurePiecesBuilder start) {
            boolean complete = false;
            int iterations = 0;
            while (!complete && iterations < 32) {
                iterations++;

                List<StructurePiece> before = new LinkedList<>(start.pieces);

                BlockPos tc = this.offsetTowerCCoords(x, y, z, howFar, direction);
                FinalCastleMazeTower13Component sTower = new FinalCastleMazeTower13Component(FinaleCastlePieces.TFFCSiTo.get(), rand, 3, tc.getX(), tc.getY(), tc.getZ(), type, direction);

                BlockPos bc = this.offsetTowerCCoords(x, y, z, 1, direction);
                FinalCastleBridgeComponent bridge = new FinalCastleBridgeComponent(this.getGenDepth() + 1, bc.getX(), bc.getY(), bc.getZ(), howFar - 7, direction);

                list.addPiece(bridge);
                bridge.addChildren(this, list, rand);

                list.addPiece(sTower);
                sTower.buildTowards(this, list, rand, dest);

                if (this.isMazeComplete(list, type)) {

                    complete = true;
                } else {

                    start.pieces.clear();
                    start.pieces.addAll(before);
                }
            }
        }
    }

    private boolean isMazeComplete(StructurePieceAccessor list, BlockState type) {
        if (list instanceof StructurePiecesBuilder start) {
            if (start.pieces.size() > 60) {

            }
            for (StructurePiece structurecomponent : start.pieces) {
                BoundingBox boundingBox = structurecomponent.getBoundingBox();
                int x = boundingBox.getCenter().getX();
                int y = boundingBox.getCenter().getY();
                int z = boundingBox.getCenter().getZ();

                if (type == TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get().defaultBlockState() && structurecomponent instanceof FinalCastleEntranceTowerComponent) {
                    return true;
                }
                if (type == TFBlocks.BLUE_CASTLE_RUNE_BRICK.get().defaultBlockState() && structurecomponent instanceof FinalCastleBellTower21Component) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected BlockPos offsetTowerCCoords(int x, int y, int z, int howFar, Direction direction) {

        int dx = getWorldX(x, z);
        int dy = getWorldY(y);
        int dz = getWorldZ(x, z);

        switch (direction) {
            case SOUTH -> dx += howFar;
            case WEST -> dz += howFar;
            case NORTH -> dx -= howFar;
            case EAST -> dz -= howFar;
            default -> {
            }
        }

        return new BlockPos(dx, dy, dz);
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {

        generateBox(world, sbb, 0, 0, 0, 48, 40, 48, false, rand, deco.randomBlocks);

        generateBox(world, sbb, 13, 30, 1, 47, 30, 12, false, rand, deco.randomBlocks);
        this.generateBox(world, sbb, 13, 31, 12, 36, 31, 12, deco.fenceState, deco.fenceState, false);
        generateBox(world, sbb, 13, 30, 36, 47, 30, 47, false, rand, deco.randomBlocks);
        this.generateBox(world, sbb, 13, 31, 36, 36, 31, 36, deco.fenceState, deco.fenceState, false);
        generateBox(world, sbb, 1, 30, 1, 12, 30, 47, false, rand, deco.randomBlocks);
        this.generateBox(world, sbb, 12, 31, 12, 12, 31, 36, deco.fenceState, deco.fenceState, false);

        generateBox(world, sbb, 38, 25, 13, 47, 25, 35, false, rand, deco.randomBlocks);

        for (int i = 0; i < 5; i++) {
            int y = 30 - i;

            makeMezzTopStairs(world, sbb, y, 10 + i, Direction.SOUTH);
            makeMezzTopStairs(world, sbb, y, 38 - i, Direction.NORTH);

            y = 25 - i;
            int x = 37 - i;
            final BlockState stairState = getStairState(deco.stairState, Direction.WEST, false);
            this.generateBox(world, sbb, x, y, 14, x, y, 22, stairState, stairState, false);
            this.generateBox(world, sbb, x, y - 1, 14, x, y - 1, 22, deco.blockState, deco.blockState, false);
            this.generateBox(world, sbb, x, y, 26, x, y, 34, stairState, stairState, false);
            this.generateBox(world, sbb, x, y - 1, 26, x, y - 1, 34, deco.blockState, deco.blockState, false);
        }

        for (int x = 11; x < 47; x += 12) {
            for (int z = 11; z < 47; z += 12) {
                this.generateBox(world, sbb, x, 1, z, x + 2, 40, z + 2, deco.pillarState, deco.blockState, false);

                makePillarBase(world, sbb, x, z, 1, false);
                makePillarBase(world, sbb, x, z, 19, true);
                makePillarBase(world, sbb, x, z, 21, false);
                makePillarBase(world, sbb, x, z, 39, true);
            }
        }

        for (Rotation rotation : RotationUtil.ROTATIONS) {
            for (int z = 11; z < 47; z += 12) {

                if (z == 23 && (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180)) {
                    continue;
                }

                this.fillBlocksRotated(world, sbb, 1, 1, z, 1, 40, z + 2, deco.pillarState, rotation);
                makeHalfPillarBase(world, sbb, rotation, 1, z, false);
                makeHalfPillarBase(world, sbb, rotation, 19, z, true);
                makeHalfPillarBase(world, sbb, rotation, 21, z, false);
                makeHalfPillarBase(world, sbb, rotation, 39, z, true);
            }
        }

        generateBox(world, sbb, 1, 20, 1, 47, 20, 47, false, rand, deco.randomBlocks);

        BlockState fieldBlock = TFBlocks.PINK_FORCE_FIELD.get().defaultBlockState();
        this.generateBox(world, sbb, 12, 1, 12, 24, 10, 12, fieldBlock, fieldBlock, false);
        this.generateBox(world, sbb, 12, 1, 12, 12, 10, 24, fieldBlock, fieldBlock, false);
        this.generateBox(world, sbb, 24, 1, 12, 24, 10, 24, fieldBlock, fieldBlock, false);
        this.generateBox(world, sbb, 12, 1, 24, 24, 10, 24, fieldBlock, fieldBlock, false);
        this.generateBox(world, sbb, 13, 10, 12, 23, 10, 24, fieldBlock, fieldBlock, false);
        this.generateBox(world, sbb, 12, 10, 12, 12, 10, 24, fieldBlock, fieldBlock, false);
        this.generateBox(world, sbb, 24, 10, 12, 24, 10, 24, fieldBlock, fieldBlock, false);

        this.generateBox(world, sbb, 17, 1, 12, 19, 4, 12, TFBlocks.PINK_CASTLE_DOOR.get().defaultBlockState(), AIR, false);
        this.generateBox(world, sbb, 17, 1, 24, 19, 4, 24, TFBlocks.PINK_CASTLE_DOOR.get().defaultBlockState(), AIR, false);

        makeSmallTowerStairs(world, sbb, Rotation.NONE);
        makeSmallTowerStairs(world, sbb, Rotation.CLOCKWISE_90);
        makeSmallTowerStairs(world, sbb, Rotation.COUNTERCLOCKWISE_90);
        makeLargeTowerStairs(world, sbb, Rotation.CLOCKWISE_180);

        this.generateBox(world, sbb, 48, 1, 23, 48, 4, 25, TFBlocks.YELLOW_CASTLE_DOOR.get().defaultBlockState(), AIR, false);

        this.generateBox(world, sbb, 0, 31, 23, 0, 34, 25, TFBlocks.BLUE_CASTLE_DOOR.get().defaultBlockState(), AIR, false);
    }

    private void makeSmallTowerStairs(WorldGenLevel world, BoundingBox sbb, Rotation rotation) {
        for (int y = 1; y < 4; y++) {
            int z = 40 + y;
            this.fillBlocksRotated(world, sbb, 1, 1, z, 4, y, z, deco.blockState, rotation);

            this.fillBlocksRotated(world, sbb, 2, y, z, 3, y, z, getStairState(deco.stairState, Direction.NORTH, false), rotation);
        }
    }

    private void makeLargeTowerStairs(WorldGenLevel world, BoundingBox sbb, Rotation rotation) {
        final BlockState stairState = getStairState(deco.stairState, Direction.NORTH, false);
        for (int y = 1; y < 4; y++) {
            int z = 38 + y;
            this.fillBlocksRotated(world, sbb, 2, 1, z, 6, y, z, deco.blockState, rotation);
            this.fillBlocksRotated(world, sbb, 3, y, z, 5, y, z, stairState, rotation);
        }
    }

    private void makeMezzTopStairs(WorldGenLevel world, BoundingBox sbb, int y, int z, Direction stairMeta) {
        final BlockState stairState = getStairState(deco.stairState, stairMeta, false);
        this.generateBox(world, sbb, 38, y, z, 46, y, z, stairState, stairState, false);
        this.generateBox(world, sbb, 38, y - 1, z, 46, y - 1, z, deco.blockState, deco.blockState, false);
        this.generateAirBox(world, sbb, 38, y + 1, z, 46, y + 3, z);
    }

    private void makeHalfPillarBase(WorldGenLevel world, BoundingBox sbb, Rotation rotation, int y, int z, boolean isFlipped) {
        this.fillBlocksRotated(world, sbb, 2, y, z - 1, 2, y, z + 3, getStairState(deco.stairState, Direction.EAST, isFlipped), rotation);
        this.setBlockStateRotated(world, getStairState(deco.stairState, Direction.NORTH, isFlipped), 1, y, z - 1, rotation, sbb);
        this.setBlockStateRotated(world, getStairState(deco.stairState, Direction.SOUTH, isFlipped), 1, y, z + 3, rotation, sbb);
    }

    private void makePillarBase(WorldGenLevel world, BoundingBox sbb, int x, int z, int y, boolean isFlipped) {
        this.generateBox(world, sbb, x, y, z + 3, x + 3, y, z + 3, getStairState(deco.stairState, Direction.SOUTH, isFlipped), AIR, false);
        this.generateBox(world, sbb, x - 1, y, z - 1, x + 2, y, z - 1, getStairState(deco.stairState, Direction.NORTH, isFlipped), AIR, false);

        this.generateBox(world, sbb, x + 3, y, z - 1, x + 3, y, z + 2, getStairState(deco.stairState, Direction.EAST, isFlipped), AIR, false);
        this.generateBox(world, sbb, x - 1, y, z, x - 1, y, z + 3, getStairState(deco.stairState, Direction.WEST, isFlipped), AIR, false);
    }
}
