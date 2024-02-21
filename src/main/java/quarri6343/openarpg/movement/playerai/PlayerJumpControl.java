package quarri6343.openarpg.movement.playerai;

import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.entity.player.Player;

public class PlayerJumpControl implements Control {
    private final Player mob;
    protected boolean jump;

    public PlayerJumpControl(Player pMob) {
        this.mob = pMob;
    }

    public void jump() {
        this.jump = true;
    }

    /**
     * Called to actually make the entity jump if isJumping is true.
     */
    public void tick() {
//        this.mob.setJumping(this.jump);
        if (this.jump) {
            this.mob.jumpFromGround();
            this.mob.setOnGround(false);
        }
        this.jump = false;
    }
}
