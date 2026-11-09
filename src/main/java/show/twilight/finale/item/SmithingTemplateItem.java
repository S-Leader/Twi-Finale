package show.twilight.finale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class SmithingTemplateItem extends net.minecraft.world.item.SmithingTemplateItem {
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final Component TWILIGHT_RETROFITTING = Component.translatable("item.twilightforest_finale.twilight_retrofitting.desc").withStyle(TITLE_FORMAT);
    private static final Component TWILIGHT_RETROFITTING_APPLIES_TO = Component.translatable("item.twilightforest_finale.twilight_retrofitting.applies_to.desc").withStyle(DESCRIPTION_FORMAT);
    private static final Component TWILIGHT_RETROFITTING_INGREDIENTS = Component.translatable("item.twilightforest_finale.twilight_retrofitting.ingredients.desc").withStyle(DESCRIPTION_FORMAT);
    private static final Component TWILIGHT_RETROFITTING_BASE_SLOT_DESCRIPTION = Component.translatable("item.twilightforest_finale.twilight_retrofitting.base_slot.desc");
    private static final Component TWILIGHT_RETROFITTING_ADDITIONS_SLOT_DESCRIPTION = Component.translatable("item.twilightforest_finale.twilight_retrofitting.additions_slot.desc");
    private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_SLOT_BOW = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "item/empty_slot_bow");
    private static final ResourceLocation EMPTY_SLOT_INGOT = ResourceLocation.withDefaultNamespace("item/empty_slot_ingot");
    private static final ResourceLocation EMPTY_SLOT_ROOT = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "item/empty_slot_root");

    public SmithingTemplateItem(Component appliesTo, Component ingredients, Component upgradeDescription, Component baseSlotDescription, Component additionsSlotDescription, List<ResourceLocation> baseSlotEmptyIcons, List<ResourceLocation> additionalSlotEmptyIcons) {
        super(appliesTo, ingredients, upgradeDescription, baseSlotDescription, additionsSlotDescription, baseSlotEmptyIcons, additionalSlotEmptyIcons);
    }

    public static SmithingTemplateItem createTwilightRetrofittingTemplate() {
        return new SmithingTemplateItem(TWILIGHT_RETROFITTING_APPLIES_TO, TWILIGHT_RETROFITTING_INGREDIENTS, TWILIGHT_RETROFITTING, TWILIGHT_RETROFITTING_BASE_SLOT_DESCRIPTION, TWILIGHT_RETROFITTING_ADDITIONS_SLOT_DESCRIPTION, SmithingTemplateItem.createUpgradeIconList(), SmithingTemplateItem.createUpgradeMaterialList());
    }

    private static List<ResourceLocation> createUpgradeIconList() {
        return List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_BOW);
    }

    private static List<ResourceLocation> createUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_INGOT, EMPTY_SLOT_ROOT);
    }
}

