package quarri6343.openarpg.ui;

import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.core.Core;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.graphics.drawable.LayerDrawable;
import icyllis.modernui.graphics.drawable.ScaleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.resources.SystemTheme;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.AbsoluteLayout;
import icyllis.modernui.widget.ProgressBar;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class TestHUD extends Fragment implements ScreenCallback {
    
    private static ProgressBar healthBar;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @icyllis.modernui.annotation.Nullable ViewGroup container,
                             @icyllis.modernui.annotation.Nullable DataSet savedInstanceState) {
        
        //絶対値でviewの座標を決める親viewの作成
        var root = new AbsoluteLayout(requireContext());
        root.setLayoutParams(new AbsoluteLayout.LayoutParams(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), 0, 0));

        {
            //ヘルスバー宣言
            healthBar = new ProgressBar(requireContext());
            healthBar.setMin(0);
            healthBar.setMax(20);
            
            {
                //ヘルスバーの赤い部分のレンダラの定義
                final Drawable track;
                {
                    var shape = new ShapeDrawable();
                    shape.setShape(ShapeDrawable.RECTANGLE);
                    shape.setSize(-1, root.dp(10));
                    shape.setColor(0xFFFF0000);
                    track = new ScaleDrawable(shape, Gravity.LEFT, 1, -1);
                }
                //ヘルスバーの裏の黒い部分の定義
                final Drawable secondaryTrack;
                {
                    var shape = new ShapeDrawable();
                    shape.setShape(ShapeDrawable.RECTANGLE);
                    shape.setSize(-1, root.dp(10));
                    shape.setColor(SystemTheme.COLOR_FOREGROUND_DISABLED);
                    secondaryTrack = shape;
                }
                //ヘルスバーにレンダラをセット
                var progress = new LayerDrawable(secondaryTrack, track);
                progress.setId(0, R.id.secondaryProgress);
                progress.setId(1, R.id.progress);
                healthBar.setProgressDrawable(progress);
            }

            //ヘルスバーの現在値を定義
            int health = (int) Minecraft.getInstance().player.getHealth();
            healthBar.setProgress(health, true);

            //ヘルスバーを絶対座標系に配置
            int healthBarHorizonSize = root.dp(100);
            int healthBarVerticalSize = root.dp(30);
            root.addView(healthBar, new AbsoluteLayout.LayoutParams(healthBarHorizonSize, healthBarVerticalSize, root.dp(40), Minecraft.getInstance().getWindow().getHeight() - root.dp(50)));
        }
        return root;
    }

    @Override
    public boolean hasDefaultBackground() {
        return false;
    }

    @SubscribeEvent
    public static void onRenderTick(@Nonnull TickEvent.RenderTickEvent event) {
        //UIスレッドでGUIを更新しないとクラッシュする
        Core.executeOnUiThread(() -> {
            if(healthBar == null || Minecraft.getInstance().player == null)
                return;
            healthBar.setProgress((int)Minecraft.getInstance().player.getHealth(), true);
        });
    }
    
    @Override
    public boolean shouldClose() {
        return false;
    }
}
