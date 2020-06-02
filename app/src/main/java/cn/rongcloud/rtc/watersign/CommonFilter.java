package cn.rongcloud.rtc.watersign;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.Surface;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/** Created by wangw on 2019/5/8. */
public class CommonFilter {

    public static final int SIZE_OF_FLOAT = 4;

    /** 模型顶点坐标 */
    public static final float[] FULL_RECTANGLE_COORDS = {
        -1f,
        -1f, // 0 左下角
        1f,
        -1f, // 1 右下角
        -1f,
        1f, // 2 左上角
        1f,
        1f // 3 右上角
    };

    private static final float[] FULL_RECTANGLE_TEX_COORDS = {
        0.0f, 0.0f, // Bottom left.
        1.0f, 0.0f, // Bottom right.
        0.0f, 1.0f, // Top left.
        1.0f, 1.0f // Top right.
    };

    protected final FloatBuffer mVertexArray;
    protected final FloatBuffer mTexCoordArray;
    protected final int mCoordsPerVertex;
    protected final int mCoordsPerTexture;
    protected final int mVertexCount;
    protected final int mTexCoordStride;
    protected final int mVertexStride;
    protected CommonProgram mProgram;
    public float[] mProjectionMatrix = new float[16]; // 投影矩阵

    public CommonFilter() {
        mVertexArray = createFloatBuffer(FULL_RECTANGLE_COORDS);
        mTexCoordArray = createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
        mCoordsPerVertex = 2;
        mCoordsPerTexture = 2;
        mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex; // 4
        mTexCoordStride = 2 * SIZE_OF_FLOAT;
        mVertexStride = 2 * SIZE_OF_FLOAT;

        resetMatrix();
        //        Matrix.setIdentityM(mProjectionMatrix, 0);
        //        Matrix.setIdentityM(mViewMatrix, 0);
        //        Matrix.setIdentityM(mModelMatrix, 0);
        //        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    protected FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZE_OF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    public void setShaderProgram(CommonProgram program) {
        mProgram = program;
    }

    public void resetMatrix() {
        Matrix.setIdentityM(mProjectionMatrix, 0);
    }

    public void setAngle(float angle, float x, float y, float z) {
        Matrix.rotateM(mProjectionMatrix, 0, angle, x, y, z);
    }

    public void angleChange(int rotaion, boolean isFrontCamera) {
        resetMatrix();
        switch (rotaion) {
            case Surface.ROTATION_0:
                if (!isFrontCamera) {
                    setAngle(90, 0, 0, 1);
                    setAngle(180, 1, 0, 0);
                } else {
                    setAngle(180f, 1f, 0f, 0f);
                    setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                if (!isFrontCamera) {
                    setAngle(180, 0, 0, 1);
                    setAngle(180, 0, 1, 0);
                } else {
                    setAngle(180f, 1f, 0f, 0f);
                }
                break;
            case Surface.ROTATION_180:
                if (!isFrontCamera) {
                    setAngle(90f, 0.0f, 0f, 1f);
                    setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                if (!isFrontCamera) {
                    setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    setAngle(180f, 0f, 1f, 0f);
                }
                break;
        }
    }

    //    public float[] mViewMatrix = new float[16]; // 摄像机位置朝向9参数矩阵
    //    public float[] mModelMatrix = new float[16];// 模型变换矩阵
    //    public float[] mMVPMatrix = new float[16];// 获取具体物体的总变换矩阵
    //    private float[] getFinalMatrix() {
    //        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
    //        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
    //        return mMVPMatrix;
    //    }

    public void onDraw(int textureId) {
        glUseProgram(mProgram.getShaderProgramId());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(mProgram.sTextureLoc, 0);
        GlUtil.checkGlError("GL_TEXTURE_2D sTexture");

        GLES20.glUniformMatrix4fv(mProgram.uMVPMatrixLoc, 1, false, mProjectionMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv uMVPMatrixLoc");

        // 设置模型顶点坐标数据
        GLES20.glEnableVertexAttribArray(mProgram.aPositionLoc);
        GLES20.glVertexAttribPointer(
                mProgram.aPositionLoc,
                mCoordsPerVertex,
                GLES20.GL_FLOAT,
                false,
                mVertexStride,
                mVertexArray);
        GlUtil.checkGlError("VAO aPositionLoc");

        // 设置纹理顶点坐标数据
        GLES20.glEnableVertexAttribArray(mProgram.aTextureCoordLoc);
        GLES20.glVertexAttribPointer(
                mProgram.aTextureCoordLoc,
                mCoordsPerTexture,
                GLES20.GL_FLOAT,
                false,
                mTexCoordStride,
                mTexCoordArray);
        GlUtil.checkGlError("VAO aTextureCoordLoc");

        glDrawArrays(GL_TRIANGLE_STRIP, 0, mVertexCount);
        glDisableVertexAttribArray(mProgram.aPositionLoc);
        glDisableVertexAttribArray(mProgram.aTextureCoordLoc);
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    public void release() {
        if (mProgram != null) mProgram.release();
    }
}
