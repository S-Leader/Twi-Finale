package show.twilight.finale.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import show.twilight.finale.block.entity.TwilightTrapChestBlockEntity;
import show.twilight.finale.init.ModBlocks;

public class TwilightTrapChestBlock extends ChestBlock {
    public static final MapCodec<TwilightTrapChestBlock> CODEC = simpleCodec(TwilightTrapChestBlock::new);

    public TwilightTrapChestBlock(BlockBehaviour.Properties properties) {
        super(properties, ModBlocks.TWILIGHT_TRAP_CHEST_ENTITY::get);
    }

    @Override
    public MapCodec<? extends ChestBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TwilightTrapChestBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TwilightTrapChestBlockEntity chest)
            chest.open(player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlocks.TWILIGHT_TRAP_CHEST_ENTITY.get(), TwilightTrapChestBlockEntity::tick);
    }
}
