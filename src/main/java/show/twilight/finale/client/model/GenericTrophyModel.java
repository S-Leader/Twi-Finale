package show.twilight.finale.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public abstract class GenericTrophyModel extends Model {
    protected GenericTrophyModel() {
        super(RenderType::entityCutoutNoCull);
    }

    public abstract void setRotations(float animation, float yaw, float pitch);
}
