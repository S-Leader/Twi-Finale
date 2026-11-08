package show.twilight.finale.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class MaceItem extends Item {
    public MaceItem(Item.Properties properties) {
        super(properties.attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5, AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4, AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build()));
    }

    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public int getEnchantmentValue() {
        return 15;
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ServerPlayer serverplayer;
        if (attacker instanceof ServerPlayer && MaceItem.canSmashAttack((LivingEntity) (serverplayer = (ServerPlayer) attacker))) {
            ServerLevel serverlevel = (ServerLevel) attacker.level();
            float fallDist = serverplayer.fallDistance;
            int unbreakingLevel = show.twilight.finale.util.EnchantmentsUtil.level(stack, Enchantments.UNBREAKING, attacker.level());
            if (unbreakingLevel > 0) {
                double upwardMotion = 0.35 + (double) unbreakingLevel * 0.15;
                serverplayer.setDeltaMovement(new Vec3(serverplayer.getDeltaMovement().x, upwardMotion, serverplayer.getDeltaMovement().z));
            } else {
                Vec3 currentMovement = serverplayer.getDeltaMovement();
                serverplayer.setDeltaMovement(new Vec3(currentMovement.x, (double) 0.01f, currentMovement.z));
            }
            serverplayer.connection.send((Packet) new ClientboundSetEntityMotionPacket((Entity) serverplayer));
            if (target.onGround()) {
                SoundEvent soundevent = fallDist > 5.0f ? SoundEvents.ANVIL_LAND : SoundEvents.PLAYER_ATTACK_STRONG;
                serverlevel.playSound((Player) null, serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), soundevent, serverplayer.getSoundSource(), 1.0f, 1.0f);
            } else {
                serverlevel.playSound((Player) null, serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, serverplayer.getSoundSource(), 1.0f, 1.0f);
            }
            int piercingLevel = show.twilight.finale.util.EnchantmentsUtil.level(stack, Enchantments.PIERCING, attacker.level());
            if (piercingLevel > 0) {
                int invulnTime = target.invulnerableTime;
                target.invulnerableTime = 0;
                float armorIgnoreRatio = Math.min(0.6f, (float) piercingLevel * 0.15f);
                float currentArmor = target.getArmorValue();
                float armorReduction = currentArmor * armorIgnoreRatio;
                DamageSource rawSource = attacker.damageSources().mobAttack(attacker);
                target.hurt(rawSource, this.damageByPiercing(target, armorReduction));
                target.invulnerableTime = invulnTime;
            }
            MaceItem.knockback((Level) serverlevel, (Player) serverplayer, (Entity) target);
        }
        return true;
    }

    private float damageByPiercing(LivingEntity target, float armorReduction) {
        float baseArmorDamage = 0.1f * armorReduction;
        return baseArmorDamage;
    }

    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        if (MaceItem.canSmashAttack(attacker)) {
            attacker.resetFallDistance();
        }
    }

    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(Items.COBBLESTONE);
    }

    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) directEntity;
            if (!MaceItem.canSmashAttack(livingentity)) {
                return 0.0f;
            }
            float f1 = livingentity.fallDistance;
            float f2 = f1 <= 3.0f ? 4.0f * f1 : (f1 <= 8.0f ? 12.0f + 2.0f * (f1 - 3.0f) : 22.0f + f1 - 8.0f);
            ItemStack weapon = livingentity.getMainHandItem();
            int powerLevel = show.twilight.finale.util.EnchantmentsUtil.level(weapon, Enchantments.POWER, livingentity.level());
            if (powerLevel > 0) {
                f2 += (float) powerLevel * 0.5f * f1;
            }
            return f2;
        }
        return 0.0f;
    }

    private static void knockback(Level level, Player player, Entity entity) {
        level.levelEvent(2004, entity.getOnPos(), 0);
        level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(3.5), MaceItem.knockbackPredicate(player, entity)).forEach(p_347296_ -> {
            Vec3 vec3 = p_347296_.position().subtract(entity.position());
            double d0 = MaceItem.getKnockbackPower(player, p_347296_, vec3);
            Vec3 vec31 = vec3.normalize().scale(d0);
            if (d0 > 0.0) {
                p_347296_.push(vec31.x, (double) 0.7f, vec31.z);
                if (p_347296_ instanceof ServerPlayer) {
                    ServerPlayer serverplayer = (ServerPlayer) p_347296_;
                    serverplayer.connection.send((Packet) new ClientboundSetEntityMotionPacket((Entity) serverplayer));
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
            boolean flag2 = !player.isAlliedTo((Entity) p_344407_);
            boolean flag6 = false;
            if (p_344407_ instanceof TamableAnimal && (tamableanimal = (TamableAnimal) p_344407_).isTame() && player.getUUID().equals(tamableanimal.getOwnerUUID())) {
                flag6 = true;
            }
            boolean flag3 = !flag6;
            boolean flag4 = true;
            if (p_344407_ instanceof ArmorStand && (armorstand = (ArmorStand) p_344407_).isMarker()) {
                flag4 = false;
            }
            boolean flag5 = entity.distanceToSqr((Entity) p_344407_) <= Math.pow(3.5, 2.0);
            return flag && flag1 && flag2 && flag3 && flag4 && flag5;
        };
    }

    private static double getKnockbackPower(Player player, LivingEntity entity, Vec3 entityPos) {
        return (3.5 - entityPos.length()) * (double) 0.7f * (double) (player.fallDistance > 5.0f ? 2 : 1) * (1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    public static boolean canSmashAttack(LivingEntity entity) {
        return entity.fallDistance > 1.5f;
    }
}

