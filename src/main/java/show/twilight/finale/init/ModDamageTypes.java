package show.twilight.finale.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import show.twilight.finale.TwilightFinaleMod;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> MONSTROSITY_FLAME = ResourceKey.create(Registries.DAMAGE_TYPE,
            TwilightFinaleMod.prefix("monstrosity_flame"));

    private ModDamageTypes() {
    }
}
