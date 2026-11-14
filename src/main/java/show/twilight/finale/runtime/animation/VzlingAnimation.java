package show.twilight.finale.runtime.animation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;

public final class VzlingAnimation {
    private final ResourceLocation id;
    public final AnimationState state = new AnimationState();
    private Object definition;

    public VzlingAnimation(VzlingAnimatable owner, ResourceLocation id) {
        this.id = id;
        owner.getAnimations().add(this);
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setDefinition(Object definition) {
        this.definition = definition;
    }

    public Object getDefinition() {
        return definition;
    }
}
