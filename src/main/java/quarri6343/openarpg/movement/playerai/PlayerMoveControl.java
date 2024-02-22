package quarri6343.openarpg.movement.playerai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PlayerMoveControl implements Control {
    public static final float MIN_SPEED = 5.0E-4F;
    public static final float MIN_SPEED_SQR = 2.5000003E-7F;
    protected static final int MAX_TURN = 90;
    protected final Player player;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected double speedModifier;
    protected float strafeForwards;
    protected float strafeRight;
    protected Operation operation = Operation.WAIT;

    public PlayerMoveControl(Player pMob) {
        this.player = pMob;
    }

    /**
     * @return If the player is currently trying to go somewhere
     */
    public boolean hasWanted() {
        return this.operation == Operation.MOVE_TO;
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    /**
     * Sets the speed and location to move to
     */
    public void setWantedPosition(double pX, double pY, double pZ, double pSpeed) {
        this.wantedX = pX;
        this.wantedY = pY;
        this.wantedZ = pZ;
        this.speedModifier = pSpeed;
        if (this.operation != Operation.JUMPING) {
            this.operation = Operation.MOVE_TO;
        }

    }

    public void strafe(float pForward, float pStrafe) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = pForward;
        this.strafeRight = pStrafe;
        this.speedModifier = 0.25D;
    }

    public void tick() {
        if (this.operation == Operation.STRAFE) {
            float f = (float) this.player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float) this.speedModifier * f;
            float f2 = this.strafeForwards;
            float f3 = this.strafeRight;
            float f4 = Mth.sqrt(f2 * f2 + f3 * f3);
            if (f4 < 1.0F) {
                f4 = 1.0F;
            }

            f4 = f1 / f4;
            f2 *= f4;
            f3 *= f4;
            float f5 = Mth.sin(this.player.getYRot() * ((float) Math.PI / 180F));
            float f6 = Mth.cos(this.player.getYRot() * ((float) Math.PI / 180F));
            float f7 = f2 * f6 - f3 * f5;
            float f8 = f3 * f6 + f2 * f5;
            if (!this.isWalkable(f7, f8)) {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
            }

            this.player.setSpeed(f1);
            this.player.zza = this.strafeForwards;
            this.player.xxa = this.strafeRight;
            this.operation = Operation.WAIT;
        } else if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double d0 = this.wantedX - this.player.getX();
            double d1 = this.wantedZ - this.player.getZ();
            double d2 = this.wantedY - this.player.getY();
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            if (d3 < (double) 2.5000003E-7F) {
                this.player.zza = 0.0f;
                return;
            }

            float f9 = (float) (Mth.atan2(d1, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
            this.player.setYRot(this.rotlerp(this.player.getYRot(), f9, 90.0F));
            this.player.setSpeed((float) this.speedModifier);
            //追加
            this.player.zza = (float) this.speedModifier;
            this.player.setXRot(0);
            //
            BlockPos blockpos = this.player.blockPosition();
            BlockState blockstate = this.player.level().getBlockState(blockpos);
            VoxelShape voxelshape = blockstate.getCollisionShape(this.player.level(), blockpos);
            if (d2 > (double) this.player.getStepHeight() && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.player.getBbWidth()) || !voxelshape.isEmpty() && this.player.getY() < voxelshape.max(Direction.Axis.Y) + (double) blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
                PlayerAIManager.playerJumpControl.jump();
                this.operation = Operation.JUMPING;
            }
        } else if (this.operation == Operation.JUMPING) {
            this.player.setSpeed((float) this.speedModifier);
            //追加
            this.player.zza = (float) this.speedModifier;
            this.player.setXRot(0);
            //
            if (this.player.onGround()) {
                this.operation = Operation.WAIT;
            }
        } else {
            this.player.zza = 0.0f;
        }

    }

    /**
     * @return true if the player can walk successfully to a given X and Z
     */
    private boolean isWalkable(float pRelativeX, float pRelativeZ) {
        PlayerPathNavigation pathnavigation = PlayerAIManager.playerPathNavigation;
        if (pathnavigation != null) {
            PlayerNodeEvaluator nodeevaluator = pathnavigation.getNodeEvaluator();
            if (nodeevaluator != null && nodeevaluator.getBlockPathType(this.player.level(), Mth.floor(this.player.getX() + (double) pRelativeX), this.player.getBlockY(), Mth.floor(this.player.getZ() + (double) pRelativeZ)) != BlockPathTypes.WALKABLE) {
                return false;
            }
        }

        return true;
    }

    /**
     * Attempt to rotate the first angle to become the second angle, but only allow overall direction change to at max be
     * third parameter
     */
    protected float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
        float f = Mth.wrapDegrees(pTargetAngle - pSourceAngle);
        if (f > pMaximumChange) {
            f = pMaximumChange;
        }

        if (f < -pMaximumChange) {
            f = -pMaximumChange;
        }

        float f1 = pSourceAngle + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    protected static enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;
    }
}
