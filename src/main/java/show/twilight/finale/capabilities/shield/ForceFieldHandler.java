package show.twilight.finale.capabilities.shield;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import show.twilight.finale.network.UpdateForceFieldPacket;

public class ForceFieldHandler
        implements IForceFieldCapability {
    private int temporaryFields;
    private int permanentFields;
    private int maxFields = 6;
    private LivingEntity host;
    private int timer;
    private int breakTimer;
    private int lastBreakTick = -100;
    private int lastShakeTick = -100;

    @Override
    public void setEntity(LivingEntity entity) {
        this.host = entity;
    }

    @Override
    public void update() {
        block6:
        {
            Player player;
            LivingEntity livingEntity;
            block5:
            {
                LivingEntity livingEntity2 = this.host;
                if (!(livingEntity2 instanceof Player)) break block5;
                Player player2 = (Player) livingEntity2;
                if (player2.getAbilities().invulnerable) break block6;
            }
            if (!(this.host.level().isClientSide() || this.getTemporaryFields() <= 0 || this.timer-- > 0 || this.breakTimer > 0 || (livingEntity = this.host) instanceof Player && (player = (Player) livingEntity).isCreative())) {
                this.breakField();
            }
            if (this.breakTimer > 0) {
                --this.breakTimer;
            }
        }
    }

    @Override
    public int getFieldsLeft() {
        return this.temporaryFields + this.permanentFields;
    }

    @Override
    public int getTemporaryFields() {
        return this.temporaryFields;
    }

    @Override
    public int getPermanentFields() {
        return this.permanentFields;
    }

    @Override
    public int getMaxFields() {
        return this.maxFields;
    }

    @Override
    public void setMaxFields(int max) {
        this.maxFields = Math.max(max, 1);
        if (this.getFieldsLeft() > this.maxFields) {
            this.temporaryFields = Math.min(this.temporaryFields, this.maxFields);
            this.permanentFields = Math.min(this.permanentFields, this.maxFields - this.temporaryFields);
        }
        this.sendUpdatePacket();
    }

    @Override
    public int getLastBreakTick() {
        return this.lastBreakTick;
    }

    @Override
    public int getLastShakeTick() {
        return this.lastShakeTick;
    }

    @Override
    public void breakField() {
        if (this.breakTimer <= 0) {
            this.lastBreakTick = this.host.tickCount;
            if (this.temporaryFields > 0) {
                --this.temporaryFields;
                this.resetTimer();
            } else if (this.permanentFields > 0) {
                --this.permanentFields;
            }
            this.sendUpdatePacket();
            this.host.level().playSound(null, this.host.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, ((this.host.getRandom().nextFloat() - this.host.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
            this.breakTimer = 20;
        }
    }

    @Override
    public void triggerShake() {
        this.lastShakeTick = this.host.tickCount;
        this.host.level().playSound(null, this.host.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.HOSTILE, 1.0f, 1.5f);
        this.sendUpdatePacket();
    }

    @Override
    public void replenishFields() {
        this.temporaryFields = 0;
        this.permanentFields = this.getMaxFields();
        this.timer = 0;
        this.sendUpdatePacket();
    }

    @Override
    public void setFields(int amount, boolean isTemporary) {
        int target = Math.min(Math.max(amount, 0), this.getMaxFields());
        if (isTemporary) {
            this.temporaryFields = target;
            this.permanentFields = 0;
            this.resetTimer();
        } else {
            this.permanentFields = target;
            this.temporaryFields = 0;
        }
        this.sendUpdatePacket();
    }

    @Override
    public void addFields(int amount, boolean isTemporary) {
        if (isTemporary) {
            if (this.temporaryFields <= 0) {
                this.resetTimer();
            }
            this.temporaryFields = Math.min(this.temporaryFields + amount, Math.max(0, this.maxFields - this.permanentFields));
        } else {
            this.permanentFields = Math.min(this.permanentFields + amount, Math.max(0, this.maxFields - this.temporaryFields));
        }
        this.sendUpdatePacket();
    }

    private void resetTimer() {
        this.timer = 240;
    }

    public void sendUpdatePacket() {
        if (this.host != null && this.host.level() != null && !this.host.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(this.host, new UpdateForceFieldPacket(this.host, this));
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("TempFields", this.temporaryFields);
        tag.putInt("PermFields", this.permanentFields);
        tag.putInt("MaxFields", this.maxFields);
        tag.putInt("LastBreakTick", this.lastBreakTick);
        tag.putInt("LastShakeTick", this.lastShakeTick);
        return tag;
    }

    public void applySnapshot(int temp, int perm, int max, boolean breaking, boolean shaking) {
        maxFields = Math.max(1, max);
        temporaryFields = Math.clamp(temp, 0, maxFields);
        permanentFields = Math.clamp(perm, 0, maxFields - temporaryFields);
        timer = temp > 0 ? 240 : 0;
        lastBreakTick = breaking && host != null ? host.tickCount : -100;
        lastShakeTick = shaking && host != null ? host.tickCount : -100;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.maxFields = tag.contains("MaxFields") ? tag.getInt("MaxFields") : 6;
        this.temporaryFields = tag.getInt("TempFields");
        this.permanentFields = tag.getInt("PermFields");
        this.lastBreakTick = tag.contains("LastBreakTick") ? tag.getInt("LastBreakTick") : -100;
        int n = this.lastShakeTick = tag.contains("LastShakeTick") ? tag.getInt("LastShakeTick") : -100;
        if (this.temporaryFields > 0) {
            this.resetTimer();
        } else {
            this.timer = 0;
        }
    }
}

