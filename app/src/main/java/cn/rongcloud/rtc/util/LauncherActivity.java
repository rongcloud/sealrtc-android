package cn.rongcloud.rtc.util;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import cn.rongcloud.rtc.R;
import android.widget.TextView;

import cn.rongcloud.rtc.MainPageActivity;

import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.SettingActivity;

/**
 * Created by suancaicai on 2016/9/27.
 */
public class LauncherActivity extends RongRTCBaseActivity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_launcher);
        ((TextView)findViewById(R.id.launcher_loading)).setTextColor(getResources().getColor(R.color.blink_launcher_grey));

        if(!SessionManager.getInstance(this).contains(SettingActivity.IS_RONGRTC_CONNECTIONMODE)){
            SessionManager.getInstance(this).put(SettingActivity.IS_RONGRTC_CONNECTIONMODE,false);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                skipToMainPage();
            }
        },1000);
    }

    private void skipToMainPage()
    {
        Intent intent = new Intent(this, MainPageActivity.class);
        startActivity(intent);
        finish();
    }
}
