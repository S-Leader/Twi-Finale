package show.twilight.finale.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;

public final class HierophantHeldItemLayer<T extends TheTwilightHierophantEntity>
        extends ItemInHandLayer<T, TheTwilightHierophantModel<T>> {
    private final ItemInHandRenderer items;

    public HierophantHeldItemLayer(RenderLayerParent<T, TheTwilightHierophantModel<T>> parent, ItemInHandRenderer items) {
        super(parent, items);
        this.items = items;
    }

    @Override
    protected void renderArmWithItem(LivingEntity living, ItemStack item, ItemDisplayContext context, HumanoidArm arm,
                                     PoseStack pose, MultiBufferSource buffers, int light) {
        if (living instanceof TheTwilightHierophantEntity boss) {
            if (boss.getAction() == TheTwilightHierophantEntity.METEORS) {
                if (item.isEmpty() || arm != boss.getMainArm()) return;
                pose.pushPose();
                getParentModel().translateToRaisedSword(arm, pose);
                items.renderItem(living, item, ItemDisplayContext.NONE, false, pose, buffers, light);
                pose.popPose();
                return;
            }
            if (boss.getAction() == TheTwilightHierophantEntity.FIERY_QUAKE) {
                if (item.isEmpty() || arm != boss.getMainArm()) return;
                pose.pushPose();
                getParentModel().translateToComboSword(pose);

                items.renderItem(living, item, ItemDisplayContext.NONE, false, pose, buffers, light);
                pose.popPose();
                return;
            }
            if (boss.getAction() == TheTwilightHierophantEntity.ICE_BOMB && boss.getActionTime() < 14) return;
            if (boss.getAction() == TheTwilightHierophantEntity.WEAPON_VOLLEY
                    && boss.getWeaponThrowProgress(boss.getActionTime()) < 0.4F) return;
        }
        super.renderArmWithItem(living, item, context, arm, pose, buffers, light);
    }
}
