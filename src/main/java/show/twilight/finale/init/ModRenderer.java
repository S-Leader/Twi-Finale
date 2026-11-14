package show.twilight.finale.init;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import twilightforest.client.model.TFModelLayers;
import twilightforest.init.TFEntities;

import show.twilight.finale.client.model.LoyalWraithModel;
import show.twilight.finale.client.renderer.*;

public class ModRenderer {
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntity.ENGINEER_ADHERENT.get(), ColoredAdherentRenderer::new);
        event.registerEntityRenderer(ModEntity.ENGINEER_WRENCH.get(), EngineerWrenchRenderer::new);
        event.registerEntityRenderer(ModEntity.ANNIHILATION_GOLEM.get(), AnnihilationGolemRenderer::new);
        event.registerEntityRenderer(ModEntity.CASTLE_MONSTROSITY.get(), CastleMonstrosityRenderer::new);
        event.registerEntityRenderer(ModEntity.MONSTROSITY_BOMB.get(), MonstrosityBombRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.TWILIGHT_TRAP_CHEST_ENTITY.get(), TwilightTrapChestRenderer::new);
        event.registerEntityRenderer(ModEntity.HIEROPHANT_METEOR.get(), HierophantMeteorRenderer::new);
        event.registerEntityRenderer(ModEntity.HIEROPHANT_THROWN_WEAPON.get(), HierophantThrownWeaponRenderer::new);
        event.registerEntityRenderer(ModEntity.HIEROPHANT_FRAGMENT.get(), HierophantFragmentRenderer::new);
        event.registerEntityRenderer(TFEntities.ADHERENT.get(), ColoredAdherentRenderer::new);
        event.registerEntityRenderer(ModEntity.HOMING_VIS_SHARD.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntity.ADHERENT_HIGH_PRIEST.get(), AdherentHighPriestRenderer::new);
        event.registerEntityRenderer(ModEntity.CASTLE_GUARDIAN.get(), CastleGuardianRenderer::new);
        event.registerEntityRenderer(ModEntity.THE_TWILIGHT_HIEROPHANT.get(), TheTwilightHierophantRenderer::new);
        event.registerEntityRenderer(ModEntity.LOYAL_WRAITH.get(), m -> new LoyalWraithRenderer(m, new LoyalWraithModel(m.bakeLayer(TFModelLayers.WRAITH)), 0.5f));
    }
}

