package quarri6343.openarpg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quarri6343.openarpg.ui.HUDManager;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    //ForgeのScreenEvent.ClosingにnewScreenのパラメータがない
    @Inject(method = "setScreen", at = @At(value = "TAIL"))
    public void setScreen(Screen pGuiScreen, CallbackInfo ci) {
        HUDManager.onCloseScreen(pGuiScreen); //TODO: Post Event
    }
}
