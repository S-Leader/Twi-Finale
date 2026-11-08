package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;

/**
 * Same block/item/worn-head renderer path as the existing priest trophy.
 */
public final class FinalBossTrophyRenderer implements BlockEntityRenderer<FinalTrophyBlockEntity>, RenderWithoutEntity {
    private final ModelPart model;
    private final RenderType type;
    private final ModelPart outer;
    private final RenderType outerType;

    public FinalBossTrophyRenderer(EntityModelSet models) {
        // Reuse the boss's actual head mesh AND robe/hood layer, not a substitute
        // player-skull cube. Each bake is independent from the living renderer.
        model = new TheTwilightHierophantModel<TheTwilightHierophantEntity>(
                models.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_SLIM), true).getHead();
        outer = new TheTwilightHierophantModel<TheTwilightHierophantEntity>(
                models.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_OUTER_SLIM), true).getHead();
        model.setPos(0, 0, 0);
        outer.setPos(0, 0, 0);
        type = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/hierophant_benimatic.png"));
        outerType = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/the_twilight_hierophant_slim_outer.png"));
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
        model.render(pose, buffers.getBuffer(type), light, overlay);
        outer.render(pose, buffers.getBuffer(outerType), light, overlay);
        pose.popPose();
    }
}
