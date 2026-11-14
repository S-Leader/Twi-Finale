package show.twilight.finale.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import twilightforest.init.TFMapDecorations;
import twilightforest.item.mapdata.TFMagicMapData;

public final class CastleMapProgress {
    private static final int[] BOSSES = {
            CastleEncounterProgress.PRIEST, CastleEncounterProgress.GOLEM, CastleEncounterProgress.HIEROPHANT
    };

    private CastleMapProgress() {
    }

    public static boolean update(ServerLevel level, TFMagicMapData map) {
        var progress = CastleEncounterProgress.get(level);
        int scale = 1 << map.scale;
        boolean changed = false;
        for (var decoration : map.getDecorations()) {
            if (decoration.type().value() != TFMapDecorations.FINAL_CASTLE.get()) continue;
            int chunkX = (map.centerX + decoration.x() * scale / 2) >> 4;
            int chunkZ = (map.centerZ + decoration.y() * scale / 2) >> 4;
            int state = progress.state(ChunkPos.asLong(chunkX, chunkZ));
            for (int boss : BOSSES) {
                String marker = marker(decoration.x(), decoration.y(), boss);
                if ((state & boss) != 0) {
                    if (!map.conqueredStructures.contains(marker)) {
                        map.conqueredStructures.add(marker);
                        changed = true;
                    }
                } else {
                    changed |= map.conqueredStructures.remove(marker);
                }
            }
        }
        if (changed) map.setDirty();
        return changed;
    }

    public static int state(TFMagicMapData map, int x, int z) {
        int state = 0;
        for (int boss : BOSSES) {
            if (map.conqueredStructures.contains(marker(x, z, boss))) state |= boss;
        }
        return state;
    }

    private static String marker(int x, int z, int boss) {
        return "twilightforest_finale:castle_" + x + "_" + z + "_" + boss;
    }
}
