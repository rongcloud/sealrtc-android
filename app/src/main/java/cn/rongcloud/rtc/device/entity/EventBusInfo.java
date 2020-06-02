package cn.rongcloud.rtc.device.entity;

public class EventBusInfo {
    private int requestCode;
    private String content;
    private int realyValue;

    public EventBusInfo(int requestCode, String content, int realVal) {
        this.requestCode = requestCode;
        this.content = content;
        this.realyValue = realVal;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getRealyValue() {
        return realyValue;
    }

    public void setRealyValue(int realyValue) {
        this.realyValue = realyValue;
    }
}
