package show.twilight.finale.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import show.twilight.finale.entity.LoyalWraithEntity;

public class LoyalWraithModel extends HumanoidModel<LoyalWraithEntity> {
    private final ModelPart dress;

    public LoyalWraithModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
        this.dress = root.getChild("dress");
    }

    public static LayerDefinition create() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition partRoot = mesh.getRoot();
        partRoot.addOrReplaceChild("dress", CubeListBuilder.create().texOffs(40, 16).addBox(-4.0f, 12.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head, this.hat);
    }

    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.dress);
    }

    public void renderToBuffer(PoseStack stack, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float alpha) {
        float finalAlpha = alpha >= 1.0f ? 0.6f : alpha;
        super.renderToBuffer(stack, buffer, light, overlay, FastColor.ARGB32.colorFromFloat(finalAlpha, r, g, b));
    }

    public void setupAnim(LoyalWraithEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float var8 = Mth.sin(this.attackTime * (float) Math.PI);
        float var9 = Mth.sin((1.0f - (1.0f - this.attackTime) * (1.0f - this.attackTime)) * (float) Math.PI);
        this.rightArm.zRot = 0.0f;
        this.leftArm.zRot = 0.0f;
        this.rightArm.yRot = -(0.1f - var8 * 0.6f);
        this.leftArm.yRot = 0.1f - var8 * 0.6f;
        this.rightArm.xRot = -1.5707964f;
        this.leftArm.xRot = -1.5707964f;
        this.rightArm.xRot -= var8 * 1.2f - var9 * 0.4f;
        this.leftArm.xRot -= var8 * 1.2f - var9 * 0.4f;
        this.rightArm.zRot += Mth.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
        this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
        this.rightArm.xRot += Mth.sin(ageInTicks * 0.067f) * 0.05f;
        this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067f) * 0.05f;
    }
}

