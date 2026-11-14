package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.world.CastleEncounterProgress;
import twilightforest.block.entity.KeepsakeCasketBlockEntity;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructures;

import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID)
public final class EngineerWorkshop {
    public static final ResourceLocation TEMPLATE = TwilightFinaleMod.prefix("final_castle/engineer_workshop");
    public static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            TwilightFinaleMod.prefix("chests/engineer_workshop"));
    private record Scan(WeakReference<ServerLevel> level, ChunkPos chunk, int retries) { }
    private static final Queue<Scan> SCANS = new ConcurrentLinkedQueue<>();
    private static final Map<ServerLevel, Set<BlockPos>> WAITING = new WeakHashMap<>();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        if (event.getChunk().getAllReferences().keySet().stream()
                .noneMatch(s -> TFStructures.FINAL_CASTLE.location().equals(registry.getKey(s)))) return;

        SCANS.add(new Scan(new WeakReference<>(level), event.getChunk().getPos(), 0));
    }

    @SubscribeEvent
    public static void afterTick(ServerTickEvent.Post event) {
        for (int i = 0, count = Math.min(32, SCANS.size()); i < count; i++) {
            Scan scan = SCANS.poll();
            if (scan == null) break;
            ServerLevel level = scan.level.get();
            if (level == null || level.getServer() != event.getServer()) continue;
            var chunk = level.getChunkSource().getChunkNow(scan.chunk.x, scan.chunk.z);
            if (chunk == null) {
                if (scan.retries < 200) SCANS.add(new Scan(scan.level, scan.chunk, scan.retries + 1));
                continue;
            }
            for (BlockPos pos : List.copyOf(chunk.getBlockEntitiesPos())) {
                var block = chunk.getBlockEntity(pos);
                if (block instanceof SpawnerBlockEntity spawner && isEmpty(level, spawner)
                        || block instanceof KeepsakeCasketBlockEntity casket && BuiltInLootTables.STRONGHOLD_LIBRARY.equals(casket.getLootTable()))
                    WAITING.computeIfAbsent(level, l -> new LinkedHashSet<>()).add(pos.immutable());
            }
        }
        if (event.getServer().getTickCount() % 20 != 0) return;
        for (var entry : WAITING.entrySet()) {
            ServerLevel level = entry.getKey();
            if (level.getServer() != event.getServer()) continue;
            for (var iterator = entry.getValue().iterator(); iterator.hasNext(); ) {
                BlockPos pos = iterator.next();
                var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (chunk != null && chunk.getBlockEntity(pos) instanceof KeepsakeCasketBlockEntity casket) {
                    if (upgradeUnopenedCasket(level, casket)) iterator.remove();
                    continue;
                }
                if (chunk == null || !(chunk.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)
                        || !isEmpty(level, spawner)) {
                    iterator.remove();
                    continue;
                }
                StructurePiece tower = CastleEncounterProgress.findCastle(level, pos).stream()
                        .flatMap(start -> start.getPieces().stream())
                        .filter(piece -> piece instanceof FinalCastleLargeTowerComponent)
                        .filter(piece -> FinaleCastleInteriors.local(piece, 6, 39, 6).equals(pos))
                        .findFirst().orElse(null);
                if (tower == null) {
                    iterator.remove();
                    continue;
                }

                if (!roomLoaded(level, pos)) continue;
                if (!furnish(level, tower)) continue;
                level.setBlock(pos, ModBlocks.ENGINEER_SPAWNER.get().defaultBlockState(), 3);
                iterator.remove();
            }
        }
    }

    private static boolean isEmpty(ServerLevel level, SpawnerBlockEntity spawner) {
        var tag = spawner.saveWithoutMetadata(level.registryAccess());
        return tag.getCompound("SpawnData").getCompound("entity").isEmpty()
                && tag.getList("SpawnPotentials", Tag.TAG_COMPOUND).isEmpty();
    }

    private static boolean furnish(ServerLevel level, StructurePiece tower) {
        var template = level.getStructureManager().get(TEMPLATE);
        if (template.isEmpty()) return false;
        var settings = new StructurePlaceSettings().setMirror(Mirror.FRONT_BACK)
                .setRotation(FinaleCastleInteriors.rotation(tower));
        BlockPos origin = FinaleCastleInteriors.local(tower, 11, 38, 1);
        CompoundTag data = template.get().save(new CompoundTag());
        var palette = data.getList("palette", Tag.TAG_COMPOUND);

        for (Tag raw : data.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag block = (CompoundTag) raw;
            BlockState state = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), palette.getCompound(block.getInt("state")))
                    .mirror(settings.getMirror()).rotate(settings.getRotation());

            if (state.isAir() || state.is(ModBlocks.ENGINEER_SPAWNER.get())) continue;
            var xyz = block.getList("pos", Tag.TAG_INT);
            BlockPos pos = StructureTemplate.calculateRelativePosition(settings,
                    new BlockPos(xyz.getInt(0), xyz.getInt(1), xyz.getInt(2))).offset(origin);
            if (!level.getBlockState(pos).isAir() || !state.canSurvive(level, pos) || !level.setBlock(pos, state, 3)) continue;
            if (block.contains("nbt", Tag.TAG_COMPOUND) && level.getBlockEntity(pos) != null) {
                var entity = level.getBlockEntity(pos);
                entity.loadWithComponents(block.getCompound("nbt").copy(), level.registryAccess());
                if (entity instanceof RandomizableContainerBlockEntity container && container.getLootTable() != null)
                    container.setLootTableSeed(level.random.nextLong());
                entity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
        return true;
    }

    private static boolean roomLoaded(ServerLevel level, BlockPos center) {
        for (int x = (center.getX() - 6) >> 4; x <= (center.getX() + 6) >> 4; x++)
            for (int z = (center.getZ() - 6) >> 4; z <= (center.getZ() + 6) >> 4; z++)
                if (level.getChunkSource().getChunkNow(x, z) == null) return false;
        return true;
    }

    private static boolean upgradeUnopenedCasket(ServerLevel level, KeepsakeCasketBlockEntity casket) {
        if (!BuiltInLootTables.STRONGHOLD_LIBRARY.equals(casket.getLootTable())) return true;
        BlockPos pos = casket.getBlockPos();
        var tower = CastleEncounterProgress.findCastle(level, pos).stream()
                .flatMap(start -> start.getPieces().stream())
                .filter(piece -> piece instanceof FinalCastleLargeTowerComponent)
                .filter(piece -> FinaleCastleInteriors.local(piece, 7, 39, 11).equals(pos)
                        || FinaleCastleInteriors.local(piece, 4, 39, 6).equals(pos)).findFirst().orElse(null);
        if (tower == null) return true;
        BlockPos center = FinaleCastleInteriors.local(tower, 6, 39, 6);
        if (!roomLoaded(level, center)) return false;
        BlockPos table = FinaleCastleInteriors.local(tower, 8, 39, 6);
        BlockPos shelf = FinaleCastleInteriors.local(tower, 8, 39, 8);
        BlockPos destination = FinaleCastleInteriors.local(tower, 7, 39, 11);
        boolean intactOldLayout = FinaleCastleInteriors.local(tower, 4, 39, 6).equals(pos)
                && level.getBlockState(table).is(TFBlocks.CANOPY_FENCE.get())
                && level.getBlockState(table.above()).is(TFBlocks.CANOPY_PLATE.get())
                && level.getBlockState(shelf).is(TFBlocks.CANOPY_BOOKSHELF.get())
                && level.getBlockState(destination).isAir();
        if (intactOldLayout && level.getStructureManager().get(TEMPLATE).isPresent()) {
            CompoundTag original = casket.saveWithFullMetadata(level.registryAccess());

            casket.setLootTable(null);
            casket.clearContent();
            level.removeBlock(pos, false);
            level.removeBlock(table.above(), false);
            level.removeBlock(table, false);
            level.removeBlock(shelf, false);
            furnish(level, tower);
            if (level.getBlockEntity(destination) instanceof KeepsakeCasketBlockEntity moved) {
                moved.loadWithComponents(original, level.registryAccess());
                moved.setLootTable(LOOT_TABLE);
                moved.setChanged();
            }
        } else {
            casket.setLootTable(LOOT_TABLE);
            casket.setChanged();
        }
        return true;
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) {
        SCANS.clear();
        WAITING.clear();
    }
}
