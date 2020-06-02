package cn.rongcloud.rtc.message;

import android.os.Parcel;
import android.support.annotation.IntDef;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import org.json.JSONException;
import org.json.JSONObject;

@MessageTag(value = "SealRTC:SetRoomInfo", flag = MessageTag.NONE)
public class RoomInfoMessage extends MessageContent {

    @IntDef({JoinMode.AUDIO_VIDEO, JoinMode.AUDIO, JoinMode.OBSERVER})
    public @interface JoinMode {
        int AUDIO_VIDEO = 0;
        int AUDIO = 1;
        int OBSERVER = 2;
    }

    private String userId;
    private String userName;
    private @JoinMode int joinMode;
    private long timeStamp;
    private boolean master;

    public RoomInfoMessage(
            String userId,
            String userName,
            @JoinMode int joinMode,
            long timeStamp,
            boolean master) {
        this.userId = userId;
        this.userName = userName;
        this.joinMode = joinMode;
        this.timeStamp = timeStamp;
        this.master = master;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setJoinMode(@JoinMode int joinMode) {
        this.joinMode = joinMode;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public int getJoinMode() {
        return joinMode;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public RoomInfoMessage(byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data));
            JSONObject valueJson = jsonObject.getJSONObject("infoValue");
            userId = valueJson.getString("userId");
            userName = valueJson.getString("userName");
            joinMode = valueJson.getInt("joinMode");
            timeStamp = valueJson.getLong("joinTime");
            master = valueJson.optInt("master") == 1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("infoKey", userId);
            JSONObject valueObject = new JSONObject();
            valueObject.put("userId", userId);
            valueObject.put("userName", userName);
            valueObject.put("joinMode", joinMode);
            valueObject.put("joinTime", timeStamp);
            valueObject.put("master", master ? 1 : 0);
            jsonObject.put("infoValue", valueObject);
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
        dest.writeString(userName);
        dest.writeInt(joinMode);
        dest.writeLong(timeStamp);
        dest.writeInt(master ? 1 : 0);
    }

    public RoomInfoMessage(Parcel parcel) {
        userId = parcel.readString();
        userName = parcel.readString();
        joinMode = parcel.readInt();
        timeStamp = parcel.readLong();
        master = parcel.readInt() == 1;
    }

    public static final Creator<RoomInfoMessage> CREATOR =
            new Creator<RoomInfoMessage>() {
                @Override
                public RoomInfoMessage createFromParcel(Parcel source) {
                    return new RoomInfoMessage(source);
                }

                @Override
                public RoomInfoMessage[] newArray(int size) {
                    return new RoomInfoMessage[size];
                }
            };
}
