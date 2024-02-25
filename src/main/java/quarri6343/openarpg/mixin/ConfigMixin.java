package quarri6343.openarpg.mixin;

import icyllis.modernui.graphics.Color;
import icyllis.modernui.mc.Config;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quarri6343.openarpg.ui.widget.MUITooltip;

import java.util.List;

import static icyllis.modernui.ModernUI.LOGGER;
import static icyllis.modernui.ModernUI.MARKER;

@Mixin(value = Config.Client.class, remap = false)
public class ConfigMixin {

    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.BooleanValue mTooltip;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.BooleanValue mRoundedTooltip;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.BooleanValue mCenterTooltipTitle;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.BooleanValue mTooltipTitleBreak;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.BooleanValue mExactTooltipPositioning;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.ConfigValue<List<? extends String>> mTooltipFill;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.ConfigValue<List<? extends String>> mTooltipStroke;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.IntValue mTooltipCycle;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.DoubleValue mTooltipWidth;
    @Final
    @Mutable
    @Shadow
    public ForgeConfigSpec.DoubleValue mTooltipShadow;
    
    @Inject(method = "reload", at = @At(value = "TAIL"))
    private void reload(CallbackInfo ci){
        MUITooltip.sTooltip = mTooltip.get();

        List<? extends String> inColors = mTooltipFill.get();
        int color = 0xFFFFFFFF;
        for (int i = 0; i < 4; i++) {
            if (inColors != null && i < inColors.size()) {
                String s = inColors.get(i);
                try {
                    color = Color.parseColor(s);
                } catch (Exception e) {
                    LOGGER.error(MARKER, "Wrong color format for tooltip background, index: {}", i, e);
                }
            }
            MUITooltip.sFillColor[i] = color;
        }
        inColors = mTooltipStroke.get();
        color = 0xFFFFFFFF;
        for (int i = 0; i < 4; i++) {
            if (inColors != null && i < inColors.size()) {
                String s = inColors.get(i);
                try {
                    color = Color.parseColor(s);
                } catch (Exception e) {
                    LOGGER.error(MARKER, "Wrong color format for tooltip border, index: {}", i, e);
                }
            }
            MUITooltip.sStrokeColor[i] = color;
        }
        //MUITooltip.sAnimationDuration = mTooltipDuration.get();
        MUITooltip.sBorderColorCycle = mTooltipCycle.get();
        MUITooltip.sExactPositioning = mExactTooltipPositioning.get();
        MUITooltip.sRoundedShapes = mRoundedTooltip.get();
        MUITooltip.sCenterTitle = mCenterTooltipTitle.get();
        MUITooltip.sTitleBreak = mTooltipTitleBreak.get();
        MUITooltip.sBorderWidth = mTooltipWidth.get().floatValue();
        MUITooltip.sShadowRadius = mTooltipShadow.get().floatValue();
    }
}
