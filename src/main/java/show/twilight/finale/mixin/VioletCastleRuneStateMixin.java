package show.twilight.finale.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class VioletCastleRuneStateMixin {
    @Unique
    private static final ResourceLocation finale$violetRune = ResourceLocation.fromNamespaceAndPath("twilightforest", "violet_castle_rune_brick");

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void finale$unbreakableRune(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> result) {
        if (finale$violetRune.equals(BuiltInRegistries.BLOCK.getKey(((BlockState) (Object) this).getBlock())))
            result.setReturnValue(-1F);
    }
}
