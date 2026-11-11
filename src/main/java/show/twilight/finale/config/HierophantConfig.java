package show.twilight.finale.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HierophantConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue ARENA_RADIUS;

    static {
        var b = new ModConfigSpec.Builder();
        b.comment("twilightforest_finale.configuration.hierophant.tooltip")
                .translation("twilightforest_finale.configuration.hierophant")
                .push("hierophant");
        ARENA_RADIUS = b.comment("twilightforest_finale.configuration.hierophant.fallbackArenaRadius.tooltip")
                .translation("twilightforest_finale.configuration.hierophant.fallbackArenaRadius")
                .defineInRange("fallbackArenaRadius", 12, 4, 32);
        b.pop();
        SPEC = b.build();
    }

    private HierophantConfig() {
    }
}
