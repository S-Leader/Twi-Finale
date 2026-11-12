package show.twilight.finale.init;

import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.item.*;
import show.twilight.finale.item.SmithingTemplateItem;
import twilightforest.init.TFArmorMaterials;
import twilightforest.item.PhantomArmorItem;
import twilightforest.util.TFToolMaterials;

import java.util.function.Supplier;

public class ModItem {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ITEM, "twilightforest_finale");

    public static final Supplier<Item> FIERY_AXE = ITEMS.register("fiery_axe", () -> new FieryAxeItem(TFToolMaterials.FIERY, new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> FIERY_HOE = ITEMS.register("fiery_hoe", () -> new FieryHoeItem(TFToolMaterials.FIERY, new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> FIERY_SHOVEL = ITEMS.register("fiery_shovel", () -> new FieryShovelItem(TFToolMaterials.FIERY, new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> KNIGHTMETAL_HOE = ITEMS.register("knightmetal_hoe", () -> new KnightmetalHoeItem(TFToolMaterials.KNIGHTMETAL, new Item.Properties()));
    public static final Supplier<Item> KNIGHTMETAL_SHOVEL = ITEMS.register("knightmetal_shovel", () -> new KnightmetalShovelItem(TFToolMaterials.KNIGHTMETAL, new Item.Properties()));
    public static final Supplier<ArmorItem> PHANTOM_LEGGINGS = ITEMS.register("phantom_leggings", () -> new PhantomLeggingsItem(TFArmorMaterials.PHANTOM, ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Supplier<ArmorItem> PHANTOM_BOOTS = ITEMS.register("phantom_boots", () -> new PhantomArmorItem(TFArmorMaterials.PHANTOM, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(30)).rarity(Rarity.UNCOMMON)));
    public static final Supplier<ArmorItem> ANNIHILATION_CHESTPLATE = ITEMS.register("annihilation_chestplate", () -> new AnnihilationChestplateItem(TFArmorMaterials.PHANTOM, ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Supplier<AdherentTrophyItem> ADHERENT_TROPHY = ITEMS.register("adherent_trophy", () -> new AdherentTrophyItem((ModBlocks.ADHERENT_TROPHY_BLOCK.get()), (ModBlocks.ADHERENT_WALL_TROPHY_BLOCK.get()), new Item.Properties().rarity(Rarity.EPIC)));
    public static final Supplier<Item> ANNIHILATION_GOLEM_TROPHY = ITEMS.register("annihilation_golem_trophy", () -> new AdherentTrophyItem(ModBlocks.ANNIHILATION_GOLEM_TROPHY_BLOCK.get(), ModBlocks.ANNIHILATION_GOLEM_WALL_TROPHY_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final Supplier<AdherentTrophyItem> HIEROPHANT_TROPHY = ITEMS.register("hierophant_trophy", () -> new AdherentTrophyItem(ModBlocks.HIEROPHANT_TROPHY_BLOCK.get(), ModBlocks.HIEROPHANT_WALL_TROPHY_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final Supplier<Item> NAGA_HELMET = ITEMS.register("naga_helmet", () -> new NagaExtraArmorItem(TFArmorMaterials.NAGA, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> NAGA_BOOTS = ITEMS.register("naga_boots", () -> new NagaExtraArmorItem(TFArmorMaterials.NAGA, ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> FINALSLAYER_SWORD = ITEMS.register("finalslayer_sword", () -> new FinalSlayerSwordItem(Tiers.DIAMOND, new Item.Properties().setNoRepair().rarity(Rarity.RARE)));
    public static final Supplier<Item> TWILIGHT_SWORD = ITEMS.register("twilight_sword", () -> new TwilightSwordItem(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final Supplier<Item> TRIPLE_CROSSBOW = ITEMS.register("triple_crossbow", () -> new TripleCrossbowItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(384)));
    public static final Supplier<Item> SEEKER_CROSSBOW = ITEMS.register("seeker_crossbow", () -> new SeekerCrossbowItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(384)));
    public static final Supplier<Item> ICE_CROSSBOW = ITEMS.register("ice_crossbow", () -> new IceCrossbowItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(384)));
    public static final Supplier<Item> ENDER_CROSSBOW = ITEMS.register("ender_crossbow", () -> new EnderCrossbowItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(384)));
    public static final Supplier<Item> CHARM_OF_LIFE_3 = ITEMS.register("charm_of_life_3", () -> new CuriosCharmItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final Supplier<Item> GLASS_HEART = ITEMS.register("glass_heart", () -> new CuriosCharmItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> PHANTOMMETAL_INGOT = ITEMS.register("phantommetal_ingot", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> TRANSFORMATION_STICK = ITEMS.register("transformation_stick", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> TRANSFORMATION_ROOT = ITEMS.register("transformation_root", () -> new TransformationRootItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final Supplier<Item> TRANSFORMATION_TOOL = ITEMS.register("transformation_tool", () -> new TransformationToolItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final Supplier<Item> PROVERBS_SCEPTER = ITEMS.register("proverbs_scepter", () -> new ProverbsWandItem(new Item.Properties().durability(99).rarity(Rarity.EPIC)));
    public static final Supplier<Item> FORCE_FIELD_SCEPTER = ITEMS.register("force_field_scepter", () -> new ForceFieldWandItem(new Item.Properties().durability(9).rarity(Rarity.EPIC)));
    public static final Supplier<Item> SHADOW_SCEPTER = ITEMS.register("shadow_scepter", () -> new ShadowWandItem(new Item.Properties().durability(99).rarity(Rarity.EPIC)));
    public static final Supplier<Item> WRAITH_SCEPTER = ITEMS.register("wraith_scepter", () -> new WraithWandItem(new Item.Properties().durability(9).rarity(Rarity.EPIC)));
    public static final Supplier<Item> ADHERENT_BOSS_SPAWNER = ITEMS.register("adherent_boss_spawner", () -> new BlockItem(ModBlocks.ADHERENT_BOSS_SPAWNER.get(), new Item.Properties()));
    public static final Supplier<Item> TWILIGHT_HIEROPHANT_BOSS_SPAWNER = ITEMS.register("twilight_hierophant_boss_spawner", () -> new BlockItem(ModBlocks.TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get(), new Item.Properties()));
    public static final Supplier<Item> ANNIHILATION_GOLEM_BOSS_SPAWNER = ITEMS.register("annihilation_golem_boss_spawner", () -> new BlockItem(ModBlocks.ANNIHILATION_GOLEM_BOSS_SPAWNER.get(), new Item.Properties()));
    public static final Supplier<Item> TWILIGHT_RETROFITTING_SMITHING_TEMPLATE = ITEMS.register("twilight_retrofitting_smithing_template", SmithingTemplateItem::createTwilightRetrofittingTemplate);
    static int adherent_bc = -16121856;
    static int adherent_hc = -15331718;
    public static final Supplier<Item> ADHERENT_SPAWN_EGG = ITEMS.register("adherent_high_priest_spawn_egg", () -> new DeferredSpawnEggItem(ModEntity.ADHERENT_HIGH_PRIEST, adherent_bc, adherent_hc, new Item.Properties()));
    static int guardian_bc = -2304820;
    static int guardian_hc = -13421768;
    public static final Supplier<Item> CASTLE_GUARDIAN_SPAWN_EGG = ITEMS.register("castle_guardian_spawn_egg", () -> new DeferredSpawnEggItem(ModEntity.CASTLE_GUARDIAN, guardian_bc, guardian_hc, new Item.Properties()));
    static int hierophant_bc = -8363878;
    static int hierophant_hc = -667584;
    public static final Supplier<Item> ANNIHILATION_GOLEM_SPAWN_EGG = ITEMS.register("annihilation_golem_spawn_egg", () -> new DeferredSpawnEggItem(ModEntity.ANNIHILATION_GOLEM, 0x666A6A, 0x25004D, new Item.Properties()));
    public static final Supplier<Item> HIEROPHANT_SPAWN_EGG = ITEMS.register("twilight_hierophant_spawn_egg", () -> new DeferredSpawnEggItem(ModEntity.THE_TWILIGHT_HIEROPHANT, hierophant_bc, hierophant_hc, new Item.Properties()));
}

