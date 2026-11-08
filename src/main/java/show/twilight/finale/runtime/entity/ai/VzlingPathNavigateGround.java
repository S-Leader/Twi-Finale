package show.twilight.finale.runtime.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

public final class VzlingPathNavigateGround extends GroundPathNavigation {
    public VzlingPathNavigateGround(PathfinderMob mob, Level level) {
        super(mob, level);
        setCanOpenDoors(true);
    }
}
