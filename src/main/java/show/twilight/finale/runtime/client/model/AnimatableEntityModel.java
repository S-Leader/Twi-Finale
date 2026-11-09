package show.twilight.finale.runtime.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.world.entity.LivingEntity;
import show.twilight.finale.runtime.animation.VzlingAnimatable;

public abstract class AnimatableEntityModel<T extends LivingEntity & VzlingAnimatable> extends HierarchicalModel<T> {
    public abstract void setupWalkAnimation(T entity, float swing, float amount);

    @Override
    public void setupAnim(T entity, float swing, float amount, float age, float headYaw, float headPitch) {
        root().getAllParts().forEach(p -> p.resetPose());
        setupWalkAnimation(entity, swing, amount);
        entity.getAnimations().forEach(a -> {
            if (a.getDefinition() instanceof AnimationDefinition definition) animate(a.state, definition, age);
        });
    }
}
