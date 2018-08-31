package cn.rongcloud.rtc;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import cn.rongcloud.rtc.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;

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
        // 内测时设置为true ， 发布时修改为false
        CrashReport.initCrashReport(getApplicationContext(), "ef48d6a01a", true);
    }
}
