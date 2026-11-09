package show.twilight.finale.capabilities.hierophant;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import show.twilight.finale.entity.TheTwilightHierophantEntity;

/**
 * Authoritative capability state. The owner saves its NBT and publishes a
 * SynchedEntityData snapshot; client snapshots never feed back into the server.
 */
public final class HierophantShield implements IHierophantShield {
    private final TheTwilightHierophantEntity owner;
    private float health = 200, maxHealth = 200, spinSpeed;
    private boolean rushing, physical;
    private int stopRemaining, stopDuration = DEFAULT_STOP_TICKS;
    private int colourTicks;

    public HierophantShield(TheTwilightHierophantEntity owner) {
        this.owner = owner;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public float getMaxHealth() {
        return maxHealth;
    }

    @Override
    public float getOpacity() {
        return isActive() ? Mth.clamp(health / maxHealth, 0.5F, 1) : 0;
    }

    @Override
    public float getSpinSpeed() {
        return spinSpeed;
    }

    @Override
    public boolean isActive() {
        return health > 0;
    }

    @Override
    public boolean isRushing() {
        return rushing;
    }

    @Override
    public boolean requiresPhysicalDamage() {
        return physical;
    }

    @Override
    public boolean isStopped() {
        return stopRemaining > 0;
    }

    @Override
    public int getStopTicksRemaining() {
        return stopRemaining;
    }

    @Override
    public int getStopDuration() {
        return stopDuration;
    }

    @Override
    public void stop(int ticks) {
        if (owner.level().isClientSide() || !isActive()) return;
        stopDuration = Mth.clamp(ticks, MIN_STOP_TICKS, DEFAULT_STOP_TICKS);
        stopRemaining = stopDuration;
        spinSpeed = 0;
        rushing = false;
        changed();
    }

    @Override
    public float absorbDamage(float amount) {
        if (owner.level().isClientSide() || !isActive() || spinSpeed > 0.01F || !Float.isFinite(amount) || amount <= 0)
            return 0;
        float absorbed = Math.min(health, amount);
        health -= absorbed;
        if (!isActive()) {
            stopRemaining = 0;
            spinSpeed = 0;
            rushing = false;
        }
        changed();
        return absorbed;
    }

    public void tick() {
        if (!owner.level().isClientSide() && isActive() && owner.isSpawned() && ++colourTicks >= 200) {
            colourTicks = 0;
            physical = !physical;
            changed();
        }
        if (!owner.level().isClientSide() && stopRemaining > 0) {
            stopRemaining--;
            changed();
        }
    }

    public void setSpinSpeed(float speed) {
        if (owner.level().isClientSide()) return;
        spinSpeed = isActive() && !isStopped() && Float.isFinite(speed)
                ? Mth.clamp(speed, 0, TheTwilightHierophantEntity.SHIELD_MAX_SPIN) : 0;
        changed();
    }

    public void setRushing(boolean rushing) {
        if (owner.level().isClientSide()) return;
        this.rushing = rushing && isActive() && !isStopped();
        changed();
    }

    public void resetMotion() {
        if (owner.level().isClientSide()) return;
        spinSpeed = 0;
        rushing = false;
        // Target loss/skill cancellation must not shorten the stop window.
        changed();
    }

    public void restore(float maximum, float current) {
        applySnapshot(current, maximum, 0, false, 0, DEFAULT_STOP_TICKS, false);
        changed();
    }

    public void applySnapshot(float current, float maximum, float speed, boolean rushing, int remaining, int duration, boolean physical) {
        maxHealth = Float.isFinite(maximum) ? Math.max(1, maximum) : 200;
        health = Float.isFinite(current) ? Mth.clamp(current, 0, maxHealth) : maxHealth;
        stopDuration = Mth.clamp(duration, MIN_STOP_TICKS, DEFAULT_STOP_TICKS);
        stopRemaining = isActive() ? Mth.clamp(remaining, 0, stopDuration) : 0;
        spinSpeed = isActive() && !isStopped() && Float.isFinite(speed)
                ? Mth.clamp(speed, 0, TheTwilightHierophantEntity.SHIELD_MAX_SPIN) : 0;
        this.rushing = rushing && isActive() && !isStopped();
        this.physical = physical;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Health", health);
        tag.putFloat("MaxHealth", maxHealth);
        tag.putInt("StopRemaining", stopRemaining);
        tag.putInt("StopDuration", stopDuration);
        tag.putBoolean("RequiresPhysical", physical);
        tag.putInt("PolarityVersion", 2);
        tag.putInt("ColourTicks", colourTicks);
        // A collision flight is cancelled on reload, never resumed into a wall.
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        float max = tag.getFloat("MaxHealth"), hp = tag.getFloat("Health");
        if (tag.getInt("PolarityVersion") < 2) {
            float replacement = show.twilight.finale.config.HierophantConfig.SHIELD_HEALTH.get().floatValue();
            hp = max > 0 ? hp / max * replacement : 0;
            max = replacement;
        }
        applySnapshot(hp, max, 0, false, 0, DEFAULT_STOP_TICKS, tag.getBoolean("RequiresPhysical"));
        colourTicks = Mth.clamp(tag.getInt("ColourTicks"), 0, 199);
        changed();
    }

    private void changed() {
        if (!owner.level().isClientSide()) owner.syncShieldState();
    }
}
