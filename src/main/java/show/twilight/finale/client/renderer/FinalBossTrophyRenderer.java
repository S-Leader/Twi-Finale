package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;

public final class FinalBossTrophyRenderer implements BlockEntityRenderer<FinalTrophyBlockEntity>, RenderWithoutEntity {
    private final ModelPart model;
    private final ModelPart outer;
    private final RenderType normalOuterType;
    private final RenderType slimOuterType;

    public FinalBossTrophyRenderer(EntityModelSet models) {

        model = new TheTwilightHierophantModel<TheTwilightHierophantEntity>(
                models.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_SLIM), true).getHead();
        outer = new TheTwilightHierophantModel<TheTwilightHierophantEntity>(
                models.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_OUTER_SLIM), true).getHead();
        model.setPos(0, 0, 0);
        outer.setPos(0, 0, 0);
        normalOuterType = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/the_twilight_hierophant_outer.png"));
        slimOuterType = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/the_twilight_hierophant_slim_outer.png"));
    }

    @Override
    public void render(FinalTrophyBlockEntity trophy, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        RenderWithoutEntity.super.render(trophy, pose, buffers, light, overlay);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.scale(1, -1, -1);

        var camera = Minecraft.getInstance().getCameraEntity();
        ResourceLocation skin = camera instanceof AbstractClientPlayer player ? player.getSkin().texture() : DefaultPlayerSkin.getDefaultTexture();
        RenderType type = RenderType.entityCutoutNoCull(skin);
        boolean slim = camera instanceof AbstractClientPlayer player
                && player.getSkin().model() == PlayerSkin.Model.SLIM;
        model.render(pose, buffers.getBuffer(type), light, overlay);
        outer.render(pose, buffers.getBuffer(slim ? slimOuterType : normalOuterType), light, overlay);
        pose.popPose();
    }
}
