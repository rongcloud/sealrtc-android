package cn.rongcloud.rtc.usbcamera;

import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.local.RongRTCAVOutputStream;

/**
 * Created by wangw on 2019/4/29.
 */
public interface UsbCameraCapturer {

    /**
     * 开始捕获视频数据
     */
    void startCapturer();

    /**
     * 停止捕获视频数据
     */
    void stopCapturer();

    /**
     * 发布流
     */
    void publishVideoStream(RongRTCResultUICallBack callBack);

    /**
     * 取消发布
     */
    void unPublishVideoStream(RongRTCResultUICallBack callback);

    /**
     * 释放资源
     */
    void release();

    /**
     * 设置预览View
     * @param videoView
     */
    void setRongRTCVideoView(RongRTCVideoView videoView);

    RongRTCAVOutputStream getVideoOutputStream();

}
