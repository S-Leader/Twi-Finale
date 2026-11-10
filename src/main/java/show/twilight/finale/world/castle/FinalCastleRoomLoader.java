package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import show.twilight.finale.TwilightFinaleMod;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Vanilla LOAD, not TemplateStructurePiece's per-chunk clipped placement.
 * Incomplete markers survive saving/unloading; completed rooms never refill loot.
 */
@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID)
public final class FinalCastleRoomLoader {
    private record Scan(WeakReference<ServerLevel> level, ChunkPos chunk, int retries) {
    }

    private static final Queue<Scan> SCANS = new ConcurrentLinkedQueue<>();
    private static final Map<ServerLevel, Set<BlockPos>> WAITING = new WeakHashMap<>();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level)
            SCANS.add(new Scan(new WeakReference<>(level), event.getChunk().getPos(), 0));
    }

    @SubscribeEvent
    public static void afterTick(ServerTickEvent.Post event) {
        // Load events can precede FULL promotion. Never query neighbours on that thread.
        for (int i = 0, n = Math.min(32, SCANS.size()); i < n; i++) {
            Scan scan = SCANS.poll();
            if (scan == null) break;
            ServerLevel level = scan.level.get();
            if (level == null || level.getServer() != event.getServer()) continue;
            LevelChunk chunk = level.getChunkSource().getChunkNow(scan.chunk.x, scan.chunk.z);
            if (chunk == null) {
                if (scan.retries < 200) SCANS.add(new Scan(scan.level, scan.chunk, scan.retries + 1));
                continue;
            }
            for (BlockPos pos : List.copyOf(chunk.getBlockEntitiesPos()))
                if (chunk.getBlockState(pos).is(Blocks.STRUCTURE_BLOCK) && chunk.getBlockEntity(pos) instanceof StructureBlockEntity block && block.getPersistentData().contains(FinalCastleRoomStructurePiece.MARKER))
                    WAITING.computeIfAbsent(level, l -> new LinkedHashSet<>()).add(pos.immutable());
        }
        if (event.getServer().getTickCount() % 20 != 0) return;
        int budget = 2;
        for (var entry : WAITING.entrySet()) {
            ServerLevel level = entry.getKey();
            if (level.getServer() != event.getServer()) continue;
            for (var iterator = entry.getValue().iterator(); iterator.hasNext(); ) {
                BlockPos pos = iterator.next();
                LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                // Its persisted marker will be discovered again when this chunk reloads.
                if (chunk == null) {
                    iterator.remove();
                    continue;
                }
                if (!(chunk.getBlockEntity(pos) instanceof StructureBlockEntity loader) || !loader.getPersistentData().contains(FinalCastleRoomStructurePiece.MARKER)) {
                    iterator.remove();
                    continue;
                }
                CompoundTag data = loader.getPersistentData().getCompound(FinalCastleRoomStructurePiece.MARKER);
                Completed completed = Completed.get(level);
                if (completed.contains(pos) || data.getBoolean("Complete")) {
                    restoreMarker(level, loader, data);
                    iterator.remove();
                    continue;
                }
                BlockPos center = BlockPos.of(data.getLong("Center"));
                // One extra block for vanilla's neighbour-shape updates, no forced loads.
                boolean ready = true;
                for (int x = (center.getX() - 9) >> 4; x <= (center.getX() + 9) >> 4; x++)
                    for (int z = (center.getZ() - 9) >> 4; z <= (center.getZ() + 9) >> 4; z++)
                        if (level.getChunkSource().getChunkNow(x, z) == null) ready = false;
                if (!ready || budget <= 0) continue;
                if (!loader.getStructureName().startsWith("twilightforest_finale:final_castle/basement_room_"))
                    continue;
                if (!loader.loadStructureInfo(level) || !loader.getStructureSize().equals(FinalCastleRoomStructurePiece.ROOM_SIZE))
                    continue;
                if (!prepareFarmLight(level, loader, data)) continue;
                if (!loader.placeStructureIfSameSize(level)) continue;
                // One server-thread operation: place all 2,601 cells, then consume the marker.
                data.putBoolean("Complete", true);
                loader.setChanged();
                completed.add(pos);
                restoreMarker(level, loader, data);
                iterator.remove();
                budget--;
            }
        }
    }

    private static boolean prepareFarmLight(ServerLevel level, StructureBlockEntity loader, CompoundTag data) {
        if (!loader.getStructureName().endsWith("/basement_room_1")) return true;
        var template = level.getStructureManager().get(ResourceLocation.parse(loader.getStructureName())).orElseThrow();
        var settings = new StructurePlaceSettings().setRotation(loader.getRotation()).setMirror(loader.getMirror());
        BlockPos origin = loader.getBlockPos().offset(loader.getStructurePos());
        if (!data.getBoolean("FarmLightingPrepared")) {
            // Structure-block LOAD immediately validates plants. Seed the lamps first,
            // let the normal lighting engine propagate, THEN load the complete room.
            for (var light : template.filterBlocks(origin, settings, Blocks.SEA_LANTERN))
                level.setBlock(light.pos(), light.state(), 2);
            data.putBoolean("FarmLightingPrepared", true);
            loader.setChanged();
            return false;
        }
        for (var wheat : template.filterBlocks(origin, settings, Blocks.WHEAT))
            if (level.getRawBrightness(wheat.pos(), 0) < 9) return false;
        return true;
    }

    private static void restoreMarker(ServerLevel level, StructureBlockEntity loader, CompoundTag data) {
        var original = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), data.getCompound("OriginalState"));
        level.setBlock(loader.getBlockPos(), original, 2);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SCANS.clear();
        WAITING.clear();
    }

    /**
     * Per-dimension, serialized completion journal; never persisted only in a static set.
     */
    private static final class Completed extends SavedData {
        private final Set<Long> positions = new HashSet<>();

        static Completed get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Completed::new, (tag, registries) -> {
                Completed value = new Completed();
                for (long pos : tag.getLongArray("CompletedRooms")) value.positions.add(pos);
                return value;
            }), "twilightforest_finale_completed_rooms");
        }

        boolean contains(BlockPos pos) {
            return positions.contains(pos.asLong());
        }

        void add(BlockPos pos) {
            if (positions.add(pos.asLong())) setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putLongArray("CompletedRooms", positions.stream().mapToLong(Long::longValue).sorted().toArray());
            return tag;
        }
    }
}
