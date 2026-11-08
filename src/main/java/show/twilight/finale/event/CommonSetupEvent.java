package show.twilight.finale.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import show.twilight.finale.entity.*;
import show.twilight.finale.init.ModEntity;

@net.neoforged.fml.common.EventBusSubscriber(modid = "twilightforest_finale", bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public class CommonSetupEvent {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntity.ROVING_CUBE.get(), twilightforest.entity.RovingCube.registerAttributes().build());
        event.put(ModEntity.ADHERENT_HIGH_PRIEST.get(), AdherentHighPriestEntity.createAttributes().build());
        event.put(ModEntity.THE_TWILIGHT_HIEROPHANT.get(), TheTwilightHierophantEntity.createAttributes().build());
        event.put(ModEntity.LOYAL_WRAITH.get(), LoyalWraithEntity.createAttributes().build());
        event.put(ModEntity.CASTLE_GUARDIAN.get(), CastleGuardianEntity.createAttributes().build());
        event.put(ModEntity.ANNIHILATION_HAND.get(), AnnihilationHandEntity.createAttributes().build());
    }
}

