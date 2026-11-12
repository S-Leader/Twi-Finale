package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import show.twilight.finale.TwilightFinaleMod;

import java.io.IOException;
import java.util.function.Function;

/**
 * One textured dissolve pass: transparent skin pixels must never reveal a white depth mask.
 */
@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class HierophantRenderTypes extends RenderStateShard {
    private static ShaderInstance dissolveShader;
    private static final Function<ResourceLocation, RenderType> DISSOLVE = Util.memoize(texture -> RenderType.create("hierophant_disintegration", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, RenderType.CompositeState.builder().setShaderState(new ShaderStateShard(() -> dissolveShader)).setTextureState(new TextureStateShard(texture, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true)));

    private HierophantRenderTypes() {
        super("hierophant_disintegration", () -> {
        }, () -> {
        });
    }

    public static RenderType disintegration(ResourceLocation texture) {
        return DISSOLVE.apply(texture);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), TwilightFinaleMod.prefix("hierophant_disintegration"), DefaultVertexFormat.NEW_ENTITY), shader -> dissolveShader = shader);
    }
}
