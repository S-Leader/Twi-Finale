package show.twilight.finale.init;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.potions.AnnihilationCubeEffect;

public class ModEffect {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT, (String) "twilightforest");
    public static final net.neoforged.neoforge.registries.DeferredHolder<MobEffect, MobEffect> ANNIHILATION_CUBE = MOB_EFFECTS.register("annihilation_cube", AnnihilationCubeEffect::new);
}

