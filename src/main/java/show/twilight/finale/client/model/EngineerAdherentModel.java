package show.twilight.finale.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import show.twilight.finale.entity.EngineerAdherentEntity;
import twilightforest.client.model.entity.AdherentModel;
import twilightforest.entity.monster.Adherent;

public final class EngineerAdherentModel extends AdherentModel {
    public EngineerAdherentModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(Adherent entity, float limb, float amount, float age, float yaw, float pitch) {
        body.yRot = rightArm.yRot = leftArm.yRot = 0;
        super.setupAnim(entity, limb, amount, age, yaw, pitch);
        if (!(entity instanceof EngineerAdherentEntity engineer)) return;
        float time = engineer.getThrowTime() + age - entity.tickCount;
        if (time < 0 || time >= EngineerAdherentEntity.THROW_DURATION) return;
        // A brief overhand throw: draw the hand back, release, then follow through.
        if (time <= EngineerAdherentEntity.THROW_TICK) {
            float windup = Mth.sin(time / EngineerAdherentEntity.THROW_TICK * Mth.HALF_PI);
            rightArm.xRot = Mth.lerp(windup, -0.2F, -2.5F);
            rightArm.yRot = -0.4F * windup;
            rightArm.zRot = -0.2F;
            body.yRot = -0.2F * windup;
        } else {
            float follow = (time - EngineerAdherentEntity.THROW_TICK) / (EngineerAdherentEntity.THROW_DURATION - EngineerAdherentEntity.THROW_TICK);
            rightArm.xRot = Mth.lerp(follow, -1.7F, 0.15F);
            rightArm.yRot = Mth.lerp(follow, 0.15F, 0);
            rightArm.zRot = -0.2F * (1 - follow);
            body.yRot = 0.15F * (1 - follow);
        }
    }
}
