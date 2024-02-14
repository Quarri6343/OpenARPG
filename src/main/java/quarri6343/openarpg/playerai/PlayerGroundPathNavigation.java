package quarri6343.openarpg.playerai;


import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class PlayerGroundPathNavigation extends PlayerPathNavigation {
    private boolean avoidSun;

    public PlayerGroundPathNavigation(Player pMob, Level pLevel) {
        super(pMob, pLevel);
    }

    protected PlayerPathFinder createPathFinder(int pMaxVisitedNodes) {
        this.nodeEvaluator = new PlayerWalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PlayerPathFinder(this.nodeEvaluator, pMaxVisitedNodes);
    }

    /**
     * If on ground or swimming and can swim
     */
    protected boolean canUpdatePath() {
        return this.player.onGround() || this.isInLiquid() || this.player.isPassenger();
    }

    protected Vec3 getTempMobPos() {
        return new Vec3(this.player.getX(), (double) this.getSurfaceY(), this.player.getZ());
    }

    /**
     * Returns path to given BlockPos
     */
    public Path createPath(BlockPos pPos, int pAccuracy) {
        if (this.level.getBlockState(pPos).isAir()) {
            BlockPos blockpos;
            for (blockpos = pPos.below(); blockpos.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(blockpos).isAir(); blockpos = blockpos.below()) {
            }

            if (blockpos.getY() > this.level.getMinBuildHeight()) {
                return super.createPath(blockpos.above(), pAccuracy);
            }

            while (blockpos.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos).isAir()) {
                blockpos = blockpos.above();
            }

            pPos = blockpos;
        }

        if (!this.level.getBlockState(pPos).isSolid()) {
            return super.createPath(pPos, pAccuracy);
        } else {
            BlockPos blockpos1;
            for (blockpos1 = pPos.above(); blockpos1.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos1).isSolid(); blockpos1 = blockpos1.above()) {
            }

            return super.createPath(blockpos1, pAccuracy);
        }
    }

    /**
     * Returns a path to the given entity or null
     */
    public Path createPath(Entity pEntity, int pAccuracy) {
        return this.createPath(pEntity.blockPosition(), pAccuracy);
    }

    /**
     * Gets the safe pathing Y position for the entity depending on if it can path swim or not
     */
    private int getSurfaceY() {
        if (this.player.isInWater() && this.canFloat()) {
            int i = this.player.getBlockY();
            BlockState blockstate = this.level.getBlockState(BlockPos.containing(this.player.getX(), (double) i, this.player.getZ()));
            int j = 0;

            while (blockstate.is(Blocks.WATER)) {
                ++i;
                blockstate = this.level.getBlockState(BlockPos.containing(this.player.getX(), (double) i, this.player.getZ()));
                ++j;
                if (j > 16) {
                    return this.player.getBlockY();
                }
            }

            return i;
        } else {
            return Mth.floor(this.player.getY() + 0.5D);
        }
    }

    /**
     * Trims path data from the end to the first sun covered block
     */
    protected void trimPath() {
        super.trimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(BlockPos.containing(this.player.getX(), this.player.getY() + 0.5D, this.player.getZ()))) {
                return;
            }

            for (int i = 0; i < this.path.getNodeCount(); ++i) {
                Node node = this.path.getNode(i);
                if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
                    this.path.truncateNodes(i);
                    return;
                }
            }
        }

    }

    protected boolean hasValidPathType(BlockPathTypes pPathType) {
        if (pPathType == BlockPathTypes.WATER) {
            return false;
        } else if (pPathType == BlockPathTypes.LAVA) {
            return false;
        } else {
            return pPathType != BlockPathTypes.OPEN;
        }
    }

    public void setCanOpenDoors(boolean pCanOpenDoors) {
        this.nodeEvaluator.setCanOpenDoors(pCanOpenDoors);
    }

    public boolean canPassDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setCanPassDoors(boolean pCanPassDoors) {
        this.nodeEvaluator.setCanPassDoors(pCanPassDoors);
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setAvoidSun(boolean pAvoidSun) {
        this.avoidSun = pAvoidSun;
    }

    public void setCanWalkOverFences(boolean pCanWalkOverFences) {
        this.nodeEvaluator.setCanWalkOverFences(pCanWalkOverFences);
    }
}
