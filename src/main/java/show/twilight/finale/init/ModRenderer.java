package show.twilight.finale.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import show.twilight.finale.client.model.LoyalWraithModel;
import show.twilight.finale.client.renderer.*;
import twilightforest.client.model.TFModelLayers;

@net.neoforged.fml.common.EventBusSubscriber(bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModRenderer {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntity.HIEROPHANT_FRAGMENT.get(), show.twilight.finale.client.renderer.HierophantFragmentRenderer::new);
        event.registerEntityRenderer(ModEntity.ROVING_CUBE.get(), twilightforest.client.renderer.entity.RovingCubeRenderer::new);
        event.registerEntityRenderer(twilightforest.init.TFEntities.ADHERENT.get(), show.twilight.finale.client.renderer.ColoredAdherentRenderer::new);
        event.registerEntityRenderer(ModEntity.HOMING_VIS_SHARD.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntity.ADHERENT_HIGH_PRIEST.get(), AdherentHighPriestRenderer::new);
        event.registerEntityRenderer(ModEntity.CASTLE_GUARDIAN.get(), CastleGuardianRenderer::new);
        event.registerEntityRenderer(ModEntity.THE_TWILIGHT_HIEROPHANT.get(), TheTwilightHierophantRenderer::new);
        event.registerEntityRenderer(ModEntity.LOYAL_WRAITH.get(), m -> new LoyalWraithRenderer(m, new LoyalWraithModel(m.bakeLayer(TFModelLayers.WRAITH)), 0.5f));
        event.registerEntityRenderer(ModEntity.ANNIHILATION_HAND.get(), AnnihilationHandRenderer::new);
    }
}

