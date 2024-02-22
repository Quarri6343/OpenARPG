package quarri6343.openarpg.movement.playerai;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarri6343.openarpg.OpenARPG;

//サブクラス同士が絡み合っているためわざとpackage privateにして対処
@Mod.EventBusSubscriber(modid = OpenARPG.MODID, value = Dist.CLIENT)
public class PlayerAIManager {
    static PlayerPathNavigation playerPathNavigation;
    static PlayerMoveControl playerMoveControl;
    static PlayerJumpControl playerJumpControl;
    static PlayerMover playerMover;

    //プレイヤーAIの移動対象地点
    private static Vec3 destination;


    public static void setDestination(Vec3 location) {
        destination = location;
    }

    public static Vec3 getDestination() {
        return destination;
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        playerPathNavigation = new PlayerGroundPathNavigation(Minecraft.getInstance().player, Minecraft.getInstance().player.level());
        playerJumpControl = new PlayerJumpControl(Minecraft.getInstance().player);
        playerMoveControl = new PlayerMoveControl(Minecraft.getInstance().player);
        playerMover = new PlayerMover(Minecraft.getInstance().player, 1, 0);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }

        playerMoveControl.tick();
        playerJumpControl.tick();
        playerPathNavigation.tick();
        playerMover.tick();
    }
}
