package show.twilight.finale.runtime.client.renderer;

import net.minecraft.world.phys.Vec3;

/**
 * Port of VzlingLib 0.98.8's renderer.entity.Shadow (MIT, Vzling).
 * Relocated to keep the 1.21.1 port independent of the 1.20.1 Forge library.
 */
public record Shadow(Vec3 position, float yBodyRot, float yHeadRot, float xRot, float age,
                     float animationProgress, float limbSwing, float limbSwingAmount) {
}
