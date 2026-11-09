package show.twilight.finale.capabilities.hierophant;

/** A shield on its owning boss, not a separate entity. Mutations are server-only. */
public interface IHierophantShield {
    int MIN_STOP_TICKS = 40;
    int DEFAULT_STOP_TICKS = 100;

    float getHealth();

    float getMaxHealth();

    /** Visual opacity only: at least 50% while active, not the HP-bar ratio. */
    float getOpacity();

    float getSpinSpeed();

    boolean isActive();

    boolean isRushing();

    boolean requiresPhysicalDamage();

    boolean isStopped();

    int getStopTicksRemaining();

    int getStopDuration();

    /** Stops the shield and opens its damage window, clamped to 2-5 seconds. */
    void stop(int ticks);

    /** Consumes HP and switches damage polarity; spinning shields reject damage. */
    float absorbDamage(float amount);
}
