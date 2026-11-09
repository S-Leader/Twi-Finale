package show.twilight.finale.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-owned balance values.
 */
public final class HierophantConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue SHIELD_HEALTH, CHARGE_DAMAGE, SHARD_DAMAGE, GRID_DAMAGE, BEAM_DAMAGE, BEAM_RANGE, BEAM_LENGTH_MULTIPLIER;
    public static final ModConfigSpec.IntValue ARENA_RADIUS, SHIELD_STOP_SECONDS;
    public static final ModConfigSpec.BooleanValue BEAM_BREAKS_BLOCKS;

    static {
        var b = new ModConfigSpec.Builder();
        b.push("hierophant");
        SHIELD_HEALTH = b.comment("Alternating magic/physical shield HP; body remains 200 HP. Replaces the legacy 100 HP shield key.").defineInRange("alternatingShieldHealth", 50D, 1D, 100000D);
        SHIELD_STOP_SECONDS = b.comment("Shield-block stop/vulnerability window in seconds. Default 5; minimum 2, maximum 5.").defineInRange("shieldStopSeconds", 5, 2, 5);
        CHARGE_DAMAGE = b.defineInRange("chargeDamage", 10D, 0D, 1000D);
        SHARD_DAMAGE = b.defineInRange("fragmentDamage", 6D, 0D, 1000D);
        GRID_DAMAGE = b.defineInRange("formationDamage", 8D, 0D, 1000D);
        BEAM_DAMAGE = b.comment("Phase-two charged beam damage. Separate key from the old ten-damage spell.").defineInRange("phaseTwoBeamDamage", 30D, 0D, 1000D);
        BEAM_RANGE = b.defineInRange("beamRange", 8D, 2D, 64D);
        BEAM_LENGTH_MULTIPLIER = b.comment("Actual beam reach = beamRange * beamLengthMultiplier, capped at 64 blocks and clipped to the arena/loaded chunks. Also lengthens beams in existing configs with beamRange=8.").defineInRange("beamLengthMultiplier", 3D, 1D, 8D);
        ARENA_RADIUS = b.comment("Fallback arena radius outside the castle forge/gazebo; saved per boss.").defineInRange("fallbackArenaRadius", 12, 4, 32);
        BEAM_BREAKS_BLOCKS = b.comment("Also requires mobGriefing; protects block entities, violet fields and blocks outside the arena.").define("beamBreaksBlocks", false);
        b.pop();
        SPEC = b.build();
    }

    private HierophantConfig() {
    }
}
