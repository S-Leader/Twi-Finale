package show.twilight.finale.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;

public class HierophantOuterLayer<T extends TheTwilightHierophantEntity>
        extends RenderLayer<T, TheTwilightHierophantModel<T>> {
    private final TheTwilightHierophantModel<T> normalModel;
    private final TheTwilightHierophantModel<T> slimModel;

    public HierophantOuterLayer(RenderLayerParent<T, TheTwilightHierophantModel<T>> parent, EntityModelSet modelSet) {
        super(parent);
        this.normalModel = new TheTwilightHierophantModel(modelSet.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_OUTER), false);
        this.slimModel = new TheTwilightHierophantModel(modelSet.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_OUTER_SLIM), true);
    }

    public void render(PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        TheTwilightHierophantModel<T> model = this.normalModel;
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/the_twilight_hierophant_outer.png");
        Entity entity2 = Minecraft.getInstance().getCameraEntity();
        if (entity2 instanceof AbstractClientPlayer && ((AbstractClientPlayer) entity2).getSkin().model() == PlayerSkin.Model.SLIM) {
            model = this.slimModel;
            texture = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/the_twilight_hierophant_slim_outer.png");
        }
        if (!entity.isInvisible()) {
            this.getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
            model.renderToBuffer(poseStack, consumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0f), FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
        }
    }
}

