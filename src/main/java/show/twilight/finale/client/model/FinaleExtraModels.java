package show.twilight.finale.client.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import show.twilight.finale.TwilightFinaleMod;

/**
 * Shared registration/lookup keys for models which are not registered items.
 */
public final class FinaleExtraModels {
    public static final ModelResourceLocation DIAMOND_BACKPLATE = ModelResourceLocation.standalone(TwilightFinaleMod.prefix("item/trophy_final"));
    public static final ModelResourceLocation PLATINUM_BACKPLATE = ModelResourceLocation.standalone(TwilightFinaleMod.prefix("item/trophy_platinum"));

    private FinaleExtraModels() {
    }
}
