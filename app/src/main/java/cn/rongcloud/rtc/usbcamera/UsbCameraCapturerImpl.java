package cn.rongcloud.rtc.usbcamera;

import android.content.Context;

import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.stream.MediaType;
import cn.rongcloud.rtc.stream.local.RongRTCAVOutputStream;
import cn.rongcloud.rtc.stream.local.RongRTCLocalSourceManager;
import cn.rongcloud.rtc.user.RongRTCLocalUser;

/**
 * Created by wangw on 2019/4/29.
 */
public class UsbCameraCapturerImpl extends AbstractUsbCameraCapturer implements UsbCameraCapturer {

    private RongRTCVideoView mVideoView;
    private RongRTCAVOutputStream mOutputStream;
    private RongRTCLocalUser mLocalUser;

    public UsbCameraCapturerImpl(Context context,RongRTCLocalUser localUser,int width,int height) {
        super(context,width,height);
        mLocalUser = localUser;
        mOutputStream = new RongRTCAVOutputStream(MediaType.VIDEO,"USB");
    }

    /**
     * 开始捕获数据
     */
    @Override
    public void startCapturer() {
        setState(STATE_START);
        log("startCapturer","");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                onStartPreview();
            }
        });
    }

    /**
     * 停止捕获数据
     */
    @Override
    public void stopCapturer() {
        setState(STATE_STOP);
        log("stopCapturer","");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                onStopPreview();
            }
        });
    }

    /**
     * 设置显示容器
     * @param videoView
     */
    @Override
    public void setRongRTCVideoView(RongRTCVideoView videoView) {
        mVideoView = videoView;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mOutputStream != null && mVideoView != null){
                    mOutputStream.setRongRTCVideoView(mVideoView);
                    mOutputStream.setRongRTCVideoTrack(RongRTCLocalSourceManager.getInstance().getCustomVideoTrack(mOutputStream));
                }
            }
        });
    }

    /**
     * 发布资源
     * @param callBack
     */
    @Override
    public void publishVideoStream(RongRTCResultUICallBack callBack) {
        log("publishVideoStream","");
        if (mLocalUser == null)
            return;
        mLocalUser.publishAVStream(mOutputStream,callBack);
    }

    /**
     * 取消发布资源
     * @param callBack
     */
    @Override
    public void unPublishVideoStream(RongRTCResultUICallBack callBack) {
        log("unPublishVideoStream","");
        if (mLocalUser == null)
            return;
        mLocalUser.unPublishAVStream(mOutputStream,callBack);
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
    public RongRTCAVOutputStream getVideoOutputStream() {
        return mOutputStream;
    }

    @Override
    protected void onFrame(byte[] bytes, int selectWidth, int selectHeight) {
        super.onFrame(bytes);
        if (mOutputStream != null)
            mOutputStream.writeByteBuffer(bytes, mReqWidth, mReqHeight,0);
    }

    @Override
    public void onTextureFrameAvailable(int oesTextureId, int width, int height, float[] transformMatrix, long timestampNs) {
        if (mOutputStream != null)
            mOutputStream.writeTextureFrame(width, height,oesTextureId,transformMatrix,0,timestampNs);
    }
}
