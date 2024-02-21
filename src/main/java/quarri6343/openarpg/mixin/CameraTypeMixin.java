package quarri6343.openarpg.mixin;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import quarri6343.openarpg.ui.HUDManager;

@Mixin(CameraType.class)
public class CameraTypeMixin {

    /**
     * @author Quarri6343
     * @reason Third Person(Front)を無効化 & イベントフック
     */
    @Overwrite
    public CameraType cycle() {
        CameraType newType = Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON;

        if (newType == CameraType.THIRD_PERSON_BACK) {
            HUDManager.initHUD();
        } else {
            HUDManager.removeHUD();
        }

        return newType;
    }
}
