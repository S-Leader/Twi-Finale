package show.twilight.finale.client.renderer;

import java.io.IOException;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.renderer.entity.AdherentRenderer;
import twilightforest.entity.monster.Adherent;

import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.client.model.EngineerAdherentModel;
import show.twilight.finale.entity.EngineerAdherentEntity;
import show.twilight.finale.entity.combat.ColoredAdherent;

public final class ColoredAdherentRenderer extends AdherentRenderer {
    private static final ResourceLocation ORIGINAL = ResourceLocation.fromNamespaceAndPath("twilightforest", "textures/entity/adherent.png");

    private static final int NATIVE_GLYPH = 0xFF8B0000;
    private static final int[] GLYPH_RGB = {0x00008B, 0x8B7300, 0x008B00, 0x8B0000, 0x66008B, 0xB9E6F5};
    private static final String[] NAMES = {"blue", "yellow", "green", "red", "purple", "wind"};
    private final ResourceLocation[] palettes = new ResourceLocation[6];

    public ColoredAdherentRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new EngineerAdherentModel(context.bakeLayer(TFModelLayers.ADHERENT));
    }

    @Override
    public ResourceLocation getTextureLocation(Adherent mob) {
        int color = mob instanceof EngineerAdherentEntity ? 5 : ((ColoredAdherent) mob).finale$getColor();
        if (color == ColoredAdherent.BLUE) return ORIGINAL;
        if (palettes[color] != null) return palettes[color];
        var minecraft = Minecraft.getInstance();
        try (var input = minecraft.getResourceManager().getResourceOrThrow(ORIGINAL).open()) {
            NativeImage image = NativeImage.read(input);
            int rgb = GLYPH_RGB[color];
            int ink = 0xFF000000 | ((rgb & 255) << 16) | (rgb & 0xFF00) | ((rgb >>> 16) & 255);
            for (int y = 0; y < image.getHeight(); y++)
                for (int x = 0; x < image.getWidth(); x++) {
                    if (image.getPixelRGBA(x, y) == NATIVE_GLYPH) image.setPixelRGBA(x, y, ink);
                }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "textures/entity/adherent_pattern/" + NAMES[color]);
            minecraft.getTextureManager().register(id, new DynamicTexture(image));
            return palettes[color] = id;
        } catch (IOException ex) {
            TwilightFinaleMod.LOGGER.error("Cannot create adherent glyph palette {}", NAMES[color], ex);
            return palettes[color] = ORIGINAL;
        }
    }
}
