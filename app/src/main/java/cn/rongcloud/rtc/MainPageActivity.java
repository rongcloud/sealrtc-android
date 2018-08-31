/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package cn.rongcloud.rtc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import cn.rongcloud.rtc.base.RongRTCBaseActivity;

import cn.rongcloud.rtc.util.AppRTCUtils;
import cn.rongcloud.rtc.engine.binstack.http.RongRTCHttpClient;
import cn.rongcloud.rtc.engine.binstack.util.FinLog;
import cn.rongcloud.rtc.entity.CMPAddress;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.util.Utils;

import cn.rongcloud.rtc.engine.binstack.http.QuicHttpCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static cn.rongcloud.rtc.SettingActivity.IS_RONGRTC_CONNECTIONMODE;

/**
 * Handles the initial setup where the user selects which room to join.
 */
public class MainPageActivity extends RongRTCBaseActivity {
    private static final String TAG = "ConnectActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static InputStream cerStream = null;
    private EditText roomEditText, edit_UserName;
    private Button connectButton;
    private ImageView settingButton;
    private TextView versionCodeView;
    private AppCompatCheckBox cbCamera;
    private AppCompatCheckBox cbObserver;
    private ProgressDialog loadingDialog;
    private ImageView logoView;
    private AlertDialog choiceServerDialog = null;

    private static final String TOKEN_SERVER_URL_EXTERNAL = "https://api.blinkcloud.cn:8800/token";
    private static final String RELEASE_HTTP_CONFIG_SERVER_URL = "https://rtcapi.ronghub.com/nav/rtclist";
    private static final String RELEASE_QUIC_CONFIG_SERVER_URL = "http://rtcapi.ronghub.com:8801/nav/rtclist";

    private String[] serverNames;
    private List<ConfigServersMode> configServersModes;
    private String tokenServerURL = TOKEN_SERVER_URL_EXTERNAL;
    private boolean isVideoMute = false;
    private boolean isObserver = false;
    private String versionCodeText;

