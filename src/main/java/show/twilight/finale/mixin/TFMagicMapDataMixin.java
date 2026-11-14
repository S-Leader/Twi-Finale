package show.twilight.finale.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import show.twilight.finale.world.CastleMapProgress;
import twilightforest.item.mapdata.TFMagicMapData;

@Mixin(value = TFMagicMapData.class, remap = false)
public abstract class TFMagicMapDataMixin {
    @Inject(method = "getUpdatePacket", at = @At("HEAD"))
    private void finale$updateCastleProgress(MapId id, Player player, CallbackInfoReturnable<Packet<?>> ci) {
        if (!(player.level() instanceof ServerLevel playerLevel)) return;
        var map = (TFMagicMapData) (Object) this;
        ServerLevel level = playerLevel.getServer().getLevel(map.dimension);
        if (level != null && CastleMapProgress.update(level, map))
            ((MapItemSavedDataAccessor) this).finale$setDecorationsDirty();
    }
}
