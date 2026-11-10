package show.twilight.finale.init;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.client.model.*;

public class ModModelLayers {
    public static final ModelLayerLocation ADHERENT = ModModelLayers.register("adherent_high_priest", "head");
    public static final ModelLayerLocation ENFORCER = ModModelLayers.register("hadean_enforcer", "head");
    public static final ModelLayerLocation BLASTER = ModModelLayers.register("hadean_blaster", "head");
    public static final ModelLayerLocation SMASHER = ModModelLayers.register("hadean_smasher", "head");
    public static final ModelLayerLocation DUCKENFORCER = ModModelLayers.register("duck_enforcer", "head");

    private static ModelLayerLocation register(String name) {
        return ModModelLayers.register(name, "main");
    }

    private static ModelLayerLocation register(String name, String varint) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TwilightFinaleMod.MOD_ID, name), varint);
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AdherentHighPriestTrophy.LAYER_LOCATION, AdherentHighPriestTrophy::createHead);
        event.registerLayerDefinition(HadeanEnforcerTropy.LAYER_LOCATION, HadeanEnforcerTropy::createHead);
        event.registerLayerDefinition(HadeanBlasterTropy.LAYER_LOCATION, HadeanBlasterTropy::createHead);
        event.registerLayerDefinition(HadeanSmasherTropy.LAYER_LOCATION, HadeanSmasherTropy::createHead);
        event.registerLayerDefinition(DuckEnforcerTropy.LAYER_LOCATION, DuckEnforcerTropy::createHead);
        event.registerLayerDefinition(AdherentHighPriestModel.LAYER_LOCATION, AdherentHighPriestModel::createBodyLayer);
        event.registerLayerDefinition(CastleGuardianModel.LAYER_LOCATION, CastleGuardianModel::createBodyLayer);
        event.registerLayerDefinition(TheTwilightHierophantModel.LAYER_LOCATION, () -> TheTwilightHierophantModel.createBodyLayer(false, 0.0f));
        event.registerLayerDefinition(TheTwilightHierophantModel.LAYER_LOCATION_SLIM, () -> TheTwilightHierophantModel.createBodyLayer(true, 0.0f));
        event.registerLayerDefinition(TheTwilightHierophantModel.LAYER_LOCATION_OUTER, () -> TheTwilightHierophantModel.createBodyLayer(false, 0.5f));
        event.registerLayerDefinition(TheTwilightHierophantModel.LAYER_LOCATION_OUTER_SLIM, () -> TheTwilightHierophantModel.createBodyLayer(true, 0.5f));
        event.registerLayerDefinition(TheTwilightHierophantModel.LAYER_LOCATION_O, ModModelLayers::createOuterArmourLayer);
        event.registerLayerDefinition(TheTwilightHierophantModel.LAYER_LOCATION_I, ModModelLayers::createInnerArmourLayer);
    }

    public static LayerDefinition createInnerArmourLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(LayerDefinitions.INNER_ARMOR_DEFORMATION, 0.0f), 64, 32);
    }

    public static LayerDefinition createOuterArmourLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(LayerDefinitions.OUTER_ARMOR_DEFORMATION, 0.0f), 64, 32);
    }
}

