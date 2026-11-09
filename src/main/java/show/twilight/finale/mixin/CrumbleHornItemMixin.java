package show.twilight.finale.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;
import twilightforest.item.CrumbleHornItem;

@Mixin(value = {CrumbleHornItem.class})
public abstract class CrumbleHornItemMixin {

    @Inject(method = {"crumbleBlock"}, at = {@At(value = "HEAD")}, cancellable = true, remap = false)
    private void onCrumbleBlock(ServerLevel world, LivingEntity living, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.isAir()) {
            cir.setReturnValue(false);
        } else if (living instanceof Player && NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, (Player) living)).isCanceled()) {
            cir.setReturnValue(false);
        } else if (world instanceof ServerLevel) {
            ServerLevel serverLevel = world;
            if ((state.is(TFBlocks.BLUE_FORCE_FIELD.get()) || state.is(TFBlocks.GREEN_FORCE_FIELD.get()) || state.is(TFBlocks.ORANGE_FORCE_FIELD.get()) || state.is(TFBlocks.PINK_FORCE_FIELD.get()) || state.is(TFBlocks.VIOLET_FORCE_FIELD.get())) && world.getRandom().nextBoolean()) {
                if (living instanceof Player player2) {
                    if (block.canHarvestBlock(state, world, pos, player2)) {
                        world.removeBlock(pos, false);
                        block.playerDestroy(world, player2, pos, state, world.getBlockEntity(pos), ItemStack.EMPTY);
                        world.playSound(null, pos, TFSounds.BLOCK_ANNIHILATED.get(), SoundSource.NEUTRAL, 0.125f, world.getRandom().nextFloat() * 0.25f + 0.75f);
                        this.annihilateParticles(serverLevel, pos);
                        cir.setReturnValue(true);
                    }
                } else if (EventHooks.canEntityGrief(world, living)) {
                    world.destroyBlock(pos, true);
                    world.playSound(null, pos, TFSounds.BLOCK_ANNIHILATED.get(), SoundSource.NEUTRAL, 0.125f, world.getRandom().nextFloat() * 0.25f + 0.75f);
                    this.annihilateParticles(serverLevel, pos);
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Unique
    private void annihilateParticles(Level level, BlockPos pos) {
        RandomSource rand = level.getRandom();
        if (level instanceof ServerLevel server) {
            for (int dx = 0; dx < 3; ++dx) {
                for (int dy = 0; dy < 3; ++dy) {
                    for (int dz = 0; dz < 3; ++dz) {
                        double x = (double) pos.getX() + ((double) dx + 0.5) / 4.0;
                        double y = (double) pos.getY() + ((double) dy + 0.5) / 4.0;
                        double z = (double) pos.getZ() + ((double) dz + 0.5) / 4.0;
                        double speed = rand.nextGaussian() * 0.2;
                        server.sendParticles((ParticleOptions) TFParticleType.ANNIHILATE.get(), x, y, z, 1, 0.0, 0.0, 0.0, speed);
                    }
                }
            }
        }
    }
}

