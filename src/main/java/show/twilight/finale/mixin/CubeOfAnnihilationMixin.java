package show.twilight.finale.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.twilight.finale.init.ModBlockTags;
import twilightforest.entity.projectile.CubeOfAnnihilation;

/**
 * The allowlist replaces TF's explosion-resistance and inclusion-tag fallbacks.
 */
@Mixin(CubeOfAnnihilation.class)
public abstract class CubeOfAnnihilationMixin {
    @Shadow
    private ItemStack stack;

    @Inject(method = "canAnnihilate", at = @At("HEAD"), cancellable = true, remap = false)
    private void finale$requireBreakableTag(BlockPos pos, BlockState state, boolean restricted,
                                            CallbackInfoReturnable<Boolean> result) {
        CubeOfAnnihilation cube = (CubeOfAnnihilation) (Object) this;
        result.setReturnValue(state.is(ModBlockTags.ANNIHILATION_CUBE_BREAKABLE)
                && (!restricted || stack != null && stack.canBreakBlockInAdventureMode(new BlockInWorld(cube.level(), pos, false))));
    }
}
