package cn.rongcloud.rtc.device.entity;

public class AudioSourceInfo {
    private String name;
    private int code;

    public AudioSourceInfo(String pName, int pCode) {
        this.name = pName;
        this.code = pCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
