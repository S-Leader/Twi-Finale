package show.twilight.finale.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.TwilightFinaleMod;
import twilightforest.config.TFConfig;
import twilightforest.init.TFItems;

import java.util.function.Supplier;

public class ModTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TwilightFinaleMod.MOD_ID);
    public static final Supplier<CreativeModeTab> ITEMS = TABS.register("items", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.twilightforest_finale.items")).icon(() -> new ItemStack(TFItems.CUBE_OF_ANNIHILATION.get())).displayItems((parameters, output) -> {
        output.accept(ModItem.KNIGHTMETAL_SHOVEL.get());
        output.accept(ModItem.KNIGHTMETAL_HOE.get());
        output.accept(ModItem.FIERY_AXE.get());
        output.accept(ModItem.FIERY_SHOVEL.get());
        output.accept(ModItem.FIERY_HOE.get());
        output.accept(ModItem.TWILIGHT_SWORD.get());
        ModTab.generateGearWithEnchants(output, ModItem.FINALSLAYER_SWORD.get(), new EnchantmentInstance(parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 4), new EnchantmentInstance(parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING), 3), new EnchantmentInstance(parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SWEEPING_EDGE), 2), new EnchantmentInstance(parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), 2));
        output.accept(ModItem.TWILIGHT_RETROFITTING_SMITHING_TEMPLATE.get());
        output.accept(ModItem.PHANTOMMETAL_INGOT.get());
        output.accept(ModItem.TRANSFORMATION_STICK.get());
        output.accept(ModItem.TRANSFORMATION_ROOT.get());
        output.accept(ModItem.TRANSFORMATION_TOOL.get());
        output.accept(ModItem.PHANTOM_LEGGINGS.get());
        output.accept(ModItem.PHANTOM_BOOTS.get());
        output.accept(ModItem.ANNIHILATION_CHESTPLATE.get());
        ModTab.generateGearWithEnchants(output, ModItem.NAGA_HELMET.get(), new EnchantmentInstance(parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION), 3));
        ModTab.generateGearWithEnchants(output, ModItem.NAGA_BOOTS.get(), new EnchantmentInstance(parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FEATHER_FALLING), 5), new EnchantmentInstance(parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_PROTECTION), 3));
        output.accept(ModItem.TRIPLE_CROSSBOW.get());
        output.accept(ModItem.SEEKER_CROSSBOW.get());
        output.accept(ModItem.ICE_CROSSBOW.get());
        output.accept(ModItem.ENDER_CROSSBOW.get());
        output.accept(ModItem.PROVERBS_SCEPTER.get());
        output.accept(ModItem.FORCE_FIELD_SCEPTER.get());
        output.accept(ModItem.SHADOW_SCEPTER.get());
        output.accept(ModItem.WRAITH_SCEPTER.get());
        output.accept(ModItem.GLASS_HEART.get());
        output.accept(ModItem.CHARM_OF_LIFE_3.get());
        output.accept(ModItem.ADHERENT_BOSS_SPAWNER.get());
        output.accept(ModItem.ANNIHILATION_GOLEM_BOSS_SPAWNER.get());
        output.accept(ModItem.ANNIHILATION_GOLEM_TROPHY.get());
        output.accept(ModItem.ANNIHILATION_GOLEM_SPAWN_EGG.get());
        output.accept(ModItem.TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get());
        output.accept(ModItem.ADHERENT_TROPHY.get());
        output.accept(ModItem.HIEROPHANT_TROPHY.get());
        output.accept(ModItem.ADHERENT_SPAWN_EGG.get());
        output.accept(ModItem.CASTLE_GUARDIAN_SPAWN_EGG.get());
        output.accept(ModItem.HIEROPHANT_SPAWN_EGG.get());
    }).build());

    private static void generateGearWithEnchants(CreativeModeTab.Output output, Item item, EnchantmentInstance... instances) {
        ItemStack stack = new ItemStack(item);
        if (TFConfig.defaultItemEnchants) {
            for (EnchantmentInstance enchant : instances) {
                stack.enchant(enchant.enchantment, enchant.level);
            }
        }
        output.accept(stack);
    }
}

