package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class AdherentHighPriestTrophy
        extends GenericTrophyModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "adherent_high_priest"), "head");
    public final ModelPart head;

    public AdherentHighPriestTrophy(ModelPart root) {
        this.head = root.getChild("head");
    }

    public static LayerDefinition createHead() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, 16.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.0f)).texOffs(0, 16).addBox(-4.0f, 16.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.15f)), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(meshdefinition, 64, 64);
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

