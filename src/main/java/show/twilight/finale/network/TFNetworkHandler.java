package show.twilight.finale.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import show.twilight.finale.client.network.FinaleClientPackets;

public final class TFNetworkHandler {
    private TFNetworkHandler() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(UpdateForceFieldPacket.TYPE, UpdateForceFieldPacket.STREAM_CODEC, (packet, context) -> context.enqueueWork(() -> FinaleClientPackets.apply(packet)));
        event.registrar("1").playToClient(AnimationPacket.TYPE, AnimationPacket.STREAM_CODEC, (packet, context) -> context.enqueueWork(() -> FinaleClientPackets.apply(packet)));
    }
}

