package cn.rongcloud.rtc.usbcamera;

import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;

/** Created by wangw on 2019/4/29. */
public interface UsbCameraCapturer {

    /** 开始捕获视频数据 */
    void startCapturer();

    /** 停止捕获视频数据 */
    void stopCapturer();

    /** 发布流 */
    void publishVideoStream(IRCRTCResultCallback callBack);

    /** 取消发布 */
    void unPublishVideoStream(IRCRTCResultCallback callback);

    /** 释放资源 */
    void release();

    /**
     * 设置预览View
     *
     * @param videoView
     */
    void setRongRTCVideoView(RCRTCVideoView videoView);

    RCRTCVideoOutputStream getVideoOutputStream();
}
