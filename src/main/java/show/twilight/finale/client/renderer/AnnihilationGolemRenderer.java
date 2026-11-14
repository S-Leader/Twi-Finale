package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.client.model.BlockbenchBossModel;
import show.twilight.finale.entity.AnnihilationGolemEntity;
import twilightforest.init.TFItems;

import java.util.HashMap;
import java.util.Map;

public final class AnnihilationGolemRenderer extends EntityRenderer<AnnihilationGolemEntity> {
    public static final ResourceLocation MODEL = TwilightFinaleMod.prefix("entity/annihilation_golem.json");
    public static final ResourceLocation TEXTURE = TwilightFinaleMod.prefix("textures/entity/annihilation_golem.png");
    public static final ResourceLocation EYES = TwilightFinaleMod.prefix("textures/entity/annihilation_golem_eyes.png");
    private static final ResourceLocation ACTIVE = TwilightFinaleMod.prefix("textures/entity/annihilation_golem_active.png");
    private final BlockbenchBossModel model = new BlockbenchBossModel(MODEL);
    private final ItemStack talisman = new ItemStack(TFItems.CUBE_TALISMAN.get());

    public AnnihilationGolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 1.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(AnnihilationGolemEntity golem) {
        return golem.isEmittingSmoke() ? ACTIVE : TEXTURE;
    }

    @Override
    public void render(AnnihilationGolemEntity golem, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        Map<String, Map<String, Vector3f>> animation = new HashMap<>();
        float seconds = (golem.getActionTime() + partial) / 20, age = golem.tickCount + partial;
        switch (golem.getAction()) {
            case AnnihilationGolemEntity.SWING -> model.sample("ATTACK", seconds, animation, 1);
            case AnnihilationGolemEntity.SMOKE -> model.sample("SMOKE", seconds, animation, 1);
            case AnnihilationGolemEntity.SLAM -> model.sample("SLAM", seconds, animation, 1);
            case AnnihilationGolemEntity.DYING -> model.sample("DEATH", seconds, animation, 1);
            default -> {
                float moving = Mth.clamp(golem.walkAnimation.speed(partial) * 3, 0, 1);
                model.sample("IDLE", age / 20, animation, 1 - moving);
                model.sample("WALK", golem.walkAnimation.position(partial) * 0.12F, animation, moving);
            }
        }
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180 - Mth.rotLerp(partial, golem.yBodyRotO, golem.yBodyRot)));
        ResourceLocation texture = getTextureLocation(golem);
        model.render(animation, pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, golem.hurtTime > 0 ? 0xFFFFAAAA : 0xFFFFFFFF, name -> !name.equals("glass_core"));
        model.render(animation, pose, buffers.getBuffer(RenderType.eyes(EYES)), 15728880, 0xFFFFFFFF, name -> !name.equals("glass_core"));
        if (!golem.isCoreBroken()) {

            pose.pushPose();
            model.moveToBone("cube_talisman_origin", animation, pose);
            pose.mulPose(Axis.XP.rotationDegrees(age * 2.5F));
            pose.mulPose(Axis.ZP.rotationDegrees(age * 3.5F));
            pose.scale(0.72F, 0.72F, 0.72F);
            Minecraft.getInstance().getItemRenderer().renderStatic(talisman, ItemDisplayContext.NONE, 15728880, OverlayTexture.NO_OVERLAY, pose, buffers, golem.level(), golem.getId());
            pose.popPose();

            model.render(animation, pose, buffers.getBuffer(RenderType.entityTranslucent(texture)), light, 0xFFFFFFFF, name -> name.equals("glass_core"));
        }
        pose.popPose();
        super.render(golem, yaw, partial, pose, buffers, light);
    }
}
