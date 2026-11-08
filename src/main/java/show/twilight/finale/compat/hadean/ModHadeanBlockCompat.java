package show.twilight.finale.compat.hadean;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.block.FloorDuckTrophyBlock;
import show.twilight.finale.block.FloorHadeanTrophyBlock;
import show.twilight.finale.block.WallDuckTrophyBlock;
import show.twilight.finale.block.WallHadeanTrophyBlock;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import show.twilight.finale.item.DuckTrophyItem;
import show.twilight.finale.item.HadeanTrophyItem;

import java.util.function.Supplier;


public class ModHadeanBlockCompat {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK, (String) "hadean_breathe");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, (String) "hadean_breathe");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ITEM, (String) "hadean_breathe");
    public static final Supplier<FloorHadeanTrophyBlock> HADEAN_ENFORCER_TROPHY_BLOCK = BLOCKS.register("hadean_enforcer_trophy", () -> new FloorHadeanTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModHadeanBlockCompat.HADEAN_ENFORCER_TROPHY_ENTITY.get()));
    public static final Supplier<FloorHadeanTrophyBlock> HADEAN_BLASTER_TROPHY_BLOCK = BLOCKS.register("hadean_blaster_trophy", () -> new FloorHadeanTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModHadeanBlockCompat.HADEAN_BLASTER_TROPHY_ENTITY.get()));
    public static final Supplier<FloorHadeanTrophyBlock> HADEAN_SMASHER_TROPHY_BLOCK = BLOCKS.register("hadean_smasher_trophy", () -> new FloorHadeanTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModHadeanBlockCompat.HADEAN_SMASHER_TROPHY_ENTITY.get()));
    public static final Supplier<FloorDuckTrophyBlock> DUCK_ENFORCER_TROPHY_BLOCK = BLOCKS.register("duck_enforcer_trophy", () -> new FloorDuckTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModHadeanBlockCompat.DUCK_ENFORCER_TROPHY_ENTITY.get()));
    public static final Supplier<WallHadeanTrophyBlock> HADEAN_ENFORCER_WALL_TROPHY_BLOCK = BLOCKS.register("hadean_enforcer_wall_trophy", () -> new WallHadeanTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModHadeanBlockCompat.HADEAN_ENFORCER_TROPHY_ENTITY.get()));
    public static final Supplier<WallHadeanTrophyBlock> HADEAN_BLASTER_WALL_TROPHY_BLOCK = BLOCKS.register("hadean_blaster_wall_trophy", () -> new WallHadeanTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModHadeanBlockCompat.HADEAN_BLASTER_TROPHY_ENTITY.get()));
    public static final Supplier<WallHadeanTrophyBlock> HADEAN_SMASHER_WALL_TROPHY_BLOCK = BLOCKS.register("hadean_smasher_wall_trophy", () -> new WallHadeanTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModHadeanBlockCompat.HADEAN_SMASHER_TROPHY_ENTITY.get()));
    public static final Supplier<WallDuckTrophyBlock> DUCK_ENFORCER_WALL_TROPHY_BLOCK = BLOCKS.register("duck_enforcer_wall_trophy", () -> new WallDuckTrophyBlock(BlockBehaviour.Properties.of().instabreak(), () -> ModHadeanBlockCompat.DUCK_ENFORCER_TROPHY_ENTITY.get()));
    public static final Supplier<BlockEntityType<FinalTrophyBlockEntity>> HADEAN_ENFORCER_TROPHY_ENTITY = BLOCK_ENTITIES.register("hadean_enforcer_trophy", () -> BlockEntityType.Builder.of(FinalTrophyBlockEntity::enforcer, (Block[]) new Block[]{(Block) HADEAN_ENFORCER_TROPHY_BLOCK.get(), (Block) HADEAN_ENFORCER_WALL_TROPHY_BLOCK.get()}).build(null));
    public static final Supplier<BlockEntityType<FinalTrophyBlockEntity>> HADEAN_BLASTER_TROPHY_ENTITY = BLOCK_ENTITIES.register("hadean_blaster_trophy", () -> BlockEntityType.Builder.of(FinalTrophyBlockEntity::blaster, (Block[]) new Block[]{(Block) HADEAN_BLASTER_TROPHY_BLOCK.get(), (Block) HADEAN_BLASTER_WALL_TROPHY_BLOCK.get()}).build(null));
    public static final Supplier<BlockEntityType<FinalTrophyBlockEntity>> HADEAN_SMASHER_TROPHY_ENTITY = BLOCK_ENTITIES.register("hadean_smasher_trophy", () -> BlockEntityType.Builder.of(FinalTrophyBlockEntity::smasher, (Block[]) new Block[]{(Block) HADEAN_SMASHER_TROPHY_BLOCK.get(), (Block) HADEAN_SMASHER_WALL_TROPHY_BLOCK.get()}).build(null));
    public static final Supplier<BlockEntityType<FinalTrophyBlockEntity>> DUCK_ENFORCER_TROPHY_ENTITY = BLOCK_ENTITIES.register("duck_enforcer_trophy", () -> BlockEntityType.Builder.of(FinalTrophyBlockEntity::duckenforcer, (Block[]) new Block[]{(Block) DUCK_ENFORCER_TROPHY_BLOCK.get(), (Block) DUCK_ENFORCER_WALL_TROPHY_BLOCK.get()}).build(null));
    public static final Supplier<HadeanTrophyItem> HADEAN_ENFORCER_TROPHY = ITEMS.register("hadean_enforcer_trophy", () -> new HadeanTrophyItem((FloorHadeanTrophyBlock) ((HADEAN_ENFORCER_TROPHY_BLOCK.get())), (WallHadeanTrophyBlock) ((HADEAN_ENFORCER_WALL_TROPHY_BLOCK.get())), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final Supplier<HadeanTrophyItem> HADEAN_BLASTER_TROPHY = ITEMS.register("hadean_blaster_trophy", () -> new HadeanTrophyItem((FloorHadeanTrophyBlock) ((HADEAN_BLASTER_TROPHY_BLOCK.get())), (WallHadeanTrophyBlock) ((HADEAN_BLASTER_WALL_TROPHY_BLOCK.get())), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final Supplier<HadeanTrophyItem> HADEAN_SMASHER_TROPHY = ITEMS.register("hadean_smasher_trophy", () -> new HadeanTrophyItem((FloorHadeanTrophyBlock) ((HADEAN_SMASHER_TROPHY_BLOCK.get())), (WallHadeanTrophyBlock) ((HADEAN_SMASHER_WALL_TROPHY_BLOCK.get())), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final Supplier<DuckTrophyItem> DUCK_ENFORCER_TROPHY = ITEMS.register("duck_enforcer_trophy", () -> new DuckTrophyItem((FloorDuckTrophyBlock) ((DUCK_ENFORCER_TROPHY_BLOCK.get())), (WallDuckTrophyBlock) ((DUCK_ENFORCER_WALL_TROPHY_BLOCK.get())), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ITEMS.register(bus);
    }
}

