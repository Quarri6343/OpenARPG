package quarri6343.openarpg.ui;

import icyllis.arc3d.core.ImageInfo;
import icyllis.arc3d.core.Matrix4;
import icyllis.arc3d.core.Rect2i;
import icyllis.modernui.core.Context;
import icyllis.modernui.core.Core;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

import static quarri6343.openarpg.OpenARPG.MODID;

//the parent must be the root view and AbsoluteLayout (for now)
@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ContainerMenuViewFullImplementation extends View implements CustomDrawable {

    private AbstractContainerMenu mContainerMenu;
    private final int mItemSize;
    
    //left and up position in the parent AbsoluteLayout
    private final int leftPos;
    private final int topPos;
    
    private int mMouseX;
    private int mMouseY;
    
    //you must specify screen position because how minecraft implements slot.x and slot.y
    public ContainerMenuViewFullImplementation(Context context, int leftPos, int topPos) {
        super(context);
        this.leftPos = leftPos;
        this.topPos = topPos;
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
            if (isHoveringSlot(slot.x, slot.y) && slot.isActive()) {
                if (slot.isHighlightable()) {
                    drawHighlight(canvas, slot.x, slot.y);
                }
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
        //todo:カスタムのdrawableの中にセットしてアイテムが移動した時ちゃんと反映されるようにする
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
        paint.setRGBA(255, 255, 255, 255);
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
    
    public void drawHighlight(@Nonnull Canvas canvas, float x, float y){
        int screenX = (int) (x * (double) Minecraft.getInstance().getWindow().getScreenWidth() / (double) Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int screenY = (int) (y * (double) Minecraft.getInstance().getWindow().getScreenHeight() / (double) Minecraft.getInstance().getWindow().getGuiScaledHeight());
        int guiXOffset = 16;
        int guiYOffset = 16;
        int screenXOffset = (int) (guiXOffset * (double) Minecraft.getInstance().getWindow().getScreenWidth() / (double) Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int screenYOffset = (int) (guiYOffset * (double) Minecraft.getInstance().getWindow().getScreenHeight() / (double) Minecraft.getInstance().getWindow().getGuiScaledHeight());
        
        Paint paint = Paint.obtain();
        paint.setRGBA(255, 255, 255, 127);
        canvas.drawRect(screenX, screenY, screenX + screenXOffset, screenY + screenYOffset, paint);
        paint.recycle();
    }

    @Override
    public DrawHandler snapDrawHandler(int backendApi, Matrix4 viewMatrix, Rect2i clipBounds, ImageInfo targetInfo) {
        return null;
    }

    @Override
    public RectF getBounds() {
        return null;
    }

    protected boolean isHoveringSlot(int pX, int pY) {
        mMouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        mMouseY = (int) Minecraft.getInstance().mouseHandler.ypos();
        int pMouseX = mMouseX - this.leftPos; //mMouseX(スクリーン座標系)からguiの左上座標(スクリーン座標系)を引く
        int pMouseY = mMouseY - this.topPos;
        int guiMouseX = (int) (pMouseX * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth());
        int guiMouseY = (int) (pMouseY * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight());
        return guiMouseX >= (double)(pX - 1) && guiMouseX < (double)(pX + 16 + 1) && guiMouseY >= (double)(pY - 1) && guiMouseY < (double)(pY + 16 + 1);
    }

    @SubscribeEvent
    public void onRenderTick(@Nonnull TickEvent.RenderTickEvent event) {
        //TODO: find better implementation
        Core.executeOnUiThread(() -> {
            if (Minecraft.getInstance().player == null)
                return;
            
            if(mMouseX == (int) Minecraft.getInstance().mouseHandler.xpos() && mMouseY == (int) Minecraft.getInstance().mouseHandler.ypos()){
                return;
            }

            if(!isLayoutRequested()){
                requestLayout();
                invalidate();
            }
        });
    }
    
    @Override
    protected void onDetachedFromWindow(){
        MinecraftForge.EVENT_BUS.unregister(this);
    }
    
    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        MinecraftForge.EVENT_BUS.register(this);
    }
}
