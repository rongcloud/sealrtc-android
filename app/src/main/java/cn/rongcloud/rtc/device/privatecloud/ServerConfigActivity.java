package cn.rongcloud.rtc.device.privatecloud;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.RongRTCEngine;
import cn.rongcloud.rtc.SettingActivity;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.utils.FinLog;

/**
 * 私有云地址自定义页面
 */
public class ServerConfigActivity extends Activity {

    private static final String TAG = "ServerConfigActivity";
    private EditText edit_appkey, edit_NavServer, edit_appSecret, edit_apiServer, edit_mediaServer;
    String appkey = "", navServer = "", appSecret = "", apiServer = "", mediaServer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        edit_appkey = (EditText) findViewById(R.id.edit_appkey);
        edit_NavServer = (EditText) findViewById(R.id.edit_NavServer);
        edit_appSecret = (EditText) findViewById(R.id.edit_appSecret);
        edit_apiServer = (EditText) findViewById(R.id.edit_apiServer);
        edit_mediaServer = (EditText) findViewById(R.id.edit_media_server);
        findViewById(R.id.settings_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SessionManager.getInstance().put(SettingActivity.IS_RONGRTC_CONNECTIONMODE, false);
        appkey = SessionManager.getInstance().getString(ServerUtils.APP_KEY_KEY);
        navServer = SessionManager.getInstance().getString(ServerUtils.NAV_SERVER_KEY);
        appSecret = SessionManager.getInstance().getString(ServerUtils.APP_SECRET_KEY);
        apiServer = SessionManager.getInstance().getString(ServerUtils.API_SERVER_KEY);
        mediaServer = SessionManager.getInstance().getString(ServerUtils.MEDIA_SERVER_URL_KEY);

        edit_appkey.setText(appkey);
        edit_NavServer.setText(navServer);
        edit_appSecret.setText(appSecret);
        edit_apiServer.setText(apiServer);
        edit_mediaServer.setText(mediaServer);
    }

    public void configClick(View view) {
        appkey = edit_appkey.getText().toString().trim();
        navServer = edit_NavServer.getText().toString().trim();
        appSecret = edit_appSecret.getText().toString().trim();
        apiServer = edit_apiServer.getText().toString().trim();
        mediaServer = edit_mediaServer.getText().toString().trim();

        if (!TextUtils.isEmpty(appkey)) {
            ServerUtils.APP_KEY = appkey;
            SessionManager.getInstance().put(ServerUtils.APP_KEY_KEY, appkey);
        } else {
            ServerUtils.APP_KEY = "";
            SessionManager.getInstance().remove(ServerUtils.APP_KEY_KEY);
        }
        if (!TextUtils.isEmpty(navServer)) {
            ServerUtils.NAV_SERVER = navServer;
            SessionManager.getInstance().put(ServerUtils.NAV_SERVER_KEY, navServer);
        } else {
            ServerUtils.NAV_SERVER = "";
            SessionManager.getInstance().remove(ServerUtils.NAV_SERVER_KEY);
        }

        if (!TextUtils.isEmpty(appSecret)) {
            ServerUtils.APP_SECRET = appSecret;
            SessionManager.getInstance().put(ServerUtils.APP_SECRET_KEY, appSecret);
        } else {
            ServerUtils.APP_SECRET = "";
            SessionManager.getInstance().remove(ServerUtils.APP_SECRET_KEY);
        }

        if (!TextUtils.isEmpty(apiServer)) {
            ServerUtils.API_SERVER = apiServer;
            SessionManager.getInstance().put(ServerUtils.API_SERVER_KEY, apiServer);
        } else {
            ServerUtils.API_SERVER = "";
            SessionManager.getInstance().remove(ServerUtils.API_SERVER_KEY);
        }

        if (!TextUtils.isEmpty(mediaServer)) {
            ServerUtils.MEDIA_SERVER = mediaServer;
            SessionManager.getInstance().put(ServerUtils.MEDIA_SERVER_URL_KEY, mediaServer);
        } else {
            ServerUtils.MEDIA_SERVER = "";
            SessionManager.getInstance().remove(ServerUtils.MEDIA_SERVER_URL_KEY);
        }
        RongRTCEngine.getInstance().setMediaServerUrl(mediaServer);

        SessionManager.getInstance().remove(ServerUtils.TOKEN_PRIVATE_CLOUD_KEY);

        Toast.makeText(this, getResources().getString(R.string.save_successful), Toast.LENGTH_SHORT).show();
        String logAppkey = (ServerUtils.APP_KEY.equals(UserUtils.APP_KEY)) ? "---" : ServerUtils.APP_KEY;
        String logNavServer = (ServerUtils.NAV_SERVER.equals(UserUtils.NAV_SERVER)) ? "---" : ServerUtils.NAV_SERVER;
        FinLog.v(TAG, "APP_KEY : " + logAppkey);
        FinLog.v(TAG, "NAV_SERVER :" + logNavServer);
        FinLog.v(TAG, "appSecret :" + appSecret);
        FinLog.v(TAG, "apiServer :" + apiServer);
        FinLog.v(TAG, "mediaServer :" + mediaServer);

        if(!TextUtils.isEmpty(ServerUtils.APP_KEY) &&
                !TextUtils.isEmpty(ServerUtils.NAV_SERVER)){
            EventBus.getDefault().post(TAG);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
