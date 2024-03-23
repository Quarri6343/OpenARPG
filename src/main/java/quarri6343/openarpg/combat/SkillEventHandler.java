package quarri6343.openarpg.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import quarri6343.openarpg.Network;
import quarri6343.openarpg.OpenARPG;
import quarri6343.openarpg.ProjectionUtil;

import java.util.Map;

import static quarri6343.openarpg.OpenARPG.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class SkillEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }

        for (Map.Entry<Integer, Skills> keySkillsEntry : OpenARPG.keySkillMap.entrySet()) {
            if(event.getButton() == keySkillsEntry.getKey()){
                if (tryExecuteSkill(keySkillsEntry.getValue())) {
                    event.setCanceled(true);
                }
                
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.Key event) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson() || Minecraft.getInstance().screen != null) {
            return;
        }

        for (Map.Entry<Integer, Skills> keySkillsEntry : OpenARPG.keySkillMap.entrySet()) {
            if(event.getKey() == keySkillsEntry.getKey()){
                tryExecuteSkill(keySkillsEntry.getValue());
                return;
            }
        }
    }

    /**
     * クリックされた場所に対してスキルを行う
     *
     * @param skill 行うスキル
     * @return 成功かどうか
     */
    public static boolean tryExecuteSkill(Skills skill) {
        double xPos = (int) Minecraft.getInstance().mouseHandler.xpos();
        double yPos = (int) Minecraft.getInstance().mouseHandler.ypos();

        Vec3 hitVec = ProjectionUtil.mouseToWorldRay((int) xPos, (int) yPos, Minecraft.getInstance().getWindow().getScreenWidth(), Minecraft.getInstance().getWindow().getScreenHeight());

        EntityHitResult entityHitResult = ProjectionUtil.rayTraceEntity(hitVec);
        if(skill == Skills.ATTACK) { //TODO: Attackというスキルクラスを実装してそこに処理を移動
            if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
                if (entityHitResult.getEntity().distanceToSqr(Minecraft.getInstance().player) >= Mth.square(Minecraft.getInstance().player.getEntityReach())) { //TODO: 射程の実装をスキル側に委ねる
                    return false;
                }

                Network.sendToServer(new PlayerSkillPacket(entityHitResult.getEntity(), null, skill));
                return true;
            }

            return false;
        }
        else {
            Entity targetEntity = null;
            if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
                targetEntity = entityHitResult.getEntity();
            }

            BlockHitResult result = ProjectionUtil.rayTraceBlock(hitVec);
            Vec3 hitLocation = null;
            if (result.getType() != HitResult.Type.MISS) {
                hitLocation = result.getLocation();
            }

            //TODO: スキルが実行可能かをスキル側に委ねる
            Network.sendToServer(new PlayerSkillPacket(targetEntity, hitLocation, skill));
            return true;
        }
    }
}
