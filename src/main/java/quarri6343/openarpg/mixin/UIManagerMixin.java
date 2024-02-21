package quarri6343.openarpg.mixin;

import com.mojang.blaze3d.platform.Window;
import icyllis.modernui.fragment.FragmentContainerView;
import icyllis.modernui.fragment.FragmentController;
import icyllis.modernui.fragment.FragmentTransaction;
import icyllis.modernui.mc.MuiScreen;
import icyllis.modernui.mc.UIManager;
import icyllis.modernui.view.ViewRoot;
import org.apache.logging.log4j.Marker;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.*;
import quarri6343.openarpg.ui.HUD;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

import static icyllis.modernui.ModernUI.LOGGER;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;

@Mixin(value = UIManager.class, remap = false)
public class UIManagerMixin {
    @Shadow
    protected volatile MuiScreen mScreen;

    @Final
    @Mutable
    @Shadow
    protected static Marker MARKER;

    @Shadow
    protected volatile FragmentController mFragmentController;

    @Final
    @Mutable
    @Shadow
    protected static int fragment_container;

    @Final
    @Mutable
    @Shadow
    protected Window mWindow;

    @Shadow
    private FragmentContainerView mFragmentContainerView;

    /**
     * @author Quarri6343
     * @reason for regsitering HUD without setting it as current screen
     */
    @Overwrite
    public void initScreen(@Nonnull MuiScreen screen) {
        ViewRoot mRoot = null;
        try {
            Field mRootField = UIManager.class.getDeclaredField("mRoot");
            mRootField.setAccessible(true);
            mRoot = (ViewRoot) mRootField.get(UIManager.getInstance());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (mScreen != screen) {
            if (mScreen != null) {
                LOGGER.warn(MARKER, "You cannot set multiple screens.");
                removed();
            }
            mRoot.mHandler.post(this::suppressLayoutTransition);
            mFragmentController.getFragmentManager().beginTransaction()
                    .add(fragment_container, screen.getFragment(), "main")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
            mRoot.mHandler.post(this::restoreLayoutTransition);
        }
        if (!(screen.getFragment() instanceof HUD)) {
            mScreen = screen;
        }
        // ensure it's resized
        resize();
    }


    /**
     * @author Quarri6343
     * @reason prevent HUD being removed when the current screen removal by
     */
    @Overwrite
    public void removed() {
        ViewRoot mRoot = null;
        try {
            Field mRootField = UIManager.class.getDeclaredField("mRoot");
            mRootField.setAccessible(true);
            mRoot = (ViewRoot) mRootField.get(UIManager.getInstance());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        MuiScreen screen = mScreen;
        if (screen == null) {
            return;
        }
        mRoot.mHandler.post(this::suppressLayoutTransition);
        mFragmentController.getFragmentManager().beginTransaction()
                .remove(screen.getFragment())
//                .runOnCommit(() -> mFragmentContainerView.removeAllViews())
                .runOnCommit(() -> mFragmentContainerView.removeView(screen.getFragment().getView()))
                .commit();
        mRoot.mHandler.post(this::restoreLayoutTransition);
        mScreen = null;
        glfwSetCursor(mWindow.getWindow(), MemoryUtil.NULL);
    }

    @Shadow
    void resize() {
    }

    @Shadow
    void suppressLayoutTransition() {
    }

    @Shadow
    void restoreLayoutTransition() {
    }
}
