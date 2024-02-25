package quarri6343.openarpg.ui.fragment;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.AbsoluteLayout;
import net.minecraft.client.Minecraft;
import quarri6343.openarpg.ui.ExampleSidedInventoryMenu;
import quarri6343.openarpg.ui.widget.*;

public class ExampleSidedInventoryFragment extends Fragment implements ScreenCallback {

    private final ExampleSidedInventoryMenu menu;
    private ContainerMenuViewFullImplementation containerMenuView;

    public ExampleSidedInventoryFragment(ExampleSidedInventoryMenu menu) {
        this.menu = menu;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @icyllis.modernui.annotation.Nullable ViewGroup container,
                             @icyllis.modernui.annotation.Nullable DataSet savedInstanceState) {
        var root = new AbsoluteLayout(requireContext());
        root.setLayoutParams(new AbsoluteLayout.LayoutParams(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), 0, 0));

        FloatingItem floatingItem = new FloatingItem(requireContext());
        MUITooltip muiTooltip = new MUITooltip(requireContext());

        int guiWidth = 176; // vanilla chest size
        int guiHeight = 166;
        int screenWidth = (int) (guiWidth * (double) Minecraft.getInstance().getWindow().getScreenWidth() / (double) Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int screenHeight = (int) (guiHeight * (double) Minecraft.getInstance().getWindow().getScreenHeight() / (double) Minecraft.getInstance().getWindow().getGuiScaledHeight());
        int screenX = Minecraft.getInstance().getWindow().getWidth() / 2 - screenWidth / 2;
        int screenY = Minecraft.getInstance().getWindow().getHeight() / 2 - screenHeight / 2;

        containerMenuView = new ExampleContainerMenuView(requireContext(), screenX, screenY, floatingItem, muiTooltip); //ContainerMenuはクライアントとサーバー両方に存在してUIスロットを互いに通信する存在、FragmentはUIを描画するキャンバス、ContainerMenuViewはFragmentがContainerMenuを受信して描画するときのペン
        containerMenuView.setContainerMenu(menu);
        containerMenuView.setBackground(new SimpleBackground(containerMenuView));

        root.addView(containerMenuView, new AbsoluteLayout.LayoutParams(screenWidth, screenHeight, screenX, screenY));
        root.addView(floatingItem, new AbsoluteLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, 0));
        root.addView(muiTooltip, new AbsoluteLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, 0));

        return root;
    }

    @Override
    public boolean hasDefaultBackground() {
        return false;
    }
}
