package quarri6343.openarpg.ui;

import icyllis.arc3d.core.Matrix4;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.MathUtil;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.text.ShapedText;
import icyllis.modernui.text.TextPaint;
import icyllis.modernui.text.TextShaper;
import icyllis.modernui.view.View;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.BufferUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * A class to render tooltip in MUI fragment
 * most parts are copied from {@link icyllis.modernui.mc.TooltipRenderer}
 */
public class MUITooltip extends View {

    public static volatile boolean sTooltip = true;

    public static final int[] sFillColor = new int[4];
    public static final int[] sStrokeColor = new int[4];
    public static volatile float sBorderWidth = 4 / 3f;
    public static volatile float sShadowRadius = 10;

    // space between mouse and tooltip
    public static final int TOOLTIP_SPACE = 12;
    public static final int H_BORDER = 4;
    public static final int V_BORDER = 4;
    //public static final int LINE_HEIGHT = 10;
    // extra space after first line
    private static final int TITLE_GAP = 2;

    //private static final List<FormattedText> sTempTexts = new ArrayList<>();

    private final FloatBuffer mMatBuf = BufferUtils.createFloatBuffer(16);
    private final Matrix4 mCoreMat = new Matrix4();

    //private static final int[] sActiveFillColor = new int[4];
    private final int[] mActiveStrokeColor = new int[4];
    //static volatile float sAnimationDuration; // milliseconds
    public static volatile int sBorderColorCycle = 1000; // milliseconds

    public static volatile boolean sExactPositioning = true;
    public static volatile boolean sRoundedShapes = true;
    public static volatile boolean sCenterTitle = true;
    public static volatile boolean sTitleBreak = true;

    //TODO: change font size and tooltip frame size when at different screen size
    private static final int globalMulti = 4;
    private static final int fontSize = 30;

    public volatile boolean mLayoutRTL;

    private boolean mDraw;
    //public static float sAlpha = 1;

    private float mScroll;
    // 0 = off, 1 = down, -1 = up
    private int mMarqueeDir;
    // the time point when marquee is at top or bottom
    private long mMarqueeEndMillis;

    private static final long MARQUEE_DELAY_MILLIS = 1200;

    private boolean mFrameGap;
    private static long mCurrTimeMillis;
    private static long mCurrDeltaMillis;
    private ItemStack itemStack;
    private List<ClientTooltipComponent> list;
    private List<Component> list2;
    private int mouseX;
    private int mouseY;
    private Font font;
    private int screenWidth;
    private int screenHeight;
    private float partialX;
    private float partialY;
    @Nullable
    private ClientTooltipPositioner positioner;
    
    public MUITooltip(Context context) {
        super(context);
    }


    public void setComponents(@Nonnull ItemStack itemStack,
                              @Nonnull List<ClientTooltipComponent> list, int mouseX, int mouseY,
                              @Nonnull Font font, int screenWidth, int screenHeight,
                              float partialX, float partialY, @Nullable ClientTooltipPositioner positioner, List<Component> list2){

        this.itemStack = itemStack;
        this.list = list;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.font = font;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.partialX = partialX;
        this.partialY = partialY;
        this.positioner = positioner;
        this.list2 = list2;
    }
    
    public static void update(long deltaMillis, long timeMillis) {
//        if (mDraw) {
//            mDraw = false;
//            if (mFrameGap) {
//                mMarqueeEndMillis = timeMillis;
//                mMarqueeDir = 1;
//                mScroll = 0;
//            }
//            mFrameGap = false;
//        } else {
//            mFrameGap = true;
//        }
        mCurrTimeMillis = timeMillis;
        mCurrDeltaMillis = deltaMillis;
    }
    
    @Override
    public void draw(Canvas canvas) {
        drawTooltip(canvas);
    }

