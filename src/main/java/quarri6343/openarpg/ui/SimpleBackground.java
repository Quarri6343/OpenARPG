package quarri6343.openarpg.ui;

import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Rect;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.view.View;

import javax.annotation.Nonnull;

import static icyllis.modernui.mc.testforge.TestPauseFragment.NETWORK_COLOR;

public class SimpleBackground extends Drawable {
    private final float mRadius;
    private int mColor;

    public SimpleBackground(View v) {
        mRadius = v.dp(16);
        setColor(NETWORK_COLOR);
    }

    public void setColor(int color) {
        mColor = 0xFF000000 | color;
    }

    @Override
    public void draw(@Nonnull Canvas canvas) {
        Rect b = getBounds();
        float stroke = mRadius * 0.25f;
        float start = stroke * 0.5f;

        Paint paint = Paint.obtain();
        paint.setRGBA(0, 0, 0, 180);
        canvas.drawRect(b.left + start , b.top + start, b.right - start, b.bottom - start, paint);
        paint.recycle();
    }
}
