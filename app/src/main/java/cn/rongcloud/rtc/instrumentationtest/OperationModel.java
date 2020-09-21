package cn.rongcloud.rtc.instrumentationtest;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView.Adapter;
import cn.rongcloud.rtc.api.callback.IRCRTCFailedCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.base.RTCErrorCode;

public class OperationModel {

    public static final String SUCCESS = "Success";
    public static final String REQUESTING = "Requesting";
    public static final String IDLE = "Idle";

    public static final int STATE_IDLE = 0;
    public static final int STATE_REQUESTING = 1;
    public static final int STATE_SUCCESS = 2;
    public static final int STATE_FAILED = 3;

    private RTCErrorCode errorCode;
    private int state = STATE_IDLE;
    private String desc;
    private int type;
    private String extra;
    private Adapter mAdapter;

    public OperationModel(String desc, int type) {
        this.desc = desc;
        this.type = type;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public int getState() {
        return state;
    }

    public void requesting(Adapter adapter) {
        mAdapter = adapter;
        state = STATE_REQUESTING;
    }

    public String getBtnText() {
        switch (state) {
            case STATE_FAILED:
                return errorCode.getValue() + "";
            case STATE_SUCCESS:
                return SUCCESS;
            case STATE_REQUESTING:
                return REQUESTING;
        }
        return IDLE;
    }


    public void setFailed(RTCErrorCode errorCode) {
        state = STATE_FAILED;
        this.errorCode = errorCode;
        notifyDataSetChanged();
    }

    public IRCRTCResultCallback createCallback(Activity activity) {
        return new RTCResultCallbackWrapper(activity) {
            @Override
            protected void onUISuccess() {
                setSuccess();
            }

            @Override
            protected void onUIFailed(RTCErrorCode errorCode) {
                setFailed(errorCode);
            }
        };
    }

    public RTCResultDataCallbackWrapper createDataCallback(Activity activity) {
        return new RTCResultDataCallbackWrapper(activity) {
            @Override
            protected void onUISuccess(Object data) {
                setSuccess();
            }

            @Override
            protected void onUIFailed(RTCErrorCode errorCode) {
                setFailed(errorCode);
            }
        };
    }

    public void setSuccess() {
        state = STATE_SUCCESS;
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public int getTextColor() {
        switch (state) {
            case STATE_FAILED:
                return Color.RED;
            case STATE_SUCCESS:
                return Color.GREEN;
            case STATE_REQUESTING:
                return Color.BLUE;
        }
        return Color.BLACK;
    }
}
