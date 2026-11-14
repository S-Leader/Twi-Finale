package show.twilight.finale.world.castle;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import twilightforest.init.TFBlocks;

import show.twilight.finale.TwilightFinaleMod;

@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID)
public final class ForgeTerrainRepair {
    private static final ResourceLocation CASTLE = ResourceLocation.fromNamespaceAndPath("twilightforest", "final_castle");
    private static final ResourceLocation TEMPLATE = TwilightFinaleMod.prefix("final_castle/forge_room_complete");
    private static final TagKey<Block> INTRUSIVE = TagKey.create(Registries.BLOCK, TwilightFinaleMod.prefix("forge_intrusion_replaceable"));

    private record Pending(WeakReference<ServerLevel> level, ChunkPos pos, int attempts) {
    }

    private record Mask(StructureTemplate template, BitSet air) {
    }

    private static final Queue<Pending> PENDING = new ConcurrentLinkedQueue<>();
    private static final Map<StructureTemplateManager, Mask> MASKS = new WeakHashMap<>();

    private static int index(int x, int y, int z) {
        return (x * 51 + z) * 31 + y;
    }

    @SubscribeEvent
    public static void onNewChunk(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) return;
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        if (event.getChunk().getAllReferences().keySet().stream().noneMatch(s -> CASTLE.equals(registry.getKey(s))))
            return;

        PENDING.add(new Pending(new WeakReference<>(level), event.getChunk().getPos(), 0));
    }

    @SubscribeEvent
    public static void afterTick(ServerTickEvent.Post event) {
        int budget = Math.min(8, PENDING.size());
        for (int i = 0; i < budget; i++) {
            Pending job = PENDING.poll();
            if (job == null) break;
            ServerLevel level = job.level.get();
            if (level == null || level.getServer() != event.getServer()) continue;
            LevelChunk chunk = level.getChunkSource().getChunkNow(job.pos.x, job.pos.z);
            if (chunk == null) {
                if (job.attempts < 200) PENDING.add(new Pending(job.level, job.pos, job.attempts + 1));
                continue;
            }
            for (var forge : forges(level, job.pos)) repair(level, chunk, forge);
        }
    }

    private static List<FinalCastleDungeonForgeRoomComponent> forges(ServerLevel level, ChunkPos pos) {
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<FinalCastleDungeonForgeRoomComponent> found = new ArrayList<>();
        for (var start : level.structureManager().startsForStructure(pos, s -> CASTLE.equals(registry.getKey(s))))
            for (var piece : start.getPieces())
                if (piece instanceof FinalCastleDungeonForgeRoomComponent forge
                        && forge.getBoundingBox().intersects(pos.getMinBlockX(), pos.getMinBlockZ(), pos.getMaxBlockX(), pos.getMaxBlockZ()))
                    found.add(forge);
        return found;
    }

    private static synchronized BitSet airMask(ServerLevel level) {
        var manager = level.getServer().getStructureManager();
        var template = manager.getOrCreate(TEMPLATE);
        Mask cached = MASKS.get(manager);
        if (cached != null && cached.template == template) return cached.air;
        if (!template.getSize().equals(new Vec3i(51, 31, 51)))
            throw new IllegalStateException("Incorrect complete forge template dimensions");
        CompoundTag root = template.save(new CompoundTag());
        ListTag palette = root.getList("palette", Tag.TAG_COMPOUND);
        BitSet air = new BitSet(51 * 31 * 51);
        for (Tag raw : root.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag block = (CompoundTag) raw;
            String id = palette.getCompound(block.getInt("state")).getString("Name");
            if (!id.equals("minecraft:air") && !id.equals("minecraft:cave_air") && !id.equals("minecraft:void_air"))
                continue;
            ListTag at = block.getList("pos", Tag.TAG_INT);
            air.set(index(at.getInt(0), at.getInt(1), at.getInt(2)));
        }
        MASKS.put(manager, new Mask(template, air));
        return air;
    }

    private static int repair(ServerLevel level, LevelChunk chunk, FinalCastleDungeonForgeRoomComponent forge) {
        BoundingBox bounds = forge.getBoundingBox();
        ChunkPos part = chunk.getPos();
        if (bounds.getXSpan() != 51 || bounds.getYSpan() != 31 || bounds.getZSpan() != 51) return 0;
        BitSet air = airMask(level);
        int removed = 0;
        for (int x = Math.max(bounds.minX(), part.getMinBlockX()); x <= Math.min(bounds.maxX(), part.getMaxBlockX()); x++)
            for (int z = Math.max(bounds.minZ(), part.getMinBlockZ()); z <= Math.min(bounds.maxZ(), part.getMaxBlockZ()); z++) {
                int lx, lz;
                switch (forge.getOrientation()) {
                    case WEST -> {
                        lx = z - bounds.minZ();
                        lz = bounds.maxX() - x;
                    }
                    case NORTH -> {
                        lx = bounds.maxX() - x;
                        lz = bounds.maxZ() - z;
                    }
                    case EAST -> {
                        lx = bounds.maxZ() - z;
                        lz = x - bounds.minX();
                    }
                    default -> {
                        lx = x - bounds.minX();
                        lz = z - bounds.minZ();
                    }
                }
                for (int y = Math.max(bounds.minY(), level.getMinBuildHeight() + 1); y <= Math.min(bounds.maxY(), level.getMaxBuildHeight() - 1); y++) {
                    if (!air.get(index(lx, y - bounds.minY(), lz))) continue;
                    BlockPos at = new BlockPos(x, y, z);
                    var state = chunk.getBlockState(at);
                    if (state.isAir() || !state.is(INTRUSIVE) || state.hasBlockEntity()) continue;

                    if (state.is(TFBlocks.CASTLE_BRICK) || state.is(TFBlocks.THICK_CASTLE_BRICK) || state.is(TFBlocks.CASTLE_BRICK_STAIRS))
                        continue;
                    if (level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)) removed++;
                }
            }
        return removed;
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("twilight_finale").requires(s -> s.hasPermission(2))
                .then(Commands.literal("repair_forge").executes(context -> {
                    var source = context.getSource();
                    var level = source.getLevel();
                    BlockPos at = BlockPos.containing(source.getPosition());
                    var matches = forges(level, new ChunkPos(at)).stream().filter(f -> f.getBoundingBox().isInside(at)).toList();
                    if (matches.isEmpty()) {
                        source.sendFailure(Component.translatable("commands.twilightforest_finale.forge_not_found"));
                        return 0;
                    }
                    int removed = 0, skipped = 0;
                    for (var forge : matches) {
                        var box = forge.getBoundingBox();
                        for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++)
                            for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) {
                                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                                if (chunk == null) {
                                    skipped++;
                                    continue;
                                }
                                removed += repair(level, chunk, forge);
                            }
                    }
                    int count = removed, unloaded = skipped;
                    source.sendSuccess(() -> Component.translatable("commands.twilightforest_finale.forge_repaired", count, unloaded), true);
                    return Command.SINGLE_SUCCESS;
                })));
    }
}
