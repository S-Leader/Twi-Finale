package show.twilight.finale.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import show.twilight.finale.capabilities.ForceFieldCapabilityList;
import show.twilight.finale.capabilities.shield.ForceFieldHandler;
import show.twilight.finale.client.event.TwilightSwordClientEffects;
import show.twilight.finale.network.AnimationPacket;
import show.twilight.finale.network.TwilightSwordStatePacket;
import show.twilight.finale.network.UpdateForceFieldPacket;
import show.twilight.finale.runtime.entity.AbstractSkillEntity;

public final class FinaleClientPackets {
    private FinaleClientPackets() {
    }

    public static void apply(TwilightSwordStatePacket packet) {
        TwilightSwordClientEffects.apply(packet);
    }

    public static void apply(UpdateForceFieldPacket p) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(p.entityId()) instanceof LivingEntity living) {
            ForceFieldCapabilityList.get(living).ifPresent(cap ->
                    ((ForceFieldHandler) cap).applySnapshot(p.tempCount(), p.permCount(), p.maxCount(), p.breaking(), p.shaking()));
        }
    }

    public static void apply(AnimationPacket p) {
        var level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(p.entityId()) instanceof AbstractSkillEntity entity) {
            entity.getAnimations().stream().filter(a -> a.getId().equals(p.animation())).forEach(a -> {
                if (p.stop()) a.state.stop();
                else a.state.start(entity.tickCount);
            });
        }
    }
}
