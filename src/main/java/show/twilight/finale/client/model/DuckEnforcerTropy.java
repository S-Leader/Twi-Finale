package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class DuckEnforcerTropy
        extends GenericTrophyModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "duck_enforcer"), "head");
    public final ModelPart head;

    public DuckEnforcerTropy(ModelPart root) {
        this.head = root.getChild("head");
    }

    public static LayerDefinition createHead() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, 15.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.0f)).texOffs(32, 0).addBox(-4.0f, 15.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.5f)).texOffs(74, 0).addBox(-4.0f, 15.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(1.0f)), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0f, 0.0f, 0.0f, 8.0f, 3.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(0.0f, 15.0f, 4.0f, -0.5672f, 0.0f, 0.0f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void setRotations(float x, float y, float z) {
        this.head.yRot = y * ((float) Math.PI / 180);
        this.head.xRot = z * ((float) Math.PI / 180);
    }

    public void renderToBuffer(PoseStack matrix, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        float red = ((color >>> 16) & 255) / 255F, green = ((color >>> 8) & 255) / 255F, blue = (color & 255) / 255F, alpha = ((color >>> 24) & 255) / 255F;
        this.head.render(matrix, buffer, packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
    }
}

