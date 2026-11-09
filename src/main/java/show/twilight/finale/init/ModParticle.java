package show.twilight.finale.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.client.particle.AnnihilateHandParticle;
import show.twilight.finale.client.particle.HierophantRewardParticle;

import java.util.function.Supplier;

public class ModParticle {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, TwilightFinaleMod.MOD_ID);
    public static final Supplier<SimpleParticleType> ANNIHILATE_HAND = PARTICLE_TYPES.register("annihilate_hand", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> HIEROPHANT_REWARD = PARTICLE_TYPES.register("hierophant_reward", () -> new SimpleParticleType(false));

    public static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ANNIHILATE_HAND.get(), AnnihilateHandParticle.Factory::new);
        event.registerSpriteSet(HIEROPHANT_REWARD.get(), HierophantRewardParticle.Provider::new);
    }
}

