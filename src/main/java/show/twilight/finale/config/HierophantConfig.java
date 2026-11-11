package show.twilight.finale.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HierophantConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue ARENA_RADIUS;

    static {
        var b = new ModConfigSpec.Builder();
        b.push("hierophant");
        ARENA_RADIUS = b.comment("Fallback square half-width for manual summons outside a known castle. Castle encounters use the actual main-tower roof structure bounds, not this value or a circle.", "非城堡内手动召唤时的后备方形场地半径；城堡战斗使用实际塔顶结构边界。").defineInRange("fallbackArenaRadius", 12, 4, 32);
        b.pop();
        SPEC = b.build();
    }

    private HierophantConfig() {
    }
}
