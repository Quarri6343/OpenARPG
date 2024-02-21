package quarri6343.openarpg.ui;

import icyllis.modernui.text.Editable;
import icyllis.modernui.text.TextWatcher;
import quarri6343.openarpg.FloatConfig;

public class FloatTextWatcher implements TextWatcher {

    private final FloatConfig config;

    public FloatTextWatcher(FloatConfig config) {
        this.config = config;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean applyFlag = true;
        float num = 0;
        try {
            num = Float.parseFloat(s.toString());
            ;
        } catch (NumberFormatException e) {
            applyFlag = false;
        }

        if (applyFlag) {
            config.setValue(num);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
