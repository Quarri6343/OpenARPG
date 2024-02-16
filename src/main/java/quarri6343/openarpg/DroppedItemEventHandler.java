package quarri6343.openarpg;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        //debug
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }
        
        
        itemEntityList.forEach(entity -> {
            Vec3 screenPos = ProjectionUtil.worldToScreen(entity.getPosition(event.getPartialTick()));
            
            event.getGuiGraphics().fill((int) screenPos.x - 5, (int) screenPos.y - 5, (int) screenPos.x + 5, (int) screenPos.y + 5, 0xFF000000);
        });
        
        itemEntityList.clear();
    }
}
