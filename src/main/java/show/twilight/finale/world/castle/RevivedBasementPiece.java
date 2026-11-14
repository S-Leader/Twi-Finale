package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.world.components.structures.TFStructureComponentOld;

import show.twilight.finale.init.ModBlocks;

public final class RevivedBasementPiece extends TFStructureComponentOld {
    final BlockPos center;
    final int floor, cell, type, turns, links;
    final boolean entrance, exit;
    private final long lootSeed;

    RevivedBasementPiece(BlockPos center, int floor, int cell, int type, int turns, int links, boolean entrance, boolean exit, long seed) {
        super(FinaleCastlePieces.REVIVED_BASEMENT.get(), 0, 0, 0, 0);
        this.center = center;
        this.floor = floor;
        this.cell = cell;
        this.type = type;
        this.turns = turns;
        this.links = links;
        this.entrance = entrance;
        this.exit = exit;
        lootSeed = seed;
        boundingBox = new BoundingBox(center.getX() - 8, center.getY() - (type == RevivedBasementTemplate.PIT ? 11 : 0), center.getZ() - 8,
                center.getX() + 8, center.getY() + 8, center.getZ() + 8);
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 2;
    }

    public RevivedBasementPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(FinaleCastlePieces.REVIVED_BASEMENT.get(), tag);
        center = BlockPos.of(tag.getLong("RoomCenter"));
        floor = tag.getInt("BasementLevel");
        cell = tag.getInt("Cell");
        type = tag.getInt("RoomType");
        turns = tag.getInt("Turns");
        links = tag.getInt("Links");
        entrance = tag.getBoolean("Entrance");
        exit = tag.getBoolean("Exit");
        lootSeed = tag.getLong("LootSeed");
        if (tag.getInt("FinaleBasementVersion") != 3 || floor < 1 || floor > 2 || cell < 0 || cell >= 25
                || type < -1 || type >= RevivedBasementTemplate.COUNT || turns < 0 || turns > 3 || links < 0 || links > 15)
            throw new IllegalArgumentException("Invalid randomized basement room");
        deco = new StructureTFDecoratorCastle();
        spawnListIndex = 2;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putInt("FinaleBasementVersion", 3);
        tag.putLong("RoomCenter", center.asLong());
        tag.putInt("BasementLevel", floor);
        tag.putInt("Cell", cell);
        tag.putInt("RoomType", type);
        tag.putInt("Turns", turns);
        tag.putInt("Links", links);
        tag.putBoolean("Entrance", entrance);
        tag.putBoolean("Exit", exit);
        tag.putLong("LootSeed", lootSeed);
    }

    private BlockPos at(int x, int y, int z) {
        return center.offset(x - 8, y, z - 8);
    }

    private void block(WorldGenLevel level, BoundingBox clip, int x, int y, int z, BlockState state) {
        BlockPos pos = at(x, y, z);
        if (clip.isInside(pos)) level.setBlock(pos, state, 18);
    }

    private BlockPos rotated(int x, int y, int z) {
        for (int i = 0; i < turns; i++) {
            int old = x;
            x = 16 - z;
            z = old;
        }
        return at(x, y, z);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
                            BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        int bottom = type == RevivedBasementTemplate.PIT ? -11 : 0;
        BlockState brick = TFBlocks.CASTLE_BRICK.get().defaultBlockState();
        for (int x = 0; x <= 16; x++)
            for (int z = 0; z <= 16; z++)
                for (int y = bottom; y <= 8; y++) {
                    BlockState state = y == bottom || y == 8 || x == 0 || x == 16 || z == 0 || z == 16 ? brick : AIR;
                    if (type < 0) state = TFBlocks.DEADROCK.get().defaultBlockState();
                    block(level, clip, x, y, z, state);
                }
        if (type >= 0) {
            var entries = RevivedBasementTemplate.get(level.getLevel().getServer().getStructureManager(), level.registryAccess()).rooms().get(type);
            Rotation rotation = Rotation.values()[turns];
            for (var e : entries) {
                BlockPos pos = rotated(e.x(), e.y(), e.z());
                if (!clip.isInside(pos)) continue;
                BlockState state = e.state().rotate(rotation);

                if (state.getBlock().getDescriptionId().contains("castle_door")) state = brick;
                if ((e.x() == 0 || e.x() == 16 || e.z() == 0 || e.z() == 16) && state.isAir()) state = brick;
                boolean bossSpawner = BasementBossProcessor.isBlazeSpawner(state, e.data());
                if (bossSpawner)
                    state = ModBlocks.ANNIHILATION_GOLEM_BOSS_SPAWNER.get().defaultBlockState();
                level.setBlock(pos, state, 18);
                if (!bossSpawner && e.data() != null && state.hasBlockEntity()) {
                    var entity = level.getBlockEntity(pos);
                    if (entity == null)
                        throw new IllegalStateException("Missing randomized room block entity at " + pos);
                    CompoundTag data = e.data().copy();
                    data.putInt("x", pos.getX());
                    data.putInt("y", pos.getY());
                    data.putInt("z", pos.getZ());
                    if (data.contains("LootTable")) data.putLong("LootTableSeed", lootSeed ^ pos.asLong());
                    entity.loadWithComponents(data, level.registryAccess());
                    entity.setChanged();
                }
            }
        } else {
            for (int x = 0; x <= 16; x++)
                for (int z = 0; z <= 16; z++)
                    for (int y = 0; y <= 5; y++) {
                        boolean path = Math.abs(x - 8) <= 1 && Math.abs(z - 8) <= 1
                                || Math.abs(x - 8) <= 1 && ((links & 1) != 0 && z <= 8 || (links & 4) != 0 && z >= 8)
                                || Math.abs(z - 8) <= 1 && ((links & 2) != 0 && x >= 8 || (links & 8) != 0 && x <= 8);
                        block(level, clip, x, y, z, path && y > 0 && y < 5 ? AIR : brick);
                    }
            block(level, clip, 8, 5, 8, Blocks.SEA_LANTERN.defaultBlockState());
        }
        for (int d = 0; type >= 0 && d < 4; d++)
            if ((links & (1 << d)) != 0) {
                int length = 8;
                for (int s = 0; s <= length; s++)
                    for (int w = -2; w <= 2; w++)
                        for (int y = 0; y <= 5; y++) {
                            int x = d == 1 ? 16 - s : d == 3 ? s : 8 + w, z = d == 0 ? s : d == 2 ? 16 - s : 8 + w;
                            if (Math.abs(w) <= 1 && y > 0 && y < 5) block(level, clip, x, y, z, AIR);
                            else if (y == 0 && type == RevivedBasementTemplate.PIT || s == 0 && (y == 0 || y == 5 || Math.abs(w) == 2))
                                block(level, clip, x, y, z, brick);
                        }
            }
    }
}
