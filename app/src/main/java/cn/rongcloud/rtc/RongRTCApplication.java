package cn.rongcloud.rtc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.engine.RTCEngineImpl;
import cn.rongcloud.rtc.faceunity.FURenderer;
import cn.rongcloud.rtc.message.RoomInfoMessage;
import cn.rongcloud.rtc.message.RoomKickOffMessage;
import cn.rongcloud.rtc.message.WhiteBoardInfoMessage;
import cn.rongcloud.rtc.util.RTCNotificationService;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;

/** Created by suancai on 2016/11/22. */
public class RongRTCApplication extends MultiDexApplication {

    private int mActiveCount = 0;
    private int mAliveCount = 0;
    private boolean isActive;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        SessionManager.initContext(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        // bugly 配置，查看对应崩溃日志。
        String processName = Utils.getCurProcessName(this);
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setUploadProcess(processName.equals(getPackageName()));
        // 初始化Bugly
        CrashReport.initCrashReport(this, "3612cc23a8", false, strategy);
        if (getApplicationInfo().packageName.equals(Utils.getCurProcessName(this))) {
            try {
                RongIMClient.registerMessageType(RoomInfoMessage.class);
                RongIMClient.registerMessageType(WhiteBoardInfoMessage.class);
                RongIMClient.registerMessageType(RoomKickOffMessage.class);
            } catch (AnnotationNotFoundException e) {
                e.printStackTrace();
            }

            // 相芯SDK 初始化
            FURenderer.initFURenderer(this);
        }

        registerLifecycleCallbacks();
    }

    private void registerLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        mAliveCount++;
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                        mActiveCount++;
                        notifyChange();
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {}

                    @Override
                    public void onActivityPaused(Activity activity) {}

                    @Override
                    public void onActivityStopped(Activity activity) {
                        mActiveCount--;
                        notifyChange();
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        mAliveCount--;
                        if (mAliveCount == 0) {
                            stopNotificationService();
                        }
                    }
                });
    }

    private void notifyChange() {
        if (mActiveCount > 0) {
            if (!isActive) {
                isActive = true;
                // AppForeground
                stopNotificationService();
            }
        } else {
            if (isActive) {
                isActive = false;
                // AppBackground
                if (RCRTCEngine.getInstance().getRoom() != null) {
                    startService(new Intent(this, RTCNotificationService.class));
                }
            }
        }
    }

    private void stopNotificationService() {
        if (RCRTCEngine.getInstance().getRoom() != null) {
          stopService(new Intent(RongRTCApplication.this, RTCNotificationService.class));
        }
    }
}
