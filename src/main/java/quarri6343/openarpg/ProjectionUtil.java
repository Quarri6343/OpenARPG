package quarri6343.openarpg;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ProjectionUtil {

    public static BlockHitResult rayTrace(Vec3 hitPos, Entity startPoint) {
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        Vec3 startPos = new Vec3(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        hitPos = hitPos.multiply(100, 100, 100); // Double view range to ensure pos can be seen.
        Vec3 endPos = new Vec3(
                (hitPos.x - startPos.x),
                (hitPos.y - startPos.y),
                (hitPos.z - startPos.z));
        return Minecraft.getInstance().level.clip(new ClipContext(startPos, startPos.add(hitPos), ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, null));
    }
    
    //https://github.com/Muirrum/MatterOverdrive
    public static Vec3 mouseToWorldRay(int mouseX, int mouseY, int width, int height)
    {
        double aspectRatio = ((double)width / (double)height);
        double fov = ((Minecraft.getInstance().options.fov().get() / 2d)) * (Math.PI / 180);
        Entity renderViewEntity = Minecraft.getInstance().cameraEntity;

        double a = -((double)mouseX / (double)width - 0.5) * 2;
        double b = -((double)mouseY / (double)height - 0.5) * 2;
        double tanf = Math.tan(fov);


        float yawn = renderViewEntity.getYRot();
        float pitch = renderViewEntity.getXRot();

        Matrix4f rot = new Matrix4f();
        rot.rotate(yawn * (float)(Math.PI / 180), new Vector3f(0, -1, 0));
        rot.rotate(pitch * (float)(Math.PI / 180), new Vector3f(1, 0, 0));
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
}
