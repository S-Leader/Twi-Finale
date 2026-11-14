package show.twilight.finale.mixin;

import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataAccessor {
    @Invoker("setDecorationsDirty")
    void finale$setDecorationsDirty();
}
