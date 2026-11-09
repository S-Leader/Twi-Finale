package show.twilight.finale.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import show.twilight.finale.init.ModEntity;
import show.twilight.finale.init.ModItem;
import twilightforest.block.ForceFieldBlock;
import twilightforest.entity.projectile.ITFProjectile;
import twilightforest.entity.projectile.TFThrowable;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFParticleType;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class ProverbsBolt extends TFThrowable implements ITFProjectile, ItemSupplier {
    public ProverbsBolt(EntityType<? extends ProverbsBolt> type, Level level) {
        super(type, level);
    }

    public ProverbsBolt(Level level, LivingEntity owner) {
        super(ModEntity.PROVERBS_BOLT.get(), level, owner);
        this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0f, 1.5f, 1.0f);
    }

    public boolean isPickable() {
        return true;
    }

    public float getPickRadius() {
        return 1.0f;
    }

    public void tick() {
        if (!this.level().isClientSide()) {
            Vec3 start = this.position(), end = start.add(this.getDeltaMovement());
            BlockHitResult field = BlockGetter.traverseBlocks(start, end, this, (bolt, pos) -> {
                BlockState state = bolt.level().getBlockState(pos);
                var shape = state.getBlock() instanceof ForceFieldBlock ? Shapes.block() : state.getCollisionShape(bolt.level(), pos, CollisionContext.of(bolt));
                return shape.clip(start, end, pos);
            }, bolt -> null);
            HitResult first = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (field != null && this.level().getBlockState(field.getBlockPos()).getBlock() instanceof ForceFieldBlock && (first.getType() == HitResult.Type.MISS || start.distanceToSqr(field.getLocation()) <= start.distanceToSqr(first.getLocation()) + 1.0E-7) && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, field)) {
                this.setPos(field.getLocation());
                this.onHit(field);
                return;
            }
        }
        super.tick();
        if (this.level().isClientSide()) this.makeTrail();
    }

    private void makeTrail() {
        for (int i = 0; i < 5; ++i) {
            double dx = this.getX() + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
            double dy = this.getY() + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
            double dz = this.getZ() + 0.5 * (this.random.nextDouble() - this.random.nextDouble());
            double s1 = (this.random.nextFloat() * 0.5f + 0.5f) * 0.32f;
            double s2 = (this.random.nextFloat() * 0.5f + 0.5f) * 0.6f;
            double s3 = (this.random.nextFloat() * 0.5f + 0.5f) * 0.72f;
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) s1, (float) s2, (float) s3), dx, dy, dz, 0.0, 0.0, 0.0);
        }
    }

    protected double getDefaultGravity() {
        return 0.001f;
    }

    public boolean hurt(DamageSource source, float amount) {
        super.hurt(source, amount);
        if (!this.level().isClientSide() && source.getEntity() != null) {
            Vec3 viewVector = source.getEntity().getLookAngle();
            this.shoot(viewVector.x(), viewVector.y(), viewVector.z(), 1.5f, 0.1f);
            Entity entity = source.getDirectEntity();
            if (entity instanceof LivingEntity living) {
                this.setOwner(living);
            }
            return true;
        }
        return false;
    }

    @OnlyIn(value = Dist.CLIENT)
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ItemStack itemstack = this.getItem();
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemstack), this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, this.random.nextDouble() * 0.2, this.random.nextGaussian() * 0.05);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            BlockPos pos = result.getBlockPos();
            BlockState state = this.level().getBlockState(pos);
            // The final boss's violet enclosure is deliberately not dispellable.
            // It still intercepts/consumes the bolt in tick(), so this cannot be
            // used to shoot through the field at the entity behind it.
            if (state.getBlock() instanceof ForceFieldBlock) {
                this.level().removeBlock(pos, false);
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
    }

    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            Entity entityHit = result.getEntity();
            Entity owner = this.getOwner();
            if (entityHit instanceof LivingEntity living) {
                if ((owner == null || entityHit != owner) && living.hurt(TFDamageTypes.getIndirectEntityDamageSource(this.level(), TFDamageTypes.TWILIGHT_SCEPTER, this, owner, new EntityType[0]), 7.0f) && this.level().getDifficulty() != Difficulty.PEACEFUL) {
                    int poisonTime = this.level().getDifficulty() == Difficulty.HARD ? 5 : 3;
                    living.addEffect(new MobEffectInstance(MobEffects.WITHER, poisonTime * 20, 0));
                }
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
        return new ItemStack(ModItem.CHARM_OF_LIFE_3.get());
    }
}

