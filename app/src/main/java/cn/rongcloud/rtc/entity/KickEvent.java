package cn.rongcloud.rtc.entity;

public class KickEvent {
    private String roomId;

    public KickEvent(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
