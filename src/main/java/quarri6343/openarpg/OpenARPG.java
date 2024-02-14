package quarri6343.openarpg;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static quarri6343.openarpg.CreativeTabInit.addToTab;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(OpenARPG.MODID)
public class OpenARPG {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "openarpg";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "OpenARPG" namespace

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> STONE_SPEAR = addToTab(ITEMS.register("stone_spear", () -> new ItemStoneSpear(new Item.Properties().stacksTo(1))));

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<EntityCamera>> CAMERA = ENTITY_TYPES.register("camera", () -> EntityType.Builder.of(EntityCamera::new, MobCategory.MISC).build(MODID + ":camera"));
    
    private static EntityCamera cameraInstance;

    public static Matrix4f projectionMatrix;
    public static Matrix4f viewModelMatrix;
    
    public static Vec3 destination;
    public static PlayerMoveControl playerMoveControl;
    public static PlayerJumpControl playerJumpControl;
    public static PlayerPathNavigation playerPathNavigation;
    public static PlayerMover playerMover;
    
    public OpenARPG() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        CreativeTabInit.TABS.register(modEventBus);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientEventHandler {
        @SubscribeEvent
        public static void onCameraRotation(ViewportEvent.ComputeCameraAngles event) {
            projectionMatrix = event.getRenderer().getProjectionMatrix(Minecraft.getInstance().options.fov().get());

            if(Minecraft.getInstance().options.getCameraType().isFirstPerson()){
                return;
            }
//            event.setYaw(45);
//            event.setPitch(45);
        }
        
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event){
            if(Minecraft.getInstance().options.getCameraType().isFirstPerson()){
                Minecraft.getInstance().setCameraEntity(Minecraft.getInstance().player);
            }
            else{
//                    Minecraft.getInstance().gameRenderer.getMainCamera().tick();
                cameraInstance.setOldPosAndRot();
                cameraInstance.setXRot(45);
                cameraInstance.setYRot(45);
                cameraInstance.setPosRaw(Minecraft.getInstance().player.getX() + 3, Minecraft.getInstance().player.getY() + 3, Minecraft.getInstance().player.getZ() - 3);
                
                if(Minecraft.getInstance().getCameraEntity() instanceof Player){
                    Minecraft.getInstance().setCameraEntity(cameraInstance);
                }
            }
        }
        
        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
            cameraInstance = CAMERA.get().create(event.getEntity().level());
        }

        @SubscribeEvent
        public static void onPlayerLoginClient(EntityJoinLevelEvent event){
            if(Minecraft.getInstance().player == null)
                return;

            playerMoveControl = new PlayerMoveControl(Minecraft.getInstance().player);
            playerJumpControl = new PlayerJumpControl(Minecraft.getInstance().player);
            playerPathNavigation = new PlayerGroundPathNavigation(Minecraft.getInstance().player, Minecraft.getInstance().player.level());
            playerMover = new PlayerMover(Minecraft.getInstance().player, 100, 1, 0);
        }

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
            if(cameraInstance != null){
                cameraInstance.remove(Entity.RemovalReason.DISCARDED);
                cameraInstance = null;
            }
        }
        
        @SubscribeEvent
        public static void onMouseClick(InputEvent.MouseButton.Pre event){
            if(Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null){
                return;
            }
            
            event.setCanceled(true);
            if(event.getButton() != GLFW_MOUSE_BUTTON_1){
                return;
            }
            
            double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
            double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();
            
            Minecraft.getInstance().mouseHandler.releaseMouse();
            
            Vec3 hitPos = ProjectionUtil.mouseToWorldRay((int)xPos, (int)yPos, Minecraft.getInstance().getWindow().getScreenWidth(), Minecraft.getInstance().getWindow().getScreenHeight());
            
            
            BlockHitResult result = ProjectionUtil.rayTrace(hitPos, cameraInstance);
            if(result.getType() == HitResult.Type.MISS){
                return;
            }
            
            Minecraft.getInstance().level.addParticle(ParticleTypes.EXPLOSION, result.getLocation().x, result.getLocation().y, result.getLocation().z, 0d, 0d, 0d);
            destination = result.getLocation();
//            Minecraft.getInstance().player.setPos(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        }

        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent event) {
            //debug
            if(Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null){
                return;
            }
            
            double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
            double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();
            double d0 = xPos * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getScreenWidth();
            double d1 = yPos * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight();
            event.getGuiGraphics().fill((int)d0 - 5, (int)d1 - 5, (int)d0 + 5, (int)d1 + 5, 0xFFFFFFFF);
        }

        @SubscribeEvent
        public static void onRenderPlayer(RenderPlayerEvent.Post event){
            viewModelMatrix = event.getPoseStack().last().pose();
        }
        
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.ClientTickEvent event){
            if(Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null){
                return;
            }

            playerMoveControl.tick();
            playerJumpControl.tick();
            playerPathNavigation.tick();
            playerMover.tick();
        }
    }
    
    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            
        }
    }
}
