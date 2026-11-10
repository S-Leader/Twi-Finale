package show.twilight.finale;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import show.twilight.finale.capabilities.ForceFieldCapabilityList;
import show.twilight.finale.capabilities.hierophant.HierophantShieldCapabilities;
import show.twilight.finale.client.animation.TwilightSwordPlayerAnimation;
import show.twilight.finale.client.event.ModClientSetupEvent;
import show.twilight.finale.compat.curios.ModCuriosCompat;
import show.twilight.finale.compat.hadean.ModHadeanBlockCompat;
import show.twilight.finale.config.HierophantConfig;
import show.twilight.finale.event.CommonSetupEvent;
import show.twilight.finale.init.*;
import show.twilight.finale.network.TFNetworkHandler;
import show.twilight.finale.resource.ModResourcePack;
import show.twilight.finale.world.castle.FinaleCastlePieces;

@Mod(TwilightFinaleMod.MOD_ID)
public class TwilightFinaleMod {
    public static final String MOD_ID = "twilightforest_finale";
    public static final Logger LOGGER = LogManager.getLogger();

    public TwilightFinaleMod(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, HierophantConfig.SPEC);
        ModItem.ITEMS.register(bus);
        ModEntity.ENTITY_TYPES.register(bus);
        ModEffect.MOB_EFFECTS.register(bus);
        ModTab.TABS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlocks.BLOCK_ENTITIES.register(bus);
        ModParticle.PARTICLE_TYPES.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModCriteriaTriggers.TRIGGERS.register(bus);
        ForceFieldCapabilityList.TYPES.register(bus);
        bus.addListener(HierophantShieldCapabilities::register);
        FinaleCastlePieces.TYPES.register(bus);
        bus.addListener(TFNetworkHandler::register);
        if (ModList.get().isLoaded("curios")) {
            NeoForge.EVENT_BUS.addListener(ModCuriosCompat::keepCurios);
            bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(ModCuriosCompat::registerItems));
        }

        bus.addListener(CommonSetupEvent::registerAttributes);
        bus.addListener(ModResourcePack::addPackFinders);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            bus.addListener(TwilightSwordPlayerAnimation::registerExtensions);
            bus.addListener(ModModelLayers::registerLayerDefinitions);
            bus.addListener(ModRenderer::registerEntityRenderers);
            bus.addListener(ModParticle::registerFactories);


            bus.addListener(ModClientSetupEvent::registerExtraModels);
            bus.addListener(ModClientSetupEvent::clientSetup);
            bus.addListener(ModClientSetupEvent::registerLayerDefinitions);
            bus.addListener(ModClientSetupEvent::registerEntityRenderers);
            bus.addListener(ModClientSetupEvent::attachRenderLayers);
        }
        if (ModList.get().isLoaded("hadean_breathe")) ModHadeanBlockCompat.register(bus);
    }

    public static ResourceLocation prefix(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}

