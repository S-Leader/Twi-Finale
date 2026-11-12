package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
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
        this.addLayer(new show.twilight.finale.client.layer.HierophantRitualLayer<>(this, context.getItemRenderer()));
        HierophantArmorLayer.addCustomArmorLayers(this, context, TheTwilightHierophantModel.LAYER_LOCATION_I, TheTwilightHierophantModel.LAYER_LOCATION_O);
        this.addLayer(new HierophantOuterLayer(this, context.getModelSet()));
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull T entity) {
        this.model = this.normalModel;
        Entity entity2 = Minecraft.getInstance().getCameraEntity();
        if (entity2 instanceof AbstractClientPlayer) {
            AbstractClientPlayer clientPlayer = (AbstractClientPlayer) entity2;
            if (clientPlayer.getSkin().model() == PlayerSkin.Model.SLIM) {
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
            float dissolve = smooth(t / 8) * (entity.isSmokePhasing() ? 1
                    : smooth((TheTwilightHierophantEntity.getActionDuration(action) - t) / 10));
            alpha = Mth.lerp(dissolve, alpha, 0.12F);
        }
        if (action == TheTwilightHierophantEntity.FIERY_QUAKE) {
            int stage = entity.getSwordComboStage();
            float local = entity.getSwordComboTime() + partialTicks;
            if (stage == TheTwilightHierophantEntity.SWORD_SMOKE)
                alpha *= 1 - smooth((local - TheTwilightHierophantEntity.SWORD_IMPACT_HOLD_TICKS) / 3F);
            else if (entity.getSwordComboIndex() > 0) {
                if (stage == TheTwilightHierophantEntity.SWORD_RISE) alpha = 0;
                else if (stage == TheTwilightHierophantEntity.SWORD_HOLD) alpha *= smooth(local / 3F);
            }
        }
        if (action == TheTwilightHierophantEntity.DEATH)
            alpha *= Mth.clamp((125 - time - partialTicks) / 55F, 0, 1);
        boolean afterimages = entity.getPhase() == 4 && action != TheTwilightHierophantEntity.REBIRTH;
        if (alpha > 0) {
            float opacity = alpha;
            stack.pushPose();
            try {
                // Metadata can arrive before vanilla's three-tick position
                // interpolation reaches the floor. Render the impact and its
                // planted/vanishing pose at the server's actual contact height.
                if (entity.isSwordComboGrounded())
                    stack.translate(0, entity.getSwordImpactY() - entity.getPosition(partialTicks).y, 0);
                super.render(entity, entityYaw, partialTicks, stack, type -> new AlphaVertexConsumer(bufferSource.getBuffer(type), opacity), light);
            } finally {
                stack.popPose();
            }
        }
        renderShadow(entity, partialTicks, stack, bufferSource, light, afterimages, alpha);
        if (entity.hasShield() && action != TheTwilightHierophantEntity.DEATH && (action != TheTwilightHierophantEntity.INTRO || time >= 20)) {
            float grow = action == TheTwilightHierophantEntity.INTRO ? smooth((time - 20 + partialTicks) / 60F) : 1;
            stack.pushPose();
            if (entity.hurtTime > 0) {
                stack.translate(Math.sin(age * 3.1) * 0.07, 0, Math.cos(age * 2.7) * 0.07);
                stack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(age * 3) * entity.hurtTime * 0.45F));
            }
            if (grow > 0)
                cube.renderShield(stack, bufferSource, light, entity.getScale(), grow, 0, entity.getShieldOpacity(), entity.isShieldRed());
            stack.popPose();
        }
    }

    @Override
    protected net.minecraft.client.renderer.RenderType getRenderType(T entity, boolean visible, boolean translucent, boolean glowing) {
        return visible || translucent ? net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(entity)) : super.getRenderType(entity, visible, translucent, glowing);
    }

    private void renderShadow(T entity, float partial, PoseStack pose, MultiBufferSource buffers, int light, boolean active, float bodyAlpha) {
        if (entity.getPhase() != 4 || entity.isDeadOrDying() || entity.getAction() == TheTwilightHierophantEntity.REBIRTH || entity.isSwordComboSmoke() || entity.isSwordComboGrounded()) {
            histories.remove(entity);
            return;
        }
        float now = entity.tickCount + partial, blur = entity.getBlur();
        var history = histories.computeIfAbsent(entity, e -> new ShadowHistory());
        float interval = 5 - 4 * blur, lifetime = 10 + 10 * blur;
        history.frames.removeIf(frame -> now - frame.sample().age() >= lifetime);
        if (active && bodyAlpha > 0 && now - history.lastSample >= interval) {
            var sample = new Shadow(entity.getPosition(partial), Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot), Mth.rotLerp(partial, entity.yHeadRotO, entity.yHeadRot), entity.getViewXRot(partial), now, getBob(entity, partial), entity.walkAnimation.position(partial), entity.walkAnimation.speed(partial));
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
                pose.mulPose(Axis.YP.rotationDegrees(180 - frame.sample().yBodyRot()));
                pose.scale(-entity.getScale(), -entity.getScale(), entity.getScale());
                scale(entity, pose, partial);
                pose.translate(0, -1.501, 0);
                int color = FastColor.ARGB32.colorFromFloat(opacity, 1, 1, 1);
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
        // The final curl/spread/dissolution must not receive vanilla's sideways roll.
        if (entity.isDeadOrDying()) pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        else super.setupRotations(entity, pose, age, yaw, partial, scale);
    }

    @Override
    public boolean shouldRender(T entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        var history = histories.get(entity);
        return history != null && !history.frames.isEmpty() || super.shouldRender(entity, frustum, x, y, z);
    }

    private static float smooth(float progress) {
        float t = Mth.clamp(progress, 0, 1);
        return t * t * (3 - 2 * t);
    }
}

