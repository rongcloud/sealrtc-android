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
        //首次启动 Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT 为 0，再次点击图标启动时就不为零了
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
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

        SessionManager.getInstance().put(SettingActivity.IS_RONGRTC_CONNECTIONMODE, false);

        ServerUtils.APP_KEY = SessionManager.getInstance().getString(ServerUtils.APP_KEY_KEY);
        ServerUtils.NAV_SERVER = SessionManager.getInstance().getString(ServerUtils.NAV_SERVER_KEY);
        ServerUtils.APP_SECRET = SessionManager.getInstance().getString(ServerUtils.APP_SECRET_KEY);
        ServerUtils.API_SERVER = SessionManager.getInstance().getString(ServerUtils.API_SERVER_KEY);
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
