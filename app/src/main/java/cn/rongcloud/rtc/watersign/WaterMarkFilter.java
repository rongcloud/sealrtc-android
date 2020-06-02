package cn.rongcloud.rtc.watersign;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import cn.rongcloud.rtc.GlTextureFrameBuffer;
import cn.rongcloud.rtc.GlUtil;

/** Created by wangw on 2019/5/8. */
public class WaterMarkFilter {

    private static final String TAG = "WaterMarkFilter";
    private CommonFilter mFrame;
    private CommonFilter mWaterSign;
    private int mWaterTexId;
    private GlTextureFrameBuffer mTextureFilter;
    private int mWaterWidth;
    private int mWaterHeight;
    private int mDisplayRotation;
    private Display mDisplay;
    private int mX;
    private int mY;

    public WaterMarkFilter(Context context, boolean isFrontCamera, Bitmap waterBmp) {
        mTextureFilter = new GlTextureFrameBuffer(GLES20.GL_RGBA);
        mWaterSign = new CommonFilter();
        mWaterSign.setShaderProgram(new CommonProgram());

        mFrame = new CommonFilter();
        mFrame.setShaderProgram(new CommonProgram());

        mWaterWidth = waterBmp.getWidth();
        mWaterHeight = waterBmp.getHeight();
        mWaterTexId = TextureHelper.loadTexture(waterBmp);
        mDisplay =
                ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
        angleChange(isFrontCamera);
    }

    public void angleChange(boolean frontCamera) {
        //        mDisplayRotation =
        // ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        if (mWaterSign != null && mDisplay != null)
            mWaterSign.angleChange(mDisplay.getRotation(), frontCamera);
    }

    public void release() {
        if (mWaterSign != null) mWaterSign.release();
        if (mFrame != null) mFrame.release();
        if (mTextureFilter != null) mTextureFilter.release();
        mDisplay = null;
    }

    public void drawFrame(int width, int height, int textureID, boolean isFrontCamera) {
        mTextureFilter.setSize(width, height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mTextureFilter.getFrameBufferId());
        GlUtil.checkNoGLES2Error("glBindFramebuffer");
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glViewport(0, 0, width, height);
        mFrame.onDraw(textureID);
        onDrawWater(width, height, isFrontCamera);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void onDrawWater(int width, int height, boolean isFrontCamera) {
        if (isFrontCamera) {
            return;
        }
        if (mDisplay != null && mDisplay.getRotation() != mDisplayRotation) {
            mDisplayRotation = mDisplay.getRotation();
            mWaterSign.angleChange(mDisplayRotation, isFrontCamera);
        }
        switch (mDisplayRotation) {
            case Surface.ROTATION_90:
                mX = width - mWaterWidth;
                mY = height - mWaterHeight;
                break;
            case Surface.ROTATION_270:
                mX = 0;
                mY = 0;
                break;
            case Surface.ROTATION_180:
            case Surface.ROTATION_0:
            default:
                mX = 0;
                mY = height - mWaterHeight;
                break;
        }

        GLES20.glViewport(mX, mY, mWaterWidth, mWaterHeight);
        mWaterSign.onDraw(mWaterTexId);
    }

    public int getTextureID() {
        return mTextureFilter.getTextureId();
    }
}
