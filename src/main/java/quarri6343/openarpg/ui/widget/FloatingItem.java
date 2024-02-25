package quarri6343.openarpg.ui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.mc.ContainerDrawHelper;
import icyllis.modernui.view.View;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//must be in root view and be attached by ContainerMenuViewFullImplementation
public class FloatingItem extends View {

    private ItemStack floatingStack;
    private int itemSize;

    public FloatingItem(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(@Nonnull Canvas canvas) {
        if (floatingStack.isEmpty()) {
            return;
        }

        int x = (int) Minecraft.getInstance().mouseHandler.xpos();
        int y = (int) Minecraft.getInstance().mouseHandler.ypos();
        ContainerDrawHelper.drawItem(canvas, floatingStack, x, y, 0, itemSize, x + y * getWidth());
        ContainerMenuViewFullImplementation.drawItemDecorations(canvas, floatingStack, x, y, 0, itemSize, x + y * getWidth());
    }

    public void setFloatingStack(@Nullable ItemStack floatingStack) {
        this.floatingStack = floatingStack;
    }

    public void setItemSize(int itemSize) {
        this.itemSize = itemSize;
    }
}
