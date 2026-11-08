package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import show.twilight.finale.client.animation.TheTwilightHierophantAnimation;
import show.twilight.finale.client.layer.HierophantArmorLayer;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.runtime.client.model.AnimatableEntityModel;

@OnlyIn(value = Dist.CLIENT)
public class TheTwilightHierophantModel<T extends TheTwilightHierophantEntity>
        extends AnimatableEntityModel<T>
        implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant"), "main");
    public static final ModelLayerLocation LAYER_LOCATION_SLIM = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant_slim"), "main");
    public static final ModelLayerLocation LAYER_LOCATION_OUTER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant"), "outer");
    public static final ModelLayerLocation LAYER_LOCATION_OUTER_SLIM = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant_slim"), "outer");
    public static final ModelLayerLocation LAYER_LOCATION_O = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant"), "armor_outer");
    public static final ModelLayerLocation LAYER_LOCATION_I = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant"), "armor_inner");
    private final ModelPart root;
    private final ModelPart entity;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_arm;
    private final ModelPart left_item;
    private final ModelPart right_arm;
    private final ModelPart right_item;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final boolean slim;

    public TheTwilightHierophantModel(ModelPart root, boolean slim) {
        this.root = root.getChild("root");
        this.slim = slim;
        this.entity = this.root.getChild("entity");
        this.body = this.entity.getChild("body");
        this.head = this.body.getChild("head");
        this.left_arm = this.body.getChild("left_arm");
        this.left_item = this.left_arm.getChild("left_item");
        this.right_arm = this.body.getChild("right_arm");
        this.right_item = this.right_arm.getChild("right_item");
        this.right_leg = this.entity.getChild("right_leg");
        this.left_leg = this.entity.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer(boolean slim, float deformation) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition entity = root.addOrReplaceChild("entity", CubeListBuilder.create(), PartPose.offset(0.0f, 6.0f, 0.0f));
        PartDefinition body = entity.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0f, -12.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)).texOffs(16, 16).addBox(-4.0f, -12.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(deformation)), PartPose.offset(0.0f, 6.0f, 0.0f));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(deformation)).texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(deformation + 0.25f)), PartPose.offset(0.0f, -12.0f, 0.0f));
        PartDefinition left_arm;
        PartDefinition left_item;
        if (slim) {
            left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)).texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(deformation)), PartPose.offset(5.0f, -10.0f, 0.0f));
            left_item = left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new CubeDeformation(deformation)), PartPose.offset(0.0f, 8.0f, 0.0f));
            PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(deformation)).texOffs(40, 32).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)), PartPose.offset(-5.0f, -10.0f, 0.0f));
            PartDefinition partDefinition = right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new CubeDeformation(deformation)), PartPose.offset(0.0f, 8.0f, 0.0f));
        } else {
            left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)).texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offset(5.0f, -10.0f, 0.0f));
            left_item = left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 8.0f, 0.0f));
            PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(40, 32).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)).mirror(false), PartPose.offset(-5.0f, -10.0f, 0.0f));
            PartDefinition partDefinition = right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 8.0f, 0.0f));
        }
        PartDefinition right_leg = entity.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.1f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(deformation)).texOffs(0, 48).mirror().addBox(-2.1f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)), PartPose.offset(-1.9f, 6.0f, 0.0f));
        PartDefinition left_leg = entity.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-1.9f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(deformation)).texOffs(0, 48).addBox(-1.9f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)), PartPose.offset(1.9f, 6.0f, 0.0f));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public ModelPart root() {
        return this.root;
    }

    public ModelPart getHead() {
        return this.head;
    }

    public void setupWalkAnimation(T entity, float limbSwing, float limbSwingAmount) {
        this.animateWalk(TheTwilightHierophantAnimation.WALK, limbSwing, limbSwingAmount, 1.5f, 2.0f);
    }

    @Override
    public void setupAnim(T boss, float swing, float amount, float age, float yaw, float pitch) {
        super.setupAnim(boss, swing, amount, age, yaw, pitch);
        int action = boss.getAction();
        float t = boss.getActionTime() + net.minecraft.util.Mth.clamp(age - boss.tickCount, 0, 1);
        if (action == TheTwilightHierophantEntity.IDLE_ACTION) return;
        left_arm.xRot = right_arm.xRot = left_arm.yRot = right_arm.yRot = left_arm.zRot = right_arm.zRot = 0;
        switch (action) {
            case TheTwilightHierophantEntity.INTRO -> {
                float uncurl = net.minecraft.util.Mth.clamp((t - 25) / 35F, 0, 1);
                body.xRot = (1 - uncurl) * 1.15F;
                head.xRot = (1 - uncurl) * 0.8F;
                left_leg.xRot = right_leg.xRot = -(1 - uncurl);
                left_arm.xRot = right_arm.xRot = -(1 - uncurl) * 1.3F;
                right_arm.xRot -= uncurl * 1.5F;
                right_arm.yRot = -0.25F * uncurl;
            }
            case TheTwilightHierophantEntity.BEAM -> {
                float aim = net.minecraft.util.Mth.clamp(t / 20F, 0, 1);
                left_arm.xRot = right_arm.xRot = -1.25F * aim;
                left_arm.yRot = 0.65F * aim;
                right_arm.yRot = -0.65F * aim;
                body.xRot = t >= 40 && t < 48 ? -0.12F : 0.12F;
            }
            case TheTwilightHierophantEntity.SUMMON -> {
                float open = Math.min(net.minecraft.util.Mth.clamp(t / 20F, 0, 1), net.minecraft.util.Mth.clamp((120 - t) / 20F, 0, 1));
                left_arm.zRot = -1.25F * open;
                right_arm.zRot = 1.25F * open;
                left_arm.xRot = right_arm.xRot = (-0.2F + (float) Math.sin(age * 0.15) * 0.06F) * open;
            }
            case TheTwilightHierophantEntity.BARRAGE -> {
                float raise = Math.min(net.minecraft.util.Mth.clamp(t / 20F, 0, 1), net.minecraft.util.Mth.clamp((160 - t) / 20F, 0, 1));
                left_arm.xRot = right_arm.xRot = -2.85F * raise;
                left_arm.zRot = -0.25F * raise;
                right_arm.zRot = 0.25F * raise;
                body.zRot = (float) Math.sin(age * 0.6) * 0.025F;
            }
            case TheTwilightHierophantEntity.SMOKE_MOVE -> {
                body.xRot = 0.7F;
                left_arm.xRot = right_arm.xRot = 0.8F;
            }
            case TheTwilightHierophantEntity.CHARGE -> {
                body.xRot = t < 30 ? 0.3F : 0.1F;
                left_arm.xRot = right_arm.xRot = -0.7F;
            }
            case TheTwilightHierophantEntity.RETURN_FRAGMENTS, TheTwilightHierophantEntity.FORMATION -> {
                left_arm.zRot = -0.6F;
                right_arm.zRot = 0.6F;
                left_arm.xRot = right_arm.xRot = -1.1F;
            }
            case TheTwilightHierophantEntity.SHIELD_BREAK -> {
                body.xRot = 0.5F;
                left_arm.xRot = right_arm.xRot = 0.4F;
            }
            case TheTwilightHierophantEntity.DEATH -> {
                float kneel = net.minecraft.util.Mth.clamp((t - 35) / 35F, 0, 1);
                entity.y += kneel * 7;
                body.xRot = kneel * 0.7F;
                head.xRot = kneel * 0.7F;
                left_leg.xRot = right_leg.xRot = -kneel * 1.5F;
                left_arm.xRot = right_arm.xRot = -kneel * 0.35F;
                entity.zRot = (float) Math.sin(age * 1.8) * (0.01F + Math.min(t, 55) / 700F) * (1 - kneel * 0.8F);
            }
        }
    }

    public void translateToHand(@NotNull HumanoidArm arm, @NotNull PoseStack stack) {
        ModelPart armPart;
        this.root.translateAndRotate(stack);
        this.entity.translateAndRotate(stack);
        this.body.translateAndRotate(stack);
        ModelPart modelPart = armPart = arm == HumanoidArm.LEFT ? this.left_arm : this.right_arm;
        if (this.slim) {
            float f = 0.5f * (float) (arm == HumanoidArm.RIGHT ? 1 : -1);
            armPart.x += f;
            armPart.translateAndRotate(stack);
            armPart.x -= f;
        } else {
            armPart.translateAndRotate(stack);
        }
        ModelPart itemPart = arm == HumanoidArm.LEFT ? this.left_item : this.right_item;
        itemPart.translateAndRotate(stack);
        stack.translate(0.0, -0.5, 0.0);
    }

    public void translateArmor(HierophantArmorLayer.ArmorModelPart part, PoseStack stack, boolean innerModel) {
        this.root.translateAndRotate(stack);
        this.entity.translateAndRotate(stack);
        switch (part) {
            case HEAD: {
                this.body.translateAndRotate(stack);
                this.head.translateAndRotate(stack);
                break;
            }
            case BODY: {
                this.body.translateAndRotate(stack);
                stack.translate(0.0, -0.75, 0.0);
                break;
            }
            case LEFT_ARM: {
                this.body.translateAndRotate(stack);
                this.left_arm.translateAndRotate(stack);
                stack.translate(0.0, -0.125, 0.0);
                stack.translate(-0.275, 0.0, 0.0);
                break;
            }
            case RIGHT_ARM: {
                this.body.translateAndRotate(stack);
                this.right_arm.translateAndRotate(stack);
                stack.translate(0.0, -0.125, 0.0);
                stack.translate(0.275, 0.0, 0.0);
                break;
            }
            case LEFT_LEG: {
                this.left_leg.translateAndRotate(stack);
                stack.translate(0.0, -0.75, 0.0);
                stack.translate(-0.125, 0.0, 0.0);
                break;
            }
            case RIGHT_LEG: {
                this.right_leg.translateAndRotate(stack);
                stack.translate(0.0, -0.75, 0.0);
                stack.translate(0.125, 0.0, 0.0);
            }
        }
    }
}

