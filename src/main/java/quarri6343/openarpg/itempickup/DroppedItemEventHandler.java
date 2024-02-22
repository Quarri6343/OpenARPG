package quarri6343.openarpg.itempickup;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarri6343.openarpg.FloatConfig;
import quarri6343.openarpg.Network;
import quarri6343.openarpg.ProjectionUtil;

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

    public static void onRenderItemEntity(ItemEntity entity) {
        renderedItemEntityList.add(entity);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }
        if (renderedItemEntityList.size() == 0)
            return;

        clickableItemInfoList.clear();
        float scale = FloatConfig.DROPPEDITEMSCALE.getValue();
        renderedItemEntityList.forEach(entity -> {
            Vec3 screenPos = ProjectionUtil.worldToScreen(entity.getPosition(event.getPartialTick()));
            String itemName = entity.getItem().getDisplayName().getString();
            int textWidth = Minecraft.getInstance().font.width(itemName);
            int textHeight = Minecraft.getInstance().font.lineHeight;
            PoseStack pose = event.getGuiGraphics().pose();

            { //draw item text
                pose.pushPose();
                pose.scale(scale, scale, 1);
                int x = (int) (((int) screenPos.x - textWidth * scale / 2) / scale);
                int y = (int) (((int) screenPos.y - textHeight * scale / 2) / scale);
                event.getGuiGraphics().drawString(Minecraft.getInstance().font, itemName, x, y, 0xFF000000);
                pose.popPose();
            }
            { //draw item frame
                pose.pushPose();
                pose.scale(scale, scale, 1);
                int minX = (int) (((int) screenPos.x - textWidth * scale / 2) / scale);
                int minY = (int) (((int) screenPos.y - textHeight * scale / 2) / scale);
                int maxX = (int) (((int) screenPos.x + textWidth * scale / 2) / scale);
                int maxY = (int) (((int) screenPos.y + textHeight * scale / 2) / scale);
                event.getGuiGraphics().fill(minX - 3, minY - 3,
                        maxX + 3, maxY + 3, 0xFFFFFFFF);
                pose.popPose();
            }
            { //store info for handle click
                int minXUnscaled = (int) (((int) screenPos.x - textWidth * scale / 2));
                int minYUnscaled = (int) (((int) screenPos.y - textHeight * scale / 2));
                int maxXUnscaled = (int) ((int) screenPos.x + textWidth * scale / 2);
                int maxYUnscaled = (int) ((int) screenPos.y + textHeight * scale / 2);
                clickableItemInfoList.add(new ClickableItemInfo(minXUnscaled - 3, minYUnscaled - 3, maxXUnscaled + 3, maxYUnscaled + 3, entity));
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

        if(tryClickItem()){
            event.setCanceled(true); //移動しない
        }
    }
    
    private static boolean tryClickItem(){
        double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
        double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();
        double d0 = xPos * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth();
        double d1 = yPos * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight();

        for (ClickableItemInfo clickableItemInfo : clickableItemInfoList) {
            if (!(d0 > clickableItemInfo.minX() && d0 < clickableItemInfo.maxX()
                    && d1 > clickableItemInfo.minY() && d1 < clickableItemInfo.maxY())) {
                continue;
            }

            if (Minecraft.getInstance().player.position().distanceToSqr(clickableItemInfo.itemEntity().position()) > FloatConfig.PICKUPRANGE.getValue()) {
                continue;
            }

            Network.sendToServer(new ItemPickUpPacket(clickableItemInfo.itemEntity()));
            
            Minecraft.getInstance().mouseHandler.releaseMouse();
            return true;
        }
        
        return false;
    }
}
