package show.twilight.finale.runtime.entity.goal;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;

public final class FreezeGoal extends Goal {
    private final BooleanSupplier freeze;

    public FreezeGoal(BooleanSupplier freeze) {
        this.freeze = freeze;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return freeze.getAsBoolean();
    }
}
