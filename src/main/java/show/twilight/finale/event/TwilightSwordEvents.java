package show.twilight.finale.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.item.TwilightSwordItem;
import show.twilight.finale.network.TwilightSwordStatePacket;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID)
public final class TwilightSwordEvents {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TwilightFinaleMod.MOD_ID);
    // Runtime-only: neither saved, copied on respawn, nor synced as an attachment.
    private static final Supplier<AttachmentType<DashState>> DASH_STATE = ATTACHMENTS.register("twilight_sword_dash", () -> AttachmentType.builder(holder -> new DashState((Player) holder)).build());

    public static void registerAttachments(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    private static final class DashState {
        final ResourceKey<Level> dimension;
        final Set<UUID> hit = new HashSet<>();
        Vec3 direction, origin, previous;
        ItemStack sword;
        int charge, dashTicks, stalledTicks;
        float maximumDistance, attackDamage;
        double travelled;
        boolean dashing, needsRelease;

        DashState(Player player) {
            dimension = player.level().dimension();
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DashState state = player.hasData(DASH_STATE) ? player.getData(DASH_STATE) : null;
        boolean eligible = player.isAlive() && !player.isSpectator() && !player.isPassenger() && !player.isSleeping() && player.getMainHandItem().is(ModItem.TWILIGHT_SWORD.get()) && player.getOffhandItem().isEmpty();
        if (!eligible || state != null && !state.dimension.equals(player.level().dimension())) {
            if (state != null) {
                stop(player, state);
                player.removeData(DASH_STATE);
            }
            return;
        }
        boolean usingSword = player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND && player.getUseItem() == player.getMainHandItem();
        if (state == null) {
            if (!usingSword) return;
            state = player.getData(DASH_STATE);
        }
        if (state.dashing) {
            tickDash(player, state);
            return;
        }
        if (state.needsRelease) {
            if (!usingSword) player.removeData(DASH_STATE);
            return;
        }
        if (usingSword) {
            if (state.charge == 0) {
                float yaw = player.getYRot() * Mth.DEG_TO_RAD;
                state.direction = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
                state.sword = player.getMainHandItem();
                state.origin = player.position();
            }
            if (player.getMainHandItem() != state.sword) {
                stop(player, state);
                return;
            }
            state.charge = Math.min(state.charge + 1, TwilightSwordItem.MIN_CHARGE_TICKS);
            if (state.charge == 1 || player.tickCount % 5 == 0) {
                sync(player, state, TwilightSwordStatePacket.CHARGING);
            }
            if (player.tickCount % 3 == 0)
                player.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 3, 0.25, 0.3, 0.25, 0.01);
        } else if (state.charge > 0) launch(player, state);
    }

    private static void launch(ServerPlayer player, DashState state) {
        if (state.charge < TwilightSwordItem.MIN_CHARGE_TICKS || player.getMainHandItem() != state.sword) {
            stop(player, state);
            return;
        }
        // Aim when the dash actually starts, not when charging began.
        // After launch this world-space direction stays fixed.
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        state.direction = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)).normalize();
        state.maximumDistance = TwilightSwordItem.DASH_DISTANCE;
        state.attackDamage = TwilightSwordItem.DASH_DAMAGE;
        state.travelled = 0;
        state.origin = state.previous = player.position();
        state.dashTicks = 0;
        state.stalledTicks = 0;
        state.hit.clear();
        state.dashing = true;
        state.needsRelease = true;
        sync(player, state, TwilightSwordStatePacket.DASHING);
        velocity(player, state, state.maximumDistance);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1, 0.65F);
    }

    private static void tickDash(ServerPlayer player, DashState state) {
        if (player.getMainHandItem() != state.sword) {
            stop(player, state);
            return;
        }
        ServerLevel level = player.serverLevel();
        state.dashTicks++;
        Vec3 current = player.position(), movement = current.subtract(state.previous);
        double forward = movement.dot(state.direction);
        double sideways = movement.x * state.direction.z - movement.z * state.direction.x;
        double step = Math.max(0, forward), remaining = Math.max(0, state.maximumDistance - state.travelled);
        if (step > remaining + 0.01) {
            // Normally the local movement cap already does this. Keep the
            // server authoritative even if latency/knockback overshoots it.
            current = state.previous.lerp(current, remaining / step);
            player.connection.teleport(current.x, current.y, current.z, player.getYRot(), player.getXRot());
            step = remaining;
        }
        if (step > 1.0E-4) {
            damageAlongPath(player, state, state.previous, current);
            level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.5, player.getZ(), 12, 0.35, 0.35, 0.35, 0.03);
        }
        state.travelled += step;
        state.previous = current;
        state.stalledTicks = step < 0.02 ? state.stalledTicks + 1 : 0;
        // A wall or a crouching ledge can clip just one movement axis.
        // End the dash instead of repeatedly pushing/sliding along that wall.
        if (state.travelled >= state.maximumDistance - 0.01 || Math.abs(sideways) > 0.05 || forward < -0.05 || state.dashTicks >= 40 || state.dashTicks > 1 && player.horizontalCollision || state.dashTicks > 6 && state.stalledTicks >= 3) {
            stop(player, state);
            return;
        }
        velocity(player, state, state.maximumDistance - state.travelled);
    }

    private static void velocity(ServerPlayer player, DashState state, double remaining) {
        double speed = Math.min(TwilightSwordItem.DASH_SPEED, Math.max(0, remaining));
        player.setDeltaMovement(state.direction.x * speed, player.getDeltaMovement().y, state.direction.z * speed);
        player.hurtMarked = true;
    }

    private static void damageAlongPath(ServerPlayer player, DashState state, Vec3 from, Vec3 to) {
        ServerLevel level = player.serverLevel();
        double halfWidth = player.getBbWidth() / 2D;
        AABB sweep = new AABB(from, to).inflate(halfWidth + 0.4, 0, halfWidth + 0.4).expandTowards(0, player.getBbHeight(), 0);
        Vec3 start = from.add(0, player.getBbHeight() / 2D, 0), end = to.add(0, player.getBbHeight() / 2D, 0);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, sweep, entity -> entity != player && entity.isAlive() && !entity.isSpectator() && !player.isAlliedTo(entity) && player.hasLineOfSight(entity))) {
            if (victim instanceof Player other && (other.isCreative() || !player.canHarmPlayer(other) || !level.getServer().isPvpAllowed()))
                continue;
            AABB box = victim.getBoundingBox().inflate(halfWidth + 0.4, player.getBbHeight() / 2D, halfWidth + 0.4);
            if (!box.contains(start) && box.clip(start, end).isEmpty() || !state.hit.add(victim.getUUID())) continue;
            if (!CommonHooks.onPlayerAttackTarget(player, victim)) continue;
            var source = player.damageSources().playerAttack(player);
            float damage = EnchantmentHelper.modifyDamage(level, state.sword, victim, source, state.attackDamage);
            if (victim.hurt(source, damage)) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(level, victim, source, state.sword);
                TwilightSwordItem.igniteTarget(state.sword, victim, player);
                state.sword.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                victim.push(state.direction.x * 0.8, 0.25, state.direction.z * 0.8);
                victim.hurtMarked = true;
                player.setLastHurtMob(victim);
                if (state.sword.isEmpty()) break;
            }
        }
    }

    private static void stop(ServerPlayer player, DashState state) {
        if (state.dashing) {
            player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
            player.hurtMarked = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
        state.dashing = false;
        state.charge = 0;
        state.needsRelease = true;
        sync(player, state, TwilightSwordStatePacket.IDLE);
        state.hit.clear();
        state.sword = ItemStack.EMPTY;
    }

    private static void sync(ServerPlayer player, DashState state, int mode) {
        Vec3 direction = state.direction == null ? Vec3.ZERO : state.direction;
        Vec3 origin = state.origin == null ? player.position() : state.origin;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new TwilightSwordStatePacket(player.getId(), player.getUUID(), mode, state.charge, (float) direction.x, (float) direction.z, state.maximumDistance, origin.x, origin.z));
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        event.getEntity().removeData(DASH_STATE);
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DashState state = player.removeData(DASH_STATE);
            if (state != null) stop(player, state);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.Clone event) {
        event.getOriginal().removeData(DASH_STATE);
        event.getEntity().removeData(DASH_STATE);
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DashState state = player.removeData(DASH_STATE);
            if (state != null) stop(player, state);
        }
    }
}
