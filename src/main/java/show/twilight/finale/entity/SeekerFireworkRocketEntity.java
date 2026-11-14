package show.twilight.finale.entity;

import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.projectile.ITFProjectile;

import show.twilight.finale.init.ModEntity;


public class SeekerFireworkRocketEntity extends FireworkRocketEntity implements ITFProjectile {
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(SeekerFireworkRocketEntity.class, EntityDataSerializers.INT);

    public SeekerFireworkRocketEntity(EntityType<? extends FireworkRocketEntity> type, Level level) {
        super(type, level);
    }

    public SeekerFireworkRocketEntity(Level level, ItemStack stack, Entity entity, double x, double y, double z, boolean angle) {
        super(ModEntity.SEEKER_FIREWORK_ROCKET.get(), level);
        this.life = 0;
        this.setPos(x, y, z);
        int i = 1;
        if (!stack.isEmpty()) {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, stack.copy());
            i += stack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(0, List.of())).flightDuration();
        }
        this.setDeltaMovement(this.random.triangle(0.0, 0.002297), 0.05, this.random.triangle(0.0, 0.002297));
        this.lifetime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
        this.entityData.set(DATA_SHOT_AT_ANGLE, angle);
        this.setOwner(entity);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET, -1);
    }

    public void tick() {
        if (this.tickCount == 1) {
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        if (this.isThisArrowFlying()) {
            Entity target;
            if (!this.level().isClientSide()) {
                this.updateTarget();
            }
            if (this.level().isClientSide() && !this.onGround()) {
                for (int i = 0; i < 4; ++i) {
                    this.level().addParticle(ParticleTypes.WITCH, this.getX() + this.getDeltaMovement().x() * (double) i / 4.0, this.getY() + this.getDeltaMovement().y() * (double) i / 4.0, this.getZ() + this.getDeltaMovement().z() * (double) i / 4.0, -this.getDeltaMovement().x(), -this.getDeltaMovement().y() + 0.2, -this.getDeltaMovement().z());
                }
            }
            if ((target = this.getTarget()) != null) {
                Vec3 targetVec = this.getVectorToTarget(target).scale(0.8);
                Vec3 courseVec = this.getMotionVec();
                double courseLen = courseVec.length();
                double targetLen = targetVec.length();
                double totalLen = Math.sqrt(courseLen * courseLen + targetLen * targetLen);
                double dotProduct = courseVec.dot(targetVec) / (courseLen * targetLen);
                if (dotProduct > 0.5) {
                    Vec3 newMotion = courseVec.scale(courseLen / totalLen).add(targetVec.scale(courseLen / totalLen));
                    this.setDeltaMovement(newMotion.add(0.0, 0.045f, 0.0));
                } else if (!this.level().isClientSide()) {
                    this.setTarget(null);
                }
            }
        }
        super.tick();
    }

    private void updateTarget() {
        Entity target = this.getTarget();
        if (target != null && !target.isAlive()) {
            target = null;
            this.setTarget(null);
        }
        if (target == null) {
            AABB positionBB;
            AABB targetBB = positionBB = new AABB(this.getX(), this.getY(), this.getZ(), this.getX(), this.getY(), this.getZ());
            Vec3 courseVec = this.getMotionVec().scale(5.0).yRot(0.5235988f);
            targetBB = targetBB.minmax(positionBB.move(courseVec));
            courseVec = this.getMotionVec().scale(5.0).yRot(-0.5235988f);
            targetBB = targetBB.minmax(positionBB.move(courseVec));
            targetBB = targetBB.inflate(0.0, 2.5, 0.0);
            double closestDot = -1.0;
            LivingEntity closestTarget = null;
            List<LivingEntity> entityList = this.level().getEntitiesOfClass(LivingEntity.class, targetBB);
            List<LivingEntity> monsters = entityList.stream().filter(l -> l instanceof Monster).toList();
            if (!monsters.isEmpty()) {
                for (LivingEntity monster : monsters) {
                    if (((Monster) monster).getTarget() != this.getOwner()) continue;
                    this.setTarget(monster);
                    return;
                }
                for (LivingEntity monster : monsters) {
                    if (monster instanceof NeutralMob || !monster.hasLineOfSight(this)) continue;
                    this.setTarget(monster);
                    return;
                }
            }
            for (LivingEntity living : entityList) {
                Vec3 targetVec;
                Vec3 motionVec;
                double dot;
                TamableAnimal animal;
                if (!living.hasLineOfSight(this) || living == this.getOwner() || this.getOwner() != null && living instanceof TamableAnimal && (animal = (TamableAnimal) living).getOwner() == this.getOwner() || !((dot = (motionVec = this.getMotionVec().normalize()).dot(targetVec = this.getVectorToTarget(living).normalize())) > Math.max(closestDot, 0.5)))
                    continue;
                closestDot = dot;
                closestTarget = living;
            }
            if (closestTarget != null) {
                this.setTarget(closestTarget);
            }
        }
    }

    private Vec3 getMotionVec() {
        return new Vec3(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z());
    }

    private Vec3 getVectorToTarget(Entity target) {
        return new Vec3(target.getX() - this.getX(), target.getY() + (double) target.getEyeHeight() - this.getY(), target.getZ() - this.getZ());
    }

    @Nullable
    private Entity getTarget() {
        return this.level().getEntity(this.getEntityData().get(TARGET));
    }

    private void setTarget(@Nullable Entity e) {
        this.getEntityData().set(TARGET, (e == null ? -1 : e.getId()));
    }

    private boolean isThisArrowFlying() {
        return !this.onGround() && this.getDeltaMovement().lengthSqr() > 1.0;
    }
}

