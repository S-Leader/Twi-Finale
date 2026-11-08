package show.twilight.finale.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.item.FinalTrophyItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import twilightforest.compat.curios.renderer.CharmOfLifeNecklaceRenderer;
import twilightforest.compat.curios.renderer.CurioHeadRenderer;

public final class ModCuriosCompat {
    private ModCuriosCompat() {
    }

    public static void registerItems() {
        ICurioItem curio = new ICurioItem() {
            @Override
            public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
                return true;
            }
        };
        ModItem.ITEMS.getEntries().forEach(item -> {
            if (item.get() instanceof show.twilight.finale.item.CurioItem) CuriosApi.registerCurio(item.get(), curio);
        });
    }

    // Modern Twilight Forest itself handles Charm of Keeping for Curios slots.
    public static void keepCurios(DropRulesEvent event) {
    }

    public static void registerCurioLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    public static void registerCurioRenderers(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(ModItem.CHARM_OF_LIFE_3.get(), () -> new CharmOfLifeNecklaceRenderer(0xFFFF8080));
            CuriosRendererRegistry.register(ModItem.GLASS_HEART.get(), () -> new CharmOfLifeNecklaceRenderer(0xFFFF8080));
            CuriosRendererRegistry.register(ModItem.ADHERENT_TROPHY.get(), CurioHeadRenderer::new);
        });
    }

    public static boolean isTrophyCurioEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).flatMap(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof FinalTrophyItem)).filter(result -> result.slotContext().visible()).isPresent();
    }
}

