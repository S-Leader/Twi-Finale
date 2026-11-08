package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import show.twilight.finale.client.animation.AdherentHighPriestAnimation;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.runtime.client.model.AnimatableEntityModel;

public class AdherentHighPriestModel<T extends AdherentHighPriestEntity> extends AnimatableEntityModel<T> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_high_priest"), "main");
    public static final ModelLayerLocation LAYER_LOCATION_O = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_high_priest"), "outer");
    public static final ModelLayerLocation LAYER_LOCATION_I = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_high_priest"), "inner");
    private final ModelPart root;
    private final ModelPart entity;
    private final ModelPart body2;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    public float alpha = 1.0f;

    public AdherentHighPriestModel(ModelPart root) {
        this.root = root.getChild("root");
        this.entity = this.root.getChild("entity");
        this.body2 = this.entity.getChild("body2");
        this.body = this.entity.getChild("body");
        this.head = this.body.getChild("head");
        this.left_arm = this.body.getChild("left_arm");
        this.right_arm = this.body.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 20.0f, 0.0f));
        PartDefinition entity = root.addOrReplaceChild("entity", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition body2 = entity.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 14.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(0.0f, -12.0f, 0.0f, 0.0873f, 0.0f, 0.0f));
        PartDefinition body = entity.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -12.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -12.0f, 0.0f));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.0f)).texOffs(0, 16).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.15f)), PartPose.offset(0.0f, -12.0f, 0.0f));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0f, -10.0f, 0.0f));
        PartDefinition cube_r1 = left_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 32).addBox(-1.0f, -2.0f, 3.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(32, 16).addBox(-1.0f, -2.0f, -1.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(0.0f, 0.0f, -1.0f, -0.4402f, -0.851f, -0.5182f));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0f, -10.0f, 0.0f));
        PartDefinition cube_r2 = right_arm.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(24, 32).mirror().addBox(-3.0f, -2.0f, 3.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(32, 16).mirror().addBox(-3.0f, -2.0f, -1.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation(0.0f, 0.0f, -1.0f, -0.4402f, 0.851f, 0.5182f));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        float red = ((color >>> 16) & 255) / 255F, green = ((color >>> 8) & 255) / 255F, blue = (color & 255) / 255F, alpha = ((color >>> 24) & 255) / 255F;
        super.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha *= this.alpha, red, green, blue));
    }

    public void translateToHand(@NotNull HumanoidArm arm, @NotNull PoseStack stack) {
        this.root.translateAndRotate(stack);
        this.entity.translateAndRotate(stack);
        this.body.translateAndRotate(stack);
        this.body2.translateAndRotate(stack);
        ModelPart armPart = arm == HumanoidArm.LEFT ? this.left_arm : this.right_arm;
        armPart.translateAndRotate(stack);
        stack.translate(0.0, -0.5, 0.0);
    }

    public ModelPart getHead() {
        return this.head;
    }

    public void setupWalkAnimation(T entity, float limbSwing, float limbSwingAmount) {
        this.animateWalk(AdherentHighPriestAnimation.WALK, limbSwing, limbSwingAmount, 1.5f, 2.0f);
    }

    public ModelPart root() {
        return this.root;
    }
}

