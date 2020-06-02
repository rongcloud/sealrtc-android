package cn.rongcloud.rtc.device.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class ColorFormat implements Parcelable {
    private int color;
    private String alias;

    public ColorFormat(int color, String alias) {
        this.color = color;
        this.alias = alias;
    }

    protected ColorFormat(Parcel in) {
        color = in.readInt();
        alias = in.readString();
    }

    public static final Creator<ColorFormat> CREATOR =
            new Creator<ColorFormat>() {
                @Override
                public ColorFormat createFromParcel(Parcel in) {
                    return new ColorFormat(in);
                }

                @Override
                public ColorFormat[] newArray(int size) {
                    return new ColorFormat[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(color);
        dest.writeString(alias);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
