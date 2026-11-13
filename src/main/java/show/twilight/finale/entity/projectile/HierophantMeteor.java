package show.twilight.finale.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.init.ModEntity;
import twilightforest.entity.boss.HydraMortar;

/**
 * Native Hydra mortar model/texture, but immediate impact and no arena-floor destruction.
 */
public final class HierophantMeteor extends HydraMortar {
    private static final EntityDataAccessor<Integer> FORM_TIME = SynchedEntityData.defineId(HierophantMeteor.class, EntityDataSerializers.INT);
    private Vec3 landing = Vec3.ZERO;

    public HierophantMeteor(EntityType<? extends HierophantMeteor> type, Level level) {
        super(type, level);
    }

    public HierophantMeteor(ServerLevel level, TheTwilightHierophantEntity owner, Vec3 landing) {
        this(ModEntity.HIEROPHANT_METEOR.get(), level);
        setOwner(owner);
        this.landing = landing;
        setPos(landing.add(0, 11, 0));
        entityData.set(FORM_TIME, formationTicks());
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        fuse = 80;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FORM_TIME, 0);
    }

    private int formationTicks() {
        return switch (level().getDifficulty()) {
            case PEACEFUL, EASY -> 12;
            case NORMAL -> 10;
            case HARD -> 8;
        };
    }

    private double initialFallSpeed() {
        return switch (level().getDifficulty()) {
            case PEACEFUL, EASY -> 0.9;
            case NORMAL -> 1.15;
            case HARD -> 1.45;
        };
    }

    private double maximumFallSpeed() {
        return switch (level().getDifficulty()) {
            case PEACEFUL, EASY -> 2;
            case NORMAL -> 2.5;
            case HARD -> 3;
        };
    }

    public float getFormationProgress(float partial) {
        return Mth.clamp(1 - (entityData.get(FORM_TIME) - partial) / formationTicks(), 0, 1);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.12;
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            if (!(getOwner() instanceof TheTwilightHierophantEntity boss) || !boss.isAlive() || boss.getPhase() != 4
                    || tickCount > 80 || !boss.insideArena(position())
                    || !boss.isAreaLoaded(getBoundingBox().expandTowards(getDeltaMovement()))) {
                discard();
                return;
            }
        }
        int forming = entityData.get(FORM_TIME);
        setNoGravity(forming > 0);
        if (forming > 0) setDeltaMovement(Vec3.ZERO);
        else setDeltaMovement(0, -Math.min(maximumFallSpeed(), Math.max(initialFallSpeed(), -getDeltaMovement().y)), 0);
        super.tick();
        if (isRemoved()) return;
        if (level() instanceof ServerLevel server) {
            // The warning is fixed at summon time. Faster projectiles increase
            // pressure without moving the danger circle after a player dodges.
            if (tickCount % 2 == 0 && server.hasChunkAt(BlockPos.containing(landing))) {
                for (int i = 0; i < 12; i++) {
                    double angle = i * Math.PI / 6;
                    server.sendParticles(ParticleTypes.FLAME, landing.x + Math.cos(angle) * 2.2,
                            landing.y + 0.08, landing.z + Math.sin(angle) * 2.2, 1, 0, 0, 0, 0);
                }
            }
            if (forming > 0) {
                entityData.set(FORM_TIME, forming - 1);
                if (forming == 1) {
                    setNoGravity(false);
                    setDeltaMovement(0, -initialFallSpeed(), 0);
                    hasImpulse = true;
                }
            }
        } else {
            int samples = forming > 0 ? 1 : 4;
            for (int i = 0; i < samples; i++) {
                Vec3 tail = position().subtract(getDeltaMovement().scale(i / 4D)).add(0, 0.4, 0);
                level().addParticle(ParticleTypes.FLAME, tail.x, tail.y, tail.z, 0, 0.035, 0);
                if (forming == 0) level().addParticle(ParticleTypes.SMOKE, tail.x, tail.y + 0.2, tail.z, 0, 0.02, 0);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entityData.get(FORM_TIME) == 0 && entity instanceof LivingEntity
                && super.canHitEntity(entity) && (getOwner() == null || !getOwner().isAlliedTo(entity));
    }

    @Override
    protected void onHit(HitResult hit) {
        if (level().isClientSide() || isRemoved()) return;
        Vec3 at = hit.getLocation();
        level().explode(this, at.x, at.y, at.z, 2.2F, Level.ExplosionInteraction.NONE);
        ((ServerLevel) level()).sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 35, 0.8, 0.5, 0.8, 0.1);
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MeteorFormTime", entityData.get(FORM_TIME));
        tag.putInt("MeteorAge", tickCount);
        tag.put("MeteorLanding", newDoubleList(landing.x, landing.y, landing.z));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(FORM_TIME, Mth.clamp(tag.getInt("MeteorFormTime"), 0, 12));
        tickCount = Mth.clamp(tag.getInt("MeteorAge"), 0, 81);
        var saved = tag.getList("MeteorLanding", Tag.TAG_DOUBLE);
        if (saved.size() == 3 && Double.isFinite(saved.getDouble(0)) && Double.isFinite(saved.getDouble(1)) && Double.isFinite(saved.getDouble(2)))
            landing = new Vec3(saved.getDouble(0), saved.getDouble(1), saved.getDouble(2));
        else landing = position().add(0, -10, 0);
    }
}
