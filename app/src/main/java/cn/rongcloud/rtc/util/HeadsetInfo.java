package cn.rongcloud.rtc.util;

public class HeadsetInfo {
    private boolean isConnected;
    private int type;

    public HeadsetInfo(boolean isInsert, int type) {
        this.isConnected = isInsert;
        this.type = type;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
