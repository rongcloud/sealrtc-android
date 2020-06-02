package cn.rongcloud.rtc.entity;

/** Created by dengxudong on 2018/3/26. */
public class ResolutionInfo {
    private String key; // 144x256
    private int Index; // 0-n

    public ResolutionInfo(String key, int index) {
        this.key = key;
        Index = index;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }
}
