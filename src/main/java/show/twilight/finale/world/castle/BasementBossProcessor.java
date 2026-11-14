package show.twilight.finale.world.castle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import show.twilight.finale.init.ModBlocks;

public final class BasementBossProcessor extends StructureProcessor {
    public static final BasementBossProcessor INSTANCE = new BasementBossProcessor();
    public static final MapCodec<BasementBossProcessor> CODEC = MapCodec.unit(INSTANCE);

    public static boolean isBlazeSpawner(BlockState state, CompoundTag data) {
        if (!state.is(Blocks.SPAWNER) || data == null) return false;
        CompoundTag spawn = data.getCompound("SpawnData");
        if ("minecraft:blaze".equals(spawn.getCompound("entity").getString("id")) || "minecraft:blaze".equals(spawn.getString("id")))
            return true;

        var potentials = data.getList("SpawnPotentials", 10);
        for (int i = 0; i < potentials.size(); i++) {
            CompoundTag entry = potentials.getCompound(i);
            if ("minecraft:blaze".equals(entry.getCompound("data").getCompound("entity").getString("id"))
                    || "minecraft:blaze".equals(entry.getCompound("Entity").getString("id"))) return true;
        }
        return false;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos origin, BlockPos pivot, StructureTemplate.StructureBlockInfo original, StructureTemplate.StructureBlockInfo block, StructurePlaceSettings settings) {
        if (block.state().is(ModBlocks.TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get()) || isBlazeSpawner(block.state(), block.nbt()))
            return new StructureTemplate.StructureBlockInfo(block.pos(), ModBlocks.ANNIHILATION_GOLEM_BOSS_SPAWNER.get().defaultBlockState(), null);
        return block;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return FinaleCastlePieces.BASEMENT_BOSS_PROCESSOR.get();
    }
}
