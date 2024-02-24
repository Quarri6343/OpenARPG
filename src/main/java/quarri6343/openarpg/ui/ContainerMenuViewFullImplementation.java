package quarri6343.openarpg.ui;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import icyllis.arc3d.core.ImageInfo;
import icyllis.arc3d.core.Matrix4;
import icyllis.arc3d.core.Rect2i;
import icyllis.arc3d.engine.DirectContext;
import icyllis.arc3d.engine.DrawableInfo;
import icyllis.modernui.animation.ObjectAnimator;
import icyllis.modernui.animation.PropertyValuesHolder;
import icyllis.modernui.animation.TimeInterpolator;
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
import icyllis.modernui.widget.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static quarri6343.openarpg.OpenARPG.MODID;

//the parent must be the root view and AbsoluteLayout (for now)
@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ContainerMenuViewFullImplementation extends AbsoluteLayout implements CustomDrawable {

    protected AbstractContainerMenu mContainerMenu;
    private final int mItemSize;

    //left and up position in the parent AbsoluteLayout
    private final int leftPos;
    private final int topPos;
    @Nullable
    protected Slot hoveredSlot;
    @Nullable
    private Slot snapbackEnd;
    @Nullable
    private Slot quickdropSlot;
    @Nullable
    private Slot lastClickSlot;

    private long quickdropTime;
    protected final Set<Slot> quickCraftSlots = Sets.newHashSet();
    protected boolean isQuickCrafting;
    private int quickCraftingType;
    private int quickCraftingButton;
    private boolean skipNextRelease;
    private int quickCraftingRemainder;
    private long lastClickTime;
    private int lastClickButton; //clicked button in the last action (cannot be -1)
    private boolean doubleclick;
    private ItemStack lastQuickMoved = ItemStack.EMPTY;
    private int currentClickButton = -1; //clicked button in the current frame

    private final FloatingItem floatingItem;
    
    private boolean pendingTooltipDraw;

    //you must specify screen position because how minecraft implements slot.x and slot.y
    public ContainerMenuViewFullImplementation(Context context, int leftPos, int topPos, FloatingItem floatingItem) {
        super(context);
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.floatingItem = floatingItem;
        mItemSize = dp(32);
        floatingItem.setItemSize(mItemSize);
        skipNextRelease = true;
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
        super.onDraw(canvas);
        var menu = mContainerMenu;
        if (menu == null) {
            return;
        }

        this.hoveredSlot = null;
        for (int i = 0; i < menu.slots.size(); ++i) {
            Slot slot = menu.slots.get(i);
            if (slot.isActive()) {
                drawSlotBG(canvas, slot);
                drawSlot(canvas, slot);
            }
            if (isHoveringSlot(slot.x, slot.y) && slot.isActive()) {
                hoveredSlot = slot;
                if (slot.isHighlightable()) {
                    drawHighlight(canvas, slot.x, slot.y);
                }
            }
        }

        ItemStack itemstack = mContainerMenu.getCarried();
        floatingItem.setFloatingStack(itemstack);
    }

    protected List<Component> getTooltipFromContainerItem(ItemStack pStack) {
        return Screen.getTooltipFromItem(Minecraft.getInstance(), pStack);
    }
    
    //TODO: custom drawable
    protected void drawSlotBG(@Nonnull Canvas canvas, @Nonnull Slot slot){
        Paint paint = Paint.obtain();
        paint.setRGBA(92,92,35, 255);

        int guiWidth = 16;
        int guiHeight = 16;
        int screenWidth = (int) (guiWidth * (double) Minecraft.getInstance().getWindow().getScreenWidth() / (double) Minecraft.getInstance().getWindow().getGuiScaledWidth());
        int screenHeight = (int) (guiHeight * (double) Minecraft.getInstance().getWindow().getScreenHeight() / (double) Minecraft.getInstance().getWindow().getGuiScaledHeight());
        int x = dp(slot.x * 2);
        int y = dp(slot.y * 2);
        
        canvas.drawRect(x, y, x + screenWidth, y + screenHeight, paint);
        paint.recycle();
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

    public static void drawItemDecorations(@Nonnull Canvas canvas, @Nonnull ItemStack item,
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

    public void drawHighlight(@Nonnull Canvas canvas, float x, float y) {
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
    public RectF getBounds() {
        return null;
    }

    protected boolean isHoveringSlot(int pX, int pY) {
        int mMouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        int mMouseY = (int) Minecraft.getInstance().mouseHandler.ypos();
        int pMouseX = mMouseX - this.leftPos; //mMouseX(スクリーン座標系)からguiの左上座標(スクリーン座標系)を引く
        int pMouseY = mMouseY - this.topPos;
        int guiMouseX = (int) (pMouseX * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth());
        int guiMouseY = (int) (pMouseY * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight());
        return guiMouseX >= (double) (pX - 1) && guiMouseX < (double) (pX + 16 + 1) && guiMouseY >= (double) (pY - 1) && guiMouseY < (double) (pY + 16 + 1);
    }

    @SubscribeEvent
    public void onRenderTick(@Nonnull TickEvent.RenderTickEvent event) {
        int pButton;
        if (GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) { //must be executed on main thread
            pButton = GLFW.GLFW_MOUSE_BUTTON_1;
        } else if (GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS) {
            pButton = GLFW.GLFW_MOUSE_BUTTON_2;
        } else {
            pButton = -1;
        }

        //TODO: find better implementation
        Core.executeOnUiThread(() -> {
            if (Minecraft.getInstance().player == null)
                return;

            if (!isLayoutRequested()) {
                requestLayout();
                invalidate();
            }

            if (pButton != -1 && currentClickButton == -1) {
                mouseClicked(pButton);
            } else if (pButton == -1 && currentClickButton != -1) {
                mouseReleased(lastClickButton);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //don't do this since we want to also check mouse click outside of this screen
//        setOnClickListener(this::mouseClicked);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public boolean mouseClicked(int pButton) {
        int pMouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        int pMouseY = (int) Minecraft.getInstance().mouseHandler.ypos();

        InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(pButton);
        boolean flag = Minecraft.getInstance().options.keyPickItem.isActiveAndMatches(mouseKey);
        Slot slot = this.findSlot();
        long i = Util.getMillis();
        this.doubleclick = this.lastClickSlot == slot && i - this.lastClickTime < 250L && this.lastClickButton == pButton;
        this.skipNextRelease = false;
//        if (pButton != 0 && pButton != 1 && !flag) {
//            this.checkHotbarMouseClicked(pButton);
//        } else {
        boolean flag1 = this.hasClickedOutside(pMouseX, pMouseY);
        if (slot != null) flag1 = false; // Forge, prevent dropping of items through slots outside of GUI boundaries
        int l = -1;
        if (slot != null) {
            l = slot.index;
        }

        if (flag1) {
            l = -999;
        }

        if (l != -1) {
            if (!this.isQuickCrafting) {
                if (mContainerMenu.getCarried().isEmpty()) {
                    if (Minecraft.getInstance().options.keyPickItem.isActiveAndMatches(mouseKey)) {
                        this.slotClicked(slot, l, pButton, ClickType.CLONE);
                    } else {
                        boolean flag2 = l != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                        ClickType clicktype = ClickType.PICKUP;
                        if (flag2) {
                            this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                            clicktype = ClickType.QUICK_MOVE;
                        } else if (l == -999) {
                            clicktype = ClickType.THROW;
                        }

                        this.slotClicked(slot, l, pButton, clicktype);
                    }

                    this.skipNextRelease = true;
                } else {
                    this.isQuickCrafting = true;
                    this.quickCraftingButton = pButton;
                    this.quickCraftSlots.clear();
                    if (pButton == 0) {
                        this.quickCraftingType = 0;
                    } else if (pButton == 1) {
                        this.quickCraftingType = 1;
                    } else if (Minecraft.getInstance().options.keyPickItem.isActiveAndMatches(mouseKey)) {
                        this.quickCraftingType = 2;
                    }
                }
            }
        }
//        }

        this.lastClickSlot = slot;
        this.lastClickTime = i;
        this.lastClickButton = pButton;
        this.currentClickButton = pButton;
        return true;
    }

    @Nullable
    private Slot findSlot() {
        for (int i = 0; i < mContainerMenu.slots.size(); ++i) {
            Slot slot = mContainerMenu.slots.get(i);
            if (isHoveringSlot(slot.x, slot.y) && slot.isActive()) {
                return slot;
            }
        }

        return null;
    }

    protected boolean hasClickedOutside(int pMouseX, int pMouseY) {
        return pMouseX < leftPos || pMouseY < topPos || pMouseX >= leftPos + getWidth() || pMouseY >= (double) (topPos + getHeight());
    }

    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        if (pSlot != null) {
            pSlotId = pSlot.index;
        }

        Minecraft.getInstance().gameMode.handleInventoryMouseClick(mContainerMenu.containerId, pSlotId, pMouseButton, pType, Minecraft.getInstance().player);
    }

    public boolean mouseReleased(int pButton) {
        int pMouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        int pMouseY = (int) Minecraft.getInstance().mouseHandler.ypos();

//        super.mouseReleased(pMouseX, pMouseY, pButton); //Forge, Call parent to release buttons
        Slot slot = this.findSlot();
        int i = this.leftPos;
        int j = this.topPos;
        boolean flag = this.hasClickedOutside(pMouseX, pMouseY);
        if (slot != null) flag = false; // Forge, prevent dropping of items through slots outside of GUI boundaries
        InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(pButton);
        int k = -1;
        if (slot != null) {
            k = slot.index;
        }

        if (flag) {
            k = -999;
        }

        if (this.doubleclick && slot != null && pButton == 0 && mContainerMenu.canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
            if (hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    for (Slot slot2 : mContainerMenu.slots) {
                        if (slot2 != null && slot2.mayPickup(Minecraft.getInstance().player) && slot2.hasItem() && slot2.isSameInventory(slot) && AbstractContainerMenu.canItemQuickReplace(slot2, this.lastQuickMoved, true)) {
                            this.slotClicked(slot2, slot2.index, pButton, ClickType.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked(slot, k, pButton, ClickType.PICKUP_ALL);
            }

            this.doubleclick = false;
            this.lastClickTime = 0L;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != pButton) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                currentClickButton = -1;
                return true;
            }

            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                currentClickButton = -1;
                return true;
            }

//            if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
//                if (pButton == 0 || pButton == 1) {
//                    if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
//                        this.draggingItem = this.clickedSlot.getItem();
//                    }
//
//                    boolean flag2 = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);
//                    if (k != -1 && !this.draggingItem.isEmpty() && flag2) {
//                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, pButton, ClickType.PICKUP);
//                        this.slotClicked(slot, k, 0, ClickType.PICKUP);
//                        if (this.menu.getCarried().isEmpty()) {
//                            this.snapbackItem = ItemStack.EMPTY;
//                        } else {
//                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, pButton, ClickType.PICKUP);
//                            this.snapbackStartX = Mth.floor(pMouseX - (double)i);
//                            this.snapbackStartY = Mth.floor(pMouseY - (double)j);
//                            this.snapbackEnd = this.clickedSlot;
//                            this.snapbackItem = this.draggingItem;
//                            this.snapbackTime = Util.getMillis();
//                        }
//                    } else if (!this.draggingItem.isEmpty()) {
//                        this.snapbackStartX = Mth.floor(pMouseX - (double)i);
//                        this.snapbackStartY = Mth.floor(pMouseY - (double)j);
//                        this.snapbackEnd = this.clickedSlot;
//                        this.snapbackItem = this.draggingItem;
//                        this.snapbackTime = Util.getMillis();
//                    }
//
//                    this.clearDraggingState();
//                }
//            } else 
            if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

                for (Slot slot1 : this.quickCraftSlots) {
                    this.slotClicked(slot1, slot1.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
                }

                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
            } else if (!mContainerMenu.getCarried().isEmpty()) {
                if (Minecraft.getInstance().options.keyPickItem.isActiveAndMatches(mouseKey)) {
                    this.slotClicked(slot, k, pButton, ClickType.CLONE);
                } else {
                    boolean flag1 = k != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                    if (flag1) {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                    }

                    this.slotClicked(slot, k, pButton, flag1 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                }
            }
        }

        if (mContainerMenu.getCarried().isEmpty()) {
            this.lastClickTime = 0L;
        }

        this.isQuickCrafting = false;
        currentClickButton = -1;
        return true;
    }

    //TODO:MouseDragged, MouseReleased, keyPressed, tick

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
    }

    @Override
    public DrawHandler snapDrawHandler(int backendApi, Matrix4 viewMatrix, Rect2i clipBounds, ImageInfo targetInfo) {
        return null;
    }
    
    /**
     * We want to use icyllis.modernui.mc.UIManager.ViewRootImpl#drawExtTooltipLocked ,
     * which is called when {@link net.minecraft.client.gui.GuiGraphics#renderTooltip} is invoked.
     * However, it must be called in the main thread.
     * To make the thing worse, If we call this method by using {@link Core#executeOnMainThread(Runnable)} or any vanilla event while the fragment is on the view, mPendingDraw will be always true.
     */
    @SubscribeEvent
    public void renderTooltipWorkAround(TickEvent.RenderTickEvent event){
        if (event.phase == TickEvent.Phase.START && mContainerMenu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack itemstack = hoveredSlot.getItem();
            GuiGraphics guigraphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
            int pX = (int) Minecraft.getInstance().mouseHandler.xpos();
            int pY = (int) Minecraft.getInstance().mouseHandler.ypos();
            int d0 = (int) (pX * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth());
            int d1 = (int) (pY * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight());
            
            guigraphics.renderTooltip(Minecraft.getInstance().font, getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, d0, d1);
        }
    }
}
