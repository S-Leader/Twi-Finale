package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.world.CastleEncounterProgress;
import show.twilight.finale.world.CastleMapProgress;
import twilightforest.item.mapdata.TFMagicMapData;

public final class FinalCastleMapIconRenderer {
    private static final float ICON_SIZE = 8;
    private static final ResourceLocation PRIEST = ResourceLocation.fromNamespaceAndPath(TwilightFinaleMod.MOD_ID, "textures/map/decorations/adherent_high_priest.png");
    private static final ResourceLocation GOLEM = ResourceLocation.fromNamespaceAndPath(TwilightFinaleMod.MOD_ID, "textures/map/decorations/annihilation_golem.png");
    private static final ResourceLocation HIEROPHANT = ResourceLocation.fromNamespaceAndPath(TwilightFinaleMod.MOD_ID, "textures/map/decorations/the_twilight_hierophant.png");

    private FinalCastleMapIconRenderer() {
    }

    public static void render(MapDecoration decoration, PoseStack stack, MultiBufferSource buffers,
                              MapItemSavedData mapData, MapDecorationTextureManager sprites, int light, int index) {
        int progress = mapData instanceof TFMagicMapData map ? CastleMapProgress.state(map, decoration.x(), decoration.y()) : 0;
        TextureAtlasSprite cross = sprites.getSprite(MapDecorationTypes.RED_X.value().assetId());
        stack.pushPose();
        stack.translate(decoration.x() / 2.0F + 64.0F, decoration.y() / 2.0F + 64.0F, -0.02F - index * 0.003F);
        icon(stack, buffers, PRIEST, -4, -8, light, (progress & CastleEncounterProgress.PRIEST) != 0, cross);
        icon(stack, buffers, GOLEM, -8, 0, light, (progress & CastleEncounterProgress.GOLEM) != 0, cross);
        icon(stack, buffers, HIEROPHANT, 0, 0, light, (progress & CastleEncounterProgress.HIEROPHANT) != 0, cross);
        stack.popPose();
    }

    private static void icon(PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, float x, float y,
                             int light, boolean defeated, TextureAtlasSprite cross) {
        quad(stack, buffers, texture, x, y, ICON_SIZE, 0, light, 0, 0, 1, 1);
        if (defeated)
            quad(stack, buffers, cross.atlasLocation(), x + 4, y + 4, 4, -0.005F, light,
                    cross.getU0(), cross.getV0(), cross.getU1(), cross.getV1());
    }

    private static void quad(PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, float x, float y, float size,
                             float depth, int light, float u0, float v0, float u1, float v1) {
        var pose = stack.last().pose();
        var vertices = buffers.getBuffer(RenderType.text(texture));
        vertices.addVertex(pose, x, y + size, depth).setColor(-1).setUv(u0, v1).setLight(light);
        vertices.addVertex(pose, x + size, y + size, depth).setColor(-1).setUv(u1, v1).setLight(light);
        vertices.addVertex(pose, x + size, y, depth).setColor(-1).setUv(u1, v0).setLight(light);
        vertices.addVertex(pose, x, y, depth).setColor(-1).setUv(u0, v0).setLight(light);
    }
}
