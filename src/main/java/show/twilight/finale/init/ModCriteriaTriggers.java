package show.twilight.finale.init;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.TwilightFinaleMod;

public final class ModCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, TwilightFinaleMod.MOD_ID);
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> DISPEL_FORCE_FIELD =
            TRIGGERS.register("dispel_force_field", PlayerTrigger::new);

    private ModCriteriaTriggers() {
    }
}
