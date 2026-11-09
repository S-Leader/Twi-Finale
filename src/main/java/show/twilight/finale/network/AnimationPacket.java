package show.twilight.finale.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import show.twilight.finale.TwilightFinaleMod;

public record AnimationPacket(int entityId, ResourceLocation animation, boolean stop) implements CustomPacketPayload {
    public static final Type<AnimationPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TwilightFinaleMod.MOD_ID, "animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationPacket> STREAM_CODEC = StreamCodec.of((buf, p) -> {
        buf.writeVarInt(p.entityId);
        buf.writeResourceLocation(p.animation);
        buf.writeBoolean(p.stop);
    }, buf -> new AnimationPacket(buf.readVarInt(), buf.readResourceLocation(), buf.readBoolean()));

    @Override
    public Type<AnimationPacket> type() {
        return TYPE;
    }
}
