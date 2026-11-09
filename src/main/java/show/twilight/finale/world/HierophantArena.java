package show.twilight.finale.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModBlocks;

import java.util.*;

/**
 * Stationary roof encounter; only the gazebo's tagged barriers are opened.
 */
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

    /**
     * The legacy type/name is retained to load old journals, not to animate a lift.
     */
    public static final class Lift {
        public final BlockPos sourceHome;
        public final int radius;
        private final int floorOffset;
        private final boolean legacy;
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
            double floor = home().getY();
            return new AABB(sourceHome.getX() - radius, floor - 1, sourceHome.getZ() - radius,
                    sourceHome.getX() + radius + 1, floor + 22, sourceHome.getZ() + radius + 1);
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
        // Old boss entities/spawners saved the intended +20 home before ascent.
        for (Lift arena : lifts.values())
            if (arena.legacy && arena.sourceHome.above(LEGACY_RISE).equals(home)) return arena;
        return null;
    }

    public Lift attach(ServerLevel level, TheTwilightHierophantEntity boss, BlockPos home) {
        Lift arena = find(home);
        if (arena == null) {
            // No floor capture, new moving entities, build-height gate or +20 home.
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
        prepare(level, arena);
        return arena;
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
        // Retire the old passenger journal. Never teleport/carry a returning player.
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
        // Include the row below the new pedestal-mounted spawner as well as
        // the old floor-level layout. Only tagged fields are removed, never
        // the castle-brick pedestal or blocks in other castle sectors.
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
        // Cancel an interrupted ascent at its already reached integer height.
        // Restore only recorded floor cells into air; never overwrite player
        // builds, reward inventories or a completed platform. No path clearing.
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
        // Only relocate a reset spawner that actually exists at the old future
        // home. Do not create a second spawner while its boss is still loading.
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
            if (arena.recoverFloor) for (Tag cellRaw : t.getList("Cells", Tag.TAG_COMPOUND)) {
                CompoundTag cell = (CompoundTag) cellRaw;
                arena.recoveryCells.add(new Cell(BlockPos.of(cell.getLong("Pos")),
                        NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), cell.getCompound("State")), cell.getCompound("BE")));
            }
            saved.lifts.put(arena.sourceHome.asLong(), arena);
        }
        // Persist the migration even for old completed/defeated arenas.
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
