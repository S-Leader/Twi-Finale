package show.twilight.finale.capabilities.shield;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface IForceFieldCapability {
    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag tag);

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath((String) "twilightforest_finale", (String) "force_field");

    public void setEntity(LivingEntity var1);

    public void update();

    public int getFieldsLeft();

    public int getTemporaryFields();

    public int getPermanentFields();

    public int getMaxFields();

    public void setMaxFields(int var1);

    public int getLastBreakTick();

    public int getLastShakeTick();

    public void breakField();

    public void triggerShake();

    public void replenishFields();

    public void setFields(int var1, boolean var2);

    public void addFields(int var1, boolean var2);
}

