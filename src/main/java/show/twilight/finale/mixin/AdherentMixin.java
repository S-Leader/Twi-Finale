package show.twilight.finale.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import show.twilight.finale.entity.combat.AdherentAttackGoal;
import show.twilight.finale.entity.combat.ColoredAdherent;
import twilightforest.entity.monster.Adherent;

/**
 * Preserve the original entity ID, eggs, loot, and structure spawn tables.
 */
@Mixin(Adherent.class)
public abstract class AdherentMixin extends Monster implements ColoredAdherent {
    @Unique
    private static final EntityDataAccessor<Integer> FINALE_COLOR = SynchedEntityData.defineId(Adherent.class, EntityDataSerializers.INT);

    protected AdherentMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void finale$defineColor(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(FINALE_COLOR, -1);
    }

    @Override
    public int finale$getColor() {
        return Math.max(0, entityData.get(FINALE_COLOR));
    }

    @Override
    public void finale$setColor(int color) {
        entityData.set(FINALE_COLOR, Math.clamp(color, 0, 4));
    }

    @Inject(method = "registerGoals", at = @At("HEAD"), cancellable = true, remap = false)
    private void finale$combatGoals(CallbackInfo ci) {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(4, new AdherentAttackGoal((Adherent) (Object) this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        ci.cancel();
    }

    @Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private void finale$replaceAttack(net.minecraft.world.entity.LivingEntity target, float power, CallbackInfo ci) {
        ci.cancel();
    }

    @Override
    public void aiStep() {
        if (!level().isClientSide() && entityData.get(FINALE_COLOR) < 0) finale$setColor(random.nextInt(4));
        super.aiStep();
        if (finale$getColor() == RED) clearFire();
    }

    @Override
    public boolean fireImmune() {
        return finale$getColor() == RED || super.fireImmune();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !(finale$getColor() == RED && source.is(DamageTypeTags.IS_FIRE)) && super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (entityData.get(FINALE_COLOR) < 0) finale$setColor(random.nextInt(4));
        super.addAdditionalSaveData(tag);
        tag.putInt("FinaleAdherentColor", finale$getColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        finale$setColor(tag.contains("FinaleAdherentColor") ? tag.getInt("FinaleAdherentColor") : random.nextInt(4));
    }
}
