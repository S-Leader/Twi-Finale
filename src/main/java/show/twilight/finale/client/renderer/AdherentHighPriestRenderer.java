package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import show.twilight.finale.client.layer.ForceFieldLayer;
import show.twilight.finale.client.model.AdherentHighPriestModel;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.runtime.client.layer.GlowLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdherentHighPriestRenderer extends MobRenderer<AdherentHighPriestEntity, AdherentHighPriestModel<AdherentHighPriestEntity>> {
    private final Map<Integer, List<Shadow>> shadows = new HashMap<Integer, List<Shadow>>();
    private final Map<Integer, Float> lastShadowTick = new HashMap<Integer, Float>();

    public AdherentHighPriestRenderer(EntityRendererProvider.Context context) {
        super(context, new AdherentHighPriestModel(context.bakeLayer(AdherentHighPriestModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
        this.addLayer(new ForceFieldLayer(this, context.bakeLayer(ForceFieldLayer.LAYER_LOCATION)));
        this.addLayer(new GlowLayer(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/adherent_high_priest_glow.png")));
    }

    protected float getFlipDegrees(@NotNull AdherentHighPriestEntity entity) {
        return 0.0f;
    }

    protected void setupRotations(@NotNull AdherentHighPriestEntity entity, @NotNull PoseStack stack, float ageInTicks, float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, stack, ageInTicks, rotationYaw, partialTicks, scale);
        float time = (float) entity.tickCount + partialTicks;
        float offsetY = -0.125f - Mth.sin(time * 0.133f) * 0.1f;
        float sX = 0.0f;
        float sY = 0.0f;
        float sZ = 0.0f;
        if (entity.deathTime > 0 && entity.deathTime < 95) {
            float deathTimeWithPartial = (float) entity.deathTime + partialTicks;
            float intensity = deathTimeWithPartial <= 50.0f ? deathTimeWithPartial / 50.0f * 0.125f : 0.125f;
            sX = sZ = Mth.sin(deathTimeWithPartial * (float) Math.PI * 2.0f) * intensity;
            sY = Mth.cos(deathTimeWithPartial * (float) Math.PI * 1.5f) * intensity;
        }
        stack.translate(sX, offsetY + sY, sZ);
    }

    public void render(@NotNull AdherentHighPriestEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack stack, @NotNull MultiBufferSource bufferSource, int light) {
        float renderAlpha = 1.0f;
        if (entity.isUsingSkill() && entity.getSkillType() == 3 && (float) entity.getSkillTick() >= 55.0f && (float) entity.getSkillTick() <= 70.0f) {
            renderAlpha = (float) entity.getSkillTick() <= 60.0f ? Mth.lerp(((float) entity.getSkillTick() + partialTicks - 55.0f) / 5.0f, 1.0f, 0.0f) : ((float) entity.getSkillTick() <= 65.0f ? 0.0f : Mth.lerp(((float) entity.getSkillTick() + partialTicks - 65.0f) / 5.0f, 0.0f, 1.0f));
        }
        if (entity.isShadowClone()) {
            renderAlpha = entity.tickCount < 10 ? Mth.lerp(((float) entity.tickCount + partialTicks) / 10.0f, 0.0f, 0.65f) : (entity.tickCount < 70 ? 0.65f : (entity.tickCount < 90 ? Mth.lerp(Mth.clamp(((float) entity.tickCount + partialTicks - 70.0f) / 20.0f, 0.0f, 1.0f), 0.65f, 0.0f) : 0.0f));
        }
        if (entity.deathTime >= 50 && entity.deathTime <= 95) {
            float deathProgress = ((float) entity.deathTime + partialTicks - 50.0f) / 45.0f;
            renderAlpha = Math.min(renderAlpha, Mth.lerp(Mth.clamp(deathProgress, 0.0f, 1.0f), 1.0f, 0.0f));
        } else if (entity.deathTime > 95) {
            renderAlpha = 0.0f;
        }
        this.getModel().alpha = renderAlpha;
        if (renderAlpha > 0.0f) {
            super.render(entity, entityYaw, partialTicks, stack, bufferSource, light);
        }
        if (entity.deathTime <= 0) {
            this.renderShadow(entity, partialTicks, stack, bufferSource, light);
        }
    }

    private void renderShadow(AdherentHighPriestEntity entity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int light) {
        float interval;
        float ageInTicks = (float) entity.level().getGameTime() + partialTicks;
        float f = interval = entity.isShadowClone() ? 0.5f : 2.0f;
        if (this.shouldRenderShadow(entity) && (this.shadows.get(entity.getId()) == null || ageInTicks - this.lastShadowTick.get(entity.getId()).floatValue() >= interval)) {
            this.shadows.computeIfAbsent(entity.getId(), id -> new ArrayList()).add(new Shadow(entity.getPosition(partialTicks), entity.yBodyRot, entity.yBodyRot, entity.getXRot(), ageInTicks, this.getBob(entity, partialTicks), entity.walkAnimation.position(partialTicks), entity.walkAnimation.speed(partialTicks)));
            this.lastShadowTick.put(entity.getId(), Float.valueOf(ageInTicks));
        }
        if (this.shadows.get(entity.getId()) != null && !this.shadows.get(entity.getId()).isEmpty()) {
            for (Shadow shadow2 : this.shadows.get(entity.getId())) {
                stack.pushPose();
                Vec3 position = entity.getPosition(partialTicks);
                stack.translate(shadow2.position.x() - position.x(), shadow2.position.y() - position.y(), shadow2.position.z() - position.z());
                stack.mulPose(Axis.YP.rotationDegrees(180.0f - shadow2.yBodyRot));
                float shadowAge = ageInTicks - shadow2.age;
                float scale = 1.0f - 0.2f * (shadowAge / 10.0f);
                stack.scale(scale, scale, scale);
                stack.scale(-1.0f, -1.0f, 1.0f);
                this.scale(entity, stack, partialTicks);
                stack.translate(0.0f, -1.501f, 0.0f);
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
                this.model.prepareMobModel(entity, shadow2.limbSwing, shadow2.limbSwingAmount, partialTicks);
                this.model.setupAnim(entity, shadow2.limbSwing, shadow2.limbSwingAmount, shadow2.animationProgress, shadow2.yHeadRot - shadow2.yBodyRot, shadow2.xRot);
                float alpha = Math.max(0.0f, 1.0f - shadowAge / 10.0f);
                float color = 1.0f - 0.5f * (shadowAge / 10.0f);
                this.getModel().renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(alpha, color, color, color));
                stack.popPose();
            }
        }
        if (this.shadows.containsKey(entity.getId())) {
            this.shadows.get(entity.getId()).removeIf(shadow -> ageInTicks - shadow.age >= 15.0f);
        }
    }

    private boolean shouldRenderShadow(AdherentHighPriestEntity entity) {
        if (entity.isShadowClone()) {
            return entity.tickCount > 3 && entity.tickCount <= 5;
        }
        return entity.walkDist != 0.0f || entity.walkDistO != 0.0f || entity.getDeltaMovement().lengthSqr() >= 0.015;
    }

    @Nullable
    protected RenderType getRenderType(@NotNull AdherentHighPriestEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (entity.isShadowClone() || entity.deathTime > 0) {
            return RenderType.entityTranslucent(this.getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    public boolean shouldRender(@NotNull AdherentHighPriestEntity entity, @NotNull Frustum frustum, double x, double y, double z) {
        if (this.shouldRenderShadow(entity) || !this.shadows.isEmpty() && this.shadows.get(entity.getId()) != null && !this.shadows.get(entity.getId()).isEmpty()) {
            return true;
        }
        return super.shouldRender(entity, frustum, x, y, z);
    }

    public ResourceLocation getTextureLocation(AdherentHighPriestEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/adherent_high_priest.png");
    }

    private record Shadow(Vec3 position, float yBodyRot, float yHeadRot, float xRot, float age, float animationProgress,
                          float limbSwing, float limbSwingAmount) {
    }
}

