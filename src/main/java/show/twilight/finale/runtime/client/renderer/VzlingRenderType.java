package show.twilight.finale.runtime.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class VzlingRenderType {
    private VzlingRenderType() {
    }

    public static RenderType getEntityGlowEffect(ResourceLocation texture) {
        return RenderType.entityTranslucentEmissive(texture);
    }
}
