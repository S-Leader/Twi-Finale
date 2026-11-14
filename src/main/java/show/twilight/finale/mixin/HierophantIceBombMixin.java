package show.twilight.finale.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.entity.projectile.IceBomb;

@Mixin(IceBomb.class)
public abstract class HierophantIceBombMixin {
    @Inject(method = "doTerrainEffect", at = @At("HEAD"), cancellable = true, remap = false)
    private void finale$noHierophantSnow(BlockPos pos, CallbackInfo ci) {
        IceBomb bomb = (IceBomb) (Object) this;
        if (!bomb.getPersistentData().getBoolean(TheTwilightHierophantEntity.ICE_BOMB_NO_SNOW_TAG)
                && !(bomb.getOwner() instanceof TheTwilightHierophantEntity)) return;
        BlockState state = bomb.level().getBlockState(pos);

        if (state.isAir() || state.is(Blocks.SNOW) || state.is(BlockTagGenerator.ICE_BOMB_REPLACEABLES))
            ci.cancel();
    }
}
