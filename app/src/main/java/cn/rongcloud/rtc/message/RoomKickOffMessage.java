package cn.rongcloud.rtc.message;

import android.os.Parcel;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import org.json.JSONException;
import org.json.JSONObject;

@MessageTag(value = "SealRTC:KickOff", flag = MessageTag.STATUS)
public class RoomKickOffMessage extends MessageContent {

    private String userId;

    public RoomKickOffMessage(String id) {
        this.userId = id;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getUserId() {
        return userId;
    }

    public RoomKickOffMessage(byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data));
            userId = jsonObject.getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
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
        dest.writeString(userId);
    }

    public RoomKickOffMessage(Parcel parcel) {
        userId = parcel.readString();
    }

    public static final Creator<RoomKickOffMessage> CREATOR =
            new Creator<RoomKickOffMessage>() {
                @Override
                public RoomKickOffMessage createFromParcel(Parcel source) {
                    return new RoomKickOffMessage(source);
                }

                @Override
                public RoomKickOffMessage[] newArray(int size) {
                    return new RoomKickOffMessage[size];
                }
            };
}
