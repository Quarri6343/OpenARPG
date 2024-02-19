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
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.joml.Vector3f;
import quarri6343.openarpg.Network;
import quarri6343.openarpg.combat.DebugMonsterSpawnPacket;

import java.util.Objects;
import java.util.Random;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MonsterSummonUI extends Fragment implements ScreenCallback {

    public static final int ID_TAB_CONTAINER = 0x0003;
    private static final String TITLE = "デバッグ用モンスター召喚";

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

        {
            var buttonColumn = new LinearLayout(requireContext());
            buttonColumn.setOrientation(LinearLayout.HORIZONTAL);
            buttonColumn.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

            //ボタンの横のテキスト
            var buttonText = new TextView(requireContext());
            String displayName = "ゾンビ";
            buttonText.setText(Objects.nonNull(displayName) ? displayName : "");
            int sideTextHorizonSize = content.dp(320);
            int sideTextVerticalSize = content.dp(30);
            var buttonparams = new LinearLayout.LayoutParams(sideTextHorizonSize, sideTextVerticalSize);
            buttonparams.setMarginsRelative(content.dp(16), content.dp(8), content.dp(16), content.dp(8));
            buttonText.setGravity(Gravity.START);
            buttonText.setGravity(Gravity.CENTER_VERTICAL);

            buttonColumn.addView(buttonText, buttonparams);

            //ボタン
            var button = new Button(requireContext());
            //ボタンが入力された時の処理
            button.setOnClickListener(v -> {
                Level world = Minecraft.getInstance().level;
                BlockPos playerPos = Minecraft.getInstance().player.blockPosition();
                
                BlockPos spawnPos = findSafeSpawnPos(world, playerPos);

                Network.sendToServer(new DebugMonsterSpawnPacket("minecraft:zombie", new Vector3f(spawnPos.getX() + 0.5f, spawnPos.getY(), spawnPos.getZ() + 0.5f)));
            });
            //ボタンの初期テキスト
            button.setText("スポーン", TextView.BufferType.EDITABLE);
            //ボタンのその他処理
            int buttonHorizonSize = content.dp(100);
            int buttonVerticalSize = content.dp(30);
            button.setGravity(Gravity.END);
            buttonText.setGravity(Gravity.CENTER_VERTICAL);
            var buttonParams = new LinearLayout.LayoutParams(buttonHorizonSize, buttonVerticalSize);
            buttonParams.setMarginsRelative(content.dp(4), content.dp(4), content.dp(16), content.dp(4));
            buttonColumn.addView(button, buttonParams);
            content.addView(buttonColumn, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
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

    private static BlockPos findSafeSpawnPos(Level world, BlockPos center) {
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            int offsetX = random.nextInt(11) - 5;
            int offsetZ = random.nextInt(11) - 5; 
            BlockPos spawnPos = center.offset(offsetX, 0, offsetZ);

            // 安全な場所かどうかをチェック
            if (isSafeSpawnPos(world, spawnPos)) {
                return spawnPos;
            }
        }
        
        return center;
    }

    private static boolean isSafeSpawnPos(Level world, BlockPos pos) {
        // ブロックが空気であることを確認し、上にブロックがないことをチェック
        return world.isEmptyBlock(pos) && world.isEmptyBlock(pos.above());
    }
}
