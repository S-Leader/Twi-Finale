package show.twilight.finale.entity.combat;

import java.util.Optional;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CombatRay {
    private CombatRay() {
    }

    public record Hit(Vec3 end, LivingEntity victim) {
    }

    public static Hit trace(LivingEntity source, Vec3 start, Vec3 desiredEnd, double width) {
        Vec3 end = source.level().clip(new ClipContext(start, desiredEnd, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, source)).getLocation();
        double nearest = start.distanceToSqr(end);
        LivingEntity victim = null;
        for (LivingEntity candidate : source.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(start, end).inflate(width), entity -> entity != source && entity.isAlive()
                        && !entity.isSpectator() && !source.isAlliedTo(entity))) {
            var intersection = candidate.getBoundingBox().inflate(width).clip(start, end);
            if (candidate.getBoundingBox().inflate(width).contains(start)) intersection = Optional.of(start);
            if (intersection.isPresent() && start.distanceToSqr(intersection.get()) <= nearest) {
                nearest = start.distanceToSqr(intersection.get());
                victim = candidate;
                end = intersection.get();
            }
        }
        return new Hit(end, victim);
    }
}
