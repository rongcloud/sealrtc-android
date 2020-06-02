package cn.rongcloud.rtc.util;

public interface OnHeadsetPlugListener {
    /**
     * 耳机连接状态监听
     *
     * @param connected 是否连接
     * @param type 连接类型 ，0：蓝牙耳机；1：有线耳机
     */
    public void onNotifyHeadsetState(boolean connected, int type);

    void onNotifySCOAudioStateChange(int scoAudioState);
}
