package show.twilight.finale.runtime.client.renderer;

import net.minecraft.world.phys.Vec3;

public record Shadow(Vec3 position, float yBodyRot, float yHeadRot, float xRot, float age,
                     float animationProgress, float limbSwing, float limbSwingAmount) {
}
