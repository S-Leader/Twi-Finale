package show.twilight.finale.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import show.twilight.finale.capabilities.shield.IForceFieldCapability;

public record UpdateForceFieldPacket(int entityId, int tempCount, int permCount, int maxCount, boolean breaking,
                                     boolean shaking) implements CustomPacketPayload {
    public static final Type<UpdateForceFieldPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "force_field"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateForceFieldPacket> STREAM_CODEC = StreamCodec.of((buf, p) -> {
        buf.writeInt(p.entityId);
        buf.writeInt(p.tempCount);
        buf.writeInt(p.permCount);
        buf.writeInt(p.maxCount);
        buf.writeBoolean(p.breaking);
        buf.writeBoolean(p.shaking);
    }, buf -> new UpdateForceFieldPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean()));

    public UpdateForceFieldPacket(Entity entity, IForceFieldCapability cap) {
        this(entity.getId(), cap.getTemporaryFields(), cap.getPermanentFields(), cap.getMaxFields(), entity.tickCount == cap.getLastBreakTick(), entity.tickCount == cap.getLastShakeTick());
    }

    @Override
    public Type<UpdateForceFieldPacket> type() {
        return TYPE;
    }
}

