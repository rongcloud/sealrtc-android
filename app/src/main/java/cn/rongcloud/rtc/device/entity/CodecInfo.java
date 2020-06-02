package cn.rongcloud.rtc.device.entity;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class CodecInfo implements Parcelable {
    private String codecName;
    private ArrayList<MediaType> mediaTypes = new ArrayList<>();

    public CodecInfo() {}

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public ArrayList<MediaType> getMediaTypes() {
        return mediaTypes;
    }

    public void setMediaTypes(ArrayList<MediaType> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public boolean isAudioCodec() {
        for (MediaType mediaType : mediaTypes) {
            if (mediaType.getMimeType().contains("audio")) {
                return true;
            }
        }
        return false;
    }

    public boolean isVideoCodec() {
        for (MediaType mediaType : mediaTypes) {
            if (mediaType.getMimeType().contains("video")) {
                return true;
            }
        }
        return false;
    }

    protected CodecInfo(Parcel in) {
        codecName = in.readString();
        mediaTypes = in.readArrayList(MediaType.class.getClassLoader());
        //        in.readTypedList(mediaTypes, MediaType.CREATOR);
    }

    public static final Creator<CodecInfo> CREATOR =
            new Creator<CodecInfo>() {
                @Override
                public CodecInfo createFromParcel(Parcel in) {
                    return new CodecInfo(in);
                }

                @Override
                public CodecInfo[] newArray(int size) {
                    return new CodecInfo[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(codecName);
        dest.writeList(mediaTypes);
    }
}
