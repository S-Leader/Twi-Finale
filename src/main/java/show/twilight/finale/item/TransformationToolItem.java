package show.twilight.finale.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.init.TFItems;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TransformationToolItem extends AbstractTransformationItem implements Equipable {
    private static final Set<ItemAbility> ACTIONS = (Set) Util.make(Collections.newSetFromMap(new IdentityHashMap()), actions -> {
        actions.addAll(ItemAbilities.DEFAULT_SWORD_ACTIONS);
        actions.addAll(ItemAbilities.DEFAULT_PICKAXE_ACTIONS);
        actions.addAll(ItemAbilities.DEFAULT_AXE_ACTIONS);
        actions.addAll(ItemAbilities.DEFAULT_SHOVEL_ACTIONS);
        actions.addAll(ItemAbilities.DEFAULT_HOE_ACTIONS);
        actions.addAll(ItemAbilities.DEFAULT_SHIELD_ACTIONS);
    });

    private static final Tier TOOL_TIER = new Tier() {

        public int getUses() {
            return 2031;
        }

        public float getSpeed() {
            return 9.0f;
        }

        public float getAttackDamageBonus() {
            return 4.5f;
        }

        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        public int getEnchantmentValue() {
            return 15;
        }

        @NotNull
        public Ingredient getRepairIngredient() {
            return Ingredient.of(TFItems.KNIGHTMETAL_INGOT.get());
        }
    };

    public TransformationToolItem(Item.Properties properties) {
        super(TOOL_TIER, properties.attributes(PickaxeItem.createAttributes(TOOL_TIER, 3, -2.5F)));
    }

    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility action) {
        return ACTIONS.contains(action);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.twilightforest_finale.transformation_tool.desc").withStyle(ChatFormatting.GRAY));
    }

    public float getDestroySpeed(@Nonnull ItemStack stack, BlockState state) {
        float destroySpeed = super.getDestroySpeed(stack, state);
        return state.is(BlockTagGenerator.MAZEBREAKER_ACCELERATED) ? destroySpeed * 128.0f : destroySpeed;
    }

    public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state) {
        return true;
    }

    public boolean isPrimaryItemFor(ItemStack stack, net.minecraft.core.Holder<Enchantment> enchantment) {
        return enchantment.value().isSupportedItem(new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD)) || enchantment.value().isSupportedItem(new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
    }

    @NotNull
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState blockstate = level.getBlockState(blockpos);
        Optional<BlockState> optional = Optional.ofNullable(blockstate.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false));
        Optional optional1 = optional.isPresent() ? Optional.empty() : Optional.ofNullable(blockstate.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false));
        Optional optional2 = optional.isPresent() || optional1.isPresent() ? Optional.empty() : Optional.ofNullable(blockstate.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false));
        ItemStack itemstack = context.getItemInHand();
        Optional<BlockState> optional3 = Optional.empty();
        if (optional.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
            optional3 = optional;
        } else if (optional1.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(player, 3005, blockpos, 0);
            optional3 = optional1;
        } else if (optional2.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(player, 3004, blockpos, 0);
            optional3 = optional2;
        }
        if (optional3.isPresent()) {
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, blockpos, itemstack);
            }
            level.setBlock(blockpos, optional3.get(), 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, optional3.get()));
            if (player != null) {
                itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player != null) {
            if (player.isCrouching()) {
                Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair;
                BlockState toolModifiedState = level.getBlockState(blockpos).getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
                Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair2 = pair = toolModifiedState == null ? null : Pair.of(ctx -> true, HoeItem.changeIntoState(toolModifiedState));
                if (pair == null) {
                    return InteractionResult.PASS;
                }
                Predicate predicate = pair.getFirst();
                Consumer consumer = pair.getSecond();
                if (predicate.test(context)) {
                    level.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (!level.isClientSide) {
                        consumer.accept(context);
                        context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else {
                if (context.getClickedFace() == Direction.DOWN) {
                    return InteractionResult.PASS;
                }
                BlockState blockstate1 = blockstate.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
                BlockState blockstate2 = null;
                if (blockstate1 != null && level.isEmptyBlock(blockpos.above())) {
                    level.playSound(player, blockpos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                    blockstate2 = blockstate1;
                } else if (blockstate.getBlock() instanceof CampfireBlock && blockstate.getValue(CampfireBlock.LIT).booleanValue()) {
                    if (!level.isClientSide()) {
                        level.levelEvent(null, 1009, blockpos, 0);
                    }
                    CampfireBlock.dowse(context.getPlayer(), level, blockpos, blockstate);
                    blockstate2 = blockstate.setValue(CampfireBlock.LIT, Boolean.valueOf(false));
                }
                if (blockstate2 != null) {
                    if (!level.isClientSide) {
                        level.setBlock(blockpos, blockstate2, 11);
                        level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, blockstate2));
                        context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    @NotNull
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    @NotNull
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.OFFHAND;
    }
}

