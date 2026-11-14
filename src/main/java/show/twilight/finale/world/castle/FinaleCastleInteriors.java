package show.twilight.finale.world.castle;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

final class FinaleCastleInteriors {
    private FinaleCastleInteriors() {
    }

    static void append(StructurePiecesBuilder pieces, StructureTemplateManager manager, long worldSeed) {

        for (StructurePiece piece : List.copyOf(pieces.pieces)) {
            if (piece instanceof FinalCastleLargeTowerComponent) {
                add(pieces, manager, piece, "parkour_large", 11, 1, 1, Mirror.FRONT_BACK);
            } else if (piece instanceof FinalCastleBellTower21Component) {
                add(pieces, manager, piece, "parkour_bell", 1, 9, 1, Mirror.NONE);
                add(pieces, manager, piece, "parkour_bell_top", 1, 57, 1, Mirror.NONE);
                add(pieces, manager, piece, "bell_entry_platform", 1, 8, 1, Mirror.NONE);
            } else if (piece instanceof FinalCastleDungeonForgeRoomComponent) {

                add(pieces, manager, piece, "forge_room_complete", 0, 0, 0, Mirror.NONE);
                pieces.addPiece(new FinalCastleForgeAccessComponent((FinalCastleDungeonForgeRoomComponent) piece));
            } else if (piece.getClass() == FinalCastleDungeonRoom31Component.class) {

                var b = piece.getBoundingBox();
                var random = RandomSource.create(worldSeed ^ b.minX() * 321534781L ^ b.minZ() * 756839L ^ b.minY() * 1099511628211L);
                int variant = random.nextInt(7);
                Rotation rotation = Rotation.values()[random.nextInt(4)];
                BlockPos center = local(piece, 15, -1, 15);
                pieces.addPiece(new FinalCastleRoomStructurePiece(center, variant, rotation, random.nextLong()));
            }
        }

        var spires = pieces.pieces.stream().filter(p -> p instanceof FinalCastleRoof13ConicalComponent).toList();
        if (spires.isEmpty())
            spires = pieces.pieces.stream().filter(p -> p instanceof FinalCastleRoof13PeakedComponent).toList();
        if (spires.isEmpty())
            throw new IllegalStateException("Final castle has no spire for its unique Finalslayer chest");
        var root = pieces.pieces.getFirst().getBoundingBox();
        var random = RandomSource.create(worldSeed ^ root.minX() * 341873128712L ^ root.minZ() * 132897987541L ^ 0x51A5EEDL);
        var roofPiece = spires.get(random.nextInt(spires.size()));
        var roof = roofPiece.getBoundingBox();
        var tower = pieces.pieces.stream().filter(p -> p instanceof FinalCastleMazeTower13Component)
                .filter(p -> p.getBoundingBox().minX() - 2 == roof.minX() && p.getBoundingBox().maxX() + 2 == roof.maxX()
                        && p.getBoundingBox().minZ() - 2 == roof.minZ() && p.getBoundingBox().maxZ() + 2 == roof.maxZ()
                        && p.getBoundingBox().maxY() - 1 == roof.minY()).findFirst().orElseThrow(
                        () -> new IllegalStateException("Missing owner of the Finalslayer spire"));

        int roofTop = roofPiece instanceof FinalCastleRoof13ConicalComponent cone
                ? roof.minY() + cone.slope * 7 + 2 : Math.max(roof.maxY(), roof.minY() + 19);
        int chestFloor = roof.minY() + (roofPiece instanceof FinalCastleRoof13ConicalComponent cone ? cone.slope * 4 : 12);
        pieces.addPiece(new FinalCastlePlainTowerPiece(tower.getBoundingBox(), roof, roofTop));

        BlockPos at = new BlockPos(roof.getCenter().getX() - 1, Math.min(chestFloor - 1, roofTop - 4), roof.getCenter().getZ() - 1);
        pieces.addPiece(new FinaleCastleTemplatePiece(manager, "spire_treasure", at, Rotation.NONE, Mirror.NONE));
    }

    private static void add(StructurePiecesBuilder pieces, StructureTemplateManager manager, StructurePiece owner,
                            String name, int x, int y, int z, Mirror mirror) {
        Rotation rotation = rotation(owner);
        var template = new FinaleCastleTemplatePiece(manager, name, local(owner, x, y, z), rotation, mirror);
        if (template.template().getSize().equals(Vec3i.ZERO))
            throw new IllegalStateException("Missing Finale castle template: " + name);
        pieces.addPiece(template);
    }

    static Rotation rotation(StructurePiece owner) {
        return switch (owner.getOrientation()) {
            case WEST -> Rotation.CLOCKWISE_90;
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    static BlockPos local(StructurePiece owner, int x, int y, int z) {
        var b = owner.getBoundingBox();
        return switch (owner.getOrientation()) {
            case WEST -> new BlockPos(b.maxX() - z, b.minY() + y, b.minZ() + x);
            case NORTH -> new BlockPos(b.maxX() - x, b.minY() + y, b.maxZ() - z);
            case EAST -> new BlockPos(b.minX() + z, b.minY() + y, b.maxZ() - x);
            default -> new BlockPos(b.minX() + x, b.minY() + y, b.minZ() + z);
        };
    }
}
