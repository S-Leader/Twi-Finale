package show.twilight.finale.world.castle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

final class RevivedBasementTemplate {
    record Entry(int x, int y, int z, BlockState state, CompoundTag data) {
    }

    record Data(List<List<Entry>> rooms) {
    }

    private record Cached(StructureTemplate template, Data data) {
    }

    private static final Map<StructureTemplateManager, Cached> CACHE = new WeakHashMap<>();
    static final int PIT = 7, COUNT = 8;

    private static final int[][] SOURCES = {{37, 77}, {61, 58}, {85, 80}, {93, 128}, {129, 115}, {105, 104}, {68, 19}, {85, 43}};
    static final ResourceLocation BASE = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "final_castle/revived_basement");

    static synchronized Data get(StructureTemplateManager manager, RegistryAccess registries) {
        StructureTemplate template = manager.getOrCreate(BASE);
        Cached old = CACHE.get(manager);
        if (old != null && old.template == template) return old.data;
        if (!template.getSize().equals(new Vec3i(150, 105, 150)))
            throw new IllegalStateException("Incorrect revived basement template dimensions");
        CompoundTag root = template.save(new CompoundTag());
        List<BlockState> palette = new ArrayList<>();
        for (Tag raw : root.getList("palette", Tag.TAG_COMPOUND))
            palette.add(NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), (CompoundTag) raw));
        List<List<Entry>> rooms = new ArrayList<>();
        for (int i = 0; i < COUNT; i++) rooms.add(new ArrayList<>());
        for (Tag raw : root.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag block = (CompoundTag) raw;
            ListTag at = block.getList("pos", Tag.TAG_INT);
            int x = at.getInt(0), y = at.getInt(1), z = at.getInt(2);
            if (x < 0 || x >= 150 || y < 0 || y >= 105 || z < 0 || z >= 150)
                throw new IllegalStateException("Revived basement block outside template bounds");
            for (int i = 0; i < COUNT; i++) {
                int sx = SOURCES[i][0], sz = SOURCES[i][1];
                if (x < sx || x > sx + 16 || z < sz || z > sz + 16 || y < (i == PIT ? 68 : 79) || y > 87) continue;
                BlockState state = palette.get(block.getInt("state"));
                CompoundTag data = block.contains("nbt", Tag.TAG_COMPOUND) ? block.getCompound("nbt").copy() : null;
                rooms.get(i).add(new Entry(x - sx, y - 79, z - sz, state, data));
            }
        }
        Data data = new Data(rooms.stream().map(List::copyOf).toList());
        CACHE.put(manager, new Cached(template, data));
        return data;
    }
}
