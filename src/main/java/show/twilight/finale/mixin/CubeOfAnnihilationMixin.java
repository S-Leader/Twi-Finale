package show.twilight.finale.mixin;

import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import show.twilight.finale.capabilities.ForceFieldCapabilityList;
import show.twilight.finale.entity.AdherentHighPriestEntity;
import twilightforest.entity.projectile.CubeOfAnnihilation;

@Mixin(value = {CubeOfAnnihilation.class})
public abstract class CubeOfAnnihilationMixin {
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void finale$breakPriestShield(EntityHitResult result, CallbackInfo callback) {
        var cube = (CubeOfAnnihilation) (Object) this;
        if (cube.level().isClientSide() || !(result.getEntity() instanceof AdherentHighPriestEntity priest)
                || priest.isShadowClone() || !priest.isAlive()) return;
        ForceFieldCapabilityList.get(priest).ifPresent(cap -> {
            if (cap.getFieldsLeft() <= 0) return;
            // Native cube damage is playerAttack(owner): its DamageSource loses
            // the projectile identity, so recognize the actual collision here.
            // Same one-layer/20-tick break cooldown and network sync as Proverbs.
            cap.breakField();
            priest.setHasShield(cap.getFieldsLeft() > 0);
            callback.cancel(); // The shield absorbs this hit; do not also damage HP.
        });
    }
}

