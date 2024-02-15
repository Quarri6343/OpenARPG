package quarri6343.openarpg;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ThirdPersonEventHandler {
    private static EntityCamera cameraInstance;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            Minecraft.getInstance().setCameraEntity(Minecraft.getInstance().player);
        } else {
//                    Minecraft.getInstance().gameRenderer.getMainCamera().tick();
            cameraInstance.setOldPosAndRot();
            cameraInstance.setXRot(45);
            cameraInstance.setYRot(45);
            cameraInstance.setPosRaw(Minecraft.getInstance().player.getX() + 3, Minecraft.getInstance().player.getY() + 3, Minecraft.getInstance().player.getZ() - 3);

            if (Minecraft.getInstance().getCameraEntity() instanceof Player) {
                Minecraft.getInstance().setCameraEntity(cameraInstance);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        cameraInstance = OpenARPG.CAMERA.get().create(event.getEntity().level());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (cameraInstance != null) {
            cameraInstance.remove(Entity.RemovalReason.DISCARDED);
            cameraInstance = null;
        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }

        event.setCanceled(true);
        if (event.getButton() != GLFW_MOUSE_BUTTON_1) {
            return;
        }

        double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
        double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();

        Minecraft.getInstance().mouseHandler.releaseMouse();

        Vec3 hitPos = ProjectionUtil.mouseToWorldRay((int) xPos, (int) yPos, Minecraft.getInstance().getWindow().getScreenWidth(), Minecraft.getInstance().getWindow().getScreenHeight());


        BlockHitResult result = ProjectionUtil.rayTrace(hitPos, cameraInstance);
        if (result.getType() == HitResult.Type.MISS) {
            return;
        }

        Minecraft.getInstance().level.addParticle(ParticleTypes.EXPLOSION, result.getLocation().x, result.getLocation().y, result.getLocation().z, 0d, 0d, 0d);
        OpenARPG.setDestination(result.getLocation());
//            Minecraft.getInstance().player.setPos(result.getLocation().x, result.getLocation().y, result.getLocation().z);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent event) {
        //debug
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }

        double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
        double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();
        double d0 = xPos * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth();
        double d1 = yPos * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight();
        event.getGuiGraphics().fill((int) d0 - 5, (int) d1 - 5, (int) d0 + 5, (int) d1 + 5, 0xFFFFFFFF);
    }
}
