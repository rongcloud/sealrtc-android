package cn.rongcloud.rtc.whiteboard;

public class WhiteBoardRoomInfo {
    String uuid;
    String roomToken;

    public WhiteBoardRoomInfo(String uuid, String roomToken) {
        this.uuid = uuid;
        this.roomToken = roomToken;
    }

    public WhiteBoardRoomInfo() {}

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRoomToken() {
        return roomToken;
    }

    public void setRoomToken(String roomToken) {
        this.roomToken = roomToken;
    }
}
