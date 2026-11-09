package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class CastleGuardianModel<T extends LivingEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "castle_guardian"), "main");
    private final ModelPart root;
    private final ModelPart mainBody;
    private final ModelPart midBody;
    private final ModelPart head;

    public CastleGuardianModel(ModelPart root) {
        this.root = root;
        this.mainBody = root.getChild("main_body");
        this.midBody = root.getChild("mid_body");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition mainBodyDef = partdefinition.addOrReplaceChild("main_body", CubeListBuilder.create().texOffs(6, 49).addBox(-7.0f, 18.8f, -7.5f, 14.0f, 2.0f, 14.0f), PartPose.rotation(0.0f, 1.5707963f, 0.0f));
        PartDefinition midBodyDef = partdefinition.addOrReplaceChild("mid_body", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offset(0.0f, 3.0f, 0.0f));
        PartDefinition headDef = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offset(0.0f, 2.5f, 0.0f));
        partdefinition.addOrReplaceChild("plate_group_1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offset(0.0f, 35.0f, 0.0f));
        PartDefinition plateGroup2 = mainBodyDef.addOrReplaceChild("plate_group_2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, 35.0f, 0.0f, 0.0f, 1.0471976f, 0.0f));
        PartDefinition plateGroup3 = mainBodyDef.addOrReplaceChild("plate_group_3", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, 35.0f, 0.0f, 0.0f, 2.0943952f, 0.0f));
        PartDefinition plateGroup4 = mainBodyDef.addOrReplaceChild("plate_group_4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, 35.0f, 0.0f, 0.0f, (float) Math.PI, 0.0f));
        PartDefinition plateGroup5 = mainBodyDef.addOrReplaceChild("plate_group_5", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, 35.0f, 0.0f, 0.0f, 4.1887903f, 0.0f));
        PartDefinition plateGroup6 = mainBodyDef.addOrReplaceChild("plate_group_6", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, 35.0f, 0.0f, 0.0f, 5.2359877f, 0.0f));
        PartDefinition plateGroup1 = mainBodyDef.addOrReplaceChild("plate_group_1_sub", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offset(0.0f, 35.0f, 0.0f));
        PartDefinition plating = plateGroup1.addOrReplaceChild("plating", CubeListBuilder.create().texOffs(0, 32).addBox(29.0f, -8.0f, -8.0f, 1.0f, 16.0f, 16.0f), PartPose.rotation(0.7853982f, 0.0f, -0.7853982f));
        plating.addOrReplaceChild("support", CubeListBuilder.create().texOffs(31, 24).addBox(23.5f, 0.0f, 0.0f, 6.0f, 7.0f, 7.0f), PartPose.offset(0.0f, -7.0f, 0.0f));
        PartDefinition spikeWrapper = plateGroup1.addOrReplaceChild("spike_wrapper", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 0.5235988f, 0.0f));
        PartDefinition spike = spikeWrapper.addOrReplaceChild("spike", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 1.0f, 12.0f, 12.0f, 12.0f), PartPose.offsetAndRotation(0.0f, -20.0f, 0.0f, 0.0f, 0.7853982f, -0.6065019f));
        spike.addOrReplaceChild("spike_cap", CubeListBuilder.create().texOffs(7, 25).addBox(0.0f, 0.0f, 0.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(8.0f, -1.0f, 8.0f));
        plateGroup1.addOrReplaceChild("inner_support", CubeListBuilder.create().texOffs(23, 38).addBox(7.0f, -1.4f, -4.0f, 11.0f, 2.0f, 8.0f), PartPose.offsetAndRotation(0.0f, -10.0f, 0.0f, 0.0f, 0.0f, -0.4553564f));
        PartDefinition plating1 = plateGroup2.addOrReplaceChild("plating_1", CubeListBuilder.create().texOffs(0, 32).addBox(29.0f, -8.0f, -8.0f, 1.0f, 16.0f, 16.0f), PartPose.rotation(0.7853982f, 0.0f, -0.7853982f));
        plating1.addOrReplaceChild("support_1", CubeListBuilder.create().texOffs(31, 24).addBox(23.5f, 0.0f, 0.0f, 6.0f, 7.0f, 7.0f), PartPose.offset(0.0f, -7.0f, 0.0f));
        PartDefinition spikeWrapper1 = plateGroup2.addOrReplaceChild("spike_wrapper_1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 0.5235988f, 0.0f));
        PartDefinition spike1 = spikeWrapper1.addOrReplaceChild("spike_1", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 1.0f, 12.0f, 12.0f, 12.0f), PartPose.offsetAndRotation(0.0f, -20.0f, 0.0f, 0.0f, 0.7853982f, -0.6065019f));
        spike1.addOrReplaceChild("spike_cap_1", CubeListBuilder.create().texOffs(7, 25).addBox(0.0f, 0.0f, 0.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(8.0f, -1.0f, 8.0f));
        plateGroup2.addOrReplaceChild("inner_support_1", CubeListBuilder.create().texOffs(23, 38).addBox(7.0f, -1.4f, -4.0f, 11.0f, 2.0f, 8.0f), PartPose.offsetAndRotation(0.0f, -10.0f, 0.0f, 0.0f, 0.0f, -0.4553564f));
        PartDefinition plating2 = plateGroup3.addOrReplaceChild("plating_2", CubeListBuilder.create().texOffs(0, 32).addBox(29.0f, -8.0f, -8.0f, 1.0f, 16.0f, 16.0f), PartPose.rotation(0.7853982f, 0.0f, -0.7853982f));
        plating2.addOrReplaceChild("support_2", CubeListBuilder.create().texOffs(31, 24).addBox(23.5f, 0.0f, 0.0f, 6.0f, 7.0f, 7.0f), PartPose.offset(0.0f, -7.0f, 0.0f));
        PartDefinition spikeWrapper2 = plateGroup3.addOrReplaceChild("spike_wrapper_2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 0.5235988f, 0.0f));
        PartDefinition spike2 = spikeWrapper2.addOrReplaceChild("spike_2", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 1.0f, 12.0f, 12.0f, 12.0f), PartPose.offsetAndRotation(0.0f, -20.0f, 0.0f, 0.0f, 0.7853982f, -0.6065019f));
        spike2.addOrReplaceChild("spike_cap_2", CubeListBuilder.create().texOffs(7, 25).addBox(0.0f, 0.0f, 0.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(8.0f, -1.0f, 8.0f));
        plateGroup3.addOrReplaceChild("inner_support_2", CubeListBuilder.create().texOffs(23, 38).addBox(7.0f, -1.4f, -4.0f, 11.0f, 2.0f, 8.0f), PartPose.offsetAndRotation(0.0f, -10.0f, 0.0f, 0.0f, 0.0f, -0.4553564f));
        PartDefinition plating3 = plateGroup4.addOrReplaceChild("plating_3", CubeListBuilder.create().texOffs(0, 32).addBox(29.0f, -8.0f, -8.0f, 1.0f, 16.0f, 16.0f), PartPose.rotation(0.7853982f, 0.0f, -0.7853982f));
        plating3.addOrReplaceChild("support_3", CubeListBuilder.create().texOffs(31, 24).addBox(23.5f, 0.0f, 0.0f, 6.0f, 7.0f, 7.0f), PartPose.offset(0.0f, -7.0f, 0.0f));
        PartDefinition spikeWrapper3 = plateGroup4.addOrReplaceChild("spike_wrapper_3", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 0.5235988f, 0.0f));
        PartDefinition spike3 = spikeWrapper3.addOrReplaceChild("spike_3", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 1.0f, 12.0f, 12.0f, 12.0f), PartPose.offsetAndRotation(0.0f, -20.0f, 0.0f, 0.0f, 0.7853982f, -0.6065019f));
        spike3.addOrReplaceChild("spike_cap_3", CubeListBuilder.create().texOffs(7, 25).addBox(0.0f, 0.0f, 0.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(8.0f, -1.0f, 8.0f));
        plateGroup4.addOrReplaceChild("inner_support_3", CubeListBuilder.create().texOffs(23, 38).addBox(7.0f, -1.4f, -4.0f, 11.0f, 2.0f, 8.0f), PartPose.offsetAndRotation(0.0f, -10.0f, 0.0f, 0.0f, 0.0f, -0.4553564f));
        PartDefinition plating4 = plateGroup5.addOrReplaceChild("plating_4", CubeListBuilder.create().texOffs(0, 32).addBox(29.0f, -8.0f, -8.0f, 1.0f, 16.0f, 16.0f), PartPose.rotation(0.7853982f, 0.0f, -0.7853982f));
        plating4.addOrReplaceChild("support_4", CubeListBuilder.create().texOffs(31, 24).addBox(23.5f, 0.0f, 0.0f, 6.0f, 7.0f, 7.0f), PartPose.offset(0.0f, -7.0f, 0.0f));
        PartDefinition spikeWrapper4 = plateGroup5.addOrReplaceChild("spike_wrapper_4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 0.5235988f, 0.0f));
        PartDefinition spike4 = spikeWrapper4.addOrReplaceChild("spike_4", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 1.0f, 12.0f, 12.0f, 12.0f), PartPose.offsetAndRotation(0.0f, -20.0f, 0.0f, 0.0f, 0.7853982f, -0.6065019f));
        spike4.addOrReplaceChild("spike_cap_4", CubeListBuilder.create().texOffs(7, 25).addBox(0.0f, 0.0f, 0.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(8.0f, -1.0f, 8.0f));
        plateGroup5.addOrReplaceChild("inner_support_4", CubeListBuilder.create().texOffs(23, 38).addBox(7.0f, -1.4f, -4.0f, 11.0f, 2.0f, 8.0f), PartPose.offsetAndRotation(0.0f, -10.0f, 0.0f, 0.0f, 0.0f, -0.4553564f));
        PartDefinition plating5 = plateGroup6.addOrReplaceChild("plating_5", CubeListBuilder.create().texOffs(0, 32).addBox(29.0f, -8.0f, -8.0f, 1.0f, 16.0f, 16.0f), PartPose.rotation(0.7853982f, 0.0f, -0.7853982f));
        plating5.addOrReplaceChild("support_5", CubeListBuilder.create().texOffs(31, 24).addBox(23.5f, 0.0f, 0.0f, 6.0f, 7.0f, 7.0f), PartPose.offset(0.0f, -7.0f, 0.0f));
        PartDefinition spikeWrapper5 = plateGroup6.addOrReplaceChild("spike_wrapper_5", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 0.5235988f, 0.0f));
        PartDefinition spike5 = spikeWrapper5.addOrReplaceChild("spike_5", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 1.0f, 12.0f, 12.0f, 12.0f), PartPose.offsetAndRotation(0.0f, -20.0f, 0.0f, 0.0f, 0.7853982f, -0.6065019f));
        spike5.addOrReplaceChild("spike_cap_5", CubeListBuilder.create().texOffs(7, 25).addBox(0.0f, 0.0f, 0.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(8.0f, -1.0f, 8.0f));
        plateGroup6.addOrReplaceChild("inner_support_5", CubeListBuilder.create().texOffs(23, 38).addBox(7.0f, -1.4f, -4.0f, 11.0f, 2.0f, 8.0f), PartPose.offsetAndRotation(0.0f, -10.0f, 0.0f, 0.0f, 0.0f, -0.4553564f));
        midBodyDef.addOrReplaceChild("cover", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -0.6f, -6.0f, 12.0f, 1.0f, 12.0f), PartPose.rotation(0.0f, 0.7853982f, 0.0f));
        midBodyDef.addOrReplaceChild("runed_siding_1", CubeListBuilder.create().texOffs(48, 48).addBox(-4.0f, -4.0f, 6.0f, 8.0f, 10.0f, 4.0f), PartPose.rotation(-0.3926991f, 0.0f, 0.0f));
        midBodyDef.addOrReplaceChild("bridge_1", CubeListBuilder.create().texOffs(36, 0).addBox(-4.0f, -3.0f, 6.0f, 8.0f, 9.0f, 3.0f), PartPose.rotation(-0.283616f, 0.7853982f, 0.0f));
        midBodyDef.addOrReplaceChild("runed_siding_2", CubeListBuilder.create().texOffs(48, 48).addBox(-4.0f, -4.0f, 6.0f, 8.0f, 10.0f, 4.0f), PartPose.rotation(-0.3926991f, 1.5707963f, 0.0f));
        midBodyDef.addOrReplaceChild("bridge_2", CubeListBuilder.create().texOffs(36, 0).addBox(-4.0f, -3.0f, 6.0f, 8.0f, 9.0f, 3.0f), PartPose.rotation(-0.283616f, 2.3561945f, 0.0f));
        midBodyDef.addOrReplaceChild("runed_siding_3", CubeListBuilder.create().texOffs(48, 48).addBox(-4.0f, -4.0f, 6.0f, 8.0f, 10.0f, 4.0f), PartPose.rotation(-0.3926991f, (float) Math.PI, 0.0f));
        midBodyDef.addOrReplaceChild("bridge_3", CubeListBuilder.create().texOffs(36, 0).addBox(-4.0f, -3.0f, 6.0f, 8.0f, 9.0f, 3.0f), PartPose.rotation(-0.283616f, -2.3561945f, 0.0f));
        midBodyDef.addOrReplaceChild("runed_siding_4", CubeListBuilder.create().texOffs(48, 48).addBox(-4.0f, -4.0f, 6.0f, 8.0f, 10.0f, 4.0f), PartPose.rotation(-0.3926991f, -1.5707963f, 0.0f));
        midBodyDef.addOrReplaceChild("bridge_4", CubeListBuilder.create().texOffs(36, 0).addBox(-4.0f, -3.0f, 6.0f, 8.0f, 9.0f, 3.0f), PartPose.rotation(-0.283616f, -0.7853982f, 0.0f));
        PartDefinition headGroup1 = headDef.addOrReplaceChild("head_group_1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offset(0.0f, -2.0f, 0.0f));
        PartDefinition headPlate5 = headGroup1.addOrReplaceChild("head_plate_5", CubeListBuilder.create().texOffs(86, 0).addBox(-5.0f, -17.0f, -9.2f, 10.0f, 17.0f, 4.0f), PartPose.rotation(0.1963495f, 0.0f, 0.0f));
        headPlate5.addOrReplaceChild("eye_frame_l", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -11.0f, 0.0f, 1.0f, 5.0f, 2.0f), PartPose.offset(-4.0f, 0.0f, -9.2f));
        headPlate5.addOrReplaceChild("eye_frame_r", CubeListBuilder.create().texOffs(4, 0).addBox(0.0f, -11.0f, 0.0f, 1.0f, 5.0f, 2.0f), PartPose.offset(3.0f, 0.0f, -9.2f));
        headPlate5.addOrReplaceChild("eye_frame_lt", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -5.0f, 0.0f, 1.0f, 5.0f, 2.0f), PartPose.offsetAndRotation(-4.0f, -10.0f, -9.2f, 0.0f, 0.0f, 0.7853982f));
        headPlate5.addOrReplaceChild("eye_frame_rt", CubeListBuilder.create().texOffs(6, 7).addBox(0.0f, 0.4f, 0.0f, 1.0f, 5.0f, 2.0f), PartPose.offsetAndRotation(4.0f, -10.0f, -9.2f, 0.0f, 0.0f, 2.3561945f));
        headPlate5.addOrReplaceChild("eye_frame_rb", CubeListBuilder.create().texOffs(6, 0).addBox(0.0f, -5.0f, 0.0f, 1.0f, 5.0f, 2.0f), PartPose.offsetAndRotation(4.0f, -7.0f, -9.2f, 0.0f, 0.0f, -2.3561945f));
        headPlate5.addOrReplaceChild("eye_frame_lb", CubeListBuilder.create().texOffs(0, 7).addBox(0.0f, 0.4f, 0.0f, 1.0f, 5.0f, 2.0f), PartPose.offsetAndRotation(-4.0f, -7.0f, -9.2f, 0.0f, 0.0f, -0.7853982f));
        headPlate5.addOrReplaceChild("head_extended_5", CubeListBuilder.create().texOffs(64, 33).addBox(-5.0f, 1.0f, 0.0f, 10.0f, 1.0f, 9.0f), PartPose.offsetAndRotation(0.0f, -18.0f, -5.0f, -0.2181662f, 0.0f, 0.0f));
        PartDefinition headSpikeWrapper5 = headGroup1.addOrReplaceChild("head_spike_wrapper_5", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 2.0943952f, 0.0f));
        headSpikeWrapper5.addOrReplaceChild("head_spike_5", CubeListBuilder.create().texOffs(57, 21).addBox(3.7f, -17.9f, 3.7f, 4.0f, 17.0f, 4.0f), PartPose.rotation(0.0f, 0.7853982f, 0.2670354f));
        PartDefinition headGroup2 = headDef.addOrReplaceChild("head_group_2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, -2.0f, 0.0f, 0.0f, 1.0471976f, 0.0f));
        PartDefinition headPlate = headGroup2.addOrReplaceChild("head_plate", CubeListBuilder.create().texOffs(58, 0).addBox(-5.0f, -17.0f, -9.2f, 10.0f, 17.0f, 4.0f), PartPose.rotation(0.1963495f, 0.0f, 0.0f));
        headPlate.addOrReplaceChild("head_extended", CubeListBuilder.create().texOffs(64, 33).mirror().addBox(-5.0f, 1.0f, 0.0f, 10.0f, 1.0f, 9.0f), PartPose.offsetAndRotation(0.0f, -18.0f, -5.0f, -0.2181662f, 0.0f, 0.0f));
        PartDefinition headSpikeWrapper = headGroup2.addOrReplaceChild("head_spike_wrapper", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 2.0943952f, 0.0f));
        headSpikeWrapper.addOrReplaceChild("head_spike", CubeListBuilder.create().texOffs(57, 21).addBox(3.7f, -17.9f, 3.7f, 4.0f, 17.0f, 4.0f), PartPose.rotation(0.0f, 0.7853982f, 0.2670354f));
        PartDefinition headGroup3 = headDef.addOrReplaceChild("head_group_3", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, -2.0f, 0.0f, 0.0f, 2.0943952f, 0.0f));
        PartDefinition headPlate1 = headGroup3.addOrReplaceChild("head_plate_1", CubeListBuilder.create().texOffs(58, 0).addBox(-5.0f, -17.0f, -9.2f, 10.0f, 17.0f, 4.0f), PartPose.rotation(0.1963495f, 0.0f, 0.0f));
        headPlate1.addOrReplaceChild("head_extended_1", CubeListBuilder.create().texOffs(64, 33).addBox(-5.0f, 1.0f, 0.0f, 10.0f, 1.0f, 9.0f), PartPose.offsetAndRotation(0.0f, -18.0f, -5.0f, -0.2181662f, 0.0f, 0.0f));
        PartDefinition headSpikeWrapper1 = headGroup3.addOrReplaceChild("head_spike_wrapper_1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 2.0943952f, 0.0f));
        headSpikeWrapper1.addOrReplaceChild("head_spike_1", CubeListBuilder.create().texOffs(57, 21).addBox(3.7f, -17.9f, 3.7f, 4.0f, 17.0f, 4.0f), PartPose.rotation(0.0f, 0.7853982f, 0.2670354f));
        PartDefinition headGroup4 = headDef.addOrReplaceChild("head_group_4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, -2.0f, 0.0f, 0.0f, (float) Math.PI, 0.0f));
        PartDefinition headPlate2 = headGroup4.addOrReplaceChild("head_plate_2", CubeListBuilder.create().texOffs(58, 0).addBox(-5.0f, -17.0f, -9.2f, 10.0f, 17.0f, 4.0f), PartPose.rotation(0.1963495f, 0.0f, 0.0f));
        headPlate2.addOrReplaceChild("head_extended_2", CubeListBuilder.create().texOffs(64, 33).mirror().addBox(-5.0f, 1.0f, 0.0f, 10.0f, 1.0f, 9.0f), PartPose.offsetAndRotation(0.0f, -18.0f, -5.0f, -0.2181662f, 0.0f, 0.0f));
        PartDefinition headSpikeWrapper2 = headGroup4.addOrReplaceChild("head_spike_wrapper_2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 2.0943952f, 0.0f));
        headSpikeWrapper2.addOrReplaceChild("head_spike_2", CubeListBuilder.create().texOffs(57, 21).addBox(3.7f, -17.9f, 3.7f, 4.0f, 17.0f, 4.0f), PartPose.rotation(0.0f, 0.7853982f, 0.2670354f));
        PartDefinition headGroup5 = headDef.addOrReplaceChild("head_group_5", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, -2.0f, 0.0f, 0.0f, 4.1887903f, 0.0f));
        PartDefinition headPlate3 = headGroup5.addOrReplaceChild("head_plate_3", CubeListBuilder.create().texOffs(58, 0).addBox(-5.0f, -17.0f, -9.2f, 10.0f, 17.0f, 4.0f), PartPose.rotation(0.1963495f, 0.0f, 0.0f));
        headPlate3.addOrReplaceChild("head_extended_3", CubeListBuilder.create().texOffs(64, 33).addBox(-5.0f, 1.0f, 0.0f, 10.0f, 1.0f, 9.0f), PartPose.offsetAndRotation(0.0f, -18.0f, -5.0f, -0.2181662f, 0.0f, 0.0f));
        PartDefinition headSpikeWrapper3 = headGroup5.addOrReplaceChild("head_spike_wrapper_3", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 2.0943952f, 0.0f));
        headSpikeWrapper3.addOrReplaceChild("head_spike_3", CubeListBuilder.create().texOffs(57, 21).addBox(3.7f, -17.9f, 3.7f, 4.0f, 17.0f, 4.0f), PartPose.rotation(0.0f, 0.7853982f, 0.2670354f));
        PartDefinition headGroup6 = headDef.addOrReplaceChild("head_group_6", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.offsetAndRotation(0.0f, -2.0f, 0.0f, 0.0f, 5.2359877f, 0.0f));
        PartDefinition headPlate4 = headGroup6.addOrReplaceChild("head_plate_4", CubeListBuilder.create().texOffs(58, 0).addBox(-5.0f, -17.0f, -9.2f, 10.0f, 17.0f, 4.0f), PartPose.rotation(0.1963495f, 0.0f, 0.0f));
        headPlate4.addOrReplaceChild("head_extended_4", CubeListBuilder.create().texOffs(64, 33).mirror().addBox(-5.0f, 1.0f, 0.0f, 10.0f, 1.0f, 9.0f), PartPose.offsetAndRotation(0.0f, -18.0f, -5.0f, -0.2181662f, 0.0f, 0.0f));
        PartDefinition headSpikeWrapper4 = headGroup6.addOrReplaceChild("head_spike_wrapper_4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), PartPose.rotation(0.0f, 2.0943952f, 0.0f));
        headSpikeWrapper4.addOrReplaceChild("head_spike_4", CubeListBuilder.create().texOffs(57, 21).addBox(3.7f, -17.9f, 3.7f, 4.0f, 17.0f, 4.0f), PartPose.rotation(0.0f, 0.7853982f, 0.2670354f));
        PartDefinition eyeBack = headDef.addOrReplaceChild("eye_back", CubeListBuilder.create().texOffs(114, 0).addBox(-3.0f, 0.0f, 0.0f, 6.0f, 9.0f, 1.0f), PartPose.offset(0.0f, -13.4f, -9.8f));
        eyeBack.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(73, 21).addBox(-2.0f, -2.0f, -2.0f, 4.0f, 4.0f, 4.0f), PartPose.offsetAndRotation(0.0f, 4.8f, 0.5f, -0.7853982f, -0.6981317f, 1.5707963f));
        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw / 57.295776f;
        this.head.xRot = headPitch / 57.295776f;
        if (entity instanceof show.twilight.finale.entity.CastleGuardianEntity guardian && guardian.isAttached()) {
            this.mainBody.yRot = 1.5707963F;
            this.midBody.yRot = 0;
            if (guardian.getLaserPhase() > 0) {
                var aim = guardian.getBeamEnd().subtract(guardian.getBeamOrigin()).toVector3f();
                aim.rotate(guardian.getAttachFace().getOpposite().getRotation().conjugate()).rotateY((float) Math.PI);
                // The eye faces model -Z; LivingEntityRenderer also mirrors X/Y.
                this.head.yRot = (float) Math.atan2(aim.x, -aim.z);
                this.head.xRot = -(float) Math.atan2(aim.y, Math.sqrt(aim.x * aim.x + aim.z * aim.z));
            }
            return;
        }
        float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float bodyRotation = Mth.rotLerp(partialTicks, (entity).yBodyRotO, (entity).yBodyRot) * (float) Math.PI / 180.0f;
        float spinAngle = ageInTicks * 0.05f;
        this.mainBody.yRot = 1.5707963f - bodyRotation + spinAngle;
        this.midBody.yRot = 0.0f - bodyRotation + spinAngle;
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        float red = ((color >>> 16) & 255) / 255F, green = ((color >>> 8) & 255) / 255F, blue = (color & 255) / 255F, alpha = ((color >>> 24) & 255) / 255F;
        this.mainBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
        this.midBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
    }

    public ModelPart root() {
        return this.root;
    }
}

