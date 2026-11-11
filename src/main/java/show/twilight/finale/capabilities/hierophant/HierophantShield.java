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
    private float health = 200, maxHealth = 200;
    private boolean physical;
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
    public boolean isActive() {
        return health > 0;
    }

    @Override
    public boolean requiresPhysicalDamage() {
        return physical;
    }

    @Override
    public float absorbDamage(float amount) {
        if (owner.level().isClientSide() || !isActive() || !Float.isFinite(amount) || amount <= 0)
            return 0;
        float absorbed = Math.min(health, amount);
        health -= absorbed;
        changed();
        return absorbed;
    }

    public void tick() {
        if (!owner.level().isClientSide() && isActive() && owner.isSpawned() && ++colourTicks >= 200) {
            colourTicks = 0;
            physical = !physical;
            changed();
        }
    }

    public void resetMotion() {
        if (owner.level().isClientSide()) return;
        // Target loss/skill cancellation must not shorten the stop window.
        changed();
    }

    public void restore(float maximum, float current) {
        applySnapshot(current, maximum, false);
        changed();
    }

    public void applySnapshot(float current, float maximum, boolean physical) {
        maxHealth = Float.isFinite(maximum) ? Math.max(1, maximum) : 200;
        health = Float.isFinite(current) ? Mth.clamp(current, 0, maxHealth) : maxHealth;
        this.physical = physical;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Health", health);
        tag.putFloat("MaxHealth", maxHealth);
        tag.putBoolean("RequiresPhysical", physical);
        tag.putInt("PolarityVersion", 2);
        tag.putInt("ColourTicks", colourTicks);
        // A collision flight is cancelled on reload, never resumed into a wall.
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        float max = tag.getFloat("MaxHealth"), hp = tag.getFloat("Health");
        if (tag.getInt("PolarityVersion") < 2) {
            float replacement = 100;
            hp = max > 0 ? hp / max * replacement : 0;
            max = replacement;
        }
        applySnapshot(hp, max, tag.getBoolean("RequiresPhysical"));
        colourTicks = Mth.clamp(tag.getInt("ColourTicks"), 0, 199);
        changed();
    }

    private void changed() {
        if (!owner.level().isClientSide()) owner.syncShieldState();
    }
}
