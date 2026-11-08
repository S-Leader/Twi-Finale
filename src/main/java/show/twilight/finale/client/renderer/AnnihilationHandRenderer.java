package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import show.twilight.finale.client.model.AnnihilationHandModel;
import show.twilight.finale.entity.AnnihilationHandEntity;

import javax.annotation.Nonnull;

public class AnnihilationHandRenderer extends MobRenderer<AnnihilationHandEntity, AnnihilationHandModel<AnnihilationHandEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/annihilation_hand.png");

    public AnnihilationHandRenderer(EntityRendererProvider.Context context) {
        super(context, new AnnihilationHandModel(context.bakeLayer(AnnihilationHandModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
    }

    @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull AnnihilationHandEntity entity) {
        return TEXTURE;
    }

    protected void scale(AnnihilationHandEntity entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
    }
}

