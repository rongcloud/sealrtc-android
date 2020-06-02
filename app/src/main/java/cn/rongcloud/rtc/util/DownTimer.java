/*
   ShengDao Android Client, DownTimer
   Copyright (c) 2014 ShengDao Tech Company Limited
*/
package cn.rongcloud.rtc.util;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * [倒计时类]
 *
 * @author devin.hu
 * @version 1.0
 * @date 2014-12-1
 */
public class DownTimer {

    private final String TAG = DownTimer.class.getSimpleName();
    private CountDownTimer mCountDownTimer;
    private DownTimerListener listener;

    /**
     * [开始倒计时功能]<br>
     * [倒计为time长的时间，时间间隔为每秒]
     *
     * @param time
     */
    public void startDown(long time) {
        startDown(time, 1000);
    }

    /**
     * [倒计为time长的时间，时间间隔为mills]
     *
     * @param time
     * @param mills
     */
    public void startDown(long time, long mills) {
        mCountDownTimer =
                new CountDownTimer(time, mills) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (listener != null) {
                            listener.onTick(millisUntilFinished);
                        } else {
                            Log.e(TAG, "DownTimerListener 监听不能为空");
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (listener != null) {
                            listener.onFinish();
                        } else {
                            Log.e(TAG, "DownTimerListener 监听不能为空");
                        }
                        if (mCountDownTimer != null) mCountDownTimer.cancel();
                    }
                }.start();
    }

    /** [停止倒计时功能]<br> */
    public void stopDown() {
        if (mCountDownTimer != null) mCountDownTimer.cancel();
    }

    /**
     * [设置倒计时监听]<br>
     *
     * @param listener
     */
    public void setListener(DownTimerListener listener) {
        this.listener = listener;
    }

    public void finish() {
        if (mCountDownTimer != null) mCountDownTimer.onFinish();
    }
}
