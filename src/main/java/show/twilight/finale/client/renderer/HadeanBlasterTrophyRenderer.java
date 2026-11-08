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
import show.twilight.finale.client.model.HadeanBlasterTropy;
import show.twilight.finale.init.ModModelLayers;

public class HadeanBlasterTrophyRenderer implements BlockEntityRenderer<FinalTrophyBlockEntity>, RenderWithoutEntity {
    private final HadeanBlasterTropy blasterModel;
    private final RenderType blasterRendertype;

    public HadeanBlasterTrophyRenderer(BlockEntityRendererProvider.Context context) {
        this(new HadeanBlasterTropy(context.bakeLayer(ModModelLayers.BLASTER)));
    }

    public HadeanBlasterTrophyRenderer(EntityModelSet modelSet) {
        this(new HadeanBlasterTropy(modelSet.bakeLayer(ModModelLayers.BLASTER)));
    }

    public HadeanBlasterTrophyRenderer(HadeanBlasterTropy blaster2Model) {
        this.blasterModel = blaster2Model;
        this.blasterRendertype = this.blasterModel.renderType(ResourceLocation.fromNamespaceAndPath((String) "hadean_breathe", (String) "textures/entity/hadean_blaster.png"));
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
        this.blasterModel.renderToBuffer(poseStack, bufferSource.getBuffer(this.blasterRendertype), packedLight, overlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
        poseStack.popPose();
    }
}

