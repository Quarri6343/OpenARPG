package quarri6343.openarpg.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quarri6343.openarpg.Config;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {

    @Inject(method = "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void tesselateBlock(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, RandomSource pRandom, long pSeed, int pPackedOverlay, ModelData modelData, RenderType renderType, CallbackInfo ci) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }
        //描画ブロックを毎秒更新し、このイベントに誘導する
        Camera cam = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera;
        Vec3 camBlockPosVec = pPos.getCenter().subtract(cam.getPosition());
        Vec3 camPlayerPosVec = Minecraft.getInstance().player.getPosition(0).subtract(cam.getPosition()); //TODO: tick()で再取得しなくて済むように
        
        float angle = camPlayerPosVec.toVector3f().angle(camBlockPosVec.toVector3f());
        if(Math.abs(angle) > Config.getObstacleTrimmedAngle() * ((float) Math.PI / 180F)){
            return;
        }
        
        if(camBlockPosVec.lengthSqr() * Config.getObstacleTrimmedDistance() > camPlayerPosVec.lengthSqr()){
            return;
        }
        
        ci.cancel();
    }
}
