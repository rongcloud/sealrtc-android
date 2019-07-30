package cn.rongcloud.rtc;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.tencent.bugly.crashreport.CrashReport;

import cn.rongcloud.rtc.message.RoomInfoMessage;
import cn.rongcloud.rtc.message.WhiteBoardInfoMessage;
import cn.rongcloud.rtc.utils.FileLogUtil;
import cn.rongcloud.rtc.util.Utils;
import io.rong.common.FileUtils;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;

/**
 * Created by suancai on 2016/11/22.
 */

public class RongRTCApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
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
        try {
            RongIMClient.registerMessageType(RoomInfoMessage.class);
            RongIMClient.registerMessageType(WhiteBoardInfoMessage.class);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }
        // 内测时设置为true ， 发布时修改为false
//        CrashReport.initCrashReport(getApplicationContext(), "ef48d6a01a", true);
    }
}
