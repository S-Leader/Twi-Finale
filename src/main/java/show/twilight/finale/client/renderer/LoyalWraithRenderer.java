package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import show.twilight.finale.client.model.LoyalWraithModel;
import show.twilight.finale.entity.LoyalWraithEntity;
import show.twilight.finale.runtime.client.layer.GlowLayer;
import twilightforest.TwilightForestMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoyalWraithRenderer
        extends HumanoidMobRenderer<LoyalWraithEntity, LoyalWraithModel> {
    private static final ResourceLocation textureWraith = TwilightForestMod.getModelTexture("ghost.png");
    private final Map<Integer, List<ShadowRecord>> shadows = new HashMap<Integer, List<ShadowRecord>>();
    private final Map<Integer, Float> lastShadowTick = new HashMap<Integer, Float>();

    public LoyalWraithRenderer(EntityRendererProvider.Context manager, LoyalWraithModel modelbiped, float shadowSize) {
        super(manager, modelbiped, shadowSize);
        this.addLayer(new GlowLayer(this, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/ghost_glow.png")));
    }

    public void render(@NotNull LoyalWraithEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack stack, @NotNull MultiBufferSource bufferSource, int light) {
        super.render(entity, entityYaw, partialTicks, stack, bufferSource, light);
        this.renderShadows(entity, partialTicks, stack, bufferSource, light);
    }

    private void renderShadows(LoyalWraithEntity entity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int light) {
        float ageInTicks = (float) entity.level().getGameTime() + partialTicks;
        if (this.shouldRecordShadow(entity) && (this.shadows.get(entity.getId()) == null || ageInTicks - this.lastShadowTick.getOrDefault(entity.getId(), Float.valueOf(0.0f)).floatValue() >= 5.0f)) {
            this.shadows.computeIfAbsent(entity.getId(), id -> new ArrayList()).add(new ShadowRecord(entity.getPosition(partialTicks), entity.yBodyRot, entity.yBodyRot, entity.getXRot(), ageInTicks, this.getBob(entity, partialTicks), entity.walkAnimation.position(partialTicks), entity.walkAnimation.speed(partialTicks)));
            this.lastShadowTick.put(entity.getId(), Float.valueOf(ageInTicks));
        }
        if (this.shadows.get(entity.getId()) != null && !this.shadows.get(entity.getId()).isEmpty()) {
            for (ShadowRecord shadow : this.shadows.get(entity.getId())) {
                stack.pushPose();
                Vec3 position = entity.getPosition(partialTicks);
                stack.translate(shadow.position.x() - position.x(), shadow.position.y() - position.y(), shadow.position.z() - position.z());
                stack.mulPose(Axis.YP.rotationDegrees(180.0f - shadow.yBodyRot));
                float scale = 1.0f - 0.2f * ((ageInTicks - shadow.age) / 10.0f);
                stack.scale(scale, scale, scale);
                stack.scale(-1.0f, -1.0f, 1.0f);
                this.scale(entity, stack, partialTicks);
                stack.translate(0.0f, -1.501f, 0.0f);
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
                this.model.prepareMobModel(entity, shadow.limbSwing, shadow.limbSwingAmount, partialTicks);
                this.model.setupAnim(entity, shadow.limbSwing, shadow.limbSwingAmount, shadow.animationProgress, shadow.yHeadRot - shadow.yBodyRot, shadow.xRot);
                float alpha = Math.max(0.0f, 1.0f - (ageInTicks - shadow.age) / 10.0f);
                float color = 1.0f - 0.5f * ((ageInTicks - shadow.age) / 10.0f);
                this.getModel().renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, color, color, color));
                stack.popPose();
            }
        }
        this.shadows.values().forEach(list -> list.removeIf(shadow -> ageInTicks - shadow.age >= 15.0f));
    }

    private boolean shouldRecordShadow(LoyalWraithEntity entity) {
        return entity.walkAnimation.speed() != 0.0f || entity.getDeltaMovement().lengthSqr() >= 0.015;
    }

    public boolean shouldRender(@NotNull LoyalWraithEntity entity, @NotNull Frustum frustum, double x, double y, double z) {
        if (this.shouldRecordShadow(entity) || this.shadows.containsKey(entity.getId()) && !this.shadows.get(entity.getId()).isEmpty()) {
            return true;
        }
        return super.shouldRender(entity, frustum, x, y, z);
    }

    @Nullable
    protected RenderType getRenderType(LoyalWraithEntity entity, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }

    public ResourceLocation getTextureLocation(LoyalWraithEntity wraith) {
        return textureWraith;
    }

    private record ShadowRecord(Vec3 position, float yBodyRot, float yHeadRot, float xRot, float age,
                                float animationProgress, float limbSwing, float limbSwingAmount) {
    }
}

