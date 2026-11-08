package show.twilight.finale.event;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import show.twilight.finale.potions.AnnihilationCubeEffect;

@EventBusSubscriber(modid = "twilightforest_finale")
public final class CubeEffectCleanup {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void removed(MobEffectEvent.Remove event) {
        if (event.getEffect().value() instanceof AnnihilationCubeEffect effect) effect.cleanup(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void expired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().value() instanceof AnnihilationCubeEffect effect)
            effect.cleanup(event.getEntity());
    }
}
