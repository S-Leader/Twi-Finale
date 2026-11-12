package show.twilight.finale.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.block.*;
import show.twilight.finale.block.entity.AdherentSpawnerBlockEntity;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import show.twilight.finale.block.entity.HierophantSpawnerBlockEntity;
import show.twilight.finale.block.entity.TwilightTrapChestBlockEntity;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, TwilightFinaleMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "twilightforest_finale");
    public static final Supplier<show.twilight.finale.block.TwilightTrapChestBlock> TWILIGHT_TRAP_CHEST = BLOCKS.register("twilight_trap_chest", () -> new TwilightTrapChestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).noOcclusion().noLootTable().strength(-1, 3600000)));
    public static final Supplier<BlockEntityType<TwilightTrapChestBlockEntity>> TWILIGHT_TRAP_CHEST_ENTITY = BLOCK_ENTITIES.register("twilight_trap_chest", () -> BlockEntityType.Builder.of(TwilightTrapChestBlockEntity::new, TWILIGHT_TRAP_CHEST.get()).build(null));
    public static final Supplier<FloorAdherentTrophyBlock> ADHERENT_TROPHY_BLOCK = BLOCKS.register("adherent_trophy", () -> new FloorAdherentTrophyBlock(BlockBehaviour.Properties.of().instabreak(), ModBlocks.ADHERENT_TROPHY_ENTITY));
    public static final Supplier<WallAdherentTrophyBlock> ADHERENT_WALL_TROPHY_BLOCK = BLOCKS.register("adherent_wall_trophy", () -> new WallAdherentTrophyBlock(BlockBehaviour.Properties.of().instabreak(), ModBlocks.ADHERENT_TROPHY_ENTITY));
    public static final Supplier<BlockEntityType<FinalTrophyBlockEntity>> ADHERENT_TROPHY_ENTITY = BLOCK_ENTITIES.register("adherent_trophy", () -> BlockEntityType.Builder.of(FinalTrophyBlockEntity::adherent, new Block[]{ADHERENT_TROPHY_BLOCK.get(), ADHERENT_WALL_TROPHY_BLOCK.get()}).build(null));
    public static final Supplier<FloorAdherentTrophyBlock> HIEROPHANT_TROPHY_BLOCK = BLOCKS.register("hierophant_trophy", () -> new HierophantTrophyBlocks.Floor(BlockBehaviour.Properties.of().instabreak(), ModBlocks.HIEROPHANT_TROPHY_ENTITY));
    public static final Supplier<WallAdherentTrophyBlock> HIEROPHANT_WALL_TROPHY_BLOCK = BLOCKS.register("hierophant_wall_trophy", () -> new HierophantTrophyBlocks.Wall(BlockBehaviour.Properties.of().instabreak().dropsLike(HIEROPHANT_TROPHY_BLOCK.get()), ModBlocks.HIEROPHANT_TROPHY_ENTITY));
    public static final Supplier<BlockEntityType<FinalTrophyBlockEntity>> HIEROPHANT_TROPHY_ENTITY = BLOCK_ENTITIES.register("hierophant_trophy", () -> BlockEntityType.Builder.of(FinalTrophyBlockEntity::hierophant, HIEROPHANT_TROPHY_BLOCK.get(), HIEROPHANT_WALL_TROPHY_BLOCK.get()).build(null));
    public static final Supplier<FinaleBossSpawnerBlock> ADHERENT_BOSS_SPAWNER = BLOCKS.register("adherent_boss_spawner", () -> new FinaleBossSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noLootTable().noOcclusion().strength(-1.0f, 3600000.8f), ModBossVariant.ADHERENT));
    public static final Supplier<BlockEntityType<AdherentSpawnerBlockEntity>> ADHERENT_SPAWNER = BLOCK_ENTITIES.register("adherent_spawner", () -> BlockEntityType.Builder.of(AdherentSpawnerBlockEntity::new, new Block[]{ADHERENT_BOSS_SPAWNER.get()}).build(null));
    public static final Supplier<FinaleBossSpawnerBlock> TWILIGHT_HIEROPHANT_BOSS_SPAWNER = BLOCKS.register("twilight_hierophant_boss_spawner", () -> new FinaleBossSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noLootTable().noOcclusion().strength(-1.0F, 3600000.8F), ModBossVariant.HIEROPHANT));
    public static final Supplier<BlockEntityType<HierophantSpawnerBlockEntity>> TWILIGHT_HIEROPHANT_SPAWNER = BLOCK_ENTITIES.register("twilight_hierophant_spawner", () -> BlockEntityType.Builder.of(HierophantSpawnerBlockEntity::new, TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get()).build(null));
}

