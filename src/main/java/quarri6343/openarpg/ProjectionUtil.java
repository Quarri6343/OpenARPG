package quarri6343.openarpg;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ProjectionUtil {

    private static final float[] mat4Tmp1 = new float[16];
    private static final float[] mat4Tmp2 = new float[16];
    private final float[] mat4Tmp3 = new float[16];
    
    private static final FloatBuffer MODELVIEW_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final FloatBuffer PROJECTION_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder())
            .asIntBuffer();
    protected static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
            .asFloatBuffer();
    protected static final FloatBuffer OBJECT_POS_BUFFER = ByteBuffer.allocateDirect(3 * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();
    
    public static Vector3f unProject(int mouseX, int mouseY) {
        // read depth of pixel under mouse
        GL11.glReadPixels(mouseX, mouseY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER);

        // rewind buffer after write by glReadPixels
        PIXEL_DEPTH_BUFFER.rewind();

        // retrieve depth from buffer (0.0-1.0f)
        float pixelDepth = PIXEL_DEPTH_BUFFER.get();

        // rewind buffer after read
        PIXEL_DEPTH_BUFFER.rewind();

        // read current rendering parameters
//        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
//        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        OpenARPG.viewModelMatrix.get(MODELVIEW_MATRIX_BUFFER);
        OpenARPG.projectionMatrix.get(PROJECTION_MATRIX_BUFFER);
        
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        // rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        // call gluUnProject with retrieved parameters
        gluUnProject(
                mouseX,
                mouseY,
                pixelDepth,
                MODELVIEW_MATRIX_BUFFER,
                PROJECTION_MATRIX_BUFFER,
                VIEWPORT_BUFFER,
                OBJECT_POS_BUFFER);

        // rewind buffers after read by gluUnProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        // rewind buffer after write by gluUnProject
        OBJECT_POS_BUFFER.rewind();

        // obtain absolute position in world
        float posX = OBJECT_POS_BUFFER.get();
        float posY = OBJECT_POS_BUFFER.get();
        float posZ = OBJECT_POS_BUFFER.get();

        // rewind buffer after read
        OBJECT_POS_BUFFER.rewind();
        return new Vector3f(posX, posY, posZ);
    }

    //from:org.jogamp.jogl:jogl-all:2.3.2
    //Copyright 2009-2024 JogAmp Community. All rights reserved.
    public static boolean gluUnProject(final float winx, final float winy, final float winz,
                                final FloatBuffer modelMatrix,
                                final FloatBuffer projMatrix,
                                final IntBuffer viewport,
                                final FloatBuffer obj_pos) {
        final int vPos = viewport.position();
        final int oPos = obj_pos.position();

        // mat4Tmp1 = P x M
        multMatrix(projMatrix, modelMatrix, mat4Tmp1);

        // mat4Tmp1 = Inv(P x M)
        if ( null == invertMatrix(mat4Tmp1, mat4Tmp1) ) {
            return false;
        }

        mat4Tmp2[0] = winx;
        mat4Tmp2[1] = winy;
        mat4Tmp2[2] = winz;
        mat4Tmp2[3] = 1.0f;

        // Map x and y from window coordinates
        mat4Tmp2[0] = (mat4Tmp2[0] - viewport.get(0+vPos)) / viewport.get(2+vPos);
        mat4Tmp2[1] = (mat4Tmp2[1] - viewport.get(1+vPos)) / viewport.get(3+vPos);

        // Map to range -1 to 1
        mat4Tmp2[0] = mat4Tmp2[0] * 2 - 1;
        mat4Tmp2[1] = mat4Tmp2[1] * 2 - 1;
        mat4Tmp2[2] = mat4Tmp2[2] * 2 - 1;

        final int raw_off = 4;
        // object raw coords = Inv(P x M) *  winPos  -> mat4Tmp2
        multMatrixVec(mat4Tmp1, 0, mat4Tmp2, 0, mat4Tmp2, raw_off);

        if (mat4Tmp2[3+raw_off] == 0.0) {
            return false;
        }

        mat4Tmp2[3+raw_off] = 1.0f / mat4Tmp2[3+raw_off];

        obj_pos.put(0+oPos, mat4Tmp2[0+raw_off] * mat4Tmp2[3+raw_off]);
        obj_pos.put(1+oPos, mat4Tmp2[1+raw_off] * mat4Tmp2[3+raw_off]);
        obj_pos.put(2+oPos, mat4Tmp2[2+raw_off] * mat4Tmp2[3+raw_off]);

        return true;
    }

    public static void multMatrix(final FloatBuffer a, final FloatBuffer b, final float[] d) {
        final int a_off = a.position();
        final int b_off = b.position();
        for (int i = 0; i < 4; i++) {
            // one row in column-major order
            final int a_off_i = a_off+i;
            final float ai0=a.get(a_off_i+0*4),  ai1=a.get(a_off_i+1*4),  ai2=a.get(a_off_i+2*4),  ai3=a.get(a_off_i+3*4); // row-i of a
            d[i+0*4] = ai0 * b.get(b_off+0+0*4) + ai1 * b.get(b_off+1+0*4) + ai2 * b.get(b_off+2+0*4) + ai3 * b.get(b_off+3+0*4) ;
            d[i+1*4] = ai0 * b.get(b_off+0+1*4) + ai1 * b.get(b_off+1+1*4) + ai2 * b.get(b_off+2+1*4) + ai3 * b.get(b_off+3+1*4) ;
            d[i+2*4] = ai0 * b.get(b_off+0+2*4) + ai1 * b.get(b_off+1+2*4) + ai2 * b.get(b_off+2+2*4) + ai3 * b.get(b_off+3+2*4) ;
            d[i+3*4] = ai0 * b.get(b_off+0+3*4) + ai1 * b.get(b_off+1+3*4) + ai2 * b.get(b_off+2+3*4) + ai3 * b.get(b_off+3+3*4) ;
        }
    }

    public static float[] multMatrixVec(final float[] m_in, final int m_in_off,
                                        final float[] v_in, final int v_in_off,
                                        final float[] v_out, final int v_out_off) {
        // (one matrix row in column-major order) X (column vector)
        v_out[0 + v_out_off] = v_in[0+v_in_off] * m_in[0*4+m_in_off  ]  +  v_in[1+v_in_off] * m_in[1*4+m_in_off  ] +
                v_in[2+v_in_off] * m_in[2*4+m_in_off  ]  +  v_in[3+v_in_off] * m_in[3*4+m_in_off  ];

        final int m_in_off_1 = 1+m_in_off;
        v_out[1 + v_out_off] = v_in[0+v_in_off] * m_in[0*4+m_in_off_1]  +  v_in[1+v_in_off] * m_in[1*4+m_in_off_1] +
                v_in[2+v_in_off] * m_in[2*4+m_in_off_1]  +  v_in[3+v_in_off] * m_in[3*4+m_in_off_1];

        final int m_in_off_2 = 2+m_in_off;
        v_out[2 + v_out_off] = v_in[0+v_in_off] * m_in[0*4+m_in_off_2]  +  v_in[1+v_in_off] * m_in[1*4+m_in_off_2] +
                v_in[2+v_in_off] * m_in[2*4+m_in_off_2]  +  v_in[3+v_in_off] * m_in[3*4+m_in_off_2];

        final int m_in_off_3 = 3+m_in_off;
        v_out[3 + v_out_off] = v_in[0+v_in_off] * m_in[0*4+m_in_off_3]  +  v_in[1+v_in_off] * m_in[1*4+m_in_off_3] +
                v_in[2+v_in_off] * m_in[2*4+m_in_off_3]  +  v_in[3+v_in_off] * m_in[3*4+m_in_off_3];

        return v_out;
    }

    public static float[] invertMatrix(final float[] msrc, final float[] mres) {
        final float scale;
        {
            float max = Math.abs(msrc[0]);

            for( int i = 1; i < 16; i++ ) {
                final float a = Math.abs(msrc[i]);
                if( a > max ) max = a;
            }
            if( 0 == max ) {
                return null;
            }
            scale = 1.0f/max;
        }

        final float a11 = msrc[0+4*0]*scale;
        final float a21 = msrc[1+4*0]*scale;
        final float a31 = msrc[2+4*0]*scale;
        final float a41 = msrc[3+4*0]*scale;
        final float a12 = msrc[0+4*1]*scale;
        final float a22 = msrc[1+4*1]*scale;
        final float a32 = msrc[2+4*1]*scale;
        final float a42 = msrc[3+4*1]*scale;
        final float a13 = msrc[0+4*2]*scale;
        final float a23 = msrc[1+4*2]*scale;
        final float a33 = msrc[2+4*2]*scale;
        final float a43 = msrc[3+4*2]*scale;
        final float a14 = msrc[0+4*3]*scale;
        final float a24 = msrc[1+4*3]*scale;
        final float a34 = msrc[2+4*3]*scale;
        final float a44 = msrc[3+4*3]*scale;

        final float m11 = + a22*(a33*a44 - a34*a43) - a23*(a32*a44 - a34*a42) + a24*(a32*a43 - a33*a42);
        final float m12 = -( + a21*(a33*a44 - a34*a43) - a23*(a31*a44 - a34*a41) + a24*(a31*a43 - a33*a41));
        final float m13 = + a21*(a32*a44 - a34*a42) - a22*(a31*a44 - a34*a41) + a24*(a31*a42 - a32*a41);
        final float m14 = -( + a21*(a32*a43 - a33*a42) - a22*(a31*a43 - a33*a41) + a23*(a31*a42 - a32*a41));
        final float m21 = -( + a12*(a33*a44 - a34*a43) - a13*(a32*a44 - a34*a42) + a14*(a32*a43 - a33*a42));
        final float m22 = + a11*(a33*a44 - a34*a43) - a13*(a31*a44 - a34*a41) + a14*(a31*a43 - a33*a41);
        final float m23 = -( + a11*(a32*a44 - a34*a42) - a12*(a31*a44 - a34*a41) + a14*(a31*a42 - a32*a41));
        final float m24 = + a11*(a32*a43 - a33*a42) - a12*(a31*a43 - a33*a41) + a13*(a31*a42 - a32*a41);
        final float m31 = + a12*(a23*a44 - a24*a43) - a13*(a22*a44 - a24*a42) + a14*(a22*a43 - a23*a42);
        final float m32 = -( + a11*(a23*a44 - a24*a43) - a13*(a21*a44 - a24*a41) + a14*(a21*a43 - a23*a41));
        final float m33 = + a11*(a22*a44 - a24*a42) - a12*(a21*a44 - a24*a41) + a14*(a21*a42 - a22*a41);
        final float m34 = -( + a11*(a22*a43 - a23*a42) - a12*(a21*a43 - a23*a41) + a13*(a21*a42 - a22*a41));
        final float m41 = -( + a12*(a23*a34 - a24*a33) - a13*(a22*a34 - a24*a32) + a14*(a22*a33 - a23*a32));
        final float m42 = + a11*(a23*a34 - a24*a33) - a13*(a21*a34 - a24*a31) + a14*(a21*a33 - a23*a31);
        final float m43 = -( + a11*(a22*a34 - a24*a32) - a12*(a21*a34 - a24*a31) + a14*(a21*a32 - a22*a31));
        final float m44 = + a11*(a22*a33 - a23*a32) - a12*(a21*a33 - a23*a31) + a13*(a21*a32 - a22*a31);

        final float det = (a11*m11 + a12*m12 + a13*m13 + a14*m14)/scale;

        if( 0 == det ) {
            return null;
        }

        mres[0+4*0] = m11 / det;
        mres[1+4*0] = m12 / det;
        mres[2+4*0] = m13 / det;
        mres[3+4*0] = m14 / det;
        mres[0+4*1] = m21 / det;
        mres[1+4*1] = m22 / det;
        mres[2+4*1] = m23 / det;
        mres[3+4*1] = m24 / det;
        mres[0+4*2] = m31 / det;
        mres[1+4*2] = m32 / det;
        mres[2+4*2] = m33 / det;
        mres[3+4*2] = m34 / det;
        mres[0+4*3] = m41 / det;
        mres[1+4*3] = m42 / det;
        mres[2+4*3] = m43 / det;
        mres[3+4*3] = m44 / det;
        return mres;
    }

    public static BlockHitResult rayTrace(Vector3f hitPos, Entity startPoint) {
        Camera camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        Vec3 startPos = new Vec3(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        LogUtils.getLogger().debug("eyelocation:" + startPoint.getEyePosition() + " location" + startPoint.getX() + ":" + startPoint.getY() + ":" +startPoint.getZ() + ":");
        hitPos.mul(2); // Double view range to ensure pos can be seen.
        Vec3 endPos = new Vec3(
                (hitPos.x - startPos.x),
                (hitPos.y - startPos.y),
                (hitPos.z - startPos.z));
        return Minecraft.getInstance().level.clip(new ClipContext(startPos, new Vec3(hitPos), ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, null));
    }
}
