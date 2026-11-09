package show.twilight.finale.event;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

@EventBusSubscriber(modid = "twilightforest_finale")
public final class HierophantDebrisEvents {
    @SubscribeEvent
    public static void onDebrisLanded(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof FallingBlockEntity brick && event.getLevel() instanceof ServerLevel server
                && brick.getRemovalReason() == Entity.RemovalReason.DISCARDED && brick.onGround()
                && brick.getPersistentData().getBoolean("FinaleCeilingDebris")) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, brick.getBlockState()),
                    brick.getX(), brick.getY() + 0.3, brick.getZ(), 24, 0.4, 0.25, 0.4, 0.08);
        }
    }
}
