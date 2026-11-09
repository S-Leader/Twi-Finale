package show.twilight.finale.client.event;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.FireworkEntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import show.twilight.finale.client.layer.ForceFieldLayer;
import show.twilight.finale.client.model.FinaleExtraModels;
import show.twilight.finale.client.renderer.*;
import show.twilight.finale.compat.hadean.HadeanClientCompat;
import show.twilight.finale.init.ModBlocks;
import show.twilight.finale.init.ModEntity;
import show.twilight.finale.init.ModItem;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import twilightforest.compat.curios.renderer.CurioHeadRenderer;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@EventBusSubscriber(modid = "twilightforest_finale", value = {Dist.CLIENT}, bus = EventBusSubscriber.Bus.MOD)
public class ModClientSetupEvent {

    @SubscribeEvent
    public static void registerExtraModels(ModelEvent.RegisterAdditional event) {
        event.register(FinaleExtraModels.DIAMOND_BACKPLATE);
        event.register(FinaleExtraModels.PLATINUM_BACKPLATE);

    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItem.TRIPLE_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, time) -> entity != null ? (CrossbowItem.isCharged(stack) ? 0.0f : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(stack, entity)) : 0.0f);
            ItemProperties.register(ModItem.TRIPLE_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pulling"), (stack, level, entity, time) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.TRIPLE_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("charged"), (stack, level, entity, time) -> CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.TRIPLE_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("firework"), (stack, level, entity, time) -> CrossbowItem.isCharged(stack) && stack.getOrDefault(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES, net.minecraft.world.item.component.ChargedProjectiles.EMPTY).contains(Items.FIREWORK_ROCKET) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.ICE_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, time) -> entity != null ? (CrossbowItem.isCharged(stack) ? 0.0f : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(stack, entity)) : 0.0f);
            ItemProperties.register(ModItem.ICE_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pulling"), (stack, level, entity, time) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.ICE_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("charged"), (stack, level, entity, time) -> CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.ICE_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("firework"), (stack, level, entity, time) -> CrossbowItem.isCharged(stack) && stack.getOrDefault(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES, net.minecraft.world.item.component.ChargedProjectiles.EMPTY).contains(Items.FIREWORK_ROCKET) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.ENDER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, time) -> entity != null ? (CrossbowItem.isCharged(stack) ? 0.0f : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(stack, entity)) : 0.0f);
            ItemProperties.register(ModItem.ENDER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pulling"), (stack, level, entity, time) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.ENDER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("charged"), (stack, level, entity, time) -> CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.ENDER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("firework"), (stack, level, entity, time) -> CrossbowItem.isCharged(stack) && stack.getOrDefault(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES, net.minecraft.world.item.component.ChargedProjectiles.EMPTY).contains(Items.FIREWORK_ROCKET) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.SEEKER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, time) -> entity != null ? (CrossbowItem.isCharged(stack) ? 0.0f : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(stack, entity)) : 0.0f);
            ItemProperties.register(ModItem.SEEKER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("pulling"), (stack, level, entity, time) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.SEEKER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("charged"), (stack, level, entity, time) -> CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.SEEKER_CROSSBOW.get(), ResourceLocation.withDefaultNamespace("firework"), (stack, level, entity, time) -> CrossbowItem.isCharged(stack) && stack.getOrDefault(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES, net.minecraft.world.item.component.ChargedProjectiles.EMPTY).contains(Items.FIREWORK_ROCKET) ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.TRANSFORMATION_ROOT.get(), ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "tool_type"), (stack, level, entity, time) -> show.twilight.finale.util.ItemData.getFloat(stack, "toolType"));
            ItemProperties.register(ModItem.TRANSFORMATION_ROOT.get(), ResourceLocation.withDefaultNamespace("blocking"), (stack, level, entity, time) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);
            ItemProperties.register(ModItem.TRANSFORMATION_TOOL.get(), ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "tool_type"), (stack, level, entity, time) -> show.twilight.finale.util.ItemData.getFloat(stack, "toolType"));
            ItemProperties.register(ModItem.TRANSFORMATION_TOOL.get(), ResourceLocation.withDefaultNamespace("blocking"), (stack, level, entity, time) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);
            BlockEntityRenderers.register(ModBlocks.ADHERENT_TROPHY_ENTITY.get(), AdherentTrophyRenderer::new);
            BlockEntityRenderers.register(ModBlocks.HIEROPHANT_TROPHY_ENTITY.get(), c -> new show.twilight.finale.client.renderer.FinalBossTrophyRenderer(c.getModelSet()));
            try {
                Class.forName(AdherentTrophyRenderer.class.getName());
                Class.forName(HadeanEnforcerTrophyRenderer.class.getName());
                Class.forName(HadeanBlasterTrophyRenderer.class.getName());
                Class.forName(HadeanSmasherTrophyRenderer.class.getName());
                Class.forName(DuckEnforcerTrophyRenderer.class.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (ModList.get().isLoaded("hadean_breathe")) {
                HadeanClientCompat.registerBlockEntityRenderers();
            }
            if (ModList.get().isLoaded("curios")) ModClientSetupEvent.registerCuriosRenderers();
        });
    }

    private static void registerCuriosRenderers() {
        CuriosRendererRegistry.register(ModItem.CHARM_OF_LIFE_3.get(), () -> new twilightforest.compat.curios.renderer.CharmOfLifeNecklaceRenderer(0xFFFF8080));
        CuriosRendererRegistry.register(ModItem.GLASS_HEART.get(), () -> new twilightforest.compat.curios.renderer.CharmOfLifeNecklaceRenderer(0xFFFF8080));
        CuriosRendererRegistry.register(ModItem.ADHERENT_TROPHY.get(), CurioHeadRenderer::new);
        CuriosRendererRegistry.register(ModItem.HIEROPHANT_TROPHY.get(), CurioHeadRenderer::new);
        if (ModList.get().isLoaded("hadean_breathe")) {
            HadeanClientCompat.registerCuriosRenderers();
        }
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntity.SEEKER_FIREWORK_ROCKET.get(), FireworkEntityRenderer::new);
        event.registerEntityRenderer(ModEntity.ANNIHILATION_BOLT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntity.PROVERBS_BOLT.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ForceFieldLayer.LAYER_LOCATION, ForceFieldLayer::createLayerDefinition);
    }

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void attachRenderLayers(EntityRenderersEvent.AddLayers event) {
        // Use the public NeoForge API; no reflection into renderer internals or duplicate player layers.
        Set<LivingEntityRenderer> attached = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof LivingEntityRenderer renderer && attached.add(renderer)) {
                renderer.addLayer(new ForceFieldLayer(renderer, event.getEntityModels().bakeLayer(ForceFieldLayer.LAYER_LOCATION)));
            }
        }
        for (var type : event.getEntityTypes()) {
            // The priest adds its shield before the glow layer in its own constructor.
            if (event.getRenderer(type) instanceof LivingEntityRenderer renderer
                    && !(renderer instanceof AdherentHighPriestRenderer) && attached.add(renderer)) {
                renderer.addLayer(new ForceFieldLayer(renderer, event.getEntityModels().bakeLayer(ForceFieldLayer.LAYER_LOCATION)));
            }
        }
    }
}

