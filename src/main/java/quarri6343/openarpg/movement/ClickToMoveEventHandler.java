package quarri6343.openarpg.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarri6343.openarpg.ProjectionUtil;
import quarri6343.openarpg.movement.playerai.PlayerAIManager;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClickToMoveEventHandler {

    //クリック時にアイテム拾得や攻撃を優先的に行うため、優先度最低
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }

        event.setCanceled(true);
        if (event.getButton() != GLFW_MOUSE_BUTTON_1) {
            return;
        }

        tryMove();
    }

    private static void tryMove() {

        double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
        double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();

        Minecraft.getInstance().mouseHandler.releaseMouse();

        Vec3 hitVec = ProjectionUtil.mouseToWorldRay((int) xPos, (int) yPos, Minecraft.getInstance().getWindow().getScreenWidth(), Minecraft.getInstance().getWindow().getScreenHeight());

        BlockHitResult result = ProjectionUtil.rayTraceBlock(hitVec);
        if (result.getType() == HitResult.Type.MISS) {
            return;
        }

        Minecraft.getInstance().level.addParticle(ParticleTypes.EXPLOSION, result.getLocation().x, result.getLocation().y, result.getLocation().z, 0d, 0d, 0d);
        PlayerAIManager.setDestination(result.getLocation());
    }
}
