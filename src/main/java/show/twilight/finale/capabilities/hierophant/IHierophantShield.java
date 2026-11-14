package show.twilight.finale.capabilities.hierophant;

public interface IHierophantShield {
    float getHealth();

    float getMaxHealth();

    float getOpacity();

    boolean isActive();

    boolean requiresPhysicalDamage();

    float absorbDamage(float amount);
}
