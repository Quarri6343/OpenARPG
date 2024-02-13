package quarri6343.openarpg;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
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
import org.joml.Vector3f;
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
            if(Minecraft.getInstance().options.getCameraType().isFirstPerson()){
                Minecraft.getInstance().setCameraEntity(Minecraft.getInstance().player);
            }
            else{
                if(Minecraft.getInstance().getCameraEntity() instanceof Player){
                    cameraInstance.setXRot(45);
                    cameraInstance.setYRot(45);
                    Vec3 eyePos = Minecraft.getInstance().getCameraEntity().getEyePosition();
                    event.getCamera().tick();
                    cameraInstance.setPosRaw(eyePos.x, eyePos.y, eyePos.z);
                    cameraInstance.setOldPosAndRot();
                    Minecraft.getInstance().setCameraEntity(cameraInstance);
                }
            }
        }
        
        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
            cameraInstance = CAMERA.get().create(event.getEntity().level());
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
            
            int xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
            int yPos = (int) (Minecraft.getInstance().getWindow().getScreenHeight() - Minecraft.getInstance().mouseHandler.ypos());
            
            Vector3f hitPos = ProjectionUtil.unProject(xPos, yPos);
            BlockHitResult result = ProjectionUtil.rayTrace(hitPos, cameraInstance);
            if(result.getType() == HitResult.Type.MISS){
                return;
            }
            
            LogUtils.getLogger().debug(result.getBlockPos().getX() + ":" + (result.getBlockPos().getY() + 1) + ":" + result.getBlockPos().getZ());
            Minecraft.getInstance().level.addParticle(ParticleTypes.EXPLOSION, result.getBlockPos().getX(), result.getBlockPos().getY() + 1, result.getBlockPos().getZ(), 0d, 0d, 0d);
        }

        @SubscribeEvent
        public static void onRenderWorld(RenderLevelStageEvent event) {
            if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL){
                projectionMatrix = event.getProjectionMatrix();
            }
        }

        @SubscribeEvent
        public static void onRenderPlayer(RenderPlayerEvent.Post event){
            viewModelMatrix = event.getPoseStack().last().pose();
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
