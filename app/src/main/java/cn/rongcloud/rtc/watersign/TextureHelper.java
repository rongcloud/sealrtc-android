package cn.rongcloud.rtc.watersign;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

/** Created by wangw on 2019/5/5. */
public class TextureHelper {

    private static final String TAG = "TextureHelper";

    /**
     * 加载纹理
     *
     * @param context
     * @param resId
     * @return
     */
    public static int loadTexture(Context context, int resId) {
        // 加载位图
        Bitmap bmp = loadBitmap(context, resId);
        return loadTexture(bmp);
    }

    public static int loadTexture(Bitmap bmp) {
        // 生成纹理ID
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            Log.e(TAG, "loadTexture: texture Id is 0");
            return 0;
        }
        if (bmp == null) {
            Log.e(TAG, "loadTexture: BitMap is NUll");
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        // 绑定纹理ID
        // 参数1：告诉OPenGl这个纹理是个二维纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
        // 设置过滤方式
        // GL_TEXTURE_MIN_FILTER：表示缩小时使用的过滤方式GL_LINEAR_MIPMAP_LINEAR（MIP贴图级别直接插值的最近邻过滤）
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        // GL_TEXTURE_MAG_FILTER: 表示放大时使用的过滤方式GL_LINEAR(双线性过滤)
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // 加载纹理到OpenGl并返回其ID
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        // 释放BitMap图像
        bmp.recycle();
        // 生成MIP贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        // 完成纹理加载后就可以解绑这个纹理了，以免调用意外调用其他方法改变这个纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureObjectIds[0];
    }

    public static float[] flip(float[] m, boolean x, boolean y) {
        if (x || y) {
            Matrix.scaleM(m, 0, x ? -1 : 1, y ? -1 : 1, 1);
        }
        return m;
    }

    public static Bitmap loadBitmap(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }
}
