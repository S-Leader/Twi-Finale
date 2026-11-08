package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import show.twilight.finale.client.model.HadeanSmasherTropy;
import show.twilight.finale.init.ModModelLayers;

public class HadeanSmasherTrophyRenderer implements BlockEntityRenderer<FinalTrophyBlockEntity>, RenderWithoutEntity {
    private final HadeanSmasherTropy smasherModel;
    private final RenderType smasherRendertype;

    public HadeanSmasherTrophyRenderer(BlockEntityRendererProvider.Context context) {
        this(new HadeanSmasherTropy(context.bakeLayer(ModModelLayers.SMASHER)));
    }

    public HadeanSmasherTrophyRenderer(EntityModelSet modelSet) {
        this(new HadeanSmasherTropy(modelSet.bakeLayer(ModModelLayers.SMASHER)));
    }

    public HadeanSmasherTrophyRenderer(HadeanSmasherTropy smasher2Model) {
        this.smasherModel = smasher2Model;
        this.smasherRendertype = this.smasherModel.renderType(ResourceLocation.fromNamespaceAndPath((String) "hadean_breathe", (String) "textures/entity/hadean_smasher.png"));
    }

    public void render(FinalTrophyBlockEntity trophy, float partial, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int overlay) {
        this.render(trophy, poseStack, bufferSource, packedLight, overlay);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 1.5f, 0.5f);
        poseStack.scale(1.0f, 1.0f, 1.0f);
        poseStack.mulPose(Axis.XP.rotation((float) Math.PI));
        this.smasherModel.renderToBuffer(poseStack, bufferSource.getBuffer(this.smasherRendertype), packedLight, overlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
        poseStack.popPose();
    }
}

