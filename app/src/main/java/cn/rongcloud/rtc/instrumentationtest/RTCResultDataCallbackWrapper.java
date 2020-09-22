package cn.rongcloud.rtc.instrumentationtest;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.base.RTCErrorCode;
import java.lang.ref.SoftReference;

public abstract class RTCResultDataCallbackWrapper<T> extends IRCRTCResultDataCallback<T> {

    private SoftReference<Activity> activity;

    public RTCResultDataCallbackWrapper() {
    }

    public RTCResultDataCallbackWrapper(Activity activity) {
        this.activity = new SoftReference(activity);
    }

    @Override
    public void onSuccess(final T data) {
        if (!isFinish()) {
            postUiThread(new Runnable() {
                @Override
                public void run() {
                    onUISuccess(data);
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

    protected abstract void onUISuccess(T data);

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
