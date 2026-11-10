package show.twilight.finale.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import show.twilight.finale.TwilightFinaleMod;

import java.util.UUID;

/**
 * Server-owned charge/dash state, shared with the wielder and nearby viewers.
 */
public record TwilightSwordStatePacket(int entityId, UUID playerId, int mode, int chargeTicks,
                                       float directionX, float directionZ, float distance,
                                       double originX, double originZ) implements CustomPacketPayload {
    public static final int IDLE = 0, CHARGING = 1, DASHING = 2;
    public static final Type<TwilightSwordStatePacket> TYPE = new Type<>(TwilightFinaleMod.prefix("twilight_sword_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TwilightSwordStatePacket> STREAM_CODEC = StreamCodec.of((buf, p) -> {
        buf.writeVarInt(p.entityId);
        buf.writeUUID(p.playerId);
        buf.writeVarInt(p.mode);
        buf.writeVarInt(p.chargeTicks);
        buf.writeFloat(p.directionX);
        buf.writeFloat(p.directionZ);
        buf.writeFloat(p.distance);
        buf.writeDouble(p.originX);
        buf.writeDouble(p.originZ);
    }, buf -> new TwilightSwordStatePacket(buf.readVarInt(), buf.readUUID(), buf.readVarInt(), buf.readVarInt(),
            buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readDouble(), buf.readDouble()));

    @Override
    public Type<TwilightSwordStatePacket> type() {
        return TYPE;
    }
}
