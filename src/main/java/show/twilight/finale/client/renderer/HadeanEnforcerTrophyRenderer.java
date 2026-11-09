package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import show.twilight.finale.block.entity.FinalTrophyBlockEntity;
import show.twilight.finale.client.model.HadeanEnforcerTropy;
import show.twilight.finale.init.ModModelLayers;

public class HadeanEnforcerTrophyRenderer
        implements BlockEntityRenderer<FinalTrophyBlockEntity>,
        RenderWithoutEntity {
    private final HadeanEnforcerTropy enforcerModel;
    private final RenderType enforcerRendertype;

    public HadeanEnforcerTrophyRenderer(BlockEntityRendererProvider.Context context) {
        this(new HadeanEnforcerTropy(context.bakeLayer(ModModelLayers.ENFORCER)));
    }

    public HadeanEnforcerTrophyRenderer(EntityModelSet modelSet) {
        this(new HadeanEnforcerTropy(modelSet.bakeLayer(ModModelLayers.ENFORCER)));
    }

    public HadeanEnforcerTrophyRenderer(HadeanEnforcerTropy enforcer2Model) {
        this.enforcerModel = enforcer2Model;
        this.enforcerRendertype = this.enforcerModel.renderType(ResourceLocation.fromNamespaceAndPath((String) "hadean_breathe", (String) "textures/entity/hadean_enforcer.png"));
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
        this.enforcerModel.renderToBuffer(poseStack, bufferSource.getBuffer(this.enforcerRendertype), packedLight, overlay, FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
        poseStack.popPose();
    }
}

