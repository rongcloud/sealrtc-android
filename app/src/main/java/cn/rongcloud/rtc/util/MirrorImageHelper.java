package cn.rongcloud.rtc.util;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import cn.rongcloud.rtc.GlTextureFrameBuffer;
import cn.rongcloud.rtc.GlUtil;
import cn.rongcloud.rtc.watersign.CommonFilter;
import cn.rongcloud.rtc.watersign.CommonProgram;

/**
 * Created by wangw on 2020/4/23.
 */
public class MirrorImageHelper {


  private static final String TAG = "MirrorImageHelper";
  private Display mDisplay;

  public MirrorImageHelper(Context context) {
    mDisplay =
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
            .getDefaultDisplay();
  }

  private GlTextureFrameBuffer mTextureFilter;
  private CommonFilter mFrame;
  private byte[] mDstData;

  /**
   * 镜像翻转 texture 数据
   *
   * @param textureID Texture类型必须为 GL_TEXTURE_2D
   * @param width
   * @param height
   * @return 翻转后的 textureID
   */
  public int onMirrorImage(int textureID, int width, int height) {
    if (textureID == 0 || width == 0 || height == 0) {
      Log.e(TAG, "onMirrorImage: textureID is Null");
      return textureID;
    }
    if (mFrame == null) {
      initGlRender();
    }
    mTextureFilter.setSize(width, height);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mTextureFilter.getFrameBufferId());
    GlUtil.checkNoGLES2Error("glBindFramebuffer");
    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

    GLES20.glViewport(0, 0, width, height);
    Log.d(TAG, "drawFrame: " + mDisplay.getRotation());
    mFrame.resetMatrix();
    switch (mDisplay.getRotation()) {
      case Surface.ROTATION_0:
      case Surface.ROTATION_180:
        Matrix.scaleM(mFrame.mProjectionMatrix, 0, 1f, -1f, 1f);
        break;
      case Surface.ROTATION_90:
      case Surface.ROTATION_270:
        Matrix.scaleM(mFrame.mProjectionMatrix, 0, -1f, 1f, 1f);
        break;
    }
    mFrame.onDraw(textureID);
    GLES20.glDisable(GLES20.GL_BLEND);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    return mTextureFilter.getTextureId();
  }

  private void initGlRender() {
    mTextureFilter = new GlTextureFrameBuffer(GLES20.GL_RGBA);
    mFrame = new CommonFilter();
    mFrame.setShaderProgram(new CommonProgram());
  }

  /**
   * 翻转 YUV 数据
   *
   * @param src    仅支持 NV21
   * @param width  视频宽
   * @param height 视频高
   * @return 翻转后的数据
   */
  public byte[] onMirrorImage(byte[] src, int width, int height) {
    if (src == null || width == 0 || height == 0) {
      Log.e(TAG, "onMirrorImage: src or dst data is Null");
      return src;
    }
    //TODO 后期改为 Texture 效率更高

    if (mDisplay.getRotation() != Surface.ROTATION_0) {

      return onHorizontalMirror(src, width, height);
    } else {
      return onVerticalMirror(src, width, height);
    }

  }

  /**
   * 垂直镜像翻转
   *
   * @param src
   * @param width
   * @param height
   */
  private byte[] onVerticalMirror(byte[] src, int width, int height) {
    //    // Y
//    for (int x = 0; x < width; x++) {
//      for (int y = 0; y < height; y++) {
//        dst[y * width + x] = src[(height - 1 - y) * width + x];
//      }
//    }
//    // UV
//    int wh = width * height;
//    int halfH = height / 2;
//    for (int x = 0; x < width; x += 2) {
//      for (int y = 0; y < halfH; y++) {
//        dst[wh + y * width + x] = src[wh + (halfH - 1 - y) * width + x];
//        dst[wh + y * width + x + 1] = src[wh + (halfH - 1 - y) * width + x + 1];
//      }
//    }
    /**
     * 抛弃使用逐个 index 交换的方式，是由于每次操作之后，下标都会增减 width，随机访问导致缓存缺失，在计算机系统中，访问内存时 CPU
     * 都有多级缓存，而缓存都是以内存地址块为单位缓存的，也就是说，如果顺序访问，那缓存缺失一次之后，后续的访问都会命中缓存，而随机访问则意味着每次访问都会缓存缺失，那这样性能当然会大打折扣。
     *
     * 实际上翻转操作并不需要这样的随机访问，我们需要的是把矩阵底部的行放到顶部去，直接逐行拷贝即可
     */
    if (mDstData == null || mDstData.length != src.length) {
      mDstData = new byte[src.length];
    }
    // Y
    for (int y = 0; y < height; y++) {
      System.arraycopy(src, (height - 1 - y) * width, mDstData, y * width, width);
    }
    // UV
    int wh = width * height;
    int halfH = height / 2;
    for (int y = 0; y < halfH; y++) {
      System.arraycopy(src, wh + y * width, mDstData, wh + (halfH - 1 - y) * width, width);
    }
    return mDstData;
  }

  /**
   * 水平镜像翻转
   *
   * @param src
   * @param width
   * @param height
   * @return
   */
  private byte[] onHorizontalMirror(byte[] src, int width, int height) {
    // 水平镜像由于不是连贯的内存，所以不能使用逐行拷贝
    int i;
    int index;
    byte temp;
    int a, b;
    //Y
    for (i = 0; i < height; i++) {
      a = i * width;
      b = (i + 1) * width - 1;
      while (a < b) {
        temp = src[a];
        src[a] = src[b];
        src[b] = temp;
        a++;
        b--;
      }
    }

    //U V
    index = width * height;
    for (i = 0; i < height / 2; i++) {
      a = i * width;
      b = (i + 1) * width - 2;
      while (a < b) {
        temp = src[a + index];
        src[a + index] = src[b + index];
        src[b + index] = temp;

        temp = src[a + index + 1];
        src[a + index + 1] = src[b + index + 1];
        src[b + index + 1] = temp;
        a += 2;
        b -= 2;
      }
    }
    return src;
  }


  public void release() {
    if (mFrame != null) {
      mFrame.release();
    }
    if (mTextureFilter != null) {
      mTextureFilter.release();
    }
    mDisplay = null;
  }

}
