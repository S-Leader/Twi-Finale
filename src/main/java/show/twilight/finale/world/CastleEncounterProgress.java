package show.twilight.finale.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.entity.AnnihilationGolemEntity;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.init.TFStructures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Per-dimension, per-castle history; unrelated castles never unlock journal endings. */
@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID)
public final class CastleEncounterProgress extends SavedData {
    public static final int PRIEST = 1, GOLEM = 2, HIEROPHANT = 4;
    private final Map<Long, Integer> defeated = new HashMap<>();

    public static CastleEncounterProgress get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(CastleEncounterProgress::new,
                CastleEncounterProgress::load), "twilightfinale_castle_encounters");
    }

    public static Optional<StructureStart> findCastle(ServerLevel level, BlockPos pos) {
        var castle = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(TFStructures.FINAL_CASTLE);
        if (castle == null) return Optional.empty();
        var start = level.structureManager().getStructureAt(pos, castle);
        return start.isValid() ? Optional.of(start) : Optional.empty();
    }

    public int state(long castle) {
        return defeated.getOrDefault(castle, 0);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        int boss;
        if (entity instanceof AdherentHighPriestEntity priest && !priest.isShadowClone()) boss = PRIEST;
        else if (entity instanceof AnnihilationGolemEntity) boss = GOLEM;
        else if (entity instanceof TheTwilightHierophantEntity pope && pope.getPhase() == 4) boss = HIEROPHANT;
        else return;
        BlockPos pos = entity.blockPosition();
        if (entity instanceof EnforcedHomePoint home && home.isRestrictionPointValid(level.dimension()))
            pos = home.getRestrictionPoint().pos();
        findCastle(level, pos).ifPresent(start -> {
            var data = get(level);
            data.defeated.merge(start.getChunkPos().toLong(), boss, (old, value) -> old | value);
            data.setDirty();
        });
    }

    private static CastleEncounterProgress load(CompoundTag tag, HolderLookup.Provider registries) {
        var data = new CastleEncounterProgress();
        for (Tag entry : tag.getList("Castles", Tag.TAG_COMPOUND)) {
            CompoundTag castle = (CompoundTag) entry;
            data.defeated.put(castle.getLong("StartChunk"), castle.getInt("Defeated") & 7);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        defeated.forEach((key, value) -> {
            CompoundTag castle = new CompoundTag();
            castle.putLong("StartChunk", key);
            castle.putInt("Defeated", value);
            entries.add(castle);
        });
        tag.put("Castles", entries);
        return tag;
    }
}
