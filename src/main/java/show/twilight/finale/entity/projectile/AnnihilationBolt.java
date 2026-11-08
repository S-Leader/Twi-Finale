package show.twilight.finale.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import show.twilight.finale.init.ModEntity;
import twilightforest.entity.projectile.ITFProjectile;
import twilightforest.entity.projectile.TFThrowable;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFItems;
import twilightforest.init.TFParticleType;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class AnnihilationBolt extends TFThrowable implements ITFProjectile, ItemSupplier {
    public AnnihilationBolt(EntityType<? extends AnnihilationBolt> type, Level level) {
        super(type, level);
    }

    public AnnihilationBolt(Level level, LivingEntity owner) {
        super(ModEntity.ANNIHILATION_BOLT.get(), level, owner);
    }

    public void tick() {
        super.tick();
        this.makeTrail(TFParticleType.ANNIHILATE.get(), 5);
    }

    protected double getDefaultGravity() {
        return 0.003f;
    }

    @OnlyIn(value = Dist.CLIENT)
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.defaultBlockState()), false, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, this.random.nextDouble() * 0.2, this.random.nextGaussian() * 0.05);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            BlockPos pos = result.getBlockPos();
            Level level = this.level();
            if (level instanceof ServerLevel server) {
                RandomSource rand = server.getRandom();
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

    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity owner = this.getOwner();
        Entity entityHit = result.getEntity();
        if (entityHit instanceof LivingEntity living) {
            if ((owner == null || entityHit != owner && entityHit != owner.getVehicle()) && entityHit.hurt(TFDamageTypes.getIndirectEntityDamageSource(this.level(), TFDamageTypes.LEAF_BRAIN, this, this.getOwner(), new EntityType[0]), 1.0f) && this.level().getDifficulty() != Difficulty.PEACEFUL) {
                int poisonTime = this.level().getDifficulty() == Difficulty.HARD ? 6 : 3;
                living.addEffect(new MobEffectInstance(MobEffects.POISON, poisonTime * 20, 0));
                living.invulnerableTime = 0;
            }
        }
    }

    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    public ItemStack getItem() {
        return new ItemStack(TFItems.CUBE_TALISMAN.get());
    }
}

