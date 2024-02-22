package quarri6343.openarpg.movement.playerai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import quarri6343.openarpg.FloatConfig;

public class PlayerMover {
    private final Player player;
    private int timeToRecalcPath;
    private final float stopDistance;
    private float oldWaterCost;
    private final float areaSize;

    /**
     * Constructs a goal allowing a player to follow others. The player must have Ground or Flying navigation.
     */
    public PlayerMover(Player player, float pStopDistance, float pAreaSize) {
        this.player = player;
        this.stopDistance = pStopDistance;
        this.areaSize = pAreaSize;
//        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        Vec3 destination = PlayerAIManager.getDestination();
        if (destination != null) {
//            this.player.lookAt(EntityAnchorArgument.Anchor.EYES, destination);
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                double d0 = this.player.getX() - destination.x;
                double d1 = this.player.getY() - destination.y;
                double d2 = this.player.getZ() - destination.z;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                if (!(d3 <= (double) (this.stopDistance * this.stopDistance))) {
                    PlayerAIManager.playerPathNavigation.moveTo(destination.x, destination.y, destination.z, FloatConfig.MOVEMENTSPEED.getValue());
                } else {
                    PlayerAIManager.playerPathNavigation.stop();
                }
            }
        }
    }

    protected int adjustedTickDelay(int pAdjustment) {
        return Mth.positiveCeilDiv(pAdjustment, 2);
    }
}
