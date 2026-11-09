package show.twilight.finale.init;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;

import java.util.Locale;
import java.util.function.Supplier;

public enum ModBossVariant implements StringRepresentable {
    ADHERENT(null, () -> ModBlocks.ADHERENT_SPAWNER.get()),
    HIEROPHANT(null, () -> ModBlocks.TWILIGHT_HIEROPHANT_SPAWNER.get());

    private final TrophyType trophyType;
    private final Supplier<BlockEntityType<? extends BossSpawnerBlockEntity<?>>> blockEntityType;

    private ModBossVariant(TrophyType trophyType, Supplier<BlockEntityType<? extends BossSpawnerBlockEntity<?>>> blockEntityType) {
        this.trophyType = trophyType;
        this.blockEntityType = blockEntityType;
    }

    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public TrophyType getTrophyType() {
        return this.trophyType;
    }

    @Nullable
    public BlockEntityType<? extends BossSpawnerBlockEntity<?>> getType() {
        return this.blockEntityType != null ? this.blockEntityType.get() : null;
    }

    public static enum TrophyType {
        GOLD("trophy"),
        IRON("trophy_minor"),
        IRONWOOD("trophy_quest");

        private final String modelName;

        private TrophyType(String modelName) {
            this.modelName = modelName;
        }
    }
}

