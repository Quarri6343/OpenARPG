package quarri6343.openarpg.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiPlayerGameMode.class)
public class MutliPlayerGameModeMixin {

    @Shadow
    private GameType localPlayerMode;

    /**
     * @author Quarri6343
     * @reason 3人称カメラで経験値バーが表示されないようにする(リソパで消してもいいがドローコールが残るので無駄)
     */
    @Overwrite
    public boolean hasExperience() {
        return this.localPlayerMode.isSurvival() && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }
}
