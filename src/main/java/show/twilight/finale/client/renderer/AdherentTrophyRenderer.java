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
import show.twilight.finale.client.model.AdherentHighPriestTrophy;
import show.twilight.finale.init.ModModelLayers;

public class AdherentTrophyRenderer
        implements BlockEntityRenderer<FinalTrophyBlockEntity>,
        RenderWithoutEntity {
    private final AdherentHighPriestTrophy adherentModel;
    private final RenderType adherentRendertype;
    private static final RenderType ADHERENT_GLOW = RenderType.eyes((ResourceLocation) ResourceLocation.fromNamespaceAndPath((String) "twilightforest_finale", (String) "textures/entity/adherent_high_priest.png"));

    public AdherentTrophyRenderer(BlockEntityRendererProvider.Context context) {
        this(new AdherentHighPriestTrophy(context.bakeLayer(ModModelLayers.ADHERENT)));
    }

    public AdherentTrophyRenderer(EntityModelSet modelSet) {
        this(new AdherentHighPriestTrophy(modelSet.bakeLayer(ModModelLayers.ADHERENT)));
    }

    public AdherentTrophyRenderer(AdherentHighPriestTrophy adhModel) {
        this.adherentModel = adhModel;
        ResourceLocation baseTexture = ResourceLocation.fromNamespaceAndPath((String) "twilightforest_finale", (String) "textures/entity/adherent_high_priest.png");
        this.adherentRendertype = this.adherentModel.renderType(baseTexture);
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
        this.adherentModel.renderToBuffer(poseStack, bufferSource.getBuffer(this.adherentRendertype), packedLight, overlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
        this.adherentModel.renderToBuffer(poseStack, bufferSource.getBuffer(ADHERENT_GLOW), 0xF000F0, overlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
        poseStack.popPose();
    }
}

