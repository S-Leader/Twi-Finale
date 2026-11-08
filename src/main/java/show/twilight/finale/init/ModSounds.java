package show.twilight.finale.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.TwilightFinaleMod;

import java.util.function.Supplier;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TwilightFinaleMod.MOD_ID);
    public static final Supplier<SoundEvent> GUARDIAN_TARGETING = SOUNDS.register("guardian_targeting",
            () -> SoundEvent.createVariableRangeEvent(TwilightFinaleMod.prefix("guardian_targeting")));

    private ModSounds() {
    }
}
