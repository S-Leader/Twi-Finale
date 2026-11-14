package show.twilight.finale.world;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import twilightforest.world.components.structures.finalcastle.FinalCastleMainComponent;
import twilightforest.world.components.structures.finalcastle.FinalCastleRoof48CrenellatedComponent;

import show.twilight.finale.config.HierophantConfig;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.world.castle.FinaleCastleMainComponent;
import show.twilight.finale.world.castle.FinaleCastleRoof48CrenellatedComponent;

@EventBusSubscriber(modid = "twilightforest_finale")
public final class HierophantArena extends SavedData {

    public static final int RADIUS = 20;
    private static final int GAZEBO_RADIUS = 10, GAZEBO_HEIGHT = 12;
    private static final int LEGACY_RISE = 20, LEGACY_DURATION = 160;
    private static final TagKey<Block> BARRIERS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "hierophant_arena_barriers"));
    private final Map<Long, Lift> lifts = new HashMap<>();

    private record Cell(BlockPos source, BlockState state, CompoundTag blockEntity) {
    }

    public static final class Lift {
        public final BlockPos sourceHome;
        public final int radius;
        private final int floorOffset;
        private final boolean legacy;
        private AABB roofBounds;
        private boolean verifiedRoofBounds;
        private final List<Cell> recoveryCells = new ArrayList<>();
        public UUID boss;
        private boolean barriersCleared, recoverFloor, defeated;

        private Lift(BlockPos home, UUID boss, int radius, int floorOffset, boolean legacy) {
            this.sourceHome = home;
            this.boss = boss;
            this.radius = radius;
            this.floorOffset = floorOffset;
            this.legacy = legacy;
        }

        public BlockPos home() {
            return sourceHome.above(floorOffset);
        }

        public double offset() {
            return floorOffset;
        }

        public boolean ready() {
            return barriersCleared && !recoverFloor;
        }

        public AABB bounds() {
            if (roofBounds != null) return roofBounds;

            int fallback = HierophantConfig.ARENA_RADIUS.get();
            double floor = home().getY();
            return new AABB(sourceHome.getX() - fallback, floor - 2, sourceHome.getZ() - fallback,
                    sourceHome.getX() + fallback + 1, floor + 24, sourceHome.getZ() + fallback + 1);
        }
    }

    public static HierophantArena get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(HierophantArena::new,
                HierophantArena::load), "twilightforest_finale_hierophant_arenas");
    }

    public Lift find(BlockPos home) {
        Lift exact = lifts.get(home.asLong());
        if (exact != null) return exact;
        for (Lift arena : lifts.values()) if (arena.home().equals(home)) return arena;

        for (Lift arena : lifts.values())
            if (arena.legacy && arena.sourceHome.above(LEGACY_RISE).equals(home)) return arena;
        return null;
    }

    public Lift attach(ServerLevel level, TheTwilightHierophantEntity boss, BlockPos home) {
        Lift arena = find(home);
        if (arena == null) {

            arena = new Lift(home.immutable(), boss.getUUID(), RADIUS, 0, false);
            lifts.put(home.asLong(), arena);
            setDirty();
        } else if (!boss.getUUID().equals(arena.boss)) {
            arena.boss = boss.getUUID();
            arena.defeated = false;
            arena.barriersCleared = false;
            setDirty();
        }
        GlobalPos restriction = GlobalPos.of(level.dimension(), arena.home());
        if (!restriction.equals(boss.getRestrictionPoint())) boss.setRestrictionPoint(restriction);
        refreshBounds(level, arena);
        prepare(level, arena);
        return arena;
    }

    public void refreshBounds(ServerLevel level, Lift arena) {

        if (arena.roofBounds != null && arena.verifiedRoofBounds) return;
        BlockPos home = arena.sourceHome;
        var chunk = level.getChunkSource().getChunkNow(home.getX() >> 4, home.getZ() >> 4);
        if (chunk == null) return;
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceLocation castle = ResourceLocation.fromNamespaceAndPath("twilightforest", "final_castle");
        Set<StructureStart> starts = new HashSet<>();
        for (var entry : chunk.getAllStarts().entrySet())
            if (castle.equals(registry.getKey(entry.getKey()))) starts.add(entry.getValue());
        for (var entry : chunk.getAllReferences().entrySet())
            if (castle.equals(registry.getKey(entry.getKey()))) for (long reference : entry.getValue()) {
                ChunkPos at = new ChunkPos(reference);
                var origin = level.getChunkSource().getChunkNow(at.x, at.z);
                if (origin == null) continue;
                StructureStart start = origin.getStartForStructure(entry.getKey());
                if (start != null) starts.add(start);
            }
        AABB found = null;
        double nearestFloor = Double.POSITIVE_INFINITY;
        for (StructureStart start : starts)
            for (var piece : start.getPieces()) {
                var box = piece.getBoundingBox();
                int minX, minZ, maxX, maxZ, floor;
                if (piece instanceof FinaleCastleRoof48CrenellatedComponent
                        || piece instanceof FinalCastleRoof48CrenellatedComponent) {

                    minX = box.minX() + 2;
                    minZ = box.minZ() + 2;
                    maxX = box.maxX() - 1;
                    maxZ = box.maxZ() - 1;
                    floor = box.minY() + 3;
                } else if (piece instanceof FinaleCastleMainComponent
                        || piece instanceof FinalCastleMainComponent) {
                    minX = box.minX();
                    minZ = box.minZ();
                    maxX = box.maxX() + 1;
                    maxZ = box.maxZ() + 1;
                    floor = box.maxY() + 2;
                } else continue;
                double difference = Math.abs(floor - home.getY());
                if (home.getX() < minX || home.getX() >= maxX || home.getZ() < minZ || home.getZ() >= maxZ
                        || difference > 4 || difference >= nearestFloor) continue;
                nearestFloor = difference;
                floor += arena.floorOffset;
                found = new AABB(minX, floor - 2, minZ, maxX, floor + 24, maxZ);
            }
        if (found != null) {

            arena.roofBounds = found;
            arena.verifiedRoofBounds = true;
            setDirty();
        }
    }

    public void defeated(BlockPos home) {
        Lift arena = find(home);
        if (arena != null) {
            arena.defeated = true;
            setDirty();
        }
    }

    public Lift forSource(BlockPos home) {
        return lifts.get(home.asLong());
    }

    @SubscribeEvent
    public static void tick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel level && level.getGameTime() % 5 == 0) {
            HierophantArena saved = get(level);
            for (Lift arena : saved.lifts.values()) if (!arena.ready()) saved.prepare(level, arena);
        }
    }

    @SubscribeEvent
    public static void rejoin(PlayerEvent.PlayerLoggedInEvent event) {

        CompoundTag data = event.getEntity().getPersistentData();
        data.remove("FinaleLiftHome");
        data.remove("FinaleLiftDimension");
        data.remove("FinaleLiftOffset");
        data.remove("FinaleLiftCarryTick");
    }

    private void prepare(ServerLevel level, Lift arena) {
        if (arena.recoverFloor && !recoverLegacyFloor(level, arena)) return;
        if (arena.barriersCleared) return;
        boolean loaded = true;

        int minY = Math.max(level.getMinBuildHeight(), arena.sourceHome.getY() - 1);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, arena.sourceHome.getY() + GAZEBO_HEIGHT - 1);
        for (int x = -GAZEBO_RADIUS; x <= GAZEBO_RADIUS; x++)
            for (int z = -GAZEBO_RADIUS; z <= GAZEBO_RADIUS; z++) {
                BlockPos column = arena.sourceHome.offset(x, 0, z);
                if (!level.hasChunkAt(column)) {
                    loaded = false;
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    BlockPos at = new BlockPos(column.getX(), y, column.getZ());
                    if (level.getBlockState(at).is(BARRIERS)) level.destroyBlock(at, false);
                }
            }
        if (loaded) {
            arena.barriersCleared = true;
            setDirty();
        }
    }

    private boolean recoverLegacyFloor(ServerLevel level, Lift arena) {

        for (Cell cell : arena.recoveryCells)
            if (!level.hasChunkAt(cell.source.above(arena.floorOffset))) return false;
        for (Cell cell : arena.recoveryCells) {
            BlockPos dest = cell.source.above(arena.floorOffset);
            if (!level.getBlockState(dest).isAir()) continue;
            level.setBlock(dest, cell.state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            BlockEntity be = level.getBlockEntity(dest);
            if (be != null && !cell.blockEntity.isEmpty()) {
                CompoundTag restored = cell.blockEntity.copy();
                restored.putInt("x", dest.getX());
                restored.putInt("y", dest.getY());
                restored.putInt("z", dest.getZ());
                be.loadWithComponents(restored, level.registryAccess());
                be.setChanged();
            }
        }

        BlockPos oldHome = arena.sourceHome.above(LEGACY_RISE), home = arena.home();
        if (!oldHome.equals(home) && level.hasChunkAt(oldHome) && level.hasChunkAt(home) && level.getBlockState(home).isAir()
                && level.getBlockState(oldHome).is(ModBlocks.TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get())) {
            level.setBlockAndUpdate(oldHome, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(home, ModBlocks.TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get().defaultBlockState());
        }
        arena.recoverFloor = false;
        arena.recoveryCells.clear();
        setDirty();
        return true;
    }

    private static HierophantArena load(CompoundTag tag, HolderLookup.Provider registries) {
        HierophantArena saved = new HierophantArena();
        for (Tag raw : tag.getList("Lifts", Tag.TAG_COMPOUND)) {
            CompoundTag t = (CompoundTag) raw;
            boolean current = t.getBoolean("Stationary");
            boolean removed = t.getBoolean("Removed"), placed = t.getBoolean("Placed");
            int floorOffset = current ? t.getInt("FloorOffset") : placed ? LEGACY_RISE
                    : removed ? (int) Math.floor(LEGACY_RISE * Math.max(0, Math.min(LEGACY_DURATION, t.getInt("Tick"))) / (double) LEGACY_DURATION) : 0;
            Lift arena = new Lift(BlockPos.of(t.getLong("Source")), t.getUUID("Boss"),
                    t.contains("Radius") ? Math.max(1, Math.min(32, t.getInt("Radius"))) : 10,
                    Math.max(0, Math.min(LEGACY_RISE, floorOffset)), !current || t.getBoolean("Legacy"));
            arena.defeated = t.getBoolean("Defeated");
            arena.barriersCleared = current ? t.getBoolean("BarriersCleared") : arena.defeated;
            arena.recoverFloor = current ? t.getBoolean("RecoverFloor") : removed && !placed;
            ListTag roof = t.getList("RoofBounds", Tag.TAG_DOUBLE);
            if (roof.size() == 6) {
                double[] values = new double[6];
                boolean finite = true;
                for (int i = 0; i < 6; i++) finite &= Double.isFinite(values[i] = roof.getDouble(i));
                if (finite && values[3] > values[0] && values[4] > values[1] && values[5] > values[2])
                    arena.roofBounds = new AABB(values[0], values[1], values[2], values[3], values[4], values[5]);
            }
            if (arena.recoverFloor) for (Tag cellRaw : t.getList("Cells", Tag.TAG_COMPOUND)) {
                CompoundTag cell = (CompoundTag) cellRaw;
                arena.recoveryCells.add(new Cell(BlockPos.of(cell.getLong("Pos")),
                        NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), cell.getCompound("State")), cell.getCompound("BE")));
            }
            saved.lifts.put(arena.sourceHome.asLong(), arena);
        }

        saved.setDirty();
        return saved;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag all = new ListTag();
        for (Lift arena : lifts.values()) {
            CompoundTag t = new CompoundTag();
            t.putLong("Source", arena.sourceHome.asLong());
            t.putUUID("Boss", arena.boss);
            t.putInt("Radius", arena.radius);
            t.putInt("FloorOffset", arena.floorOffset);
            t.putBoolean("Stationary", true);
            t.putBoolean("Legacy", arena.legacy);
            t.putBoolean("BarriersCleared", arena.barriersCleared);
            t.putBoolean("RecoverFloor", arena.recoverFloor);
            t.putBoolean("Defeated", arena.defeated);
            if (arena.roofBounds != null) {
                AABB box = arena.roofBounds;
                ListTag roof = new ListTag();
                for (double value : new double[]{box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ})
                    roof.add(DoubleTag.valueOf(value));
                t.put("RoofBounds", roof);
            }
            ListTag cells = new ListTag();
            for (Cell cell : arena.recoveryCells) {
                CompoundTag c = new CompoundTag();
                c.putLong("Pos", cell.source.asLong());
                c.put("State", NbtUtils.writeBlockState(cell.state));
                c.put("BE", cell.blockEntity);
                cells.add(c);
            }
            t.put("Cells", cells);
            all.add(t);
        }
        tag.put("Lifts", all);
        return tag;
    }
}
