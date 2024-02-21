package quarri6343.openarpg.ui;

import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.mc.MuiScreen;
import icyllis.modernui.mc.UIManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class HUDManager {

    private static Screen hud;


    public static Screen getHud(){
        return hud;
    }
    
    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event){
        if(Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK){
            initHUD();
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event){
        removeHUD();
    }
    
    public static void initHUD(){
        try {
            Field uiManagerField = UIManager.class.getDeclaredField("sInstance");
            uiManagerField.setAccessible(true);
            UIManager uiManager = (UIManager) uiManagerField.get(null);

            Class<?> c = Class.forName("icyllis.modernui.mc.forge.SimpleScreen");
            Constructor<?> constructor = c.getDeclaredConstructor(UIManager.class, Fragment.class);
            constructor.setAccessible(true);
            hud = (Screen) constructor.newInstance(uiManager, new HUD());
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        hud.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
    }
    
    public static void removeHUD(){
        if(hud == null){
            return;
        }

        try {
            Field uiManagerField = UIManager.class.getDeclaredField("sInstance");
            uiManagerField.setAccessible(true);
            UIManager uiManager = (UIManager) uiManagerField.get(null);

            Field mScreenField = UIManager.class.getDeclaredField("mScreen");
            mScreenField.setAccessible(true);
            MuiScreen currentScreen = (MuiScreen) mScreenField.get(uiManager);
            mScreenField.set(uiManager, hud);
            uiManager.removed();
            mScreenField.set(uiManager, currentScreen);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        hud = null;
    }
}
