package quarri6343.openarpg;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import icyllis.modernui.mc.forge.MuiForgeApi;
import icyllis.modernui.mc.text.TextLayoutEngine;
import icyllis.modernui.mc.text.mixin.MixinFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class DroppedItemEventHandler {

    private static List<ItemEntity> itemEntityList = new ArrayList<>();
    
    @SubscribeEvent
    public static void onPlayerPickUp(EntityItemPickupEvent event) {
        event.setCanceled(true);
    }
    
    public static void onRenderItemEntity(ItemEntity entity){
        itemEntityList.add(entity);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }
        
        float scale = 0.5f;
        itemEntityList.forEach(entity -> {
            Vec3 screenPos = ProjectionUtil.worldToScreen(entity.getPosition(event.getPartialTick()));
            String itemName = entity.getItem().getDisplayName().getString();
            int textWidth = Minecraft.getInstance().font.width(itemName);
            int textHeight = Minecraft.getInstance().font.lineHeight;
            PoseStack pose = event.getGuiGraphics().pose();
            
            {
                pose.pushPose();
                pose.scale(scale, scale, 1);
                int x = (int) (((int)screenPos.x - textWidth * scale / 2 ) / scale);
                int y = (int) (((int)screenPos.y - textHeight * scale / 2 ) / scale);
                event.getGuiGraphics().drawString(Minecraft.getInstance().font, itemName, x, y, 0xFF000000);
                pose.popPose();
            }
            {
                pose.pushPose();
                pose.scale(scale, scale, 1);
                int minX = (int) (((int)screenPos.x - textWidth * scale / 2 ) / scale);
                int minY = (int) (((int)screenPos.y - textHeight * scale / 2 ) / scale);
                int maxX = (int) (((int)screenPos.x + textWidth * scale / 2 ) / scale);
                int maxY = (int) (((int)screenPos.y + textHeight * scale / 2 ) / scale);
                event.getGuiGraphics().fill(minX - 3, minY - 3,
                        maxX + 3, maxY + 3, 0xFFFFFFFF);
                pose.popPose();
            }
        });
        itemEntityList.clear();
    }
}
