package show.twilight.finale.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;

public class DestroyArea {

    final BoundingBox destroyBox;

    public DestroyArea(BoundingBox tower, RandomSource rand, int y) {

        int bx = tower.minX() - 2 + rand.nextInt(tower.getXSpan());
        int bz = tower.minZ() - 2 + rand.nextInt(tower.getZSpan());

        this.destroyBox = new BoundingBox(bx, y - 10, bz, bx + 4, y, bz + 4);
    }

    public boolean isEntirelyAbove(int y) {
        return this.destroyBox.minY() > y;
    }

    public boolean isVecInside(BlockPos pos) {
        return destroyBox.isInside(pos);
    }

    public static DestroyArea createNonIntersecting(BoundingBox tower, RandomSource rand, int y, ArrayList<DestroyArea> otherAreas) {
        int attempts = 100;

        DestroyArea area = null;
        for (int i = 0; i < attempts && area == null; i++) {
            DestroyArea testArea = new DestroyArea(tower, rand, y);

            if (otherAreas.size() == 0) {
                area = testArea;
            } else {
                for (DestroyArea otherArea : otherAreas) {
                    if (otherArea == null || !testArea.intersectsWith(otherArea)) {
                        area = testArea;
                    }
                }
            }
        }
        return area;
    }

    private boolean intersectsWith(DestroyArea otherArea) {
        return this.destroyBox.intersects(otherArea.destroyBox.maxX() + 1, otherArea.destroyBox.minX() - 1, otherArea.destroyBox.maxZ() + 1, otherArea.destroyBox.minZ() - 1);
    }
}
