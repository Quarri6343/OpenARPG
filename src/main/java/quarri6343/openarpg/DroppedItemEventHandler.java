package quarri6343.openarpg;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class DroppedItemEventHandler {

    /**
     * クライアントの描画範囲内にあるアイテムのリスト
     */
    private static List<ItemEntity> renderedItemEntityList = new ArrayList<>();
    /**
     * クリック可能なドロップアイテムの検知範囲リスト
     */
    private static List<ClickableItemInfo> clickableItemInfoList = new ArrayList<>();
    
    @SubscribeEvent
    public static void onPlayerPickUp(EntityItemPickupEvent event) {
        event.setCanceled(true);
    }
    
    public static void onRenderItemEntity(ItemEntity entity){
        renderedItemEntityList.add(entity);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }
        if(renderedItemEntityList.size() == 0)
            return;
        
        clickableItemInfoList.clear();
        float scale = 0.5f;
        renderedItemEntityList.forEach(entity -> {
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

                int minXUnscaled = (int)screenPos.x - textWidth / 2;
                int minYUnscaled = (int)screenPos.y - textHeight / 2;
                int maxXUnscaled = (int)screenPos.x + textWidth / 2;
                int maxYUnscaled = (int)screenPos.y + textHeight / 2;
                clickableItemInfoList.add(new ClickableItemInfo(minXUnscaled, minYUnscaled, maxXUnscaled, maxYUnscaled, entity));
            }
        });
        renderedItemEntityList.clear();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }
        
        if (event.getButton() != GLFW_MOUSE_BUTTON_1) {
            return;
        }

        double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
        double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();
        double d0 = xPos * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth();
        double d1 = yPos * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight();
        
        for (ClickableItemInfo clickableItemInfo : clickableItemInfoList) {
            if(d0 > clickableItemInfo.minX() && d0 < clickableItemInfo.maxX()
                && d1 > clickableItemInfo.minY() && d1 < clickableItemInfo.maxY()){
                NetWork.sendToServer(new ItemPickUpPacket(clickableItemInfo.itemEntity()));

                event.setCanceled(true); //移動しない
                Minecraft.getInstance().mouseHandler.releaseMouse();
                return;
            }
        }
    }
}
