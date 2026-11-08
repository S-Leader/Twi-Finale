package show.twilight.finale.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.twilight.finale.world.castle.FinalCastleMainComponent;
import twilightforest.world.components.structures.type.FinalCastleStructure;

@Mixin(FinalCastleStructure.class)
public abstract class FinalCastleStructureMixin {
    @Inject(method = "getFirstPiece", at = @At("HEAD"), cancellable = true, remap = false)
    private void finale$legacyLayout(Structure.GenerationContext context, RandomSource random, ChunkPos chunk,
                                     int x, int y, int z, CallbackInfoReturnable<StructurePiece> result) {
        FinalCastleMainComponent root = new FinalCastleMainComponent(0, x, y, z);
        // Never publish a failed/half-built plan as a valid structure start.
        result.setReturnValue(root.prepareLayout(context, random) ? root : null);
    }
}
