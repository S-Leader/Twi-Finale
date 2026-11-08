package show.twilight.finale.potions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import show.twilight.finale.init.ModItem;
import twilightforest.entity.projectile.CubeOfAnnihilation;
import twilightforest.init.TFEntities;

import java.util.UUID;

public class AnnihilationCubeEffect extends MobEffect {
    private static final String CUBE_UUID_KEY = "CubeEntity";

    public AnnihilationCubeEffect() {
        super(MobEffectCategory.BENEFICIAL, 5688317);
    }

    public boolean applyEffectTick(LivingEntity living, int amplifier) {
        if (!(living instanceof Player player)) {
            return true;
        }
        if (living.level().isClientSide()) {
            return true;
        }
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (player.isShiftKeyDown()) {
            if (chestplate.getItem() == ModItem.ANNIHILATION_CHESTPLATE.get() && !player.getCooldowns().isOnCooldown(chestplate.getItem())) {
                this.spawnCube(player);
            } else {
                this.removeCube(player);
                if (chestplate.getItem() == ModItem.ANNIHILATION_CHESTPLATE.get()) {
                    player.getCooldowns().addCooldown(chestplate.getItem(), 200);
                }
            }
        }
        return true;
    }

    private void spawnCube(Player player) {
        if (AnnihilationCubeEffect.getCubeEntity(player.level(), player) != null) {
            return;
        }
        CubeOfAnnihilation cube = new CubeOfAnnihilation(TFEntities.CUBE_OF_ANNIHILATION.get(), player.level(), player, player.getItemBySlot(EquipmentSlot.CHEST));
        cube.setPos(player.getX(), player.getY() + 0.5, player.getZ());
        player.level().addFreshEntity(cube);
        AnnihilationCubeEffect.setCubeUUID(player, cube);
    }

    private void removeCube(Player player) {
        CubeOfAnnihilation cube = AnnihilationCubeEffect.getCubeEntity(player.level(), player);
        if (cube != null) {
            cube.discard();
        }
        AnnihilationCubeEffect.clearCubeUUID(player);
    }

    private void applyChestplateCooldown(LivingEntity living) {
        ItemStack chestplate;
        Player player;
        if (living instanceof Player && !(player = (Player) living).level().isClientSide() && (chestplate = player.getItemBySlot(EquipmentSlot.CHEST)).getItem() == ModItem.ANNIHILATION_CHESTPLATE.get()) {
            player.getCooldowns().addCooldown(chestplate.getItem(), 200);
        }
    }

    @Nullable
    private static CubeOfAnnihilation getCubeEntity(Level level, Player player) {
        Entity entity;
        UUID cubeUUID = AnnihilationCubeEffect.getCubeUUID(player);
        if (cubeUUID == null) {
            return null;
        }
        if (level instanceof ServerLevel && (entity = ((ServerLevel) level).getEntity(cubeUUID)) instanceof CubeOfAnnihilation) {
            return (CubeOfAnnihilation) entity;
        }
        return null;
    }

    @Nullable
    private static UUID getCubeUUID(Player player) {
        CompoundTag tag = player.getPersistentData();
        if (tag.hasUUID(CUBE_UUID_KEY)) {
            return tag.getUUID(CUBE_UUID_KEY);
        }
        return null;
    }

    private static void setCubeUUID(Player player, CubeOfAnnihilation cube) {
        CompoundTag tag = player.getPersistentData();
        tag.putUUID(CUBE_UUID_KEY, cube.getUUID());
    }

    private static void clearCubeUUID(Player player) {
        CompoundTag tag = player.getPersistentData();
        tag.remove(CUBE_UUID_KEY);
    }

    public void cleanup(LivingEntity living) {
        Player player;
        if (living instanceof Player && !(player = (Player) living).level().isClientSide()) {
            this.removeCube(player);
            this.applyChestplateCooldown(living);
        }
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

