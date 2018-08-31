package cn.rongcloud.rtc.base;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

public class RongRTCBaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 字体大小不随系统变化而变化 字体待定
     * @return
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        try {
            Configuration config=new Configuration();
            config.setToDefaults();
            res.updateConfiguration(config,res.getDisplayMetrics() );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
