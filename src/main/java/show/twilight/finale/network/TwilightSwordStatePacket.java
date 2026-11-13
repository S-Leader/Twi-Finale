package show.twilight.finale.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import show.twilight.finale.TwilightFinaleMod;

import java.util.UUID;

/**
 * Server-owned charge/dash state, shared with the wielder and nearby viewers.
 */
public record TwilightSwordStatePacket(int entityId, UUID playerId, int mode,
                                       int chargeTicks) implements CustomPacketPayload {
    public static final int IDLE = 0, CHARGING = 1, DASHING = 2;
    public static final Type<TwilightSwordStatePacket> TYPE = new Type<>(TwilightFinaleMod.prefix("twilight_sword_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TwilightSwordStatePacket> STREAM_CODEC = StreamCodec.of((buf, p) -> {
        buf.writeVarInt(p.entityId);
        buf.writeUUID(p.playerId);
        buf.writeVarInt(p.mode);
        buf.writeVarInt(p.chargeTicks);
    }, buf -> new TwilightSwordStatePacket(buf.readVarInt(), buf.readUUID(), buf.readVarInt(), buf.readVarInt()));

    @Override
    public Type<TwilightSwordStatePacket> type() {
        return TYPE;
    }
}
