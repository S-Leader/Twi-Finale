package show.twilight.finale.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import show.twilight.finale.init.ModBossVariant;
import twilightforest.block.entity.spawner.BossSpawnerBlockEntity;

public class FinaleBossSpawnerBlock extends BaseEntityBlock {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    private final ModBossVariant boss;

    public FinaleBossSpawnerBlock(BlockBehaviour.Properties props, ModBossVariant variant) {
        super(props);
        this.boss = variant;
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.boss.getType().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return FinaleBossSpawnerBlock.createTickerHelper(type, this.boss.getType(), BossSpawnerBlockEntity::tick);
    }

    public boolean canEntityDestroy(BlockState state, BlockGetter getter, BlockPos pos, Entity entity) {
        return state.getDestroySpeed(getter, pos) >= 0.0f;
    }
}

