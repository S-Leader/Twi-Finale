package show.twilight.finale.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import show.twilight.finale.capabilities.shield.ForceFieldHandler;
import show.twilight.finale.capabilities.shield.IForceFieldCapability;

import java.util.Optional;
import java.util.function.Supplier;

public final class ForceFieldCapabilityList {
    public static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "twilightforest_finale");
    public static final Supplier<AttachmentType<ForceFieldHandler>> FORCE_FIELD = TYPES.register("force_field", () ->
            AttachmentType.builder(ForceFieldHandler::new).serialize(CompoundTag.CODEC.xmap(tag -> {
                ForceFieldHandler handler = new ForceFieldHandler();
                handler.deserializeNBT(tag);
                return handler;
            }, ForceFieldHandler::serializeNBT)).copyOnDeath().build());

    public static Optional<IForceFieldCapability> get(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return Optional.empty();
        ForceFieldHandler handler = living.getData(FORCE_FIELD);
        handler.setEntity(living);
        return Optional.of(handler);
    }
}

