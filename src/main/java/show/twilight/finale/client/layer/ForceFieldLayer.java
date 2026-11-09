package show.twilight.finale.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import show.twilight.finale.capabilities.ForceFieldCapabilityList;
import show.twilight.finale.capabilities.shield.IForceFieldCapability;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.runtime.client.renderer.VzlingRenderType;

public class ForceFieldLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "force_field"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/force_field.png");
    private final ModelPart outerLayer;
    private final ModelPart innerLayer;

    public ForceFieldLayer(RenderLayerParent<T, M> renderer, ModelPart root) {
        super(renderer);
        this.outerLayer = root.getChild("outer");
        this.innerLayer = root.getChild("inner");
    }

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeListBuilder cube = CubeListBuilder.create().texOffs(0, 0).addBox(-13.0f, -38.0f, -12.0f, 26.0f, 38.0f, 26.0f);
        partdefinition.addOrReplaceChild("outer", cube, PartPose.offset(0.0f, 24.0f, 0.0f));
        partdefinition.addOrReplaceChild("inner", CubeListBuilder.create().texOffs(0, 0).addBox(-13.0f, -38.0f, -12.0f, 26.0f, 38.0f, 26.0f, new CubeDeformation(-1.0f)), PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void render(PoseStack stack, MultiBufferSource buffer, int light, T living, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ForceFieldCapabilityList.get(living).ifPresent(arg_0 -> this.renderFields(living, partialTicks, buffer, stack, light, arg_0));
    }

    private void renderShieldPart(PoseStack stack, VertexConsumer vc, ModelPart part, int light, float sx, float sy, float sz, float bS, float baseScale, float time, float offset, float alpha) {
        float breath = baseScale + 0.03f * Mth.sin((time - offset) * 0.05f);
        stack.pushPose();
        stack.translate(sx, 1.5 + (double) sy, sz);
        stack.scale(bS * breath, bS * breath, bS * breath);
        stack.translate(0.0, -1.5, 0.0);
        part.render(stack, vc, light, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(alpha, 1.0f, 1.0f, 1.0f));
        stack.popPose();
    }

    private void renderFields(LivingEntity living, float partialTicks, MultiBufferSource buffer, PoseStack stack, int light, IForceFieldCapability cap) {
        float time = living.tickCount + partialTicks;
        float breakDelta = cap.getLastBreakTick() <= -100 ? 1000 : time - cap.getLastBreakTick();
        float shakeDelta = cap.getLastShakeTick() <= -100 ? 1000 : time - cap.getLastShakeTick();
        int fields = cap.getFieldsLeft();
        if (fields <= 0 && breakDelta >= 20) return;
        float alpha = 1;
        if (living instanceof AdherentHighPriestEntity priest) {
            if (priest.deathTime > 0 && breakDelta >= 20) return;
            if (priest.isUsingSkill() && priest.getSkillType() == 3) {
                float tick = priest.getSkillTick() + partialTicks;
                if (tick >= 55 && tick <= 70)
                    alpha = tick <= 60 ? 1 - (tick - 55) / 5 : tick <= 65 ? 0 : (tick - 65) / 5;
            }
            if (priest.isShadowClone())
                alpha *= time < 10 ? time / 10 * 0.65F : time < 70 ? 0.65F : Mth.clamp(1 - (time - 70) / 20, 0, 1) * 0.65F;
        }
        float ratio = (float) Math.max(1, fields) / Math.max(1, cap.getMaxFields());
        float scale = 1;
        alpha *= Mth.lerp(0.5F + 0.5F * Mth.sin(time * 0.05F), 0.7F, 1);
        if (breakDelta >= 0 && breakDelta <= 10) {
            scale += breakDelta * 0.02F;
            alpha *= 1 - breakDelta / 10;
        } else if (breakDelta > 10 && breakDelta <= 20) {
            alpha *= fields == 0 ? 0 : (breakDelta - 10) / 10;
        }
        if (alpha <= 0) return;
        float sx = 0, sy = 0;
        if (living.isAlive() && shakeDelta >= 0 && shakeDelta < 10) {
            float intensity = (1 - shakeDelta / 10) * 0.125F;
            sx = Mth.sin(shakeDelta * Mth.PI * 2) * intensity;
            sy = Mth.cos(shakeDelta * Mth.PI * 1.5F) * intensity;
        }
        var vc = buffer.getBuffer(VzlingRenderType.getEntityGlowEffect(TEXTURE));
        renderShieldPart(stack, vc, outerLayer, light, sx, sy, sx, scale, 1.05F, time, 0, 0.25F * ratio * alpha);
        renderShieldPart(stack, vc, innerLayer, light, sx, sy, sx, scale, 1.05F, time, 20, 0.125F * ratio * alpha);
    }
}

