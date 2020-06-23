package cn.rongcloud.rtc.screen_cast;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import cn.rongcloud.rtc.core.CreateEglContextException;
import cn.rongcloud.rtc.core.EglBase;
import cn.rongcloud.rtc.core.GlUtil;
import cn.rongcloud.rtc.core.Logging;
import cn.rongcloud.rtc.core.ThreadUtils;
import cn.rongcloud.rtc.core.TimestampAligner;
import cn.rongcloud.rtc.core.VideoFrame;
import cn.rongcloud.rtc.core.YuvConverter;
import java.util.concurrent.Callable;

public class RongRTCSurfaceTextureHelper {
    private static final String TAG = "RongRTCSurfaceTextureHelper";

    public static RongRTCSurfaceTextureHelper create(
            final String threadName,
            final EglBase.Context sharedContext,
            final boolean alignTimestamps,
            final YuvConverter yuvConverter) {

        final HandlerThread thread = new HandlerThread(threadName);
        thread.start();
        final Handler handler = new Handler(thread.getLooper());

        // The onFrameAvailable() callback will be executed on the SurfaceTexture ctor thread. See:
        // http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.1.1_r1/android/graphics/SurfaceTexture.java#195.
        // Therefore, in order to control the callback thread on API lvl < 21, the
        // SurfaceTextureHelper
        // is constructed on the |handler| thread.
        return ThreadUtils.invokeAtFrontUninterruptibly(
                handler,
                new Callable<RongRTCSurfaceTextureHelper>() {
                    @Nullable
                    @Override
                    public RongRTCSurfaceTextureHelper call() {
                        try {
                            return new RongRTCSurfaceTextureHelper(
                                    sharedContext, handler, alignTimestamps, yuvConverter);
                        } catch (Exception e) {
                            Logging.e(TAG, threadName + " create failure", e);
                            return null;
                        }
                    }
                });
    }

    public static RongRTCSurfaceTextureHelper create(
            final String threadName, final EglBase.Context sharedContext) {
        return create(threadName, sharedContext, /* alignTimestamps= */ false, new YuvConverter());
    }

    public static RongRTCSurfaceTextureHelper create(
            final String threadName, final EglBase.Context sharedContext, boolean alignTimestamps) {
        return create(threadName, sharedContext, alignTimestamps, new YuvConverter());
    }

    private final Handler handler;
    private final EglBase eglBase;
    private final SurfaceTexture surfaceTexture;
    private final int oesTextureId;
    private final YuvConverter yuvConverter;
    @Nullable private final TimestampAligner timestampAligner;

    // These variables are only accessed from the |handler| thread.
    @Nullable private ScreenFrameSink listener;
    @Nullable private ScreenFrameSink pendingListener;

    // The possible states of this class.
    private boolean hasPendingTexture;
    private volatile boolean isTextureInUse;
    private boolean isQuitting;
    private int frameRotation;
    private int textureWidth;
    private int textureHeight;

    public int getOesTextureId() {
        return oesTextureId;
    }

    final Runnable setListenerRunnable =
            new Runnable() {
                @Override
                public void run() {
                    listener = pendingListener;
                    pendingListener = null;
                    // May have a pending frame from the previous capture session - drop it.
                    if (hasPendingTexture) {
                        // Calling updateTexImage() is neccessary in order to receive new frames.
                        updateTexImage();
                        hasPendingTexture = false;
                    }
                }
            };

