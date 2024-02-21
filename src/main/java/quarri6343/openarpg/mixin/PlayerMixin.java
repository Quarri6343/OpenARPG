package quarri6343.openarpg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * プレイヤーの移動速度がAttribute依存にハードコードされていて、mod側でカスタマイズできないのを変更
 * Attributeはログインや装備変更、ポーション飲用ごとにリセットされるはずの為、流用するには再設定の手間が現実的ではない
 */
@Mixin(Player.class)
abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * @author Quarri6343
     * @reason see class description
     */
    @Overwrite
    public float getSpeed() {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
        }
        return super.getSpeed();
    }
}
