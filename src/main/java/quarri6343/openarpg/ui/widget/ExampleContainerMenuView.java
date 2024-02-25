package quarri6343.openarpg.ui.widget;

import icyllis.modernui.animation.ObjectAnimator;
import icyllis.modernui.animation.PropertyValuesHolder;
import icyllis.modernui.animation.TimeInterpolator;
import icyllis.modernui.core.Context;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.widget.*;

public class ExampleContainerMenuView extends ContainerMenuViewFullImplementation{
    
    //debug only
    private ObjectAnimator rightAnim;
    
    public ExampleContainerMenuView(Context context, int leftPos, int topPos, FloatingItem floatingItem, MUITooltip muiTooltip) {
        super(context, leftPos, topPos, floatingItem, muiTooltip);


        //debug only
        ObjectAnimator anim;
        {
            var pvh2 = PropertyValuesHolder.ofFloat(ROTATION_Y, 0, 30);
            anim = ObjectAnimator.ofPropertyValuesHolder(this, pvh2);
            anim.setDuration(1200);
            anim.setInterpolator(TimeInterpolator.ACCELERATE_DECELERATE);
            //anim.setRepeatCount(ValueAnimator.INFINITE);
            //anim.start();
            rightAnim = anim;
        }
        Button animButton = new Button(getContext());
        animButton.setText("Rotate Right");
        animButton.setTextColor(0xFF28A3F3);
        animButton.setTextStyle(Typeface.BOLD);
        animButton.setOnClickListener(__ -> {
            if (rightAnim != null) {
                rightAnim.start();
            }
        });
        addView(animButton, new AbsoluteLayout.LayoutParams(dp(100), dp(30), 0, 0));
        var inputField = new EditText(context);
        SimpleBackground background = new SimpleBackground(inputField);
        background.setColor(0xFF28A3F3);
        inputField.setBackground(background);
        addView(inputField, new AbsoluteLayout.LayoutParams(dp(100), dp(30), 250, 0));
    }
}
