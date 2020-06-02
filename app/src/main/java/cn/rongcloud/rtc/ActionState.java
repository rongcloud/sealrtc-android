package cn.rongcloud.rtc;

/** Created by dengxudong on 2017/12/27. */
public class ActionState {

    private int type;
    private String hostUid;
    private String userid;

    public ActionState() {}

    public ActionState(int type, String hostUid, String userid) {
        this.type = type;
        this.hostUid = hostUid;
        this.userid = userid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHostUid() {
        return hostUid;
    }

    public void setHostUid(String hostUid) {
        this.hostUid = hostUid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
