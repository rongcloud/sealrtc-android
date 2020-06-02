package cn.rongcloud.rtc.message;

import android.os.Parcel;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import org.json.JSONException;
import org.json.JSONObject;

@MessageTag(value = "SealRTC:WhiteBoardInfo", flag = MessageTag.STATUS)
public class WhiteBoardInfoMessage extends MessageContent {

    private String uuid;
    private String roomToken;

    public WhiteBoardInfoMessage(String uuid, String roomToken) {
        this.uuid = uuid;
        this.roomToken = roomToken;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getRoomToken() {
        return roomToken;
    }

    public void setRoomToken(String roomToken) {
        this.roomToken = roomToken;
    }

    public WhiteBoardInfoMessage(byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data));
            uuid = jsonObject.getString("uuid");
            roomToken = jsonObject.getString("roomToken");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("roomToken", roomToken);
            return jsonObject.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(roomToken);
    }

    public WhiteBoardInfoMessage(Parcel parcel) {
        uuid = parcel.readString();
        roomToken = parcel.readString();
    }

    public static final Creator<WhiteBoardInfoMessage> CREATOR =
            new Creator<WhiteBoardInfoMessage>() {
                @Override
                public WhiteBoardInfoMessage createFromParcel(Parcel source) {
                    return new WhiteBoardInfoMessage(source);
                }

                @Override
                public WhiteBoardInfoMessage[] newArray(int size) {
                    return new WhiteBoardInfoMessage[size];
                }
            };
}
