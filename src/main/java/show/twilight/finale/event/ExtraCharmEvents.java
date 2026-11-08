package show.twilight.finale.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import show.twilight.finale.capabilities.ForceFieldCapabilityList;
import show.twilight.finale.init.ModItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import twilightforest.entity.CharmEffect;
import twilightforest.init.TFEntities;
import twilightforest.init.TFSounds;
import twilightforest.init.TFStats;
import twilightforest.util.TFItemStackUtils;

import java.util.Optional;

@net.neoforged.fml.common.EventBusSubscriber(modid = "twilightforest_finale")
public class ExtraCharmEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void applyDeathItems(LivingDeathEvent event) {
        Player player;
        block5:
        {
            block4:
            {
                LivingEntity living = event.getEntity();
                if (living.level().isClientSide() || !(living instanceof Player)) break block4;
                player = (Player) living;
                if (!(living instanceof FakePlayer) && !player.isCreative() && !player.isSpectator()) break block5;
            }
            return;
        }
        if (ExtraCharmEvents.charmOfLife(player)) {
            event.setCanceled(true);
        }
    }

    private static boolean charmOfLife(Player player) {
        boolean glassheart;
        boolean charm3 = TFItemStackUtils.consumeInventoryItem((Player) player, (Item) ((Item) ModItem.CHARM_OF_LIFE_3.get()), null, false) || ExtraCharmEvents.hasCharmCurio((Item) ModItem.CHARM_OF_LIFE_3.get(), player);
        boolean bl = glassheart = TFItemStackUtils.consumeInventoryItem((Player) player, (Item) ((Item) ModItem.GLASS_HEART.get()), null, false) || ExtraCharmEvents.hasCharmCurio((Item) ModItem.GLASS_HEART.get(), player);
        if (charm3 || glassheart) {
            if (glassheart) {
                player.setHealth(6.0f);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 4));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 4));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
            }
            if (charm3) {
                player.setHealth(player.getMaxHealth());
                ForceFieldCapabilityList.get(player).ifPresent(cap -> cap.replenishFields());
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            }
            CharmEffect effect = new CharmEffect((EntityType) TFEntities.CHARM_EFFECT.get(), player.level(), (LivingEntity) player, new net.minecraft.world.item.ItemStack(glassheart ? ModItem.GLASS_HEART.get() : ModItem.CHARM_OF_LIFE_3.get()));
            player.level().addFreshEntity((Entity) effect);
            CharmEffect effect2 = new CharmEffect((EntityType) TFEntities.CHARM_EFFECT.get(), player.level(), (LivingEntity) player, new net.minecraft.world.item.ItemStack(glassheart ? ModItem.GLASS_HEART.get() : ModItem.CHARM_OF_LIFE_3.get()));
            effect2.offset = (float) Math.PI;
            player.level().addFreshEntity((Entity) effect2);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent) TFSounds.CHARM_LIFE.get(), player.getSoundSource(), 1.0f, 1.0f);
            if (player instanceof ServerPlayer) {
                player.awardStat((ResourceLocation) TFStats.LIFE_CHARMS_ACTIVATED.get());
            }
            return true;
        }
        return false;
    }

    private static boolean hasCharmCurio(Item item, Player player) {
        Optional slot;
        if (ModList.get().isLoaded("curios") && (slot = CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.findFirstCurio(stack -> stack.is(item)))).isPresent()) {
            ((SlotResult) slot.get()).stack().shrink(1);
            return true;
        }
        return false;
    }
}

