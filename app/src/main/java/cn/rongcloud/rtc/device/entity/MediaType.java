package cn.rongcloud.rtc.device.entity;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class MediaType implements Parcelable {

    private String mimeType;
    private ArrayList<ColorFormat> colorFormats = new ArrayList<>();

    public MediaType() {}

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public ArrayList<ColorFormat> getColorFormats() {
        return colorFormats;
    }

    public void setColorFormats(ArrayList<ColorFormat> colorFormats) {
        this.colorFormats = colorFormats;
    }

    protected MediaType(Parcel in) {
        mimeType = in.readString();
        colorFormats = in.readArrayList(ColorFormat.class.getClassLoader());
    }

    public static final Creator<MediaType> CREATOR =
            new Creator<MediaType>() {
                @Override
                public MediaType createFromParcel(Parcel in) {
                    return new MediaType(in);
                }

                @Override
                public MediaType[] newArray(int size) {
                    return new MediaType[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mimeType);
        dest.writeList(colorFormats);
    }
}
