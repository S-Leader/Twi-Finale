package show.twilight.finale.runtime.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import show.twilight.finale.network.AnimationPacket;

public final class MiscUtil {
    private MiscUtil() {
    }

    public static void sendAnimationPacket(Level level, int action, int entityId, ResourceLocation animation) {
        if (!level.isClientSide() && level.getEntity(entityId) != null)
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(level.getEntity(entityId), new AnimationPacket(entityId, animation, action != 0));
    }
}
