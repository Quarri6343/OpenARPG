package quarri6343.openarpg.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarri6343.openarpg.Network;
import quarri6343.openarpg.ProjectionUtil;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClickToAttackEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }

        if (event.getButton() != GLFW_MOUSE_BUTTON_1) {
            return;
        }

        double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
        double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();

        Minecraft.getInstance().mouseHandler.releaseMouse();

        Vec3 hitVec = ProjectionUtil.mouseToWorldRay((int) xPos, (int) yPos, Minecraft.getInstance().getWindow().getScreenWidth(), Minecraft.getInstance().getWindow().getScreenHeight());

        EntityHitResult entityHitResult = ProjectionUtil.rayTraceEntity(hitVec);
        if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
            if (entityHitResult.getEntity().distanceToSqr(Minecraft.getInstance().player) >= Mth.square(Minecraft.getInstance().player.getEntityReach())) { //TODO:武器ごとに射程を変える
                return;
            }

            Network.sendToServer(new PlayerAttackPacket(entityHitResult.getEntity()));
            event.setCanceled(true);
        }
    }
}
