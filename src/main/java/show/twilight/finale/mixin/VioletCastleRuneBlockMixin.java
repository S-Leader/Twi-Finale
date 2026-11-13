package show.twilight.finale.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class VioletCastleRuneBlockMixin implements IBlockExtension {
    @Unique
    private static final ResourceLocation finale$violetRune = ResourceLocation.fromNamespaceAndPath("twilightforest", "violet_castle_rune_brick");

    @Inject(method = "getExplosionResistance", at = @At("HEAD"), cancellable = true)
    private void finale$blastProofRune(CallbackInfoReturnable<Float> result) {
        if (finale$violetRune.equals(BuiltInRegistries.BLOCK.getKey((Block) (Object) this)))
            result.setReturnValue(3600000F);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return !finale$violetRune.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))
                && IBlockExtension.super.canEntityDestroy(state, level, pos, entity);
    }
}
