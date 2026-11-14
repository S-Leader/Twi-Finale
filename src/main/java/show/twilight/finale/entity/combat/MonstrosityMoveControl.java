package show.twilight.finale.entity.combat;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
import show.twilight.finale.entity.CastleMonstrosityEntity;

public final class MonstrosityMoveControl extends MoveControl {
    private final CastleMonstrosityEntity beast;
    private boolean holdingRange;

    public MonstrosityMoveControl(CastleMonstrosityEntity beast) {
        super(beast);
        this.beast = beast;
    }

    @Override
    public void tick() {
        if (!beast.isAlive() || beast.getAction() != CastleMonstrosityEntity.CHASE || beast.getTarget() == null) {
            operation = Operation.WAIT;
            holdingRange = false;
            stop();
            return;
        }
        Vec3 toTarget = beast.getTarget().position().subtract(beast.position());
        double range = Math.max(4.2, (beast.getBbWidth() + beast.getTarget().getBbWidth()) * 0.5 + 0.65);

        double stopRange = range + (holdingRange ? 1 : 0);
        holdingRange = toTarget.horizontalDistanceSqr() < stopRange * stopRange && Math.abs(toTarget.y) < 4;
        if (holdingRange || beast.isPreparingAttack()) {
            beast.getNavigation().stop();
            operation = Operation.WAIT;
            if (toTarget.horizontalDistanceSqr() > 0.04) turn(toTarget.x, toTarget.z);
            stop();
            return;
        }
        if (operation == Operation.JUMPING) {
            super.tick();
            alignBody();
            return;
        }
        if (operation != Operation.MOVE_TO) {
            stop();
            return;
        }
        operation = Operation.WAIT;
        double dx = wantedX - beast.getX(), dz = wantedZ - beast.getZ(), dy = wantedY - beast.getY();

        if (dx * dx + dz * dz < 0.04) {
            stop();
            return;
        }
        float wanted = turn(dx, dz);
        float remaining = Math.abs(Mth.wrapDegrees(wanted - beast.getYRot()));
        if (remaining > 30) stop();
        else {
            beast.setSpeed((float) (speedModifier * beast.getAttributeValue(Attributes.MOVEMENT_SPEED)) * Mth.cos(remaining * Mth.DEG_TO_RAD));
            beast.setXxa(0);

            Vec3 forward = Vec3.directionFromRotation(0, beast.getYRot());
            double speed = Math.max(0, beast.getDeltaMovement().dot(forward));
            beast.setDeltaMovement(forward.x * speed, beast.getDeltaMovement().y, forward.z * speed);
            if (dy > beast.maxUpStep() && dx * dx + dz * dz < Math.max(1, beast.getBbWidth()) && beast.onGround()) {
                beast.getJumpControl().jump();
                operation = Operation.JUMPING;
            }
        }
        alignBody();
    }

    private float turn(double dx, double dz) {
        float wanted = (float) (Mth.atan2(dz, dx) * 180 / Math.PI) - 90;

        beast.setYRot(Mth.wrapDegrees(Mth.approachDegrees(beast.getYRot(), wanted, 8)));
        alignBody();
        return wanted;
    }

    private void alignBody() {
        beast.setYBodyRot(beast.getYRot());
        beast.setYHeadRot(beast.getYRot());
    }

    private void stop() {
        beast.setSpeed(0);
        beast.setXxa(0);
        beast.setZza(0);
        beast.setDeltaMovement(0, beast.getDeltaMovement().y, 0);
    }
}
