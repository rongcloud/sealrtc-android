package cn.rongcloud.rtc.device.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class AVConfigInfo implements Parcelable {

    private String mItemTitle;
    private int mRequestCode;
    private String mItemValue;
    private String mItemValueNew;
    private int mItemRealValue;

    public AVConfigInfo(String itemTitle, int requestCode, String itemValue) {
        this.mItemTitle = itemTitle;
        this.mRequestCode = requestCode;
        this.mItemValue = itemValue;
        this.mItemRealValue = 0;
    }

    public AVConfigInfo(String itemTitle, int requestCode, String itemValue, int itemRealValue) {
        this.mItemTitle = itemTitle;
        this.mRequestCode = requestCode;
        this.mItemValue = itemValue;
        this.mItemRealValue = itemRealValue;
    }

    protected AVConfigInfo(Parcel in) {
        mItemTitle = in.readString();
        mRequestCode = in.readInt();
        mItemValue = in.readString();
        mItemRealValue = in.readInt();
    }

    public static final Creator<AVConfigInfo> CREATOR =
            new Creator<AVConfigInfo>() {
                @Override
                public AVConfigInfo createFromParcel(Parcel in) {
                    return new AVConfigInfo(in);
                }

                @Override
                public AVConfigInfo[] newArray(int size) {
                    return new AVConfigInfo[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mItemTitle);
        dest.writeInt(mRequestCode);
        dest.writeString(mItemValue);
        dest.writeInt(mItemRealValue);
    }

    public String getTitle() {
        return mItemTitle;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public String getItemValue() {
        if (!TextUtils.isEmpty(this.mItemValueNew)) return this.mItemValueNew;
        return mItemValue;
    }

    public String getItemValueOld() {
        return mItemValue;
    }

    public String getItemValueNew() {
        return mItemValueNew;
    }

    public void setItemValue(String mDefaultValue) {
        this.mItemValueNew = mDefaultValue;
    }

    public int getItemRealValue() {
        return mItemRealValue;
    }

    public void setItemRealValue(int mItemRealValue) {
        this.mItemRealValue = mItemRealValue;
    }
}
