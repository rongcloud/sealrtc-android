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

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.entity.CountryInfo;
import cn.rongcloud.rtc.message.RoomInfoMessage;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.utils.FinLog;
import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.callback.JoinRoomUICallBack;
import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.local.RongRTCCapture;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.common.DeviceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static cn.rongcloud.rtc.SettingActivity.IS_AUTO_TEST;
import static cn.rongcloud.rtc.util.UserUtils.OBSERVER_MUST;
import static cn.rongcloud.rtc.util.UserUtils.VIDEOMUTE_MUST;
import static cn.rongcloud.rtc.util.UserUtils.isObserver_key;
import static cn.rongcloud.rtc.util.UserUtils.isVideoMute_key;

/**
 * Handles the initial setup where the user selects which room to join.
 */
public class MainPageActivity extends RongRTCBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final int CHECK_BUTTON_DELATY = 1100;
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1200;
    private static final int REQUEST_CODE_VERIFY = 1300;
    private static final int STATE_IDLE = 0;    //未初始化
    private static final int STATE_INIT = 1;    //已初始化
    private static final int STATE_JOINING = 2; //加入中
    private static final int STATE_JOINED = 3;  //已加入
    private static final int STATE_FAILED = -1; //加入失败
    private static final String TAG = "MainPageActivity";
    private static final String BASE_URL = "https://sealrtc.rongcloud.cn/";
    private static final String URL_GET_TOKEN_NEW = "user/get_token_new";
    private static final int CONNECTION_REQUEST = 1;
    private static InputStream cerStream = null;
    private EditText roomEditText, edit_UserName, edit_room_phone, userNameEditText;
    private Button connectButton;
    private ImageView settingButton;
    private TextView versionCodeView;
    private TextView mTvCountry;
    private TextView mTvRegion;
    private AppCompatCheckBox cbCamera;
    private AppCompatCheckBox cbObserver;
    private ImageView logoView;

    //进入房间时是否关闭摄像头
    private static boolean isVideoMute = false;
    //当前房间大于30人时，只能以观察者身份加入房间，不能发布音视频流，app层产品逻辑
    private static boolean isObserver = false;
    //当前房间大于9人时，只能发布音频流，不能发布视频流，app层产品逻辑
    private boolean canOnlyPublishAudio = false;
    private String versionCodeText;
    private int mStatus = STATE_IDLE;

    private String roomId;
    private String username;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private RongRTCConfig.Builder configBuilder;
    private boolean isDebug;
    List<String> unGrantedPermissions;
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.READ_PHONE_STATE",
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.BLUETOOTH"
    };


    public static final String CR_720x1280 = "720x1280";
    public static final String CR_1080x1920 = "1088x1920";
    public static final String CR_480x720 = "480x720";
    public static final String CR_480x640 = "480x640";
    public static final String CR_368x640 = "368x640";
    public static final String CR_368x480 = "368x480";
    public static final String CR_240x320 = "240x320";
    public static final String CR_144x256 = "144x256";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        roomId = getIntent().getStringExtra("roomId");
        checkPermissions();
        initViews();
    }

    private void initViews() {
        roomEditText = (EditText) findViewById(R.id.room_inputnumber);
        roomId = SessionManager.getInstance(Utils.getContext()).getString(UserUtils.ROOMID_KEY);
        if (!TextUtils.isEmpty(roomId)) {
            roomEditText.setText(roomId);
        }
//        if (!TextUtils.isEmpty(roomId)) {
//            roomEditText.setEnabled(false);
//            roomEditText.setText(roomId);
//        }
//        roomEditText.requestFocus();

        edit_room_phone = (EditText) findViewById(R.id.room_phone);
        String phoneNum = SessionManager.getInstance(Utils.getContext()).getString(UserUtils.PHONE);
        if (!TextUtils.isEmpty(phoneNum)) {
            edit_room_phone.setText(phoneNum);
        }

        edit_UserName = (EditText) findViewById(R.id.room_userName);
        edit_UserName.requestFocus();
        edit_UserName.setText(username);
        userNameEditText = (EditText) findViewById(R.id.tv_user_name);
        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setText(R.string.room_connect_button);
        if (TextUtils.isEmpty(edit_room_phone.getText().toString().trim()) || TextUtils.isEmpty(roomId)) {
            connectButton.setBackgroundResource(R.drawable.shape_corner_button_blue_invalid);
            connectButton.setClickable(false);
        } else {
            connectButton.setClickable(true);
            connectButton.setBackgroundResource(R.drawable.shape_corner_button_blue);
        }
        connectButton.setOnClickListener(this);
        settingButton = (ImageView) findViewById(R.id.connect_settings);
        settingButton.setOnClickListener(this);
        versionCodeView = (TextView) findViewById(R.id.main_page_version_code);
        cbCamera = (AppCompatCheckBox) findViewById(R.id.room_cb_close_camera);
        cbObserver = (AppCompatCheckBox) findViewById(R.id.room_cb_observer);
        cbCamera.setOnCheckedChangeListener(this);
        cbObserver.setOnCheckedChangeListener(this);
        logoView = (ImageView) findViewById(R.id.img_logo);
        versionCodeView.setText(getResources().getString(R.string.blink_description_version) + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "_Debug" : ""));
        versionCodeView.setTextColor(getResources().getColor(R.color.blink_text_green));
        versionCodeText = versionCodeView.getText().toString();
        ((TextView) findViewById(R.id.main_page_version)).setTextColor(getResources().getColor(R.color.blink_text_green));
        ((TextView) findViewById(R.id.room_number_description)).setTextColor(getResources().getColor(R.color.blink_blue));
        ((TextView) findViewById(R.id.blink_copyright)).setTextColor(getResources().getColor(R.color.blink_text_grey));
        findViewById(R.id.tv_country).setOnClickListener(this);
        connectButton.setOnClickListener(this);

        roomEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    connectButton.setBackgroundResource(R.drawable.shape_corner_button_blue);
                    connectButton.setClickable(true);
                } else {
                    SessionManager.getInstance(Utils.getContext()).remove(UserUtils.ROOMID_KEY);
                    connectButton.setBackgroundResource(R.drawable.shape_corner_button_blue_invalid);
                    connectButton.setClickable(false);
                }

            }
        });
        mTvCountry = (TextView) findViewById(R.id.tv_country);
        mTvRegion = (TextView) findViewById(R.id.tv_region);
        updateCountry();
    }

    private void clearCer() {
        cerStream = null;
    }

    private String cerUrl = null;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_settings:
                startSetting();
                break;
            case R.id.connect_button:
                if (Utils.isFastDoubleClick()) {
                    return;
                }
                if (null == roomEditText || TextUtils.isEmpty(roomEditText.getText().toString().trim())) {
                    Toast.makeText(this, getResources().getString(R.string.input_roomId), Toast.LENGTH_SHORT).show();
                    return;
                }
                final String phoneNumber = edit_room_phone.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(this, getResources().getString(R.string.input_room_phoneNum), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(phoneNumber.length()<1){
                    Toast.makeText(this, getResources().getString(R.string.input_room_phoneNum_error), Toast.LENGTH_SHORT).show();
                    return;
                }
                SessionManager.getInstance(Utils.getContext()).put(UserUtils.ROOMID_KEY, roomEditText.getText().toString().trim());
                if (!SessionManager.getInstance(Utils.getContext()).contains(phoneNumber)) {
                    startVerifyActivity(phoneNumber);
                } else {
                    if (mStatus == STATE_JOINING)
                        return;
                    mStatus = STATE_JOINING;
                    FinLog.i(TAG,"CurrentConnectionStatu : "+RongIMClient.getInstance().getCurrentConnectionStatus().name());
                    if (RongIMClient.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                        connectToRoom();
                        return;
                    }else {
                        if(isDebug){
                            connectButton.setBackgroundColor(Color.RED);
                        }

                    }
                    String token = SessionManager.getInstance(Utils.getContext()).getString(phoneNumber);
                    FinLog.i(TAG,"token 存在 ："+token);
                    RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {
                            mStatus = STATE_FAILED;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainPageActivity.this, "Token验证失败，请重新获取！", Toast.LENGTH_SHORT).show();
                                    startVerifyActivity(phoneNumber);
                                }
                            });
                        }

                        @Override
                        public void onSuccess(String s) {
                            SessionManager.getInstance(Utils.getContext()).put(UserUtils.PHONE,phoneNumber);
                            connectToRoom();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            mStatus = STATE_FAILED;
                            FinLog.e(TAG, "RongIMClient connect errorcode :" + errorCode);
                        }
                    });
                }
                break;
            case R.id.tv_country:
                Intent intent = new Intent(MainPageActivity.this, CountryListActivity.class);
                startActivityForResult(intent,REQUEST_CODE_SELECT_COUNTRY);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCamerCheck();
        updateConfiguration();
        isDebug = SessionManager.getInstance(this).getBoolean(IS_AUTO_TEST);
        if(isDebug){
            connectButton.setBackgroundColor(R.drawable.shape_corner_button_blue);
        }

        String phoneNum = SessionManager.getInstance(Utils.getContext()).getString(UserUtils.PHONE);
        if (!TextUtils.isEmpty(phoneNum)) {
            edit_room_phone.setText(phoneNum);
        }
    }

    private void updateConfiguration() {
        configBuilder = new RongRTCConfig.Builder();
        //Set max and min bitrate
        String minBitRate = SessionManager.getInstance(this).getString(SettingActivity.BIT_RATE_MIN);
        if (!TextUtils.isEmpty(minBitRate) && minBitRate.length() > 4) {
            int bitRateIntvalue = Integer.valueOf(minBitRate.substring(0, minBitRate.length() - 4));
            FinLog.i(TAG, "BIT_RATE_MIN=" + bitRateIntvalue);
            configBuilder.setMinRate(bitRateIntvalue);
        }
        String maxBitRate = SessionManager.getInstance(this).getString(SettingActivity.BIT_RATE_MAX);
        if (!TextUtils.isEmpty(maxBitRate) && maxBitRate.length() > 4) {
            int bitRateIntvalue = Integer.valueOf(maxBitRate.substring(0, maxBitRate.length() - 4));
            FinLog.i(TAG, "BIT_RATE_MAX=" + bitRateIntvalue);
            configBuilder.setMaxRate(bitRateIntvalue);
        }
        //set resolution
        String resolution = SessionManager.getInstance(this).getString(SettingActivity.RESOLUTION);
        String fps = SessionManager.getInstance(this).getString(SettingActivity.FPS);
        RongRTCConfig.RongRTCVideoProfile videoProfile = selectiveResolution(resolution, fps);
        configBuilder.videoProfile(videoProfile);
        String codec = SessionManager.getInstance(this).getString(SettingActivity.CODECS);
        if (!TextUtils.isEmpty(codec)) {
            if ("VP8".equals(codec)) {
                configBuilder.videoCodecs(RongRTCConfig.RongRTCVideoCodecs.VP8);
            } else {
                configBuilder.videoCodecs(RongRTCConfig.RongRTCVideoCodecs.H264);
            }
        }
        boolean enableTinyStream = SessionManager.getInstance(this).getIsSupportTiny(SettingActivity.IS_STREAM_TINY);
        configBuilder.enableTinyStream(enableTinyStream);
        FinLog.i(TAG, "enableTinyStream: " + enableTinyStream);
        RongRTCCapture.getInstance().setRTCConfig(configBuilder.build());
    }

    private void updateCamerCheck() {
        cbCamera.setChecked(isVideoMute);
        cbObserver.setChecked(isObserver);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.room_cb_close_camera:
                if (isChecked) {
                    isVideoMute = true;
                    cbObserver.setChecked(false);
                } else isVideoMute = false;
                break;
            case R.id.room_cb_observer:
                if (isChecked) {
                    isObserver = true;
                    cbCamera.setChecked(false);
                } else isObserver = false;
                break;
            default:
                break;
        }
    }

    private void startSetting() {
        Intent intent = new Intent(MainPageActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    private void connectToRoom() {
        mStatus = STATE_JOINING;
        if (RongIMClient.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            LoadDialog.show(this);
            final String roomId = roomEditText.getText().toString();
            RongRTCEngine.getInstance().joinRoom(roomId, new JoinRoomUICallBack() {
                @Override
                protected void onUiSuccess(RongRTCRoom rtcRoom) {
                    LoadDialog.dismiss(MainPageActivity.this);
                    Toast.makeText(MainPageActivity.this, getResources().getString(R.string.join_room_success), Toast.LENGTH_SHORT).show();
                    int userCount = rtcRoom.getRemoteUsers().size();
                    if (userCount >= OBSERVER_MUST) {
                        AlertDialog dialog = new AlertDialog.Builder(MainPageActivity.this)
                                .setMessage(getResources().getString(R.string.join_room_observer_prompt))
                                .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        quitRoom(roomId);
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        canOnlyPublishAudio = false;
                                        startCallActivity(true, true);
                                    }
                                })
                                .create();
                        dialog.setCancelable(false);
                        dialog.show();
                    } else if (userCount >= VIDEOMUTE_MUST) {
                        AlertDialog dialog = new AlertDialog.Builder(MainPageActivity.this)
                                .setMessage(getResources().getString(R.string.join_room_audio_only_prompt))
                                .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        quitRoom(roomId);
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        canOnlyPublishAudio = true;
                                        startCallActivity(true, false);

                                    }
                                })
                                .create();
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        canOnlyPublishAudio = false;
                        startCallActivity(isVideoMute, isObserver);
                    }
                }

                @Override
                protected void onUiFailed(RTCErrorCode errorCode) {
                    mStatus = STATE_FAILED;
                    LoadDialog.dismiss(MainPageActivity.this);
                    Toast.makeText(MainPageActivity.this, getResources().getString(R.string.join_room_failed), Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            mStatus = STATE_FAILED;
            Toast.makeText(MainPageActivity.this, getResources().getString(R.string.im_connect_failed), Toast.LENGTH_SHORT).show();
        }

    }

    private void startCallActivity(boolean muteVideo, boolean observer) {
        if (mStatus == STATE_JOINED)
            return;
        mStatus = STATE_JOINED;
        Intent intent = new Intent(this, CallActivity.class);
        //加入房间之前 置为默认状态
        SessionManager.getInstance(Utils.getContext()).put("VideoModeKey", "smooth");
        //
        intent.putExtra(CallActivity.EXTRA_ROOMID, roomEditText.getText().toString());
        intent.putExtra(CallActivity.EXTRA_CAMERA, muteVideo);
        intent.putExtra(CallActivity.EXTRA_OBSERVER, observer);
        intent.putExtra(CallActivity.EXTRA_AUTO_TEST, SessionManager.getInstance(Utils.getContext()).getBoolean(IS_AUTO_TEST));
        RongRTCRoom rongRTCRoom = CenterManager.getInstance().getRongRTCRoom();
        int joinMode = RoomInfoMessage.JoinMode.AUDIO_VIDEO;
        if (observer) {
            joinMode = RoomInfoMessage.JoinMode.OBSERVER;
        }
        if (muteVideo) {
            joinMode = RoomInfoMessage.JoinMode.AUDIO;
        }
        String userId = rongRTCRoom.getLocalUser().getUserId();
        String userName = userNameEditText.getText().toString();
        RoomInfoMessage roomInfoMessage = new RoomInfoMessage(userId, userName, joinMode, System.currentTimeMillis());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("userName", userName);
            jsonObject.put("joinMode", joinMode);
            jsonObject.put("joinTime", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        rongRTCRoom.setRoomAttributeValue(jsonObject.toString(), userId, roomInfoMessage, new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {

            }

            @Override
            public void onUiFailed(RTCErrorCode errorCode) {

            }
        });
        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CONNECTION_REQUEST:
                mStatus = STATE_INIT;
                break;
            case REQUEST_CODE_SELECT_COUNTRY:
            case REQUEST_CODE_VERIFY:
                updateCountry();
                break;
        }
    }

    private void updateCountry() {
        String json = SessionManager.getInstance(this).getString(UserUtils.COUNTRY);
        CountryInfo info;
        if (TextUtils.isEmpty(json)){
            info = CountryInfo.createDefault();
        }else {
            try{
                info = new Gson().fromJson(json, CountryInfo.class);
            }catch (Exception e){
                info = CountryInfo.createDefault();
            }
        }
        mTvCountry.setText(getString(R.string.select_country_hint)+" "+(Utils.isZhLanguage() ? info.zh : info.en));
        mTvRegion.setText("+"+info.region);
    }

    private void connect() {
        String token = sp.getString("tokenNew", "");
        if (!TextUtils.isEmpty(token)) {
            RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                @Override
                public void onTokenIncorrect() {
                    getTokenNew();
                }

                @Override
                public void onSuccess(String s) {
                    FinLog.d(TAG, "IM  connect success ");
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    Toast.makeText(MainPageActivity.this, "连接IM失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            getTokenNew();
        }
    }

    private void getTokenNew() {
        JSONObject loginInfo = new JSONObject();
        try {
            loginInfo.put("id", DeviceUtils.getDeviceId(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(BASE_URL + URL_GET_TOKEN_NEW)
                .method(RequestMethod.POST)
                .body(loginInfo.toString())
                .build();
        HttpClient.getDefault().request(request, new HttpClient.ResultCallback() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int code = jsonObject.getInt("code");
                    if (code == 200) {
                        jsonObject = jsonObject.getJSONObject("result");
                        final String token = jsonObject.getString("token");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                                    @Override
                                    public void onTokenIncorrect() {
                                        LoadDialog.dismiss(MainPageActivity.this);
                                        Toast.makeText(MainPageActivity.this, "token获取失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onSuccess(String s) {
                                        FinLog.d(TAG, "IM  connect success in getTokenNew");
                                        LoadDialog.dismiss(MainPageActivity.this);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("tokenNew", token);
                                        editor.commit();
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {
                                        LoadDialog.dismiss(MainPageActivity.this);
                                        Toast.makeText(MainPageActivity.this, "连接IM失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                        FinLog.d(TAG, "IM  connect = " + errorCode.getValue());
                                    }
                                });
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode) {
                FinLog.d(TAG, "getToken error = " + errorCode);
            }

            @Override
            public void onError(IOException exception) {
                FinLog.d(TAG, "getToken IOException = " + exception.getMessage());
            }
        });
    }

    private void checkPermissions() {
        unGrantedPermissions = new ArrayList();
        for (String permission : MANDATORY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        if (unGrantedPermissions.size() == 0) {//已经获得了所有权限，开始加入聊天室
            initSDK();
        } else {//部分权限未获得，重新请求获取权限
            String[] array = new String[unGrantedPermissions.size()];
            ActivityCompat.requestPermissions(this, unGrantedPermissions.toArray(array), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        unGrantedPermissions.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                unGrantedPermissions.add(permissions[i]);
        }
        if (unGrantedPermissions.size() > 0) {
            for (String permission : unGrantedPermissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    Toast.makeText(this, getString(R.string.PermissionStr) + permission + getString(R.string.plsopenit), Toast.LENGTH_SHORT).show();
                    finish();
                } else ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
            }
        } else {
            initSDK();
        }

    }

    private void quitRoom(String roomId) {
        mStatus = STATE_INIT;
        canOnlyPublishAudio = false;
        RongRTCEngine.getInstance().quitRoom(roomId, new RongRTCResultUICallBack() {

                    @Override
                    public void onUiSuccess() {

                    }

                    @Override
                    public void onUiFailed(RTCErrorCode errorCode) {

                    }
                }
        );
    }

    private void initSDK() {
        mStatus = STATE_INIT;
        RongIMClient.init(getApplication(), "z3v5yqkbv8v30", false);
    }

    /**
     * 构造分辨率对应的BlinkVideoProfile对象
     *
     * @param resolutionStr
     * @return
     */
    private RongRTCConfig.RongRTCVideoProfile selectiveResolution(String resolutionStr, String fpsStr) {
        RongRTCConfig.RongRTCVideoProfile profile = null;
        if (TextUtils.isEmpty(fpsStr)) {
            fpsStr = "15";
        }
        if (CR_144x256.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_144P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_144P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_144P_30f;
            }
        } else if (CR_240x320.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_30f;
            }
        } else if (CR_368x480.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_15f_1;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_24f_1;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_30f_1;
            }
        } else if (CR_368x640.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_15f_2;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_24f_2;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_30f_2;
            }
        } else if (CR_480x640.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_1;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_24f_1;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_30f_1;
            }
        } else if (CR_480x720.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_2;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_24f_2;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_30f_2;
            }
        } else if (CR_720x1280.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_30f;
            }
        } else if (CR_1080x1920.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_30f;
            }
        } else if (CR_720x1280.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_30f;
            }
        }
        return profile;
    }

    private void startVerifyActivity(String phoneNumber){
        SessionManager.getInstance(Utils.getContext()).put(UserUtils.PHONE, phoneNumber);
        SessionManager.getInstance(Utils.getContext()).put(isVideoMute_key, isVideoMute);
        SessionManager.getInstance(Utils.getContext()).put(isObserver_key, isObserver);
        Intent intent = new Intent(MainPageActivity.this, VerifyActivity.class);
        startActivityForResult(intent,REQUEST_CODE_VERIFY);
    }
}
