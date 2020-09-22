package cn.rongcloud.rtc.instrumentationtest;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.base.RTCErrorCode;
import java.lang.ref.SoftReference;

public abstract class RTCResultCallbackWrapper extends IRCRTCResultCallback {

    private SoftReference<Activity> activity;

    public RTCResultCallbackWrapper() {
    }

    public RTCResultCallbackWrapper(Activity activity) {
        this.activity = new SoftReference(activity);
    }

    @Override
    public void onSuccess() {
        if (!isFinish()) {
            postUiThread(new Runnable() {
                @Override
                public void run() {
                    onUISuccess();
                }
            });
        }
    }

    private boolean isFinish() {
        // 如果 没有传 Activity 引用默认不检查 Activity 生命周期
        if (activity == null) {
            return false;
        }
        Activity activity = this.activity.get();
        if (activity == null) {
            return true;
        }
        return activity.isDestroyed() || activity.isFinishing();

    }

    protected abstract void onUISuccess();

    private void postUiThread(final Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!isFinish()) {
                    runnable.run();
                }
            }
        });
    }

    @Override
    public void onFailed(final RTCErrorCode errorCode) {
        if (!isFinish()) {
            postUiThread(new Runnable() {
                @Override
                public void run() {
                    onUIFailed(errorCode);
                }
            });
        }
    }

    protected abstract void onUIFailed(RTCErrorCode errorCode);
}
