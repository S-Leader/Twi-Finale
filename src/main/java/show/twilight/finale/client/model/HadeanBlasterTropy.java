package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class HadeanBlasterTropy extends GenericTrophyModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "hadean_blaster"), "head");
    public final ModelPart head;

    public HadeanBlasterTropy(ModelPart root) {
        this.head = root.getChild("head");
    }

    public static LayerDefinition createHead() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0f, 15.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.0f)).texOffs(0, 89).addBox(-4.0f, 15.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.5f)).texOffs(32, 89).addBox(-4.0f, 15.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(1.0f)), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(80, 33).addBox(-4.0f, -32.0f, 3.0f, 8.0f, 9.0f, 2.0f, new CubeDeformation(0.3f)), PartPose.offsetAndRotation(0.0f, 46.3816f, -11.9237f, -0.1309f, 0.0f, 0.0f));
        PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(64, 49).addBox(-1.0f, -0.5f, -1.0f, 2.0f, 1.0f, 1.0f, new CubeDeformation(0.3f)), PartPose.offsetAndRotation(0.0f, 20.4607f, -5.7879f, -0.2603f, 0.0049f, 0.015f));
        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(86, 0).addBox(-1.0f, -7.0f, -1.0f, 2.0f, 7.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(2.0f, 16.1689f, -3.9116f, 0.5544f, -0.233f, 0.4733f));
        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(64, 87).addBox(-1.0f, -7.0f, -1.0f, 2.0f, 7.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(0, 83).addBox(-1.0f, -7.0f, 1.0f, 2.0f, 2.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(-2.0f, 16.1689f, -3.9116f, 0.5544f, 0.233f, -0.4733f));
        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(24, 51).addBox(-0.5f, -0.5f, -2.0f, 1.0f, 1.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(-2.0f, 21.22f, -5.8316f, 0.4074f, 0.5293f, 0.2145f));
        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(24, 54).addBox(-0.5f, -0.5f, -2.0f, 1.0f, 1.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(2.0f, 21.22f, -5.8316f, 0.4074f, -0.5293f, -0.2145f));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 18).addBox(-6.9977f, 23.4064f, -4.9998f, 16.0f, 1.0f, 16.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-6.9977f, 22.4064f, -4.9998f, 16.0f, 2.0f, 16.0f, new CubeDeformation(0.0f)).texOffs(32, 35).addBox(-2.9977f, 20.4064f, -0.9998f, 8.0f, 3.0f, 8.0f, new CubeDeformation(0.0f)).texOffs(32, 46).addBox(-2.9977f, 20.4064f, -0.9998f, 8.0f, 3.0f, 8.0f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation(0.0f, -9.625f, 0.0f, -0.1309f, 0.0019f, 0.0436f));
        PartDefinition cube_r5 = hat.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(63, 73).addBox(-0.3292f, 19.7814f, -11.146f, 0.0f, 6.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(2.0f, -3.0f, 0.0f, 0.0f, -2.9234f, 0.0f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void setRotations(float x, float y, float z) {
        this.head.yRot = y * ((float) Math.PI / 180);
        this.head.xRot = z * ((float) Math.PI / 180);
    }

    public void renderToBuffer(PoseStack matrix, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        float red = ((color >>> 16) & 255) / 255F, green = ((color >>> 8) & 255) / 255F, blue = (color & 255) / 255F, alpha = ((color >>> 24) & 255) / 255F;
        this.head.render(matrix, buffer, packedLight, packedOverlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
    }
}

