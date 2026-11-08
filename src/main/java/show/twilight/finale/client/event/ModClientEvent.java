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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import show.twilight.finale.compat.curios.ModCuriosCompat;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.item.FinalTrophyItem;

import java.util.IdentityHashMap;
import java.util.Map;

@net.neoforged.fml.common.EventBusSubscriber(modid = "twilightforest_finale", value = {Dist.CLIENT})
public class ModClientEvent {
    private static final Map<EntityModel<?>, HeadVisibility> HIDDEN_HEADS = new IdentityHashMap<>();

    private record HeadVisibility(ModelPart head, boolean headVisible, ModelPart hat, boolean hatVisible) {
        void restore() {
            head.visible = headVisible;
            if (hat != null) hat.visible = hatVisible;
        }
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        ItemStack stack;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player != null && level != null && ((stack = player.getMainHandItem()).is((Item) ModItem.TRANSFORMATION_ROOT.get()) || stack.is((Item) ModItem.TRANSFORMATION_TOOL.get()))) {
            EntityHitResult entityHitResult;
            Entity entity;
            HitResult result = minecraft.hitResult;
            if (result instanceof BlockHitResult) {
                BlockHitResult blockHitResult = (BlockHitResult) result;
                BlockState state = level.getBlockState(blockHitResult.getBlockPos());
                if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                    show.twilight.finale.util.ItemData.putFloat(stack, "toolType", 2.0f);
                } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
                    show.twilight.finale.util.ItemData.putFloat(stack, "toolType", 3.0f);
                } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                    if (player.isCrouching() && state.getToolModifiedState(new UseOnContext((Player) player, InteractionHand.MAIN_HAND, blockHitResult), ItemAbilities.HOE_TILL, false) != null) {
                        show.twilight.finale.util.ItemData.putFloat(stack, "toolType", 5.0f);
                    } else {
                        show.twilight.finale.util.ItemData.putFloat(stack, "toolType", 4.0f);
                    }
                } else if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
                    show.twilight.finale.util.ItemData.putFloat(stack, "toolType", 5.0f);
                }
            } else if (result instanceof EntityHitResult && (entity = (entityHitResult = (EntityHitResult) result).getEntity()) instanceof LivingEntity) {
                LivingEntity entity2 = (LivingEntity) entity;
                if (entity2.isBlocking()) {
                    show.twilight.finale.util.ItemData.putFloat(stack, "toolType", 3.0f);
                } else {
                    show.twilight.finale.util.ItemData.putFloat(stack, "toolType", 1.0f);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void unrenderHeadWithTrophies(RenderLivingEvent.Pre<?, ?> event) {
        // RenderLivingEvent itself is abstract and cannot be registered on the NeoForge event bus.
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
        // Renderers share their model: hiding a trophy wearer's head must not affect the next entity.
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

