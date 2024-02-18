package quarri6343.openarpg.camera;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarri6343.openarpg.OpenARPG;
import quarri6343.openarpg.ProjectionUtil;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class CameraEventHandler {
    private static EntityCamera cameraInstance;
    
    private static final float XROT = 53.1301024f;
    private static final float YROT = 45f;
    
    private static float renderTickCount;

    @SubscribeEvent
    public static void onCameraRotate(ViewportEvent.ComputeCameraAngles event) {
        if(renderTickCount ++ % 60 == 0){
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                return;
            }

            if(Minecraft.getInstance().player == null || Minecraft.getInstance().level == null){
                return;
            }

            Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
            Player player = Minecraft.getInstance().player;
            int minX = Math.min(cam.getBlockPosition().getX(), player.getBlockX()) - 5;
            int maxX = Math.max(cam.getBlockPosition().getX(), player.getBlockX()) + 5;
            int minY = Math.min(cam.getBlockPosition().getY(), player.getBlockY()) - 5;
            int maxY = Math.max(cam.getBlockPosition().getY(), player.getBlockY()) + 5;
            int minZ = Math.min(cam.getBlockPosition().getZ(), player.getBlockZ()) - 5;
            int maxZ = Math.max(cam.getBlockPosition().getZ(), player.getBlockZ()) + 5;
            Minecraft.getInstance().levelRenderer.setBlocksDirty(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            Minecraft.getInstance().setCameraEntity(Minecraft.getInstance().player);
        } else {
            if(cameraInstance == null) //after logout
                return;
            
//                    Minecraft.getInstance().gameRenderer.getMainCamera().tick();
            cameraInstance.setOldPosAndRot();
            cameraInstance.setXRot(XROT);
            cameraInstance.setYRot(YROT);
            cameraInstance.setPosRaw(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ());

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