    private static final boolean LANMODE = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connect);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        UnhandledExceptionHandler.prepareFolder();

        initViews();

        setupListeners();

        if (LANMODE) {
            Utils.getConfigListError(mHandler);
        } else {
            chooseServer();
        }
    }

    private void initViews() {
        roomEditText = (EditText) findViewById(R.id.room_inputnumber);
        roomEditText.requestFocus();

        edit_UserName = (EditText) findViewById(R.id.room_userName);
        edit_UserName.requestFocus();
        try {
            String username = "";
            username = SessionManager.getInstance(Utils.getContext()).getString(UserUtils.USERNAME_KEY);
            edit_UserName.setText(username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setText(R.string.app_btn_loading);
        changeButtonState(false);

        settingButton = (ImageView) findViewById(R.id.connect_settings);
        versionCodeView = (TextView) findViewById(R.id.main_page_version_code);
        cbCamera = (AppCompatCheckBox) findViewById(R.id.room_cb_close_camera);
        cbObserver = (AppCompatCheckBox) findViewById(R.id.room_cb_observer);
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
        logoView = (ImageView) findViewById(R.id.img_logo);

        versionCodeView.setText(getResources().getString(R.string.blink_description_version) + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "_Debug" : ""));
        versionCodeView.setTextColor(getResources().getColor(R.color.blink_text_green));
        versionCodeText = versionCodeView.getText().toString();
        ((TextView) findViewById(R.id.main_page_version)).setTextColor(getResources().getColor(R.color.blink_text_green));
        ((TextView) findViewById(R.id.room_number_description)).setTextColor(getResources().getColor(R.color.blink_blue));
        ((TextView) findViewById(R.id.blink_copyright)).setTextColor(getResources().getColor(R.color.blink_text_grey));

    }

    private void getServerList(final String url, final boolean needDialog) {
//        boolean isQuic = SessionManager.getInstance(this).getBoolean(IS_RONGRTC_CONNECTIONMODE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String result = RongRTCHttpClient.getInstance().doGet(url);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(result) && null != mHandler) {
                            Utils.getConfigListError(mHandler);
                        } else {
                            parseResult(result, needDialog);
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * @param result
     * @param needDiolog debug:true release:false
     */
    private void parseResult(String result, final boolean needDiolog) {
        if (!TextUtils.isEmpty(result)) {
            clearCer();
            try {
                JSONArray jsonArray = new JSONArray(result);
                if (jsonArray != null && jsonArray.length() != 0) {
                    serverNames = new String[jsonArray.length()];
                    configServersModes = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ConfigServersMode configServersMode = new ConfigServersMode();
                        if (jsonObject.has("name")) {
                            configServersMode.setName(jsonObject.getString("name"));
                            serverNames[i] = configServersMode.getName();
                        }else{
                            serverNames[i]="环境 "+(i+1);
                        }
                        if (jsonObject.has("nav"))
                            configServersMode.setNav(jsonObject.getString("nav"));
                        if (jsonObject.has("cmp"))
                            configServersMode.setCmp(jsonObject.getString("cmp"));
                        if (jsonObject.has("cmptls"))
                            configServersMode.setCmptls(jsonObject.getString("cmptls"));
//                        if (jsonObject.has("sniffer"))
//                            configServersMode.sniffer = jsonObject.getString("sniffer");
//                        if (jsonObject.has("sniffertls"))
//                            configServersMode.sniffertls = jsonObject.getString("sniffertls");
                        if (jsonObject.has("token"))
                            configServersMode.setToken(jsonObject.getString("token"));
                        if (jsonObject.has("crt"))
                            configServersMode.setCrt(jsonObject.getString("crt"));

                        if (jsonObject.has("quic")) {
                            JSONObject quicjb = jsonObject.getJSONObject("quic");
                            ConfigServersMode.QuicBean quicBean = new ConfigServersMode.QuicBean();
                            quicBean.setCmp(quicjb.getString("cmp"));
                            quicBean.setNav(quicjb.getString("nav"));
                            quicBean.setToken(quicjb.getString("token"));
                            configServersMode.setQuic(quicBean);
                        }
                        if (jsonObject.has("tcp")) {
                            JSONObject tcpjb = jsonObject.getJSONObject("tcp");
                            ConfigServersMode.TcpBean tcpBean = new ConfigServersMode.TcpBean();
                            tcpBean.setCmp(tcpjb.getString("cmp"));
                            tcpBean.setNav(tcpjb.getString("nav"));
                            tcpBean.setToken(tcpjb.getString("token"));
                            configServersMode.setTcp(tcpBean);
                        }
                        configServersModes.add(configServersMode);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleLoadingDialog(true);
                int configServerModesSize = 0;
                if (null != configServersModes) {
                    configServerModesSize = configServersModes.size();
                }
                if (configServersModes != null && configServerModesSize > 0) {
                    chooseServerMode(0);
                }
            }
        });
    }

    private void toggleLoadingDialog(boolean close) {
        if (!close && null != loadingDialog) {
            loadingDialog.show();
        } else {
            loadingDialog.dismiss();
        }
    }
    private void clearCer() {
        cerStream = null;
        RongRTCEngine.setCertificate(null, null);
    }

    private String cmpServer = "";
    private String cerUrl = null;

    private void chooseServerMode(int index) {
        ConfigServersMode configServersMode = configServersModes.get(index);
        cmpServer = !TextUtils.isEmpty(configServersMode.getCmptls()) ? configServersMode.getCmptls() : configServersMode.getCmp();
        tokenServerURL = configServersMode.getToken();
        cerUrl = configServersMode.getCrt();

        boolean isquic = SessionManager.getInstance(this).getBoolean(IS_RONGRTC_CONNECTIONMODE);
        if (isquic && null != configServersMode.getQuic()) {
            ConfigServersMode.QuicBean quicBean = configServersMode.getQuic();
            StringBuffer sb_quic = new StringBuffer(Utils.QUIC);
            cmpServer = sb_quic.append(quicBean.getCmp()).toString();
            tokenServerURL = quicBean.getToken();
            cerUrl = "";
        } else if (!isquic && null != configServersMode.getTcp()) {
            StringBuffer sb_tcp = new StringBuffer(Utils.TCP);
            ConfigServersMode.TcpBean tcpBean = configServersMode.getTcp();
            cmpServer = sb_tcp.append(tcpBean.getCmp()).toString();
            tokenServerURL = tcpBean.getToken();
            cerUrl = "";
        } else {
            StringBuffer sb_tcp = new StringBuffer(Utils.TCP);
            cmpServer = sb_tcp.append(cmpServer).toString();
            SessionManager.getInstance(this).put(IS_RONGRTC_CONNECTIONMODE, false);
//            RongRTCEngine.getInstance().setRongRTCConnectionMode(false);
        }

        SessionManager.getInstance(Utils.getContext()).put(AppRTCUtils.CER_URL, cerUrl);
        AppRTCUtils.setCMPAddress(cmpServer, tokenServerURL, 0);

        if (versionCodeView != null && null != configServersMode && !TextUtils.isEmpty(configServersMode.getName())) {
            versionCodeView.setText(versionCodeText + "_" + configServersMode.getName());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (cerStream == null && !TextUtils.isEmpty(cerUrl)) {
                        cerStream = AppRTCUtils.downLoadFromUrl(cerUrl);
                        InputStream input = cerStream;

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = input.read(buffer)) > -1) {
                            baos.write(buffer, 0, len);
                        }
                        baos.flush();
                        InputStream stream1 = new ByteArrayInputStream(baos.toByteArray());
                        InputStream stream2 = new ByteArrayInputStream(baos.toByteArray());
                        RongRTCEngine.setCertificate(stream1, stream2);
                    }

                    if (TextUtils.isEmpty(cerUrl)) {
                        RongRTCEngine.setCertificate(null, null);
                    }
                    Message message = new Message();
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("cmpServer", cmpServer);
//                    bundle.putString("snifferServer",snifferServer);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RongRTCEngine.init(Utils.getContext(), cmpServer);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                try {
                    connectButton.setText(getResources().getString(R.string.room_connect_button));
                    changeButtonState(true);
                    Bundle bundle = msg.getData();
                    RongRTCEngine.init(Utils.getContext(), bundle.get("cmpServer").toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msg.what == 2) {
                try {
                    connectButton.setText(getResources().getString(R.string.room_connect_button));
                    changeButtonState(true);
                    Bundle bundle = msg.getData();
                    RongRTCEngine.init(Utils.getContext(), bundle.get("cmpServer").toString());
                    AppRTCUtils.setCMPAddress(bundle.get("cmpServer").toString(), bundle.get("tokenUrl").toString(), 0);
                    toggleLoadingDialog(true);
//                    Utils.resetConnectionMode(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private void setupListeners() {
        settingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ButtentSolp.check(v.getId(), 1600)) {
                    return;
                }
                Intent intent = new Intent(MainPageActivity.this, SettingActivity.class);
                startActivity(intent);
            }

        });
        cbCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isVideoMute = true;
                    cbObserver.setChecked(false);
                } else isVideoMute = false;
            }
        });
        cbObserver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isObserver = true;
                    cbCamera.setChecked(false);
                } else isObserver = false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        String room = roomEditText.getText().toString();
        SessionManager.getInstance(this).put(SessionManager.ROOM_NUM, room);
    }

    @Override
    public void onResume() {
        super.onResume();
        String room = SessionManager.getInstance(this).getString(SessionManager.ROOM_NUM);
        roomEditText.setText(room);
    }

    private void changeButtonState(boolean state) {
        if (state) {
            connectButton.setBackgroundResource(R.drawable.shape_corner_button_blue);
            connectButton.setOnClickListener(connectListener);
        } else {
            connectButton.setBackgroundResource(R.drawable.shape_corner_button_blue_invalid);
            connectButton.setOnClickListener(null);
        }
    }


    private void chooseServer() {
        if (Utils.isNetWorkAvailable(MainPageActivity.this)) {
            toggleLoadingDialog(false);
            String configServerUrl = "";
            boolean isNeedDialog = true;
            boolean isQuic = SessionManager.getInstance(this).getBoolean(IS_RONGRTC_CONNECTIONMODE);
            configServerUrl = isQuic ? RELEASE_QUIC_CONFIG_SERVER_URL : RELEASE_HTTP_CONFIG_SERVER_URL;
            isNeedDialog = false;
            getServerList(configServerUrl, isNeedDialog);
        } else {
            connectButton.setText(R.string.Thecurrentnetworkisnotavailable);
        }
    }

    private CMPAddress cmpAddress = null;

    private void connectToRoom(String userName) {
        Intent intent = new Intent(this, CallActivity.class);
        if (SessionManager.getInstance(Utils.getContext()).contains(AppRTCUtils.CUSTOM_CMPKEY)) {
            cmpAddress = AppRTCUtils.getCMPAddress(AppRTCUtils.CUSTOM_CMPKEY);
            if (null != cmpAddress && !TextUtils.isEmpty(cmpAddress.getServerURL())) {
                tokenServerURL = cmpAddress.getServerURL();
                clearCer();
                RongRTCEngine.setVOIPServerAddress(cmpAddress.getCmpServer());
            }
        } else if (SessionManager.getInstance(Utils.getContext()).contains(AppRTCUtils.SELECT_KEY)) {
            cmpAddress = AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY);
            if (null != cmpAddress && !TextUtils.isEmpty(cmpAddress.getServerURL())) {
                if(cmpAddress.getCmpServer().indexOf(Utils.QUIC)!=-1){
                    clearCer();
                }
                tokenServerURL = cmpAddress.getServerURL();
            }
        }
        //加入房间之前 置为默认状态
        SessionManager.getInstance(Utils.getContext()).put("VideoModeKey","smooth");
        //
        intent.putExtra(CallActivity.EXTRA_ROOMID, roomEditText.getText().toString().trim());
        intent.putExtra(CallActivity.EXTRA_SERVER_URL, tokenServerURL);
        intent.putExtra(CallActivity.EXTRA_CAMERA, isVideoMute);
        intent.putExtra(CallActivity.EXTRA_OBSERVER, isObserver);
        intent.putExtra(CallActivity.EXTRA_USER_NAME, userName);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    private final OnClickListener connectListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ButtentSolp.check(view.getId(), 1100)) {
                return;
            }
            if (null != edit_UserName && null != roomEditText) {
                String userName = edit_UserName.getText().toString().trim();
                String channelText = roomEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(channelText)) {
                    String msg = UserUtils.startInspect(userName);
                    if (TextUtils.isEmpty(msg)) {
                        SessionManager.getInstance(Utils.getContext()).put(UserUtils.USERNAME_KEY, userName);
                        connectToRoom(userName);
                    } else {
                        Toast.makeText(MainPageActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainPageActivity.this, R.string.Waitingforinput, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainPageActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBusComplaint(String val) {
        if (val.equals("1")) {
            chooseServer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().removeStickyEvent("1");
        EventBus.getDefault().unregister(this);

        if (null != choiceServerDialog && choiceServerDialog.isShowing()) {
            choiceServerDialog.dismiss();
        }
        if (null != loadingDialog && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
