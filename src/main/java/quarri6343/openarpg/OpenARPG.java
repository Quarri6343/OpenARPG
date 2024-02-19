package quarri6343.openarpg;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import icyllis.modernui.mc.forge.MuiForgeApi;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
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
import org.slf4j.Logger;
import quarri6343.openarpg.camera.EntityCamera;
import quarri6343.openarpg.ui.DebugSettingUI;
import quarri6343.openarpg.ui.MonsterSummonUI;

import static quarri6343.openarpg.CreativeTabInit.addToTab;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(OpenARPG.MODID)
public class OpenARPG {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "openarpg";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "OpenARPG" namespace

    private static MinecraftServer server;

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> STONE_SPEAR = addToTab(ITEMS.register("stone_spear", () -> new ItemStoneSpear(new Item.Properties().stacksTo(1))));

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<EntityCamera>> CAMERA = ENTITY_TYPES.register("camera", () -> EntityType.Builder.of(EntityCamera::new, MobCategory.MISC).build(MODID + ":camera"));

    //プレイヤーの移動対象地点
    private static Vec3 destination;

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

    public static void setDestination(Vec3 location) {
        destination = location;
    }

    public static Vec3 getDestination() {
        return destination;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Network.register();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static Entity getEntityById(int entityId) { //TODO: こういうのが溜まってきたらutilitiesに移行
        for (ServerLevel world : OpenARPG.getServer().getAllLevels()) {
            Entity entity = world.getEntity(entityId);
            if(entity != null) {
                return entity;
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        if(!Dist.CLIENT.isClient()){
            return;
        }

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("arpg")
                .then(Commands.literal("settings").executes(context -> {
                    Minecraft.getInstance().execute(()->{
                        MuiForgeApi.openScreen(new DebugSettingUI());
                    });
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("summon").executes(context -> {
                    Minecraft.getInstance().execute(()->{
                        MuiForgeApi.openScreen(new MonsterSummonUI());
                    });
                    return Command.SINGLE_SUCCESS;
                }));
        // コマンドの登録
        event.getDispatcher().register(builder);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
