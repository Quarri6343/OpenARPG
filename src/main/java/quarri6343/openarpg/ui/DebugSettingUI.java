package quarri6343.openarpg.ui;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import quarri6343.openarpg.FloatConfig;

import java.util.Objects;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DebugSettingUI extends Fragment implements ScreenCallback {

    public static final int ID_TAB_CONTAINER = 0x0002;
    private static final String TITLE = "デバッグ設定";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @icyllis.modernui.annotation.Nullable ViewGroup container,
                             @icyllis.modernui.annotation.Nullable DataSet savedInstanceState) {
        
        //ルート
        var root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);

        //タイトル
        var titleText = new TextView(requireContext());
        titleText.setText(TITLE);
        titleText.setTextColor(0xFFC5C9ED);
        int titleTextHorizonSize = root.dp(350);
        int titleTextVerticalSize = root.dp(30);
        var titleParams = new LinearLayout.LayoutParams(titleTextHorizonSize, titleTextVerticalSize);
        titleParams.setMarginsRelative(root.dp(16), root.dp(8), root.dp(16), root.dp(8));
        titleText.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(titleText, titleParams);

        //スクロールバー階層
        var scroll = new ScrollView(requireContext());
        scroll.setBackground(new SimpleBackground(scroll));
        scroll.setId(ID_TAB_CONTAINER);
        scroll.setFillViewport(false);

        //スクロールバーの中身(必ず1つしか存在してはいけない)
        var content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);

        for (FloatConfig config : FloatConfig.values()) {
            //入力欄を入れる行
            var inputFieldColumn = new LinearLayout(requireContext());
            inputFieldColumn.setOrientation(LinearLayout.HORIZONTAL);
            inputFieldColumn.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

            //入力欄の横のテキスト
            var inputFieldText = new TextView(requireContext());
            String displayName = config.getDisplayName();
            inputFieldText.setText(Objects.nonNull(displayName) ? displayName : "");
            int sideTextHorizonSize = content.dp(320);
            int sideTextVerticalSize = content.dp(30);
            var inputFieldparams = new LinearLayout.LayoutParams(sideTextHorizonSize, sideTextVerticalSize);
            inputFieldparams.setMarginsRelative(content.dp(16), content.dp(8), content.dp(16), content.dp(8));
            inputFieldText.setGravity(Gravity.START);
            inputFieldText.setGravity(Gravity.CENTER_VERTICAL);
            
            //テキストのツールチップ
            inputFieldText.setTooltipText(config.getDescription());

            inputFieldColumn.addView(inputFieldText, inputFieldparams);

            //入力欄
            var inputField = new EditText(requireContext());
            //入力欄が入力された時の処理
            inputField.addTextChangedListener(new FloatTextWatcher(config));
            //入力欄の初期テキスト
            inputField.setText(String.valueOf(config.getValue()), TextView.BufferType.EDITABLE);
            //入力欄のその他処理
            int inputFieldHorizonSize = content.dp(100);
            int inputFieldVerticalSize = content.dp(30);
            inputField.setGravity(Gravity.END);
            inputFieldText.setGravity(Gravity.CENTER_VERTICAL);
            var inputFieldParams = new LinearLayout.LayoutParams(inputFieldHorizonSize, inputFieldVerticalSize);
            inputFieldParams.setMarginsRelative(content.dp(4), content.dp(4), content.dp(16), content.dp(4));
            inputFieldColumn.addView(inputField, inputFieldParams);
            content.addView(inputFieldColumn, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
        
        //contentの最終設定
        scroll.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, root.dp(320), Gravity.CENTER));
        scroll.addView(content);
        root.addView(scroll);

        return root;
    }

    @Override
    public boolean hasDefaultBackground() {
        return false;
    }
}
