package show.twilight.finale.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import show.twilight.finale.init.ModItem;

@EventBusSubscriber(modid = "twilightforest")
public class ToolEvents {

    @SubscribeEvent
    public static void fieryToolSetFire(LivingIncomingDamageEvent event) {
        LivingEntity living;
        Entity entity = event.getSource().getEntity();
        if (entity instanceof LivingEntity && ((living = (LivingEntity) entity).getMainHandItem().is((Item) ModItem.FIERY_HOE.get()) || living.getMainHandItem().is((Item) ModItem.FIERY_SHOVEL.get()) || living.getMainHandItem().is((Item) ModItem.FIERY_AXE.get())) && !event.getEntity().fireImmune()) {
            event.getEntity().igniteForSeconds(1);
        }
    }

    @SubscribeEvent
    public static void onKnightmetalToolDamage(LivingIncomingDamageEvent event) {
        ItemStack weapon;
        Entity entity;
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide() && (entity = event.getSource().getDirectEntity()) instanceof LivingEntity && !(weapon = ((LivingEntity) entity).getMainHandItem()).isEmpty() && target.getArmorValue() > 0 && (weapon.is((Item) ModItem.KNIGHTMETAL_HOE.get()) || weapon.is((Item) ModItem.KNIGHTMETAL_SHOVEL.get()))) {
            if (target.getArmorCoverPercentage() > 0.0f) {
                int moreBonus = (int) (2.0f * target.getArmorCoverPercentage());
                event.setAmount(event.getAmount() + (float) moreBonus);
            } else {
                event.setAmount(event.getAmount() + 2.0f);
            }
        }
    }
}

