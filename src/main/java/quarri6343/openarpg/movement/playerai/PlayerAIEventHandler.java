package quarri6343.openarpg.movement.playerai;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarri6343.openarpg.OpenARPG;
import quarri6343.openarpg.movement.playerai.*;

//サブクラス同士が絡み合っているためわざとpackage privateにして対処
@Mod.EventBusSubscriber(modid = OpenARPG.MODID, value = Dist.CLIENT)
public class PlayerAIEventHandler {
    static PlayerPathNavigation playerPathNavigation;
    static PlayerMoveControl playerMoveControl;
    static PlayerJumpControl playerJumpControl;
    static PlayerMover playerMover;

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getEntity().equals(Minecraft.getInstance().player))
            return;

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
