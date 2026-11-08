package show.twilight.finale.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.init.ModBlocks;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.entity.RovingCube;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.init.TFSounds;
import twilightforest.loot.TFLootTables;

/**
 * Native cube geometry/attributes, deliberately NO attack or navigation AI yet.
 * The ordinary twilightforest:roving_cube is not modified.
 */
public final class RovingCubeBoss extends RovingCube implements EnforcedHomePoint {
    private static final ResourceKey<LootTable> REWARD = ResourceKey.create(Registries.LOOT_TABLE, TwilightFinaleMod.prefix("chests/roving_cube"));
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.twilightforest.roving_cube"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private GlobalPos home;
    private boolean rewardPending;
    private boolean rewardCreated;

    public RovingCubeBoss(EntityType<? extends RovingCubeBoss> type, Level level) {
        super(type, level);
        setNoAi(true);
        setPersistenceRequired();
        setCanPickUpLoot(false);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) bossBar.setProgress(Math.max(0, getHealth() / getMaxHealth()));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public @Nullable GlobalPos getRestrictionPoint() {
        return home;
    }

    @Override
    public void setRestrictionPoint(@Nullable GlobalPos pos) {
        home = pos;
    }

    @Override
    public int getHomeRadius() {
        return 24;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void die(DamageSource source) {
        if (deathTime == 0 && !rewardCreated) rewardPending = true;
        super.die(source);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        saveHomePointToNbt(tag);
        tag.putBoolean("FinaleRewardPending", rewardPending);
        tag.putBoolean("FinaleRewardCreated", rewardCreated);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        loadHomePointFromNbt(tag);
        rewardPending = tag.getBoolean("FinaleRewardPending");
        rewardCreated = tag.getBoolean("FinaleRewardCreated");
        setNoAi(true);
        setPersistenceRequired();
        setCanPickUpLoot(false);
    }

    @Override
    public void checkDespawn() {
        if (level().getDifficulty() == Difficulty.PEACEFUL) {
            if (home != null && home.dimension().equals(level().dimension()) && level().hasChunkAt(home.pos())
                    && level().getBlockEntity(home.pos()) == null)
                level().setBlockAndUpdate(home.pos(), ModBlocks.ROVING_CUBE_BOSS_SPAWNER.get().defaultBlockState());
            discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED && rewardPending && !rewardCreated && level() instanceof ServerLevel server
                && server.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            rewardCreated = true;
            rewardPending = false;
            BlockPos origin = home != null && home.dimension().equals(server.dimension()) ? home.pos() : blockPosition();
            boolean placed = false;
            // Same safety principle as TF boss chests: never overwrite an existing block entity.
            for (int dy = 0; dy < 16 && !placed; dy++) {
                BlockPos at = origin.above(dy);
                if (at.getY() >= server.getMaxBuildHeight() || !server.hasChunkAt(at)) break;
                var state = server.getBlockState(at);
                if (server.getBlockEntity(at) != null || state.getPistonPushReaction() == PushReaction.BLOCK) continue;
                TFLootTables.generateLootContainer(server, at, TFBlocks.CANOPY_CHEST.get().defaultBlockState(), 3, server.getRandom().nextLong(), REWARD);
                placed = server.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
                if (placed) {
                    server.playSound(null, at, TFSounds.BOSS_CHEST_APPEAR.get(), SoundSource.HOSTILE, 8, 0.5F);
                    server.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 40, 0.5, 0.5, 0.5, 0.05);
                }
            }
            if (!placed) {
                spawnAtLocation(new ItemStack(TFItems.CUBE_TALISMAN.get()));
            }
        }
        bossBar.removeAllPlayers();
        super.remove(reason);
    }
}
