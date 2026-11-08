package show.twilight.finale.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import show.twilight.finale.capabilities.ForceFieldCapabilityList;
import show.twilight.finale.capabilities.shield.IForceFieldCapability;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.entity.projectile.ProverbsBolt;
import show.twilight.finale.network.UpdateForceFieldPacket;

@net.neoforged.fml.common.EventBusSubscriber
public class ForceFieldEvents {
    @SubscribeEvent
    public static void updateCaps(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        ForceFieldCapabilityList.get(event.getEntity()).ifPresent(IForceFieldCapability::update);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        ForceFieldCapabilityList.get(entity).ifPresent(cap -> {
            if (cap.getFieldsLeft() > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                if (entity instanceof AdherentHighPriestEntity) {
                    if (source.getDirectEntity() instanceof ProverbsBolt) {
                        cap.breakField();
                    } else {
                        cap.triggerShake();
                        Entity patt1992$temp = source.getEntity();
                        if (patt1992$temp instanceof ServerPlayer) {
                            ServerPlayer player = (ServerPlayer) patt1992$temp;
                            player.displayClientMessage((Component) Component.translatable((String) "message.twilight_finale.shield_no_break"), true);
                        }
                    }
                } else {
                    cap.breakField();
                }
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ForceFieldCapabilityList.get(event.getOriginal()).ifPresent(oldCap -> ForceFieldCapabilityList.get(event.getEntity()).ifPresent(newCap -> newCap.deserializeNBT(oldCap.serializeNBT())));
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer) player;
            ForceFieldEvents.updateCapabilities(player2, (Entity) player2);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer) player;
            ForceFieldEvents.updateCapabilities(player2, (Entity) player2);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer) player;
            ForceFieldEvents.updateCapabilities(player2, (Entity) player2);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer) player;
            ForceFieldEvents.updateCapabilities(player2, event.getTarget());
        }
    }

    private static void updateCapabilities(ServerPlayer clientTarget, Entity shielded) {
        ForceFieldCapabilityList.get(shielded).ifPresent(cap -> PacketDistributor.sendToPlayer(clientTarget, new UpdateForceFieldPacket(shielded, cap)));
    }
}