    public void drawTooltip(@Nonnull Canvas canvas) {
        if(itemStack == null || itemStack.isEmpty()){
            return;
        }
        
        mDraw = true;

        //全部X倍してなんとか見えるレベルにしてる
        int tooltipWidth;
        int tooltipHeight;
        boolean titleGap = false;
        int titleBreakHeight = 0;
        if (list.size() == 1) {
            ClientTooltipComponent component = list.get(0);
            tooltipWidth = component.getWidth(font) * globalMulti;
            tooltipHeight = component.getHeight() * globalMulti - TITLE_GAP;
        } else {
            tooltipWidth = 0;
            tooltipHeight = 0;
            for (int i = 0; i < list.size(); i++) {
                ClientTooltipComponent component = list.get(i);
                tooltipWidth = Math.max(tooltipWidth, component.getWidth(font) * globalMulti);
                int componentHeight = component.getHeight() * globalMulti;
                tooltipHeight += componentHeight;
                if (i == 0 && !itemStack.isEmpty() &&
                        component instanceof ClientTextTooltip) {
                    titleGap = true;
                    titleBreakHeight = componentHeight;
                }
            }
            if (!titleGap) {
                tooltipHeight -= TITLE_GAP;
            }
        }

        //TODO: これをずらしてマウスとツールチップが重ならないようにする
        float tooltipX;
        float tooltipY;
        if (positioner != null) {
            var pos = positioner.positionTooltip(screenWidth, screenHeight,
                    mouseX, mouseY,
                    tooltipWidth, tooltipHeight);
            tooltipX = pos.x();
            tooltipY = pos.y();
        } else {
            if (mLayoutRTL) {
                tooltipX = mouseX + TOOLTIP_SPACE + partialX - 24 - tooltipWidth;
                if (tooltipX - partialX < 4) {
                    tooltipX += 24 + tooltipWidth;
                }
            } else {
                tooltipX = mouseX + TOOLTIP_SPACE + partialX;
                if (tooltipX - partialX + tooltipWidth + 4 > screenWidth) {
                    tooltipX -= 28 + tooltipWidth;
                }
            }
            partialX = (tooltipX - (int) tooltipX);

            tooltipY = mouseY - TOOLTIP_SPACE + partialY;
            if (tooltipY + tooltipHeight + 6 > screenHeight) {
                tooltipY = screenHeight - tooltipHeight - 6;
            }
            if (tooltipY < 6) {
                tooltipY = 6;
            }
            partialY = (tooltipY - (int) tooltipY);
        }

        float maxScroll = 6 + tooltipHeight + 6 - screenHeight;
        if (maxScroll > 0) {
            mScroll = MathUtil.clamp(mScroll, 0, maxScroll);

            if (mMarqueeDir != 0 && mCurrTimeMillis - mMarqueeEndMillis >= MARQUEE_DELAY_MILLIS) {
                mScroll += mMarqueeDir * mCurrDeltaMillis * 0.018f;
                if (mMarqueeDir > 0) {
                    if (mScroll >= maxScroll) {
                        mMarqueeDir = -1;
                        mMarqueeEndMillis = mCurrTimeMillis;
                    }
                } else {
                    if (mScroll <= 0) {
                        mMarqueeDir = 1;
                        mMarqueeEndMillis = mCurrTimeMillis;
                    }
                }
            }
        } else {
            mScroll = 0;
        }

        if (sBorderColorCycle > 0) {
            updateBorderColor();
        }
        
//        gr.pose().pushPose();
//        // because of the order of draw calls, we actually don't need z-shifting
//        gr.pose().translate(0, -mScroll, 400);
//        final Matrix4f pose = gr.pose().last().pose();

        // we should disable depth test, because texts may be translucent
        // for compatibility reasons, we keep this enabled, and it doesn't seem to be a big problem
//        RenderSystem.enableDepthTest();
        
        if (sRoundedShapes) {
            drawRoundBackGround(canvas,
                    tooltipX, tooltipY, tooltipWidth, tooltipHeight,
                    titleGap, titleBreakHeight);
        } else {
//            drawVanillaBackground(gr, pose,
//                    tooltipX, tooltipY, tooltipWidth, tooltipHeight,
//                    titleGap, titleBreakHeight);
        }

        final int drawX = (int) tooltipX;
        int drawY = (int) tooltipY;

//        RenderSystem.enableDepthTest();
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
        
//        final MultiBufferSource.BufferSource source = gr.bufferSource();
//        gr.pose().translate(partialX, partialY, 0);
        for (int i = 0; i < list2.size(); i++) {
            drawY += 30;
            
            Component component = list2.get(i);
            Paint paint = Paint.obtain();
            paint.setRGBA(255, 255, 255, 255);
            paint.setFontSize(fontSize); //テキストの大きさを決める
            TextPaint textPaint = TextPaint.obtain();
            textPaint.setFontSize(fontSize); //テキストの幅を決める
            ShapedText shapedText = TextShaper.shapeTextRun(
                    component.getString().toCharArray(),
                    0, component.getString().length(),
                    0, component.getString().length(),
                    false,
                    textPaint
            );
            if (titleGap && i == 0 && sCenterTitle) {
                ClientTooltipComponent clientComponent = list.get(0);
//                component.renderText(font, drawX + (tooltipWidth - component.getWidth(font)) / 2, drawY, pose, source);
                canvas.drawShapedText(shapedText, drawX + (float) (tooltipWidth - clientComponent.getWidth(font) * globalMulti) / 2, drawY, paint);
            } else if (mLayoutRTL) {
//                component.renderText(font, drawX + tooltipWidth - component.getWidth(font), drawY, pose, source);
                ClientTooltipComponent clientComponent = list.get(i);
                canvas.drawShapedText(shapedText, drawX + tooltipWidth - clientComponent.getWidth(font) * globalMulti, drawY, paint);
            } else {
//                component.renderText(font, drawX, drawY, pose, source);
                canvas.drawShapedText(shapedText, drawX, drawY, paint);
            }
            if (titleGap && i == 0) {
                drawY += TITLE_GAP * globalMulti;
            }
            textPaint.recycle();
            paint.recycle();
        }
//        gr.flush();

//        drawY = (int) tooltipY;
//
//        for (int i = 0; i < list.size(); i++) {
//            ClientTooltipComponent component = list.get(i);
//            if (mLayoutRTL) {
//                component.renderImage(font, drawX + tooltipWidth - component.getWidth(font), drawY, gr);
//            } else {
//                component.renderImage(font, drawX, drawY, gr);
//            }
//            if (titleGap && i == 0) {
//                drawY += TITLE_GAP;
//            }
//            drawY += component.getHeight();
//        }
//        gr.pose().popPose();
    }
    
