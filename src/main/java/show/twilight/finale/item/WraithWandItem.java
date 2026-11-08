package show.twilight.finale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import show.twilight.finale.entity.LoyalWraithEntity;
import show.twilight.finale.init.ModEntity;
import twilightforest.init.TFSounds;

import java.util.List;

public class WraithWandItem
        extends FinaleScepterItem {
    public WraithWandItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult result;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() == stack.getMaxDamage()) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide() && (result = WraithWandItem.getPlayerPOVHitResult((Level) level, (Player) player, (ClipContext.Fluid) ClipContext.Fluid.NONE)).getType() != HitResult.Type.MISS) {
            LoyalWraithEntity zombie = (LoyalWraithEntity) ModEntity.LOYAL_WRAITH.get().create(level);
            zombie.moveTo(result.getLocation());
            if (!level.noCollision((Entity) zombie, zombie.getBoundingBox())) {
                return InteractionResultHolder.pass(stack);
            }
            zombie.spawnAnim();
            zombie.setTame(true, true);
            zombie.setOwnerUUID(player.getUUID());
            zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));
            level.addFreshEntity((Entity) zombie);
            level.gameEvent((Entity) player, GameEvent.ENTITY_PLACE, result.getBlockPos());
            twilightforest.util.TFItemStackUtils.hurtButDontBreak(stack, 1, (net.minecraft.server.level.ServerLevel) level, player);
            zombie.playSound((SoundEvent) TFSounds.ZOMBIE_SCEPTER_USE.get(), 1.0f, zombie.getVoicePitch());
        }
        return InteractionResultHolder.sidedSuccess(stack, (boolean) level.isClientSide());
    }

    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add((Component) Component.translatable((String) "item.twilightforest.scepter.desc", (Object[]) new Object[]{stack.getMaxDamage() - stack.getDamageValue()}).withStyle(ChatFormatting.GRAY));
    }
}

