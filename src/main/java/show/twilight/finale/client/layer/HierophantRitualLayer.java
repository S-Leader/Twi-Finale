package show.twilight.finale.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModItem;
import show.twilight.finale.world.HierophantArena;

public final class HierophantRitualLayer<T extends TheTwilightHierophantEntity> extends RenderLayer<T, TheTwilightHierophantModel<T>> {
    private final ItemRenderer items;

    public HierophantRitualLayer(RenderLayerParent<T, TheTwilightHierophantModel<T>> parent, ItemRenderer items) {
        super(parent);
        this.items = items;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T boss, float swing, float amount, float partial, float age, float yaw, float pitch) {
        if (boss.getAction() != TheTwilightHierophantEntity.REBIRTH) return;
        float t = boss.getActionTime() + partial;
        if (t < 105) {
            double angle = t * 0.15;
            draw(pose, buffers, boss, new ItemStack(ModItem.GLASS_HEART.get()), Math.cos(angle) * 1.05, 0.4 + Math.sin(angle * 2) * 0.25, Math.sin(angle) * 1.05, 0.5F, light, t * 4);
        }
        if (t >= 60 && t < 110) {
            float f = Mth.clamp((t - 60) / 50F, 0, 1);
            f = f * f * (3 - 2 * f);

            var arm = boss.getMainArm();
            PoseStack hand = new PoseStack();
            getParentModel().translateToHand(arm, hand);
            hand.mulPose(Axis.XP.rotationDegrees(-90));
            hand.mulPose(Axis.YP.rotationDegrees(180));
            hand.translate((arm == HumanoidArm.LEFT ? -1 : 1) / 16F, 0.125, -0.625);
            var end = hand.last().pose().transformPosition(new Vector3f());
            double side = arm == HumanoidArm.LEFT ? 1 : -1;
            draw(pose, buffers, boss, new ItemStack(ModItem.TWILIGHT_SWORD.get()), end.x + side * (1 - f) * (HierophantArena.RADIUS + 4),
                    end.y - Math.sin(f * Math.PI) * 2, end.z, 1, light, -40 + f * 40);
        }
    }

    private void draw(PoseStack pose, MultiBufferSource buffers, T boss, ItemStack stack, double x, double y, double z, float scale, int light, float rotation) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(Axis.ZP.rotationDegrees(rotation));
        pose.scale(scale, scale, scale);
        items.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, boss.level(), boss.getId());
        pose.popPose();
    }
}