    private void drawRoundBackGround(Canvas canvas,
                                     float tooltipX, float tooltipY,
                                     int tooltipWidth, int tooltipHeight,
                                     boolean titleGap, int titleBreakHeight){
        Paint paint = Paint.obtain();
        
        /*for (int i = 0; i < 4; i++) {
            int color = sFillColor[i];
            int alpha = (int) ((color >>> 24) * sAlpha + 0.5f);
            sActiveFillColor[i] = (color & 0xFFFFFF) | (alpha << 24);
        }*/
        paint.setStyle(Paint.FILL);
        {
            float spread = 0;
            if (sShadowRadius >= 2) {
                spread = sShadowRadius * 0.5f - 1f;
                paint.setSmoothWidth(sShadowRadius);
            }
            canvas.drawRoundRectGradient(tooltipX - H_BORDER - spread, tooltipY - V_BORDER - spread,
                    tooltipX + tooltipWidth + H_BORDER + spread,
                    tooltipY + tooltipHeight + V_BORDER + spread,
                    sFillColor[0], sFillColor[1],
                    sFillColor[2], sFillColor[3],
                    (3 + spread) * globalMulti, paint);
            paint.setSmoothWidth(0);
        }

        if (titleGap && sTitleBreak) {
            paint.setColor(0xE0C8C8C8);
            paint.setStrokeWidth(1f * globalMulti);
            canvas.drawLine(tooltipX, tooltipY + titleBreakHeight,
                    tooltipX + tooltipWidth, tooltipY + titleBreakHeight,
                    paint);
        }

        /*for (int i = 0; i < 4; i++) {
            int color = sStrokeColor[i];
            int alpha = (int) ((color >>> 24) * sAlpha + 0.5f);
            sActiveStrokeColor[i] = (color & 0xFFFFFF) | (alpha << 24);
        }*/
        paint.setStyle(Paint.STROKE);
        paint.setStrokeWidth(sBorderWidth);
        canvas.drawRoundRectGradient(tooltipX - H_BORDER, tooltipY - V_BORDER,
                tooltipX + tooltipWidth + H_BORDER,
                tooltipY + tooltipHeight + V_BORDER,
                chooseBorderColor(0), chooseBorderColor(1),
                chooseBorderColor(2), chooseBorderColor(3),
                3 * globalMulti, paint);

        paint.recycle();
    }

    void updateBorderColor() {
        float p = (mCurrTimeMillis % sBorderColorCycle) / (float) sBorderColorCycle;
        if (mLayoutRTL) {
            int pos = (int) ((mCurrTimeMillis / sBorderColorCycle) & 3);
            for (int i = 0; i < 4; i++) {
                mActiveStrokeColor[i] = lerpInLinearSpace(p,
                        sStrokeColor[(i + pos) & 3],
                        sStrokeColor[(i + pos + 1) & 3]);
            }
        } else {
            int pos = 3 - (int) ((mCurrTimeMillis / sBorderColorCycle) & 3);
            for (int i = 0; i < 4; i++) {
                mActiveStrokeColor[i] = lerpInLinearSpace(p,
                        sStrokeColor[(i + pos) & 3],
                        sStrokeColor[(i + pos + 3) & 3]);
            }
        }
    }
    
    private int chooseBorderColor(int corner) {
        if (sBorderColorCycle > 0) {
            return mActiveStrokeColor[corner];
        } else {
            return sStrokeColor[corner];
        }
    }

    static int lerpInLinearSpace(float fraction, int startValue, int endValue) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            float s = ((startValue >> (i << 3)) & 0xff) / 255.0f;
            float t = ((endValue >> (i << 3)) & 0xff) / 255.0f;
            float v = MathUtil.lerp(s, t, fraction);
            result |= Math.round(v * 255.0f) << (i << 3);
        }
        return result;
    }
}
