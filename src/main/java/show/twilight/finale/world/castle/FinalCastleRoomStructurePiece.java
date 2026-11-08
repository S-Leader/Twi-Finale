package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import show.twilight.finale.TwilightFinaleMod;

/**
 * Worldgen writes ONE load block, never a clipped slice of the furniture.
 * Actual LOAD runs on the server thread only after every room chunk is FULL.
 */
public final class FinalCastleRoomStructurePiece extends StructurePiece {
    static final String MARKER = "FinaleCompleteRoom";
    static final Vec3i ROOM_SIZE = new Vec3i(17, 9, 17);
    private final BlockPos center;
    private final int variant;
    private final Rotation rotation;
    private final long seed;

    public FinalCastleRoomStructurePiece(BlockPos center, int variant, Rotation rotation, long seed) {
        super(FinaleCastlePieces.ROOM_LOAD.get(), 10, new BoundingBox(
                center.getX() - 8, center.getY() - 1, center.getZ() - 8,
                center.getX() + 8, center.getY() + 8, center.getZ() + 8));
        this.center = center;
        this.variant = variant;
        this.rotation = rotation;
        this.seed = seed;
    }

    public FinalCastleRoomStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(FinaleCastlePieces.ROOM_LOAD.get(), tag);
        center = BlockPos.of(tag.getLong("RoomCenter"));
        variant = tag.getInt("RoomVariant");
        rotation = Rotation.valueOf(tag.getString("RoomRotation"));
        seed = tag.getLong("RoomSeed");
        if (variant < 0 || variant >= 7) throw new IllegalArgumentException("Invalid Finale room variant: " + variant);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putLong("RoomCenter", center.asLong());
        tag.putInt("RoomVariant", variant);
        tag.putString("RoomRotation", rotation.name());
        tag.putLong("RoomSeed", seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                            RandomSource random, BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        BlockPos marker = center.below();
        if (!clip.isInside(marker)) return;
        if (level.getBlockEntity(marker) instanceof StructureBlockEntity existing
                && existing.getPersistentData().contains(MARKER)) return;
        var original = level.getBlockState(marker);
        if (original.hasBlockEntity())
            throw new IllegalStateException("Room LOAD marker would overwrite a block entity at " + marker);
        var state = Blocks.STRUCTURE_BLOCK.defaultBlockState().setValue(StructureBlock.MODE, StructureMode.LOAD);
        level.setBlock(marker, state, 2);
        if (!(level.getBlockEntity(marker) instanceof StructureBlockEntity loader))
            throw new IllegalStateException("Missing room LOAD block entity at " + marker);
        BlockPos origin = switch (rotation) {
            case CLOCKWISE_90 -> center.offset(8, 0, -8);
            case CLOCKWISE_180 -> center.offset(8, 0, 8);
            case COUNTERCLOCKWISE_90 -> center.offset(-8, 0, 8);
            default -> center.offset(-8, 0, -8);
        };
        loader.setStructureName(TwilightFinaleMod.prefix("final_castle/basement_room_" + variant));
        loader.setStructurePos(origin.subtract(marker));
        loader.setStructureSize(ROOM_SIZE);
        loader.setRotation(rotation);
        loader.setMirror(Mirror.NONE);
        loader.setIntegrity(1);
        loader.setSeed(seed == 0 ? 1 : seed);
        loader.setIgnoreEntities(true);
        loader.setShowAir(false);
        loader.setShowBoundingBox(false);
        CompoundTag data = new CompoundTag();
        data.put("OriginalState", NbtUtils.writeBlockState(original));
        data.putLong("Center", center.asLong());
        data.putBoolean("Complete", false);
        loader.getPersistentData().put(MARKER, data);
        loader.setChanged();
    }
}
