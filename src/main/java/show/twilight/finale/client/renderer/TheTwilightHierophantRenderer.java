package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import show.twilight.finale.client.layer.HierophantArmorLayer;
import show.twilight.finale.client.layer.HierophantOuterLayer;
import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.runtime.client.renderer.Shadow;

@OnlyIn(value = Dist.CLIENT)
public class TheTwilightHierophantRenderer<T extends TheTwilightHierophantEntity> extends MobRenderer<T, TheTwilightHierophantModel<T>> {
    private final TheTwilightHierophantModel<T> slimModel;
    private final TheTwilightHierophantModel<T> normalModel;
    private final HierophantCubeMesh cube;
    private final TheTwilightHierophantModel<T> ghostNormal, ghostSlim, ghostOuterNormal, ghostOuterSlim;
    private final java.util.Map<T, ShadowHistory> histories = new java.util.WeakHashMap<>();

    private record ShadowFrame(Shadow sample, java.util.List<TheTwilightHierophantModel.ShadowBone> bones,
                               boolean slim) {
    }

    private static final class ShadowHistory {
        final java.util.List<ShadowFrame> frames = new java.util.ArrayList<>();
        float lastSample = Float.NEGATIVE_INFINITY;
    }

    public TheTwilightHierophantRenderer(EntityRendererProvider.Context context) {
        super(context, new TheTwilightHierophantModel(context.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION), false), 0.5f);
        this.slimModel = new TheTwilightHierophantModel(context.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_SLIM), true);
        this.normalModel = this.model;
        this.cube = new HierophantCubeMesh(context);
        ghostNormal = new TheTwilightHierophantModel<>(context.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION), false);
        ghostSlim = new TheTwilightHierophantModel<>(context.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_SLIM), true);
        ghostOuterNormal = new TheTwilightHierophantModel<>(context.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_OUTER), false);
        ghostOuterSlim = new TheTwilightHierophantModel<>(context.bakeLayer(TheTwilightHierophantModel.LAYER_LOCATION_OUTER_SLIM), true);
        this.addLayer(new show.twilight.finale.client.layer.HierophantHeldItemLayer<>(this, context.getItemInHandRenderer()));
        HierophantArmorLayer.addCustomArmorLayers(this, context, TheTwilightHierophantModel.LAYER_LOCATION_I, TheTwilightHierophantModel.LAYER_LOCATION_O);
        this.addLayer(new HierophantOuterLayer(this, context.getModelSet()));
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull T entity) {
        this.model = this.normalModel;
        Entity entity2 = Minecraft.getInstance().getCameraEntity();
        if (entity2 instanceof AbstractClientPlayer) {
            AbstractClientPlayer clientPlayer = (AbstractClientPlayer) entity2;
            if (clientPlayer.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM) {
                this.model = this.slimModel;
            }
            return clientPlayer.getSkin().texture();
        }
        return DefaultPlayerSkin.getDefaultTexture();
    }

    public void render(@NotNull T entity, float entityYaw, float partialTicks, @NotNull PoseStack stack, @NotNull MultiBufferSource bufferSource, int light) {
        getTextureLocation(entity); // Select the model before setupAnim, not halfway through rendering.
        float age = entity.tickCount + partialTicks;
        int action = entity.getAction(), time = entity.getActionTime();
        float blur = entity.getBlur();
        float alpha = 1 - blur * 0.45F;
        if (action == TheTwilightHierophantEntity.SMOKE_MOVE) {
            float t = time + partialTicks;
            float dissolve = smooth(t / 8) * smooth((TheTwilightHierophantEntity.getActionDuration(action) - t) / 10);
            alpha = net.minecraft.util.Mth.lerp(dissolve, alpha, 0.12F);
        }
        if (action == TheTwilightHierophantEntity.DEATH)
            alpha *= net.minecraft.util.Mth.clamp((125 - time - partialTicks) / 55F, 0, 1);
        boolean afterimages = entity.getPhase() == 2 && (blur > 0.03F || action == TheTwilightHierophantEntity.SUMMON || action == TheTwilightHierophantEntity.BARRAGE || action == TheTwilightHierophantEntity.SMOKE_MOVE);
        if (alpha > 0) {
            float opacity = alpha;
            super.render(entity, entityYaw, partialTicks, stack, type -> new AlphaVertexConsumer(bufferSource.getBuffer(type), opacity), light);
        }
        renderShadow(entity, partialTicks, stack, bufferSource, light, afterimages, alpha);
        if (entity.hasShield() && action != TheTwilightHierophantEntity.DEATH && (action != TheTwilightHierophantEntity.INTRO || time >= 55)) {
            float grow = action == TheTwilightHierophantEntity.INTRO ? smooth((time - 55 + partialTicks) / 25F) : 1;
            stack.pushPose();
            if (entity.hurtTime > 0) {
                stack.translate(Math.sin(age * 3.1) * 0.07, 0, Math.cos(age * 2.7) * 0.07);
                stack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) Math.sin(age * 3) * entity.hurtTime * 0.45F));
            }
            if (grow > 0)
                cube.renderShield(stack, bufferSource, light, entity.getScale(), grow, entity.getShieldRotation(partialTicks), entity.getShieldOpacity(), entity.isShieldRed());
            stack.popPose();
        }
    }

    @Override
    protected net.minecraft.client.renderer.RenderType getRenderType(T entity, boolean visible, boolean translucent, boolean glowing) {
        return visible || translucent ? net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(entity)) : super.getRenderType(entity, visible, translucent, glowing);
    }

    /**
     * Port of VzlingLib ExampleMobEntityRenderer.renderShadow: record position,
     * orientation and animation, replay at that position, then linearly fade.
     * Low health changes density/lifetime only, never adds artificial orbit/jitter.
     */
    private void renderShadow(T entity, float partial, PoseStack pose, MultiBufferSource buffers, int light, boolean active, float bodyAlpha) {
        if (entity.getPhase() != 2 || entity.isDeadOrDying()) {
            histories.remove(entity);
            return;
        }
        float now = entity.tickCount + partial, blur = entity.getBlur();
        var history = histories.computeIfAbsent(entity, e -> new ShadowHistory());
        float interval = 5 - 4 * blur, lifetime = 10 + 10 * blur;
        history.frames.removeIf(frame -> now - frame.sample().age() >= lifetime);
        if (active && bodyAlpha > 0 && now - history.lastSample >= interval) {
            var sample = new Shadow(entity.getPosition(partial), net.minecraft.util.Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot), net.minecraft.util.Mth.rotLerp(partial, entity.yHeadRotO, entity.yHeadRot), entity.getViewXRot(partial), now, getBob(entity, partial), entity.walkAnimation.position(partial), entity.walkAnimation.speed(partial));
            history.frames.add(new ShadowFrame(sample, model.captureShadowPose(), model == slimModel));
            history.lastSample = now;
        }
        for (var frame : history.frames) {
            float age = now - frame.sample().age();
            if (age <= 0) continue;
            float opacity = bodyAlpha * Math.max(0, 1 - age / lifetime);
            var ghost = frame.slim() ? ghostSlim : ghostNormal;
            var robe = frame.slim() ? ghostOuterSlim : ghostOuterNormal;
            ghost.applyShadowPose(frame.bones());
            robe.applyShadowPose(frame.bones());
            pose.pushPose();
            try {
                var offset = frame.sample().position().subtract(entity.getPosition(partial));
                pose.translate(offset.x, offset.y, offset.z);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180 - frame.sample().yBodyRot()));
                pose.scale(-entity.getScale(), -entity.getScale(), entity.getScale());
                scale(entity, pose, partial);
                pose.translate(0, -1.501, 0);
                int color = net.minecraft.util.FastColor.ARGB32.colorFromFloat(opacity, 1, 1, 1);
                ghost.renderToBuffer(pose, buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(entity))), light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, color);
                var texture = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", frame.slim() ? "textures/entity/the_twilight_hierophant_slim_outer.png" : "textures/entity/the_twilight_hierophant_outer.png");
                robe.renderToBuffer(pose, buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(texture)), light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, color);
            } finally {
                pose.popPose();
            }
        }
    }

    @Override
    protected void setupRotations(T entity, PoseStack pose, float age, float yaw, float partial, float scale) {
        // The custom kneeling death pose must not receive vanilla's sideways roll.
        if (entity.isDeadOrDying()) pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        else super.setupRotations(entity, pose, age, yaw, partial, scale);
    }

    @Override
    public boolean shouldRender(T entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        var history = histories.get(entity);
        return history != null && !history.frames.isEmpty() || super.shouldRender(entity, frustum, x, y, z);
    }

    private static float smooth(float progress) {
        float t = net.minecraft.util.Mth.clamp(progress, 0, 1);
        return t * t * (3 - 2 * t);
    }
}

