package show.twilight.finale.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-owned balance values.
 */
public final class HierophantConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue SHIELD_HEALTH, CHARGE_DAMAGE, SHARD_DAMAGE, GRID_DAMAGE, BEAM_DAMAGE, BEAM_RANGE;
    public static final ModConfigSpec.IntValue ARENA_RADIUS;
    public static final ModConfigSpec.BooleanValue BEAM_BREAKS_BLOCKS;

    static {
        var b = new ModConfigSpec.Builder();
        b.push("hierophant");
        SHIELD_HEALTH = b.comment("Independent shield HP; the body remains 100 HP.").defineInRange("shieldHealth", 100D, 1D, 100000D);
        CHARGE_DAMAGE = b.defineInRange("chargeDamage", 10D, 0D, 1000D);
        SHARD_DAMAGE = b.defineInRange("fragmentDamage", 6D, 0D, 1000D);
        GRID_DAMAGE = b.defineInRange("formationDamage", 8D, 0D, 1000D);
        BEAM_DAMAGE = b.comment("TF Spell Pack Annihilation base damage/range.").defineInRange("beamDamage", 10D, 0D, 1000D);
        BEAM_RANGE = b.defineInRange("beamRange", 8D, 2D, 64D);
        ARENA_RADIUS = b.comment("Fallback arena radius outside the castle forge/gazebo; saved per boss.").defineInRange("fallbackArenaRadius", 12, 4, 32);
        BEAM_BREAKS_BLOCKS = b.comment("Also requires mobGriefing; protects block entities, violet fields and blocks outside the arena.").define("beamBreaksBlocks", false);
        b.pop();
        SPEC = b.build();
    }

    private HierophantConfig() {
    }
}
