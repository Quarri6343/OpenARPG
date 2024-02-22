package quarri6343.openarpg.ui;

import icyllis.arc3d.core.ImageInfo;
import icyllis.arc3d.core.Matrix4;
import icyllis.arc3d.core.Rect2i;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.CustomDrawable;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.RectF;
import icyllis.modernui.graphics.text.ShapedText;
import icyllis.modernui.mc.ContainerDrawHelper;
import icyllis.modernui.text.TextPaint;
import icyllis.modernui.text.TextShaper;
import icyllis.modernui.view.View;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerMenuViewFullImplementation extends View implements CustomDrawable {

    private AbstractContainerMenu mContainerMenu;
    private final int mItemSize;

    public ContainerMenuViewFullImplementation(Context context) {
        super(context);
        mItemSize = dp(32);
    }

    public void setContainerMenu(AbstractContainerMenu containerMenu) {
        mContainerMenu = containerMenu;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(@Nonnull Canvas canvas) {
        var menu = mContainerMenu;
        if (menu == null) {
            return;
        }
        for (int i = 0; i < menu.slots.size(); ++i) {
            Slot slot = menu.slots.get(i);
            if (slot.isActive()) {
                drawSlot(canvas, slot);
            }
        }
    }

    protected void drawSlot(@Nonnull Canvas canvas, @Nonnull Slot slot) {
        ItemStack item = slot.getItem();
        if (item.isEmpty()) {
            return;
        }
        //adjust slot position for center rendering
        int guiWidth = 8;
        int guiHeight = 8;
        int screenWidth = (int) (guiWidth * (double) Minecraft.getInstance().getWindow().getScreenWidth() / (double) Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int screenHeight = (int) (guiHeight * (double) Minecraft.getInstance().getWindow().getScreenHeight() / (double) Minecraft.getInstance().getWindow().getGuiScaledHeight());
        int x = dp(slot.x * 2) + screenWidth;
        int y = dp(slot.y * 2) + screenHeight;
        ContainerDrawHelper.drawItem(canvas, item, x, y, 0, mItemSize, x + y * getWidth());
        drawItemDecorations(canvas, item, x, y, 0, mItemSize, x + y * getWidth());
    }
    
    public void drawItemDecorations(@Nonnull Canvas canvas, @Nonnull ItemStack item,
                                    float x, float y, float z, float size, int seed) {
        String count = String.valueOf(item.getCount());
        
        //テキストの位置調整
        int guiX = 8 - count.length() * 4; //左に寄せる
        int guiY = 8;
        int screenX = (int) (guiX * (double) Minecraft.getInstance().getWindow().getScreenWidth() / (double) Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int screenY = (int) (guiY * (double) Minecraft.getInstance().getWindow().getScreenHeight() / (double) Minecraft.getInstance().getWindow().getGuiScaledHeight());
        x += screenX;
        y += screenY;
        
        Paint paint = Paint.obtain();
        paint.setRGBA(255, 255, 255, 180);
        paint.setFontSize(30); //テキストの大きさを決める
        TextPaint textPaint = TextPaint.obtain();
        textPaint.setFontSize(30); //テキストの幅を決める
        ShapedText shapedText = TextShaper.shapeTextRun(
                count.toCharArray(),
                0, count.length(),
                0, count.length(),
                false,
                textPaint
        );
        canvas.drawShapedText(shapedText, x, y, paint);
        textPaint.recycle();
        paint.recycle();
        
        //TODO:enchant,durability,cooldown
        //see GuiGraphics#renderItemDecorations
    }

    @Override
    public DrawHandler snapDrawHandler(int backendApi, Matrix4 viewMatrix, Rect2i clipBounds, ImageInfo targetInfo) {
        return null;
    }

    @Override
    public RectF getBounds() {
        return null;
    }
}
