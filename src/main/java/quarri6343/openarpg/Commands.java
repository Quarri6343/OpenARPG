package quarri6343.openarpg;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import icyllis.modernui.mc.forge.MuiForgeApi;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import quarri6343.openarpg.ui.DebugSettingUI;
import quarri6343.openarpg.ui.ExampleSidedInventoryMenu;
import quarri6343.openarpg.ui.HUDManager;
import quarri6343.openarpg.ui.MonsterSummonUI;

import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class Commands {

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        //server command
        LiteralArgumentBuilder<CommandSourceStack> serverBuilder = net.minecraft.commands.Commands.literal("arpgserver")
                .then(net.minecraft.commands.Commands.literal("testcontainer").executes(context -> {
                    if(context.getSource().getEntity() instanceof ServerPlayer serverPlayer){
                        NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                                (containerId, playerInventory, player) -> new ExampleSidedInventoryMenu(containerId, playerInventory, OpenARPG.TEST_SERVER_STORAGE),
                                Component.translatable("menu.title.test")));
                    }
                    return Command.SINGLE_SUCCESS;
                }));
        event.getDispatcher().register(serverBuilder);

        if (!Dist.CLIENT.isClient()) {
            return;
        }

        LiteralArgumentBuilder<CommandSourceStack> builder = net.minecraft.commands.Commands.literal("arpg")
                .then(net.minecraft.commands.Commands.literal("settings").executes(context -> {
                    Minecraft.getInstance().execute(() -> {
                        MuiForgeApi.openScreen(new DebugSettingUI());
                    });
                    return Command.SINGLE_SUCCESS;
                }))
                .then(net.minecraft.commands.Commands.literal("summon").executes(context -> {
                    Minecraft.getInstance().execute(() -> {
                        MuiForgeApi.openScreen(new MonsterSummonUI());
                    });
                    return Command.SINGLE_SUCCESS;
                }))
                .then(net.minecraft.commands.Commands.literal("hud").executes(context -> {
                    Minecraft.getInstance().execute(() -> {
                        if (HUDManager.getHud() == null) {
                            HUDManager.initHUD();
                        } else {
                            HUDManager.removeHUD();
                        }
                    });
                    return Command.SINGLE_SUCCESS;
                }));
        // コマンドの登録
        event.getDispatcher().register(builder);
    }
}
