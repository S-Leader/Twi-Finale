package show.twilight.finale.entity.combat;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.entity.projectile.AnnihilationBolt;
import show.twilight.finale.entity.projectile.HomingVisShard;
import twilightforest.entity.monster.Adherent;
import twilightforest.entity.projectile.NatureBolt;
import twilightforest.init.TFDamageTypes;

import java.util.EnumSet;

public final class AdherentAttackGoal extends Goal {
    private final Adherent mob;
    private int cooldown, casting;
    private boolean active;

    public AdherentAttackGoal(Adherent mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && !target.isSpectator();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        active = false;
        casting = 0;
        mob.setCharging(false);
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || !(mob.level() instanceof ServerLevel level)) return;
        int color = ((ColoredAdherent) mob).finale$getColor();
        mob.getLookControl().setLookAt(target, active && color == ColoredAdherent.RED ? 3 : 20, 20);
        boolean visible = mob.getSensing().hasLineOfSight(target);
        if (!active) {
            if (cooldown > 0) cooldown--;
            if (!visible || mob.distanceToSqr(target) > 24 * 24) {
                mob.getNavigation().moveTo(target, 1);
                return;
            }
            mob.getNavigation().stop();
            if (cooldown > 0) return;
            active = true;
            casting = 0;
            mob.setCharging(true);
            mob.playSound(color == ColoredAdherent.YELLOW ? SoundEvents.BLAZE_SHOOT : SoundEvents.EVOKER_PREPARE_ATTACK, 1, 0.8F);
        }
        mob.getNavigation().stop();
        casting++;
        if (casting < 40) {
            if (casting % 4 == 0) {
                int rgb = ColoredAdherent.tint(color);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, rgb),
                        mob.getX(), mob.getEyeY(), mob.getZ(), 3, 0.35, 0.25, 0.35, 0);
            }
            return;
        }
        if (color == ColoredAdherent.RED) {
            // Hydra uses a 100-tick breath, 19 fire damage, and three seconds of burning.
            if (casting < 140) breathe(level);
            else finish(80);
        } else if (color == ColoredAdherent.PURPLE) {
            if (casting == 40 && visible) {
                for (int i = 0; i < 5; i++) level.addFreshEntity(new HomingVisShard(level, mob, target, i));
                mob.playSound(SoundEvents.EVOKER_CAST_SPELL, 1, 0.8F);
            }
            finish(100);
        } else if (color == ColoredAdherent.YELLOW) {
            if (casting == 40 && visible) {
                Vec3 start = mob.getEyePosition().add(mob.getLookAngle().scale(0.8));
                Vec3 aim = target.getEyePosition().subtract(start).normalize();
                LargeFireball fireball = new LargeFireball(level, mob, aim, 1);
                fireball.setPos(start);
                fireball.setDeltaMovement(aim.scale(0.6));
                level.addFreshEntity(fireball);
                mob.playSound(SoundEvents.GHAST_SHOOT, 1, 1);
            }
            finish(80);
        } else {
            if ((casting == 40 || casting == 44 || casting == 48) && visible) {
                Projectile bolt = color == ColoredAdherent.BLUE ? new AnnihilationBolt(level, mob) : new NatureBolt(level, mob);
                Vec3 start = mob.getEyePosition().add(mob.getLookAngle().scale(0.5));
                bolt.setPos(start);
                Vec3 aim = target.getEyePosition().subtract(start);
                bolt.shoot(aim.x, aim.y, aim.z, 1.25F, 1.5F);
                level.addFreshEntity(bolt);
                mob.playSound(SoundEvents.GHAST_SHOOT, 0.7F, 1.2F);
            }
            if (casting >= 48) finish(60);
        }
    }

    private void finish(int delay) {
        active = false;
        casting = 0;
        cooldown = delay;
        mob.setCharging(false);
    }

    private void breathe(ServerLevel level) {
        Vec3 start = mob.getEyePosition();
        var hit = CombatRay.trace(mob, start, start.add(mob.getLookAngle().scale(30)), 0.35);
        Vec3 direction = hit.end().subtract(start).normalize();
        double length = start.distanceTo(hit.end());
        for (double distance = 0.5; distance < length; distance += 0.7) {
            Vec3 at = start.add(direction.scale(distance));
            level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 1, 0.15, 0.15, 0.15, 0.01);
        }
        if (casting % 10 == 0 && hit.victim() != null && !hit.victim().fireImmune()
                && hit.victim().hurt(TFDamageTypes.getEntityDamageSource(level, TFDamageTypes.HYDRA_FIRE, mob), 19))
            hit.victim().igniteForSeconds(3);
    }
}
