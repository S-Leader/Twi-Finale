package show.twilight.finale.event;

import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.entity.AnnihilationGolemEntity;
import show.twilight.finale.entity.CastleGuardianEntity;
import show.twilight.finale.entity.CastleMonstrosityEntity;
import show.twilight.finale.entity.EngineerAdherentEntity;
import show.twilight.finale.entity.LoyalWraithEntity;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModEntity;

public class CommonSetupEvent {

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntity.ENGINEER_ADHERENT.get(), EngineerAdherentEntity.createAttributes().build());
        event.put(ModEntity.ANNIHILATION_GOLEM.get(), AnnihilationGolemEntity.createAttributes().build());
        event.put(ModEntity.CASTLE_MONSTROSITY.get(), CastleMonstrosityEntity.createAttributes().build());
        event.put(ModEntity.ADHERENT_HIGH_PRIEST.get(), AdherentHighPriestEntity.createAttributes().build());
        event.put(ModEntity.THE_TWILIGHT_HIEROPHANT.get(), TheTwilightHierophantEntity.createAttributes().build());
        event.put(ModEntity.LOYAL_WRAITH.get(), LoyalWraithEntity.createAttributes().build());
        event.put(ModEntity.CASTLE_GUARDIAN.get(), CastleGuardianEntity.createAttributes().build());
    }
}

