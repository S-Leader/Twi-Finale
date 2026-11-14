package show.twilight.finale.world.castle;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

final class CastleGenerationBounds {
    static final int REFERENCE_RADIUS = 8;

    private CastleGenerationBounds() {
    }

    static boolean inReferenceWindow(ChunkPos start, BoundingBox box) {
        return (box.minX() >> 4) >= start.x - REFERENCE_RADIUS && (box.maxX() >> 4) <= start.x + REFERENCE_RADIUS
                && (box.minZ() >> 4) >= start.z - REFERENCE_RADIUS && (box.maxZ() >> 4) <= start.z + REFERENCE_RADIUS;
    }

    static boolean allows(StructurePieceAccessor pieces, StructurePiece candidate) {
        if (pieces instanceof StructurePiecesBuilder builder && !builder.pieces.isEmpty()
                && builder.pieces.getFirst() instanceof FinaleCastleMainComponent main)
            return main.allowsDungeonPiece(candidate.getBoundingBox());
        return true;
    }
}
