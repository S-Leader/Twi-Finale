package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;

public class AnnihilationHandModel<T extends Entity> extends HierarchicalModel<T> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "annihilation"), "main");
    private final ModelPart root;

    public AnnihilationHandModel(ModelPart root) {
        this.root = root.getChild("root");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -3.0f, -5.0f, 6.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)).texOffs(0, 12).addBox(-1.0f, -3.0f, -5.0f, 6.0f, 6.0f, 6.0f, new CubeDeformation(0.25f)), PartPose.offset(-2.0f, 21.0f, 2.0f));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        float red = ((color >>> 16) & 255) / 255F, green = ((color >>> 8) & 255) / 255F, blue = (color & 255) / 255F, alpha = ((color >>> 24) & 255) / 255F;
        super.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
    }

    public void translateToHand(@NotNull HumanoidArm arm, @NotNull PoseStack stack) {
        this.root.translateAndRotate(stack);
        ModelPart armPart = this.root;
        armPart.translateAndRotate(stack);
        stack.translate(0.325, -1.9, -0.3);
    }

    public ModelPart root() {
        return this.root;
    }
}

