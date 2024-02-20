package quarri6343.openarpg.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    @Shadow
    public Input input;
    @Shadow
    public float yBob;
    @Shadow
    public float xBob;
    @Shadow
    public float yBobO;
    @Shadow
    public float xBobO;
    
    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    /**
     * @author Quarri6343
     * @reason 3人称カメラの時にサーバーにパケットが送られないのを修正
     */
    @Overwrite
    protected boolean isControlledCamera() {
        return true;
    }

    /**
     * @author Quarri6343
     * @reason 3人称カメラの時にWASD入力を無効化する //TODO: プレイヤーが見ている方向ではなくカメラの方向に応じた前後左右移動をする
     */
    @Overwrite
    public void serverAiStep() {
        super.serverAiStep();
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            this.xxa = this.input.leftImpulse;
            this.zza = this.input.forwardImpulse;
            this.jumping = this.input.jumping;
            this.yBobO = this.yBob;
            this.xBobO = this.xBob;
            this.xBob += (this.getXRot() - this.xBob) * 0.5F;
            this.yBob += (this.getYRot() - this.yBob) * 0.5F;
        }
    }
}
