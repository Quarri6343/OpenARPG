package quarri6343.openarpg;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ProjectionUtil {

    private static final float REACH_DISTANCE = 30f;
    
    public static EntityHitResult rayTraceEntity(Vec3 hitVec){
        
        Vec3 position = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        Vec3 max = position.add(hitVec.x * REACH_DISTANCE, hitVec.y * REACH_DISTANCE, hitVec.z * REACH_DISTANCE);
        AABB searchBox =
                Minecraft.getInstance().player.getBoundingBox().inflate(10.0D, 10.0D, 10.0D);
        
        return ProjectileUtil.getEntityHitResult(Minecraft.getInstance().player, position, max, searchBox,
                v -> true, REACH_DISTANCE * REACH_DISTANCE);
    }
    
    public static BlockHitResult rayTrace(Vec3 hitVec) {
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        Vec3 startPos = new Vec3(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        hitVec = hitVec.multiply(100, 100, 100); // Double view range to ensure pos can be seen.
        return Minecraft.getInstance().level.clip(new ClipContextEX(startPos, startPos.add(hitVec), ClipContextEX.Block.NOTWALLORHIGHPLACE, ClipContext.Fluid.ANY, null));
    }

    //https://github.com/Muirrum/MatterOverdrive
    public static Vec3 mouseToWorldRay(int mouseX, int mouseY, int width, int height) {
        double aspectRatio = ((double) width / (double) height);
        double fov = ((Minecraft.getInstance().options.fov().get() / 2d)) * (Math.PI / 180);
        Entity renderViewEntity = Minecraft.getInstance().cameraEntity;

        double a = -((double) mouseX / (double) width - 0.5) * 2;
        double b = -((double) mouseY / (double) height - 0.5) * 2;
        double tanf = Math.tan(fov);


        float yawn = renderViewEntity.getYRot();
        float pitch = renderViewEntity.getXRot();

        Matrix4f rot = new Matrix4f();
        rot.rotate(yawn * (float) (Math.PI / 180), new Vector3f(0, -1, 0));
        rot.rotate(pitch * (float) (Math.PI / 180), new Vector3f(1, 0, 0));
        Vector4f foward = new Vector4f(0, 0, 1, 0);
        Vector4f up = new Vector4f(0, 1, 0, 0);
        Vector4f left = new Vector4f(1, 0, 0, 0);
//        Matrix4f.transform(rot, foward, foward);
//        Matrix4f.transform(rot, up, up);
//        Matrix4f.transform(rot, left, left);
        rot.transform(foward);
        rot.transform(up);
        rot.transform(left);

        return new Vec3(foward.x, foward.y, foward.z)
                .add(left.x * tanf * aspectRatio * a, left.y * tanf * aspectRatio * a, left.z * tanf * aspectRatio * a)
                .add(up.x * tanf * b, up.y * tanf * b, up.z * tanf * b)
                .normalize();
    }

    public static Vec3 worldToScreen(Vec3 pos) {
        Minecraft mc = Minecraft.getInstance();
        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 o = cam.getPosition();

        Vector3f pos1 = new Vector3f((float) (o.x - pos.x), (float) (o.y - pos.y), (float) (o.z - pos.z));
        Quaternionf rot = new Quaternionf();
        rot.set(cam.rotation());
        rot.conjugate();
        pos1.rotate(rot);

        Window w = mc.getWindow();
        float sc = w.getGuiScaledHeight() / 2f / pos1.z() / (float) Math.tan(Math.toRadians((mc.options.fov().get() / 2f)));
        pos1.mul(-sc, -sc, 1f);
        pos1.add(w.getGuiScaledWidth() / 2f, w.getGuiScaledHeight() / 2f, 0f);

        return new Vec3(pos1);
    }

    /**
     * clipするとき専用のコリジョン判定を行えるようにするためのクラス
     */
    public static class ClipContextEX extends ClipContext {

        private final ProjectionUtil.ClipContextEX.Block block2;

        private final CollisionContext collisionContext2;
        
        public ClipContextEX(Vec3 pFrom, Vec3 pTo, ProjectionUtil.ClipContextEX.Block pBlock, Fluid pFluid, @Nullable Entity pEntity) {
            super(pFrom, pTo, null, pFluid, pEntity);
            this.block2 = pBlock;
            this.collisionContext2 = pEntity == null ? CollisionContext.empty() : CollisionContext.of(pEntity);
        }

        @Override
        public VoxelShape getBlockShape(BlockState pBlockState, BlockGetter pLevel, BlockPos pPos) {
            return this.block2.get(pBlockState, pLevel, pPos, this.collisionContext2);
        }

        public static enum Block implements ClipContext.ShapeGetter {
            NOTWALLORHIGHPLACE((pState, pBlock, pPos, pCollisionContext) -> {
                if(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null){
                    return Shapes.empty();
                }

                if(!Minecraft.getInstance().level.getBlockState(pPos.above()).getShape(Minecraft.getInstance().level, pPos).isEmpty()){ //上のブロックに実体がある時、それは壁なので無視してスキャンを続ける
                    return Shapes.empty();
                }
                
                if(pPos.getY() > Minecraft.getInstance().player.getY() + 3){ //ブロックがプレイヤーに対して高過ぎるとき、無視してスキャンを続ける
                    return Shapes.empty();
                }

                return pState.getShape(Minecraft.getInstance().level, pPos);
            });

            private final ClipContext.ShapeGetter shapeGetter;

            private Block(ClipContext.ShapeGetter pShapeGetter) {
                this.shapeGetter = pShapeGetter;
            }

            public VoxelShape get(BlockState pState, BlockGetter pBlock, BlockPos pPos, CollisionContext pCollisionContext) {
                return this.shapeGetter.get(pState, pBlock, pPos, pCollisionContext);
            }
        }
    }
}
