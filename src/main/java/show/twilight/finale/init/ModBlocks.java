package show.twilight.finale.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.block.FinaleBossSpawnerBlock;
import show.twilight.finale.block.FloorAdherentTrophyBlock;
import show.twilight.finale.block.WallAdherentTrophyBlock;
import show.twilight.finale.block.entity.AdherentSpawnerBlockEntity;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import show.twilight.finale.block.entity.HierophantSpawnerBlockEntity;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK, (String) "twilightforest_finale");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, (String) "twilightforest_finale");
    public static final Supplier<Block> ANNIHILATED_CASTLE_BRICK = BLOCKS.register("annihilated_castle_brick", () -> new Block(BlockBehaviour.Properties.ofFullCopy(twilightforest.init.TFBlocks.CASTLE_BRICK.get()).mapColor(MapColor.COLOR_PURPLE)));
    public static final Supplier<FloorAdherentTrophyBlock> ADHERENT_TROPHY_BLOCK = BLOCKS.register("adherent_trophy", () -> new FloorAdherentTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModBlocks.ADHERENT_TROPHY_ENTITY.get()));
    public static final Supplier<WallAdherentTrophyBlock> ADHERENT_WALL_TROPHY_BLOCK = BLOCKS.register("adherent_wall_trophy", () -> new WallAdherentTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModBlocks.ADHERENT_TROPHY_ENTITY.get()));
    public static final Supplier<BlockEntityType<FinalTrophyBlockEntity>> ADHERENT_TROPHY_ENTITY = BLOCK_ENTITIES.register("adherent_trophy", () -> BlockEntityType.Builder.of(FinalTrophyBlockEntity::adherent, (Block[]) new Block[]{(Block) ADHERENT_TROPHY_BLOCK.get(), (Block) ADHERENT_WALL_TROPHY_BLOCK.get()}).build(null));
    public static final Supplier<FloorAdherentTrophyBlock> HIEROPHANT_TROPHY_BLOCK = BLOCKS.register("hierophant_trophy", () -> new show.twilight.finale.block.HierophantTrophyBlocks.Floor(BlockBehaviour.Properties.of().instabreak(), () -> ModBlocks.HIEROPHANT_TROPHY_ENTITY.get()));
    public static final Supplier<WallAdherentTrophyBlock> HIEROPHANT_WALL_TROPHY_BLOCK = BLOCKS.register("hierophant_wall_trophy", () -> new show.twilight.finale.block.HierophantTrophyBlocks.Wall(BlockBehaviour.Properties.of().instabreak().dropsLike(HIEROPHANT_TROPHY_BLOCK.get()), () -> ModBlocks.HIEROPHANT_TROPHY_ENTITY.get()));
    public static final Supplier<BlockEntityType<FinalTrophyBlockEntity>> HIEROPHANT_TROPHY_ENTITY = BLOCK_ENTITIES.register("hierophant_trophy", () -> BlockEntityType.Builder.of(FinalTrophyBlockEntity::hierophant, HIEROPHANT_TROPHY_BLOCK.get(), HIEROPHANT_WALL_TROPHY_BLOCK.get()).build(null));
    public static final Supplier<FinaleBossSpawnerBlock> ADHERENT_BOSS_SPAWNER = BLOCKS.register("adherent_boss_spawner", () -> new FinaleBossSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noLootTable().noOcclusion().strength(-1.0f, 3600000.8f), ModBossVariant.ADHERENT));
    public static final Supplier<BlockEntityType<AdherentSpawnerBlockEntity>> ADHERENT_SPAWNER = BLOCK_ENTITIES.register("adherent_spawner", () -> BlockEntityType.Builder.of(AdherentSpawnerBlockEntity::new, (Block[]) new Block[]{(Block) ADHERENT_BOSS_SPAWNER.get()}).build(null));
    public static final Supplier<FinaleBossSpawnerBlock> HIEROPHANT_BOSS_SPAWNER = BLOCKS.register("hierophant_boss_spawner", () -> new FinaleBossSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noLootTable().noOcclusion().strength(-1.0F, 3600000.8F), ModBossVariant.HIEROPHANT));
    public static final Supplier<BlockEntityType<HierophantSpawnerBlockEntity>> HIEROPHANT_SPAWNER = BLOCK_ENTITIES.register("hierophant_spawner", () -> BlockEntityType.Builder.of(HierophantSpawnerBlockEntity::new, HIEROPHANT_BOSS_SPAWNER.get()).build(null));
    public static final Supplier<FinaleBossSpawnerBlock> ROVING_CUBE_BOSS_SPAWNER = BLOCKS.register("roving_cube_boss_spawner", () -> new FinaleBossSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noLootTable().strength(-1.0F, 3600000.8F), ModBossVariant.ROVING_CUBE));
    public static final Supplier<BlockEntityType<show.twilight.finale.block.entity.RovingCubeSpawnerBlockEntity>> ROVING_CUBE_SPAWNER = BLOCK_ENTITIES.register("roving_cube_spawner", () -> BlockEntityType.Builder.of(show.twilight.finale.block.entity.RovingCubeSpawnerBlockEntity::new, ROVING_CUBE_BOSS_SPAWNER.get()).build(null));
}

