package quarri6343.openarpg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quarri6343.openarpg.OpenARPG;

import java.lang.reflect.Field;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    
    //render HUD every tick
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lnet/minecraft/client/gui/GuiGraphics;)V", shift = At.Shift.BEFORE))
    public void render(float pPartialTicks, long pNanoTime, boolean pRenderLevel, CallbackInfo ci) {
        if(OpenARPG.getHud() != null){
            RenderBuffers renderBuffers;
            try {
                Field renderBuffersField = GameRenderer.class.getDeclaredField("renderBuffers");
                renderBuffersField.setAccessible(true);
                renderBuffers = (RenderBuffers) renderBuffersField.get(Minecraft.getInstance().gameRenderer);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            GuiGraphics guigraphics = new GuiGraphics(Minecraft.getInstance(), renderBuffers.bufferSource());
            int i = (int)(Minecraft.getInstance().mouseHandler.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getScreenWidth());
            int j = (int)(Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight());
            net.minecraftforge.client.ForgeHooksClient.drawScreen(OpenARPG.getHud(), guigraphics, i, j, Minecraft.getInstance().getDeltaFrameTime());   
        }
    }
}
