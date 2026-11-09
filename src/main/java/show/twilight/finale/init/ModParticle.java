package show.twilight.finale.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import show.twilight.finale.client.particle.AnnihilateHandParticle;

import java.util.function.Supplier;

@EventBusSubscriber(modid = "twilightforest_finale", bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModParticle {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE, (String) "twilightforest_finale");
    public static final Supplier<SimpleParticleType> ANNIHILATE_HAND = PARTICLE_TYPES.register("annihilate_hand", () -> new SimpleParticleType(false));

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet((ParticleType) ANNIHILATE_HAND.get(), AnnihilateHandParticle.Factory::new);
    }
}