    private RongRTCSurfaceTextureHelper(
            EglBase.Context sharedContext,
            Handler handler,
            boolean alignTimestamps,
            YuvConverter yuvConverter)
            throws CreateEglContextException {
        if (handler.getLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException(
                    "SurfaceTextureHelper must be created on the handler thread");
        }
        this.handler = handler;
        this.timestampAligner = alignTimestamps ? new TimestampAligner() : null;
        this.yuvConverter = yuvConverter;

        eglBase = EglBase.create(sharedContext, EglBase.CONFIG_PIXEL_BUFFER);
        try {
            // Both these statements have been observed to fail on rare occasions, see
            // BUG=webrtc:5682.
            eglBase.createDummyPbufferSurface();
            eglBase.makeCurrent();
        } catch (RuntimeException e) {
            // Clean up before rethrowing the exception.
            eglBase.release();
            handler.getLooper().quit();
            throw e;
        }

        oesTextureId = GlUtil.generateTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        surfaceTexture = new SurfaceTexture(oesTextureId);
        setOnFrameAvailableListener(
                surfaceTexture,
                new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        hasPendingTexture = true;
                        tryDeliverTextureFrame();
                    }
                },
                handler);
    }

    @TargetApi(21)
    private static void setOnFrameAvailableListener(
            SurfaceTexture surfaceTexture,
            SurfaceTexture.OnFrameAvailableListener listener,
            Handler handler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            surfaceTexture.setOnFrameAvailableListener(listener, handler);
        } else {
            // The documentation states that the listener will be called on an arbitrary thread, but
            // in
            // pratice, it is always the thread on which the SurfaceTexture was constructed. There
            // are
            // assertions in place in case this ever changes. For API >= 21, we use the new API to
            // explicitly specify the handler.
            surfaceTexture.setOnFrameAvailableListener(listener);
        }
    }

    /**
     * Start to stream textures to the given |listener|. If you need to change listener, you need to
     * call stopListening() first.
     */
    public void startListening(final ScreenFrameSink listener) {
        if (this.listener != null || this.pendingListener != null) {
            throw new IllegalStateException("SurfaceTextureHelper listener has already been set.");
        }
        this.pendingListener = listener;
        handler.post(setListenerRunnable);
    }

    /**
     * Stop listening. The listener set in startListening() is guaranteded to not receive any more
     * onFrame() callbacks after this function returns.
     */
    public void stopListening() {
        Logging.d(TAG, "stopListening()");
        handler.removeCallbacks(setListenerRunnable);
        ThreadUtils.invokeAtFrontUninterruptibly(
                handler,
                new Runnable() {
                    @Override
                    public void run() {
                        listener = null;
                        pendingListener = null;
                    }
                });
    }

    /**
     * Use this function to set the texture size. Note, do not call setDefaultBufferSize() yourself
     * since this class needs to be aware of the texture size.
     */
    public void setTextureSize(final int textureWidth, final int textureHeight) {
        if (textureWidth <= 0) {
            throw new IllegalArgumentException(
                    "Texture width must be positive, but was " + textureWidth);
        }
        if (textureHeight <= 0) {
            throw new IllegalArgumentException(
                    "Texture height must be positive, but was " + textureHeight);
        }
        surfaceTexture.setDefaultBufferSize(textureWidth, textureHeight);
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        RongRTCSurfaceTextureHelper.this.textureWidth = textureWidth;
                        RongRTCSurfaceTextureHelper.this.textureHeight = textureHeight;
                    }
                });
    }

    /** Set the rotation of the delivered frames. */
    public void setFrameRotation(final int rotation) {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        RongRTCSurfaceTextureHelper.this.frameRotation = rotation;
                    }
                });
    }

    /**
     * Retrieve the underlying SurfaceTexture. The SurfaceTexture should be passed in to a video
     * producer such as a camera or decoder.
     */
    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    /**
     * Retrieve the handler that calls onFrame(). This handler is valid until dispose() is called.
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * This function is called when the texture frame is released. Only one texture frame can be in
     * flight at once, so this function must be called before a new frame is delivered.
     */
    private void returnTextureFrame() {
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        isTextureInUse = false;
                        if (isQuitting) {
                            release();
                        } else {
                            tryDeliverTextureFrame();
                        }
                    }
                });
    }

    public boolean isTextureInUse() {
        return isTextureInUse;
    }

    /**
     * Call disconnect() to stop receiving frames. OpenGL resources are released and the handler is
     * stopped when the texture frame has been released. You are guaranteed to not receive any more
     * onFrame() after this function returns.
     */
    public void dispose() {
        Logging.d(TAG, "dispose()");
        ThreadUtils.invokeAtFrontUninterruptibly(
                handler,
                new Runnable() {
                    @Override
                    public void run() {
                        isQuitting = true;
                        if (!isTextureInUse) {
                            release();
                        }
                    }
                });
    }

    /**
     * Posts to the correct thread to convert |textureBuffer| to I420.
     *
     * @deprecated Use toI420() instead.
     */
    @Deprecated
    public VideoFrame.I420Buffer textureToYuv(final VideoFrame.TextureBuffer textureBuffer) {
        return textureBuffer.toI420();
    }

    private void updateTexImage() {
        // SurfaceTexture.updateTexImage apparently can compete and deadlock with eglSwapBuffers,
        // as observed on Nexus 5. Therefore, synchronize it with the EGL functions.
        // See https://bugs.chromium.org/p/webrtc/issues/detail?id=5702 for more info.
        synchronized (EglBase.lock) {
            surfaceTexture.updateTexImage();
        }
    }

    private void tryDeliverTextureFrame() {
        if (handler.getLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Wrong thread.");
        }
        if (isQuitting || !hasPendingTexture || isTextureInUse || listener == null) {
            return;
        }
        isTextureInUse = true;
        hasPendingTexture = false;

        updateTexImage();

        final float[] transformMatrix = new float[16];
        surfaceTexture.getTransformMatrix(transformMatrix);
        long timestampNs = surfaceTexture.getTimestamp();
        if (timestampAligner != null) {
            timestampNs = timestampAligner.translateTimestamp(timestampNs);
        }
        if (textureWidth == 0 || textureHeight == 0) {
            throw new RuntimeException("Texture size has not been set.");
        }
        if (listener != null) {
            listener.onTexture(
                    textureWidth,
                    textureHeight,
                    oesTextureId,
                    transformMatrix,
                    this.frameRotation,
                    timestampNs);
            returnTextureFrame();
        }
    }

    private void release() {
        if (handler.getLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Wrong thread.");
        }
        if (isTextureInUse || !isQuitting) {
            throw new IllegalStateException("Unexpected release.");
        }
        yuvConverter.release();
        GLES20.glDeleteTextures(1, new int[] {oesTextureId}, 0);
        surfaceTexture.release();
        eglBase.release();
        handler.getLooper().quit();
        if (timestampAligner != null) {
            timestampAligner.dispose();
        }
    }
}
