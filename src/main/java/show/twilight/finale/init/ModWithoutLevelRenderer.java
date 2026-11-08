package show.twilight.finale.init;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import show.twilight.finale.client.model.FinaleExtraModels;
import show.twilight.finale.client.renderer.*;
import show.twilight.finale.compat.hadean.HadeanRenderCompat;
import twilightforest.config.TFConfig;

import java.util.function.Supplier;


public class ModWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    private final AdherentTrophyRenderer adherentTrophyRenderer;
    private final FinalBossTrophyRenderer hierophantTrophyRenderer;
    private final HadeanEnforcerTrophyRenderer enforcerTrophyRenderer;
    private final HadeanBlasterTrophyRenderer blasterTrophyRenderer;
    private final HadeanSmasherTrophyRenderer smasherTrophyRenderer;
    private final DuckEnforcerTrophyRenderer duckenforcerTrophyRenderer;
    private final ModelResourceLocation diamondBackplate = FinaleExtraModels.DIAMOND_BACKPLATE;
    private final ModelResourceLocation platinumBackplate = FinaleExtraModels.PLATINUM_BACKPLATE;
    private static final Supplier<ModWithoutLevelRenderer> INSTANCE = Suppliers.memoize(() -> new ModWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
    private static final RenderType ADHERENT_ITEM_GLOW = RenderType.eyes(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/adherent_high_priest.png"));
    public static final IClientItemExtensions ITEM_EXTENSION = Util.make(() -> new IClientItemExtensions() {

        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return INSTANCE.get();
        }
    });

    private ModWithoutLevelRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.adherentTrophyRenderer = new AdherentTrophyRenderer(modelSet);
        this.hierophantTrophyRenderer = new FinalBossTrophyRenderer(modelSet);
        this.enforcerTrophyRenderer = new HadeanEnforcerTrophyRenderer(modelSet);
        this.blasterTrophyRenderer = new HadeanBlasterTrophyRenderer(modelSet);
        this.smasherTrophyRenderer = new HadeanSmasherTrophyRenderer(modelSet);
        this.duckenforcerTrophyRenderer = new DuckEnforcerTrophyRenderer(modelSet);
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (stack.is(ModItem.HIEROPHANT_TROPHY.get())) {
            ModWithoutLevelRenderer.render(stack, context, poseStack, bufferSource, packedLight, packedOverlay, hierophantTrophyRenderer, diamondBackplate);
            return;
        }
        if (stack.is(ModItem.ADHERENT_TROPHY.get())) {
            ModWithoutLevelRenderer.render(stack, context, poseStack, bufferSource, packedLight, packedOverlay, this.adherentTrophyRenderer, this.diamondBackplate);
            return;
        }
        if (ModList.get().isLoaded("hadean_breathe")) {
            if (HadeanRenderCompat.isEnforcerTrophy(stack)) {
                ModWithoutLevelRenderer.render(stack, context, poseStack, bufferSource, packedLight, packedOverlay, this.enforcerTrophyRenderer, this.platinumBackplate);
            } else if (HadeanRenderCompat.isBlasterTrophy(stack)) {
                ModWithoutLevelRenderer.render(stack, context, poseStack, bufferSource, packedLight, packedOverlay, this.blasterTrophyRenderer, this.platinumBackplate);
            } else if (HadeanRenderCompat.isSmasherTrophy(stack)) {
                ModWithoutLevelRenderer.render(stack, context, poseStack, bufferSource, packedLight, packedOverlay, this.smasherTrophyRenderer, this.platinumBackplate);
            } else if (HadeanRenderCompat.isDuckEnforcerTrophy(stack)) {
                ModWithoutLevelRenderer.render(stack, context, poseStack, bufferSource, packedLight, packedOverlay, this.duckenforcerTrophyRenderer, this.platinumBackplate);
            }
        }
    }

    private static void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, RenderWithoutEntity withoutEntity, ModelResourceLocation backplateLocation) {
        poseStack.pushPose();
        MultiBufferSource.BufferSource bufferSourceMain = Minecraft.getInstance().renderBuffers().bufferSource();
        if (context == ItemDisplayContext.GUI) {
            poseStack.pushPose();
            BakedModel backplate = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(backplateLocation);
            poseStack.translate(0.5f, 0.5f, -1.5f);
            Lighting.setupForFlatItems();
            Minecraft.getInstance().getItemRenderer().render(stack, context, false, poseStack, bufferSourceMain, 0xF000F0, OverlayTexture.NO_OVERLAY, backplate.applyTransform(context, poseStack, false));
            bufferSourceMain.endBatch();
            Lighting.setupFor3DItems();
            poseStack.popPose();
            poseStack.translate(0.5f, 0.5f, 0.0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0f));
            poseStack.mulPose(Axis.YN.rotationDegrees(TFConfig.rotateTrophyHeadsGui ? (float) (net.minecraft.Util.getMillis() % 24000L) * 0.015F : -45.0F));
            poseStack.translate(-0.5f, -0.25f, -0.5f);
        }
        withoutEntity.render(poseStack, bufferSourceMain, packedLight, packedOverlay);
        bufferSourceMain.endBatch(ADHERENT_ITEM_GLOW);
        poseStack.popPose();
    }
}

