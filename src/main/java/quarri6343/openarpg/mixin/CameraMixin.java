package quarri6343.openarpg.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Camera.class)
public class CameraMixin {

    /**
     * 3人称カメラがどれだけプレイヤーから引くか
     */
    private static final float ZOOM = 12f;
    
    /**
     * @author Quarri6343
     * @reason 独自にカメラを制御するため
     */
    @Overwrite
    private double getMaxZoom(double pStartingDistance) {
        return ZOOM;
    }
}
