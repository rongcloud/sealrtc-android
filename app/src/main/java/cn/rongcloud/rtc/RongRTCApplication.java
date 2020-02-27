package cn.rongcloud.rtc;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;


import cn.rongcloud.rtc.message.RoomInfoMessage;
import cn.rongcloud.rtc.message.RoomKickOffMessage;
import cn.rongcloud.rtc.message.WhiteBoardInfoMessage;
import cn.rongcloud.rtc.util.RTCNotificationService;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FileLogUtil;
import io.rong.common.FileUtils;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;

/**
 * Created by suancai on 2016/11/22.
 */

public class RongRTCApplication extends MultiDexApplication {

    private int mActiveCount = 0;
    private int mAliveCount=0;
    private boolean isActive;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        SessionManager.initContext(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        //bugly 配置，查看对应崩溃日志。
        String processName = Utils.getCurProcessName(this);
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setUploadProcess(processName.equals(getPackageName()));
        // 初始化Bugly
        CrashReport.initCrashReport(this, "3612cc23a8", false, strategy);
        MultiDex.install(this);
        if (getApplicationInfo().packageName.equals(Utils.getCurProcessName(this))) {
            String logPath = FileUtils.getCachePath(this, "/ronglog");
            String filePath = logPath + "/rcvoip.log";
            FileLogUtil.setFileLog(filePath);
        }

//        if (getApplicationContext().getApplicationInfo().getid)

        try {
            RongIMClient.registerMessageType(RoomInfoMessage.class);
            RongIMClient.registerMessageType(WhiteBoardInfoMessage.class);
            RongIMClient.registerMessageType(RoomKickOffMessage.class);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }

        // 内测时设置为true ， 发布时修改为false
//        CrashReport.initCrashReport(getApplicationContext(), "ef48d6a01a", true);
        registerLifecycleCallbacks();
    }

    private void registerLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
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
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActiveCount--;
                notifyChange();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

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
                //AppForeground
                stopNotificationService();
            }
        } else {
            if (isActive) {
                isActive = false;
                //AppBackground
                if (CenterManager.getInstance().isInRoom()) {
                    startService(new Intent(this, RTCNotificationService.class));
                }
            }
        }
    }

    private void stopNotificationService() {
        if (CenterManager.getInstance().isInRoom()) {
            boolean val=stopService(new Intent(RongRTCApplication.this, RTCNotificationService.class));
        }
    }
}
