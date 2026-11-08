package show.twilight.finale.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import show.twilight.finale.util.EnchantmentsUtil;
import twilightforest.item.GiantSwordItem;

import java.util.function.Predicate;

@Mixin(value = {GiantSwordItem.class})
public abstract class GiantSwordItemMixin extends SwordItem {
    public GiantSwordItemMixin(Tier material, int attackDamage, float attackSpeed, Item.Properties properties) {
        super(material, properties.attributes(SwordItem.createAttributes(material, attackDamage, attackSpeed)));
    }

    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public int getEnchantmentValue() {
        return 15;
    }

    public boolean isPrimaryItemFor(ItemStack stack, net.minecraft.core.Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.POWER) || enchantment.is(Enchantments.PIERCING)) {
            return true;
        }
        return super.isPrimaryItemFor(stack, enchantment);
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        var enchantments = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (EnchantmentsUtil.hasStored(book, Enchantments.POWER) || EnchantmentsUtil.hasStored(book, Enchantments.PIERCING) || EnchantmentsUtil.hasStored(book, Enchantments.THORNS)) {
            return true;
        }
        return super.isBookEnchantable(stack, book);
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ServerPlayer serverplayer;
        if (attacker instanceof ServerPlayer && GiantSwordItemMixin.canSmashAttack(serverplayer = (ServerPlayer) attacker)) {
            int thornsLevel;
            double ySpeed;
            ServerLevel serverlevel = (ServerLevel) attacker.level();
            float calculatedFallDist = serverplayer.fallDistance;
            if (calculatedFallDist <= 0.0f && !serverplayer.onGround() && (ySpeed = serverplayer.getDeltaMovement().y) < -0.1) {
                calculatedFallDist = (float) (Math.abs(ySpeed) * 5.0);
            }
            if (calculatedFallDist < 1.6f) {
                calculatedFallDist = 2.0f;
            }
            float f2 = calculatedFallDist <= 3.0f ? 2.0f * calculatedFallDist : (this.fallDistThreshold(calculatedFallDist) ? 6.0f + (calculatedFallDist - 3.0f) : 11.0f + (calculatedFallDist - 8.0f) * 0.5f);
            int powerLevel = EnchantmentsUtil.level(stack, Enchantments.POWER, attacker.level());
            if (powerLevel > 0) {
                f2 += (float) powerLevel * 0.25f * calculatedFallDist;
            }
            if (f2 > 0.0f) {
                DamageSource smashSource = attacker.damageSources().mobAttack(attacker);
                target.hurt(smashSource, f2);
            }
            if ((thornsLevel = EnchantmentsUtil.level(stack, Enchantments.THORNS, attacker.level())) > 0) {
                double upwardMotion = 0.35 + (double) thornsLevel * 0.15;
                serverplayer.setDeltaMovement(new Vec3(serverplayer.getDeltaMovement().x, upwardMotion, serverplayer.getDeltaMovement().z));
            } else {
                Vec3 currentMovement = serverplayer.getDeltaMovement();
                serverplayer.setDeltaMovement(new Vec3(currentMovement.x, 0.01f, currentMovement.z));
            }
            serverplayer.connection.send(new ClientboundSetEntityMotionPacket(serverplayer));
            if (target.onGround()) {
                SoundEvent soundevent = calculatedFallDist > 5.0f ? SoundEvents.ANVIL_LAND : SoundEvents.PLAYER_ATTACK_STRONG;
                serverlevel.playSound(null, serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), soundevent, serverplayer.getSoundSource(), 1.0f, 1.0f);
            } else {
                serverlevel.playSound(null, serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, serverplayer.getSoundSource(), 1.0f, 1.0f);
            }
            int piercingLevel = EnchantmentsUtil.level(stack, Enchantments.PIERCING, attacker.level());
            if (piercingLevel > 0) {
                int invulnTime = target.invulnerableTime;
                target.invulnerableTime = 0;
                float armorIgnoreRatio = Math.min(0.6f, (float) piercingLevel * 0.15f);
                float currentArmor = target.getArmorValue();
                float armorReduction = currentArmor * armorIgnoreRatio;
                DamageSource rawSource = attacker.damageSources().mobAttack(attacker);
                target.hurt(rawSource, 0.1f * armorReduction);
                target.invulnerableTime = invulnTime;
            }
            GiantSwordItemMixin.knockback(serverlevel, serverplayer, target);
            serverplayer.resetFallDistance();
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private boolean fallDistThreshold(float dist) {
        return dist <= 8.0f;
    }

    private static void knockback(Level level, Player player, Entity entity) {
        level.levelEvent(2004, entity.getOnPos(), 0);
        level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(3.5), GiantSwordItemMixin.knockbackPredicate(player, entity)).forEach(p_347296_ -> {
            Vec3 vec3 = p_347296_.position().subtract(entity.position());
            double d0 = GiantSwordItemMixin.getKnockbackPower(player, p_347296_, vec3);
            Vec3 vec31 = vec3.normalize().scale(d0);
            if (d0 > 0.0) {
                p_347296_.push(vec31.x, 0.7f, vec31.z);
                if (p_347296_ instanceof ServerPlayer serverplayer) {
                    serverplayer.connection.send(new ClientboundSetEntityMotionPacket(serverplayer));
                }
            }
        });
    }

    private static Predicate<LivingEntity> knockbackPredicate(Player player, Entity entity) {
        return p_344407_ -> {
            ArmorStand armorstand;
            TamableAnimal tamableanimal;
            boolean flag = !p_344407_.isSpectator();
            boolean flag1 = p_344407_ != player && p_344407_ != entity;
            boolean flag2 = !player.isAlliedTo(p_344407_);
            boolean flag6 = p_344407_ instanceof TamableAnimal && (tamableanimal = (TamableAnimal) p_344407_).isTame() && player.getUUID().equals(tamableanimal.getOwnerUUID());
            boolean flag3 = !flag6;
            boolean flag4 = !(p_344407_ instanceof ArmorStand) || !(armorstand = (ArmorStand) p_344407_).isMarker();
            boolean flag5 = entity.distanceToSqr(p_344407_) <= Math.pow(3.5, 2.0);
            return flag && flag1 && flag2 && flag3 && flag4 && flag5;
        };
    }

    private static double getKnockbackPower(Player player, LivingEntity entity, Vec3 entityPos) {
        double virtualFall = player.fallDistance;
        if (virtualFall <= 0.0 && player.getDeltaMovement().y < 0.0) {
            virtualFall = Math.abs(player.getDeltaMovement().y) * 5.0;
        }
        return (3.5 - entityPos.length()) * (double) 0.7f * (double) (virtualFall > 5.0 ? 2 : 1) * (1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    private static boolean canSmashAttack(LivingEntity entity) {
        return entity.fallDistance > 0.5f || !entity.onGround() && entity.getDeltaMovement().y < -0.1;
    }
}

