package show.twilight.finale.capabilities.hierophant;

/**
 * A shield on its owning boss, not a separate entity. Mutations are server-only.
 */
public interface IHierophantShield {
    float getHealth();

    float getMaxHealth();

    /**
     * Remaining visual strength in [0, 1]; the renderer fades both faces and borders with it.
     */
    float getOpacity();

    boolean isActive();

    boolean requiresPhysicalDamage();

    /**
     * Consumes HP. Polarity is timed by the capability, never toggled by a hit.
     */
    float absorbDamage(float amount);
}
