package show.twilight.finale.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import show.twilight.finale.client.animation.TwilightSwordPlayerAnimation;

@Mixin(HumanoidModel.class)
public abstract class TwilightSwordPlayerModelMixin {
    @Unique
    private PartPose[] twilightFinale$swordOriginalPose;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void twilightFinale$restoreSwordPose(LivingEntity entity, float swing, float amount,
                                                 float age, float yaw, float pitch, CallbackInfo ci) {
        if (twilightFinale$swordOriginalPose == null) return;
        ModelPart[] bones = twilightFinale$swordBones();
        for (int i = 0; i < bones.length; i++) bones[i].loadPose(twilightFinale$swordOriginalPose[i]);
        twilightFinale$swordOriginalPose = null;
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void twilightFinale$animateSword(LivingEntity entity, float swing, float amount,
                                             float age, float yaw, float pitch, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayer player && TwilightSwordPlayerAnimation.isActive(player, age)) {
            ModelPart[] bones = twilightFinale$swordBones();
            twilightFinale$swordOriginalPose = new PartPose[bones.length];
            for (int i = 0; i < bones.length; i++) twilightFinale$swordOriginalPose[i] = bones[i].storePose();
            TwilightSwordPlayerAnimation.apply((HumanoidModel<?>) (Object) this, player, age);
        }
    }

    @Unique
    private ModelPart[] twilightFinale$swordBones() {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        return new ModelPart[]{model.head, model.hat, model.body, model.leftArm, model.rightArm, model.leftLeg, model.rightLeg};
    }
}
