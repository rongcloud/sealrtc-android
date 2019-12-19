package cn.rongcloud.rtc.util;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import cn.rongcloud.rtc.MainPageActivity;
import cn.rongcloud.rtc.R;

import android.widget.ImageView;
import android.widget.TextView;

import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.SettingActivity;
import cn.rongcloud.rtc.device.privatecloud.ServerUtils;

/**
 * Created by suancaicai on 2016/9/27.
 */
public class LauncherActivity extends RongRTCBaseActivity {
    private ImageView iv_logo;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launcher);
        ((TextView) findViewById(R.id.launcher_loading)).setTextColor(getResources().getColor(R.color.blink_launcher_grey));
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        if (iv_logo != null) {
            if (ServerUtils.usePrivateCloud()) {
                iv_logo.setImageResource(R.drawable.ic_launcher_privatecloud);
            } else {
                iv_logo.setImageResource(R.drawable.ic_launcher);
            }
        }

        SessionManager.getInstance(this).put(SettingActivity.IS_RONGRTC_CONNECTIONMODE, false);

        ServerUtils.APP_KEY = SessionManager.getInstance(Utils.getContext()).getString(ServerUtils.APP_KEY_KEY);
        ServerUtils.NAV_SERVER = SessionManager.getInstance(Utils.getContext()).getString(ServerUtils.NAV_SERVER_KEY);
        ServerUtils.APP_SECRET = SessionManager.getInstance(Utils.getContext()).getString(ServerUtils.APP_SECRET_KEY);
        ServerUtils.API_SERVER = SessionManager.getInstance(Utils.getContext()).getString(ServerUtils.API_SERVER_KEY);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                skipToMainPage();
            }
        },1000);
    }

    private void skipToMainPage() {
        Intent intent = new Intent(this, MainPageActivity.class);
        startActivity(intent);
        finish();
    }
}
