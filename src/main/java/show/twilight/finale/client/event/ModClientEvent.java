package show.twilight.finale.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.compat.curios.ModCuriosCompat;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.init.ModWithoutLevelRenderer;
import show.twilight.finale.item.AbstractTransformationItem;
import show.twilight.finale.item.FinalTrophyItem;
import show.twilight.finale.util.ItemData;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID, value = Dist.CLIENT)
public class ModClientEvent {
    private static final Map<EntityModel<?>, HeadVisibility> HIDDEN_HEADS = new IdentityHashMap<>();
    private static final Map<ItemStack, Float> LAST_TOOL_FORMS = new WeakHashMap<>();

    private record HeadVisibility(ModelPart head, boolean headVisible, ModelPart hat, boolean hatVisible) {
        void restore() {
            head.visible = headVisible;
            if (hat != null) hat.visible = hatVisible;
        }
    }

    public static float getTransformationToolType(ItemStack stack, ClientLevel level, LivingEntity holder, int seed) {
        float previous = LAST_TOOL_FORMS.getOrDefault(stack, ItemData.getFloat(stack, "toolType"));
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (holder == null && player != null && stack.getEntityRepresentation() == null && ItemStack.isSameItemSameComponents(stack, player.getMainHandItem()))
            holder = player;
        if (holder == null || !ItemStack.isSameItemSameComponents(stack, holder.getMainHandItem())) return previous;
        boolean local = holder == player && minecraft.level != null;
        HitResult result = local ? minecraft.hitResult : null;
        if (local && (result == null || result.getType() == HitResult.Type.MISS))
            result = player.pick(player.blockInteractionRange(), 1, false);

        if (result instanceof EntityHitResult hit && AbstractTransformationItem.isPickaxeTarget(hit.getEntity())) {
            LAST_TOOL_FORMS.put(stack, 2.0F);
            return 2;
        }
        if (MaceItem.canSmashAttack(holder) || ItemData.getFloat(stack, AbstractTransformationItem.MACE_FORM_TAG) > 0)
            return AbstractTransformationItem.MACE_FORM;
        if (!local) return previous;
        float form = previous;
        if (result instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            BlockState state = minecraft.level.getBlockState(hit.getBlockPos());
            if (AbstractTransformationItem.isPickaxeBlock(state)) {
                form = 2;
            } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
                form = 3;
            } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                form = player.isCrouching() && state.getToolModifiedState(new UseOnContext(player, InteractionHand.MAIN_HAND, hit), ItemAbilities.HOE_TILL, true) != null ? 5 : 4;
            } else if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
                form = 5;
            }
        } else if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            form = target.isBlocking() ? 3 : 1;
        }

        LAST_TOOL_FORMS.put(stack, form);
        return form;
    }

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(ModWithoutLevelRenderer.ITEM_EXTENSION, ModItem.ADHERENT_TROPHY.get(), ModItem.HIEROPHANT_TROPHY.get(), ModItem.ANNIHILATION_GOLEM_TROPHY.get());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void unrenderHeadWithTrophies(RenderLivingEvent.Pre<?, ?> event) {

        LivingEntity entity = event.getEntity();
        EntityModel<?> model = event.getRenderer().getModel();
        HeadVisibility previous = HIDDEN_HEADS.remove(model);
        if (previous != null) previous.restore();
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
        if ((stack.getItem() instanceof FinalTrophyItem || areCuriosEquipped(entity)) && model instanceof HeadedModel headed) {
            ModelPart head = headed.getHead();
            ModelPart hat = model instanceof HumanoidModel<?> humanoid ? humanoid.hat : null;
            HIDDEN_HEADS.put(model, new HeadVisibility(head, head.visible, hat, hat != null && hat.visible));
            head.visible = false;
            if (hat != null) hat.visible = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void restoreHeadAfterTrophies(RenderLivingEvent.Post<?, ?> event) {

        HeadVisibility previous = HIDDEN_HEADS.remove(event.getRenderer().getModel());
        if (previous != null) previous.restore();
    }

    private static boolean areCuriosEquipped(LivingEntity entity) {
        if (ModList.get().isLoaded("curios")) {
            return ModCuriosCompat.isTrophyCurioEquipped(entity);
        }
        return false;
    }

}

