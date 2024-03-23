package quarri6343.openarpg.mixin.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    /**
     * 3人称で画面を閉じた時画面中央にカーソルを移動させない
     */
    @Inject(method = "grabMouse", at = @At(value = "HEAD"), cancellable = true)
    public void grabMouse(CallbackInfo ci) {
        if(!Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            ci.cancel();
        }
    }
}
