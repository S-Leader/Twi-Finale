package show.twilight.finale.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.twilight.finale.resource.ModResourcePack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-only companion loading; never edits the user's selected-pack settings.
 */
@Mixin(PackRepository.class)
public abstract class JappaPackRepositoryMixin {
    @Unique
    private static final ResourceLocation finale$JAPPA_MARKER =
            ResourceLocation.fromNamespaceAndPath("twilightforest", "jappa_models.marker");

    @Inject(method = "openAllSelected", at = @At("RETURN"), cancellable = true)
    private void finale$followTwilightJappa(CallbackInfoReturnable<List<PackResources>> ci) {
        Pack companion = ((PackRepository) (Object) this).getPack(ModResourcePack.JAPPA_PACK_ID);
        if (companion == null) return; // Not the client resource-pack repository.
        List<PackResources> packs = ci.getReturnValue();
        int lastJappa = -1;
        for (int i = 0; i < packs.size(); i++) {
            PackResources resources = packs.get(i);
            if (ModResourcePack.JAPPA_PACK_ID.equals(resources.packId()))
                return; // Manual selection wins; no duplicate.
            // Identical detection to Twilight Forest's JappaPackReloadListener,
            // independent of the zip filename or translated display name.
            if (resources.getResource(PackType.CLIENT_RESOURCES, finale$JAPPA_MARKER) != null) lastJappa = i;
        }
        if (lastJappa < 0) return;
        List<PackResources> combined = new ArrayList<>(packs);
        // Same reload, directly above Jappa. Higher-priority user packs retain
        // their overrides, and ResourceManager owns/ closes the opened companion.
        combined.add(lastJappa + 1, companion.open());
        ci.setReturnValue(List.copyOf(combined));
    }
}
