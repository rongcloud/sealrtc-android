package cn.rongcloud.rtc.usbcamera;

import android.content.Context;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCLocalUser;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCVideoSource;
import cn.rongcloud.rtc.api.callback.IRCRTCVideoSource.IRCVideoConsumer;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;

/** Created by wangw on 2019/4/29. */
public class UsbCameraCapturerImpl extends AbstractUsbCameraCapturer implements UsbCameraCapturer {

    public static final String STREAM_TAG = "USB";
    private RCRTCVideoView mVideoView;
    private RCRTCVideoOutputStream mOutputStream;
    private RCRTCLocalUser mLocalUser;
    private volatile IRCVideoConsumer videoConsumer;
    private volatile boolean observerEnabled = false;

    public UsbCameraCapturerImpl(Context context, RCRTCLocalUser localUser, RCRTCVideoResolution videoResolution) {
        super(context, videoResolution);
        mLocalUser = localUser;
        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        videoConfigBuilder.setVideoResolution(videoResolution);
        mOutputStream = RCRTCEngine.getInstance().createVideoStream(STREAM_TAG, videoConfigBuilder.build());
        mOutputStream.setSource(new IRCRTCVideoSource() {
            @Override
            public void onInit(IRCVideoConsumer observer) {
                videoConsumer = observer;
            }

            @Override
            public void onStart() {
                observerEnabled = true;
            }

            @Override
            public void onStop() {
                videoConsumer = null;
            }

            @Override
            public void onDispose() {
                observerEnabled = false;
            }
        });
    }

    /** 开始捕获数据 */
    @Override
    public void startCapturer() {
        setState(STATE_START);
        log("startCapturer", "");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                onStartPreview();
            }
        });
    }

    /** 停止捕获数据 */
    @Override
    public void stopCapturer() {
        setState(STATE_STOP);
        log("stopCapturer", "");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                onStopPreview();
            }
        });
    }

    /**
     * 设置显示容器
     *
     * @param videoView
     */
    @Override
    public void setRongRTCVideoView(RCRTCVideoView videoView) {
        mVideoView = videoView;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mOutputStream != null && mVideoView != null) {
                    mOutputStream.setVideoView(mVideoView);
                }
            }
        });
    }

    /**
     * 发布资源
     *
     * @param callBack
     */
    @Override
    public void publishVideoStream(IRCRTCResultCallback callBack) {
        log("publishVideoStream", "");
        if (mLocalUser == null) return;
        mLocalUser.publishStream(mOutputStream, callBack);
    }

    /**
     * 取消发布资源
     *
     * @param callBack
     */
    @Override
    public void unPublishVideoStream(IRCRTCResultCallback callBack) {
        log("unPublishVideoStream", "");
        if (mLocalUser == null) return;
        mLocalUser.unpublishStream(mOutputStream, callBack);
    }

    @Override
    public void release() {
        log("release", "");
        setState(STATE_IDLE);
        if (mOutputStream != null) {
            mOutputStream.release();
        }
        mOutputStream = null;
        onRelease();
    }

    @Override
    public RCRTCVideoOutputStream getVideoOutputStream() {
        return mOutputStream;
    }

    @Override
    protected void onFrame(byte[] bytes, int selectWidth, int selectHeight) {
        super.onFrame(bytes);
        if (videoConsumer != null && observerEnabled) {
            videoConsumer.writeYuvData(bytes, mReqWidth, mReqHeight, 0);
        }
    }

    @Override
    public void onTextureFrameAvailable(
        int oesTextureId, int width, int height, float[] transformMatrix, long timestampNs) {
        if (videoConsumer != null && observerEnabled) {
            videoConsumer.writeTexture(width, height, oesTextureId, transformMatrix, 0, timestampNs);
        }
    }
}
