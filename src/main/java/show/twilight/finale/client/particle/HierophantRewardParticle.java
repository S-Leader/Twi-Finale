package show.twilight.finale.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Native totem glitter carried by a rotating, travelling cluster, not a static beam.
 */
@OnlyIn(Dist.CLIENT)
public final class HierophantRewardParticle extends TotemParticle {
    private final Vec3 start, destination, along, across, up;
    private final int flightTicks;
    private final double phase, orbitRadius, spread, arcHeight;

    private HierophantRewardParticle(ClientLevel level, double x, double y, double z,
                                     double targetX, double targetY, double targetZ, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0, sprites);
        start = new Vec3(x, y, z);
        // The existing TF particle packet sends the destination offset in its
        // velocity fields without the Gaussian spread of a vanilla particle packet.
        Vec3 offset = new Vec3(targetX, targetY, targetZ);
        destination = start.add(offset);
        double distance = offset.length();
        along = distance > 1.0E-6 ? offset.scale(1 / distance) : new Vec3(0, 1, 0);
        Vec3 reference = Math.abs(along.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        across = along.cross(reference).normalize();
        up = across.cross(along).normalize();
        flightTicks = Mth.clamp(Mth.ceil(distance * 1.5), 40, 80);
        lifetime = flightTicks + 12;
        arcHeight = Math.min(1.5, 0.5 + distance * 0.12);
        // Three narrow groups make the rotation readable instead of filling a
        // rotationally symmetric tube with evenly distributed points.
        phase = random.nextInt(3) * Math.PI * 2 / 3 + random.nextGaussian() * 0.16;
        orbitRadius = 0.35 + random.nextDouble() * 0.2;
        spread = (random.nextDouble() - 0.5) * 0.4;
        hasPhysics = false;
        gravity = 0;
        setSpriteFromAge(sprites);
        moveAlongSpiral(0);
        xo = this.x;
        yo = this.y;
        zo = this.z;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        oRoll = roll;
        if (++age >= lifetime) {
            remove();
            return;
        }
        // Deliberately skip TotemParticle's falling/friction tick: these are
        // genuinely moving particles following a shared centre towards the chest.
        moveAlongSpiral(age);
        roll += 0.12F;
        alpha = 1 - Mth.clamp((age - flightTicks) / 12F, 0, 1);
        setSpriteFromAge(sprites);
    }

    private void moveAlongSpiral(int time) {
        double progress = Mth.clamp(time / (double) flightTicks, 0, 1);
        double travel = progress * progress * (3 - 2 * progress);
        Vec3 centre = start.lerp(destination, travel).add(0, Math.sin(progress * Math.PI) * arcHeight, 0);
        double gather = 1 - Mth.clamp((progress - 0.65) / 0.35, 0, 1);
        gather = gather * gather * (3 - 2 * gather);
        double angle = phase + time * 0.32;
        Vec3 point = centre.add(across.scale(Math.cos(angle) * orbitRadius * gather))
                .add(up.scale(Math.sin(angle) * orbitRadius * gather))
                .add(along.scale(spread * gather));
        setPos(point.x, point.y, point.z);
    }

    @OnlyIn(Dist.CLIENT)
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double targetX, double targetY, double targetZ) {
            return new HierophantRewardParticle(level, x, y, z, targetX, targetY, targetZ, sprites);
        }
    }
}
