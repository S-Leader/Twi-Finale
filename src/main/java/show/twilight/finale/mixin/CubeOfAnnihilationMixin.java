package show.twilight.finale.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import show.twilight.finale.init.ModBlockTags;
import twilightforest.entity.projectile.CubeOfAnnihilation;
import twilightforest.init.TFBlocks;

/**
 * The allowlist replaces TF's explosion-resistance and inclusion-tag fallbacks.
 */
@Mixin(CubeOfAnnihilation.class)
public abstract class CubeOfAnnihilationMixin {
    @Shadow
    private ItemStack stack;

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void finale$protectOwner(EntityHitResult hit, CallbackInfo callback) {
        CubeOfAnnihilation cube = (CubeOfAnnihilation) (Object) this;
        if (hit.getEntity() == cube.getOwner()) callback.cancel();
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean finale$identifyCubeAgainstPriest(Entity victim, DamageSource source, float amount) {
        if (victim instanceof AdherentHighPriestEntity) {
            // TF normally reports the owner as the direct attacker, hiding the cube from shield checks.
            CubeOfAnnihilation cube = (CubeOfAnnihilation) (Object) this;
            source = new DamageSource(source.typeHolder(), cube, source.getEntity());
        }
        return victim.hurt(source, amount);
    }

    @Inject(method = "canAnnihilate", at = @At("HEAD"), cancellable = true, remap = false)
    private void finale$requireBreakableTag(BlockPos pos, BlockState state, boolean restricted,
                                            CallbackInfoReturnable<Boolean> result) {
        CubeOfAnnihilation cube = (CubeOfAnnihilation) (Object) this;
        result.setReturnValue(!state.is(TFBlocks.VIOLET_CASTLE_RUNE_BRICK.get()) && state.is(ModBlockTags.ANNIHILATION_CUBE_BREAKABLE)
                && (!restricted || stack != null && stack.canBreakBlockInAdventureMode(new BlockInWorld(cube.level(), pos, false))));
    }
}
