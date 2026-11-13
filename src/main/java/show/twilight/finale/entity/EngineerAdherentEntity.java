package show.twilight.finale.entity;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.entity.combat.ColoredAdherent;
import show.twilight.finale.entity.projectile.EngineerWrench;
import show.twilight.finale.item.EngineeringNotes;
import show.twilight.finale.world.CastleEncounterProgress;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.entity.monster.Adherent;

/** A unique workshop occupant: a short throwing swing, never an adherent charge. */
public final class EngineerAdherentEntity extends Adherent implements EnforcedHomePoint {
    public static final int THROW_TICK = 5, THROW_DURATION = 12;
    private static final EntityDataAccessor<Long> THROW_START = SynchedEntityData.defineId(EngineerAdherentEntity.class, EntityDataSerializers.LONG);
    private GlobalPos home;

    public EngineerAdherentEntity(EntityType<? extends EngineerAdherentEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_HOE));
        setDropChance(EquipmentSlot.MAINHAND, 0);
        ((ColoredAdherent) (Object) this).finale$setColor(ColoredAdherent.BLUE);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Adherent.registerAttributes().add(Attributes.MAX_HEALTH, 50).add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.FOLLOW_RANGE, 24).add(Attributes.ATTACK_DAMAGE, 6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(THROW_START, -1000L);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1, 38, 14));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public int getThrowTime() {
        return (int) Math.min(1000, Math.max(0, level().getGameTime() - entityData.get(THROW_START)));
    }

    @Override
    public boolean isCharging() {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (level().isClientSide() || getThrowTime() < THROW_DURATION) return;
        entityData.set(THROW_START, level().getGameTime());
        swing(InteractionHand.MAIN_HAND, true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server) || !isAlive() || getThrowTime() != THROW_TICK) return;
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || isAlliedTo(target) || !hasLineOfSight(target)) return;
        var wrench = new EngineerWrench(server, this);
        Vec3 aim = target.getEyePosition().subtract(wrench.position());
        double horizontal = aim.horizontalDistance();
        wrench.shoot(aim.x, aim.y + horizontal * 0.08, aim.z, 1.25F, 1.5F);
        server.addFreshEntity(wrench);
        playSound(SoundEvents.SNOWBALL_THROW, 1, 0.65F + random.nextFloat() * 0.2F);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof Adherent || other instanceof AdherentHighPriestEntity
                || other instanceof AnnihilationGolemEntity || super.isAlliedTo(other);
    }

    @Override
    public void die(DamageSource source) {
        if (dead) return;
        ServerPlayer killer = source.getEntity() instanceof ServerPlayer player ? player
                : getKillCredit() instanceof ServerPlayer player ? player : null;
        // Vanilla can award player_killed_entity inside super.die, before loot generation.
        boolean giveNotes = killer != null && !EngineeringNotes.hasDiscovered(killer);
        int storyState = 0;
        if (level() instanceof ServerLevel level) {
            var anchor = isRestrictionPointValid(level.dimension()) ? home.pos() : blockPosition();
            storyState = CastleEncounterProgress.findCastle(level, anchor)
                    .map(start -> CastleEncounterProgress.get(level).state(start.getChunkPos().toLong())).orElse(0);
        }
        super.die(source);
        if (dead && giveNotes && level() instanceof ServerLevel server && server.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))
            spawnAtLocation(EngineeringNotes.create(storyState));
    }

    @Override public GlobalPos getRestrictionPoint() { return home; }
    @Override public void setRestrictionPoint(GlobalPos pos) { home = pos; }
    @Override public int getHomeRadius() { return 24; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        saveHomePointToNbt(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        loadHomePointFromNbt(tag);
        entityData.set(THROW_START, -1000L);
        ((ColoredAdherent) (Object) this).finale$setColor(ColoredAdherent.BLUE);
    }
}
