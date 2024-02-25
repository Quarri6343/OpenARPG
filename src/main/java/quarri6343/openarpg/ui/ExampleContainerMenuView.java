package quarri6343.openarpg.ui;

import icyllis.modernui.animation.ObjectAnimator;
import icyllis.modernui.animation.PropertyValuesHolder;
import icyllis.modernui.animation.TimeInterpolator;
import icyllis.modernui.core.Context;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.widget.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

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
        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ItemStack itemStack = mContainerMenu.getSlot(37).getItem().copy();
                if(itemStack.isEmpty())
                    return;
                
                CompoundTag nbt = itemStack.getOrCreateTag();
                nbt.putString("displayname", s.toString());
                itemStack.setTag(nbt);
                mContainerMenu.getSlot(37).set(itemStack);
            }

            @Override
            public void afterTextChanged(Editable s) {
                
            }
        });
        SimpleBackground background = new SimpleBackground(inputField);
        background.setColor(0xFF28A3F3);
        inputField.setBackground(background);
        addView(inputField, new AbsoluteLayout.LayoutParams(dp(100), dp(30), 250, 0));
    }
}
