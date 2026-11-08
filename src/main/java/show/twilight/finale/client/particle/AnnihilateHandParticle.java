package show.twilight.finale.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class AnnihilateHandParticle extends TextureSheetParticle {
    public AnnihilateHandParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, float scale) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.bCol = 1.0f;
        this.gCol = 1.0f;
        this.rCol = 1.0f;
        this.quadSize *= 0.75f;
        this.quadSize *= scale;
        this.lifetime = 1;
        this.hasPhysics = false;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    public int getLightColor(float partialTicks) {
        return 0xF000F0;
    }

    @OnlyIn(value = Dist.CLIENT)
    public record Factory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            AnnihilateHandParticle particle = new AnnihilateHandParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, 0.75f);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}

