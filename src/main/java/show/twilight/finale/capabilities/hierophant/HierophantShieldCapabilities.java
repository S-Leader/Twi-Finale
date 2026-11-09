package show.twilight.finale.capabilities.hierophant;

import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.init.ModEntity;

/**
 * Only a capability provider is registered; no new entity type or instance.
 */
public final class HierophantShieldCapabilities {
    public static final EntityCapability<IHierophantShield, Void> SHIELD = EntityCapability.createVoid(
            TwilightFinaleMod.prefix("hierophant_shield"), IHierophantShield.class);

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerEntity(SHIELD, ModEntity.THE_TWILIGHT_HIEROPHANT.get(), (boss, context) -> boss.getCubeShield());
    }

    private HierophantShieldCapabilities() {
    }
}
