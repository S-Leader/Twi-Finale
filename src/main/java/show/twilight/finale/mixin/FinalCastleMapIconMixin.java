package show.twilight.finale.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.twilight.finale.client.renderer.FinalCastleMapIconRenderer;
import twilightforest.client.renderer.map.ConqueredMapIconRenderer;
import twilightforest.init.TFMapDecorations;

@Mixin(value = ConqueredMapIconRenderer.class, remap = false)
public abstract class FinalCastleMapIconMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void finale$renderCastleHeads(MapDecoration decoration, PoseStack stack, MultiBufferSource buffers,
                                          MapItemSavedData mapData, MapDecorationTextureManager sprites, boolean inItemFrame,
                                          int light, int index, CallbackInfoReturnable<Boolean> ci) {
        if (decoration.type().value() != TFMapDecorations.FINAL_CASTLE.get()) return;
        FinalCastleMapIconRenderer.render(decoration, stack, buffers, mapData, sprites, light, index);
        ci.setReturnValue(true);
    }
}
