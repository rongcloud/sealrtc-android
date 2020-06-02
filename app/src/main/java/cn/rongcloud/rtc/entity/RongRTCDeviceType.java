package cn.rongcloud.rtc.entity;

public enum RongRTCDeviceType {
    /** 摄像头 */
    Camera(1),

    /** 麦克风 */
    Microphone(2),

    /** 摄像头+麦克风 */
    CameraAndMicrophone(3),

    /** 摄像头+麦克风 */
    ScreenShare(3),

    /** 无效参数 */
    None(-1);

    int value;

    RongRTCDeviceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
