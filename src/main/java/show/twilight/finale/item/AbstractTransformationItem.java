package show.twilight.finale.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import show.twilight.finale.util.ItemData;

public abstract class AbstractTransformationItem extends PickaxeItem {
    public static final float MACE_FORM = 6.0F;
    public static final String MACE_FORM_TAG = "maceForm";
    public static final TagKey<Block> PICKAXE_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "transformation_pickaxe_blocks"));
    public static final TagKey<EntityType<?>> PICKAXE_TARGETS = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "transformation_pickaxe_targets"));

    protected AbstractTransformationItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static boolean isPickaxeBlock(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(PICKAXE_BLOCKS)) return true;
        return state.requiresCorrectToolForDrops() && !state.is(BlockTags.MINEABLE_WITH_AXE) && !state.is(BlockTags.MINEABLE_WITH_SHOVEL) && !state.is(BlockTags.MINEABLE_WITH_HOE);
    }

    public static boolean isPickaxeTarget(Entity target) {
        return target != null && target.getType().is(PICKAXE_TARGETS);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float speed = super.getDestroySpeed(stack, state);
        if (isPickaxeBlock(state) || state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(BlockTags.MINEABLE_WITH_HOE))
            return Math.max(speed, getTier().getSpeed());
        return speed;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide()) {
            boolean smashing = selected && entity instanceof LivingEntity living && living.getMainHandItem() == stack && MaceItem.canSmashAttack(living);
            ItemData.putFloat(stack, MACE_FORM_TAG, smashing ? 1.0F : 0.0F);
        }
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
        return super.getAttackDamageBonus(target, damage, source) + (isPickaxeTarget(target) ? 0 : Items.MACE.getAttackDamageBonus(target, damage, source));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!isPickaxeTarget(target) && MaceItem.canSmashAttack(attacker))
            return Items.MACE.hurtEnemy(stack, target, attacker);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!isPickaxeTarget(target) && MaceItem.canSmashAttack(attacker)) {
            Items.MACE.postHurtEnemy(stack, target, attacker);
            ItemData.putFloat(stack, MACE_FORM_TAG, 0.0F);
        } else {
            super.postHurtEnemy(stack, target, attacker);
            if (isPickaxeTarget(target)) ItemData.putFloat(stack, MACE_FORM_TAG, 0.0F);
        }
    }
}
