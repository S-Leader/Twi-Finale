package show.twilight.finale.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import show.twilight.finale.TwilightFinaleMod;
import twilightforest.events.ToolEvents;

@Mixin(value = ToolEvents.class, remap = false)
public abstract class ToolEventsMixin {

    @Unique
    private static final TagKey<Item> twilightforest_finale$MAZEBREAKER_LIKE =
            TagKey.create(Registries.ITEM, TwilightFinaleMod.prefix("mazebreaker-like"));

    @Inject(
            method = "damageNonMazebreakerToolsMore",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void twilightforest_finale$skipMazebreakerPenalty(BlockEvent.BreakEvent event, CallbackInfo ci) {
        if (event.getPlayer().getMainHandItem().is(twilightforest_finale$MAZEBREAKER_LIKE)) {
            ci.cancel();
        }
    }
}
