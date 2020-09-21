package cn.rongcloud.rtc.screen_cast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Surface;
import cn.rongcloud.rtc.api.callback.IRCRTCVideoSource;
import cn.rongcloud.rtc.api.callback.IRCRTCVideoSource.IRCVideoConsumer;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;
import cn.rongcloud.rtc.engine.view.RongRTCVideoViewManager;

public class RongRTCScreenCastHelper {
    private static final String TAG = "RongRTCScreenSender";
    public static final String VIDEO_TAG = "RongRTCScreenVideo";

    private static final int DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                    | DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;
    // DPI for VirtualDisplay, does not seem to matter for us.
    private static final int VIRTUAL_DISPLAY_DPI = 400;

    private RCRTCVideoOutputStream mOutputStream;
    private Intent mMediaProjectionData;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mediaProjectionManager;

    private VirtualDisplay mVirtualDisplay;
    private RongRTCSurfaceTextureHelper mSurfaceTextureHelper;
    private IRCVideoConsumer videoConsumer;
    private volatile boolean enabled = false;

    private boolean disposed;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public synchronized void init(Context appContext,
        RCRTCVideoOutputStream outputStream, Intent mediaProjectionData, int width, int height) {

        this.mOutputStream = outputStream;
        this.mSurfaceTextureHelper = RongRTCSurfaceTextureHelper
            .create("ScreenCapturer", RongRTCVideoViewManager.getInstance().getBaseContext());

        this.mMediaProjectionData = mediaProjectionData;

        this.mediaProjectionManager = (MediaProjectionManager)
            appContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        this.mMediaProjection = mediaProjectionManager
            .getMediaProjection(Activity.RESULT_OK, this.mMediaProjectionData);
        this.mVirtualDisplay = createVirtualDisplay(width, height);
        outputStream.setSource(new IRCRTCVideoSource() {
            @Override
            public void onInit(IRCVideoConsumer observer) {
                videoConsumer = observer;
            }

            @Override
            public void onStart() {
                enabled = true;
            }

            @Override
            public void onStop() {
                enabled = false;
            }

            @Override
            public void onDispose() {
                videoConsumer = null;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public synchronized void start() {
        this.mSurfaceTextureHelper.startListening(screenFrameSink);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public synchronized void stop() {
        if (disposed) {
            return;
        }
        disposed = true;
        if (this.mSurfaceTextureHelper != null) {
            this.mSurfaceTextureHelper.stopListening();
            this.mSurfaceTextureHelper.dispose();
            this.mSurfaceTextureHelper = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay(int width, int height) {
        this.mSurfaceTextureHelper.setTextureSize(width, height);
        return this.mMediaProjection.createVirtualDisplay(
                "RongRTC_ScreenCapture",
                width,
                height,
                VIRTUAL_DISPLAY_DPI,
                DISPLAY_FLAGS,
                new Surface(this.mSurfaceTextureHelper.getSurfaceTexture()),
                null,
                null);
    }

    private ScreenFrameSink screenFrameSink = new ScreenFrameSink() {
        @Override
        public void onTexture(int textureWidth,
            int textureHeight, int oexTextureId, float[] transformMatrix, int rotation, long timestampNs) {

            if (TextUtils.isEmpty(RongRTCScreenCastHelper.this.mOutputStream.getStreamId())) {
                return;
            }
            if (videoConsumer != null && enabled) {
                videoConsumer.writeTexture(
                    textureWidth, textureHeight, oexTextureId, transformMatrix, rotation, timestampNs);
            }
        }
    };
}
