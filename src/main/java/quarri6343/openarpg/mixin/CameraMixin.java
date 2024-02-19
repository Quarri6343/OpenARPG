package quarri6343.openarpg.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import quarri6343.openarpg.Config;

@Mixin(Camera.class)
public class CameraMixin {
    
    /**
     * @author Quarri6343
     * @reason 独自にカメラを制御するため
     */
    @Overwrite
    private double getMaxZoom(double pStartingDistance) {
        return Config.getZoom();
    }
}
