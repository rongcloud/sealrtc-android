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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import cn.rongcloud.rtc.util.CustomizedEncryptionUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.api.RCRTCConfig.Builder;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.stream.RCRTCAudioStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.base.RCRTCParamsType.AECMode;
import cn.rongcloud.rtc.base.RCRTCParamsType.NSLevel;
import cn.rongcloud.rtc.base.RCRTCParamsType.NSMode;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RCRTCParamsType.VideoBitrateMode;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.call.CallActivity;
import cn.rongcloud.rtc.device.AVSettingsActivity;
import cn.rongcloud.rtc.device.privatecloud.ServerUtils;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.entity.CountryInfo;
import cn.rongcloud.rtc.entity.KickEvent;
import cn.rongcloud.rtc.entity.KickedOfflineEvent;
import cn.rongcloud.rtc.util.http.HttpClient;
import cn.rongcloud.rtc.util.http.Request;
import cn.rongcloud.rtc.util.http.RequestMethod;
import cn.rongcloud.rtc.message.RoomInfoMessage;
import cn.rongcloud.rtc.updateapk.UpDateApkHelper;
import cn.rongcloud.rtc.util.PromptDialog;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import io.rong.common.RLog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectionErrorCode;
import io.rong.imlib.RongIMClient.DatabaseOpenStatus;
import io.rong.imlib.common.DeviceUtils;

import static android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION;
import static cn.rongcloud.rtc.SettingActivity.IS_AUTO_TEST;
import static cn.rongcloud.rtc.SettingActivity.IS_MIRROR;
import static cn.rongcloud.rtc.SettingActivity.IS_WATER;
import static cn.rongcloud.rtc.util.UserUtils.APP_KEY;
import static cn.rongcloud.rtc.util.UserUtils.OBSERVER_MUST;
import static cn.rongcloud.rtc.util.UserUtils.VIDEOMUTE_MUST;
import static cn.rongcloud.rtc.util.UserUtils.isObserver_key;
import static cn.rongcloud.rtc.util.UserUtils.isVideoMute_key;

/** Handles the initial setup where the user selects which room to join. */
public class MainPageActivity extends RongRTCBaseActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int CHECK_BUTTON_DELATY = 1100;
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1200;
    private static final int REQUEST_CODE_VERIFY = 1300;
    private static final int STATE_IDLE = 0; // 未初始化
    private static final int STATE_INIT = 1; // 已初始化
    private static final int STATE_JOINING = 2; // 加入中
    private static final int STATE_JOINED = 3; // 已加入
    private static final int STATE_FAILED = 4; // 加入失败
    private static final String TAG = "MainPageActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static final long KICK_SILENT_PERIOD = 5 * 60 * 1000L;
    private EditText roomEditText, edit_UserName, edit_room_phone, userNameEditText;
    private Button connectButton;
    private ImageView settingButton;
    private TextView versionCodeView;
    private TextView mTvCountry;
    private TextView mTvRegion;
    private AppCompatCheckBox cbCamera;
    private AppCompatCheckBox cbObserver,room_cb_quiktest;
    private ImageView logoView;

    // 进入房间时是否关闭摄像头
    private static boolean isVideoMute = false;
    // 当前房间大于30人时，只能以观察者身份加入房间，不能发布音视频流，app层产品逻辑
    private static boolean isObserver = false;
    // 当前房间大于9人时，只能发布音频流，不能发布视频流，app层产品逻辑
    private boolean canOnlyPublishAudio = false;
    private String versionCodeText;
    private int mStatus = STATE_IDLE;

    private String roomId;
    private String username;
    private boolean isDebug;
    //设置>自动化测试开关，自动化测试时，IM未连接状态，监听IM连接状态，连接成功再加入房间
    //这个变量专门为自动化测试加
    private boolean joinRoomWhenConnectedInAutoTest;
    //IM 是非 CONNECTED,调用connect接口会返回RC_CONNECTION_EXIST，需要监听setConnectionStatusListener加入房间
    private boolean joinRoomWhenReconnected;
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

    private int tapStep = 0;
    private long lastClickTime = 0;
    private View mLiveView;
    private boolean mIsLive = false;

    private boolean TokenIncorrectMark = true; // 记录私有云环境请求token失败后，仅重试一次
    private static final String QUICK_TEST_KEY ="QUICK_TEST_KEY";//记录是否使用快速测试界面

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        roomId = getIntent().getStringExtra("roomId");
        checkPermissions();
        initViews();
        if (!ServerUtils.usePrivateCloud()) { // 私有云的安装包不检查升级
            new UpDateApkHelper(this).diffVersionFromServer();
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

    }

    private void initOrUpdateRTCEngine() {
        SessionManager sm = SessionManager.getInstance();


        Builder configBuilder = Builder.create();
        boolean audioEncryption = false;
        boolean videoEncryption = false;
        boolean isSRTP = false;
        if (sm.getBoolean(getResources().getString(R.string.key_use_av_setting), false)) {
            boolean enableStereo = sm.getBoolean(Consts.SP_AUDIO_STEREO_ENABLE, false);
            int audioBitrate = sm.getInt(Consts.SP_AUDIO_TRANSPORT_BIT_RATE, 30);
            int audioSampleRate = sm.getInt(Consts.SP_AUDIO_SAMPLE_RATE, 48000);
            int audioSource = sm.getInt(Consts.SP_AUDIO_SOURCE, VOICE_COMMUNICATION);
            VideoBitrateMode videoBitrateMode;
            String encoderBitRateMode = sm.getString(Consts.SP_ENCODER_BIT_RATE_MODE);
            if (TextUtils.equals(encoderBitRateMode, Consts.ENCODE_BIT_RATE_MODE_CQ)) {
                videoBitrateMode = VideoBitrateMode.CQ;
            } else if (TextUtils.equals(encoderBitRateMode, Consts.ENCODE_BIT_RATE_MODE_VBR)) {
                videoBitrateMode = VideoBitrateMode.VBR;
            } else {
                videoBitrateMode = VideoBitrateMode.CBR;
            }
            audioEncryption = sm.getBoolean(SettingActivity.IS_AUDIO_ENCRYPTION, false);
            videoEncryption = sm.getBoolean(SettingActivity.IS_VIDEO_ENCRYPTION,  false);
            isSRTP = sm.getBoolean(SettingActivity.IS_SRTP_ENABLED, false);
            /* 是否启动 AudioRecord */
            configBuilder.enableMicrophone(true)
                    /* 是否采用双声道 */
                    .enableStereo(enableStereo)
                    /* 设置麦克采集来源 */
                    .setAudioSource(audioSource)
                    /* 设置音频码率 */
                    .setAudioBitrate(audioBitrate)
                    /* 设置音频采样率 */
                    .setAudioSampleRate(audioSampleRate)
                    /* 视频编码配置 */
                    .enableHardwareEncoder(sm.getBoolean(Consts.SP_ENCODER_TYPE_KEY, true))
                    .setHardwareEncoderColor(sm.getInt(Consts.SP_ENCODER_COLOR_FORMAT_VAL_KEY, 0))
                    .enableEncoderTexture(sm.getBoolean(Consts.ACQUISITION_MODE_KEY, true))
                    .enableHardwareEncoderHighProfile(sm.getBoolean(Consts.SP_ENCODER_LEVEL_KEY, false))
                    /* 视频解码配置 */
                    .enableHardwareDecoder(sm.getBoolean(Consts.SP_DECODER_TYPE_KEY, true))
                    .setHardwareDecoderColor(sm.getInt(Consts.SP_DECODER_COLOR_FORMAT_VAL_KEY, 0))
                    /* 编码码率控制模式 */
                    .setHardwareEncoderBitrateMode(videoBitrateMode)
                    /* 开启自定义音频加解密 */
                    .enableAudioEncryption(audioEncryption)
                    /* 开启自定义视频加解密 */.enableVideoEncryption(videoEncryption)
                /* 开启SRTP*/.enableSRTP(isSRTP);
        }
        RCRTCEngine.getInstance().unInit();
        //自定义加解密 so 加载
        if(audioEncryption|| videoEncryption){
            CustomizedEncryptionUtil.getInstance().init();
        }
        RLog.d(TAG, "initOrUpdateRTCEngine: ");
        FinLog.d(TAG + "", "init --> enter");
        RCRTCEngine.getInstance().init(getApplicationContext(), configBuilder.build());
        FinLog.d(TAG + "", "init --> over");

        RCRTCAudioStreamConfig.Builder audioConfigBuilder = RCRTCAudioStreamConfig.Builder.create();
        if (sm.getBoolean(getResources().getString(R.string.key_use_av_setting), false)) {
            int echoCancelMode = sm.getInt(Consts.SP_AUDIO_ECHO_CANCEL_MODE, 0);
            int noiseSuppression = sm.getInt(Consts.SP_AUDIO_NOISE_SUPPRESSION_MODE, 0);
            int noiseSuppressionLevel = sm.getInt(Consts.SP_AUDIO_NOISE_SUPPRESSION_LEVEL, 1);
            boolean enableHighPassFilter = sm.getBoolean(Consts.SP_AUDIO_NOISE_HIGH_PASS_FILTER, true);
            /* Audio Echo Cancel */
            audioConfigBuilder.setEchoCancel(AECMode.parseValue(echoCancelMode))
                    .enableEchoFilter(sm.getBoolean(Consts.SP_AUDIO_ECHO_CANCEL_FILTER_ENABLE, false))
                    /* Audio Noise Suppression */
                    .setNoiseSuppression(NSMode.parseValue(noiseSuppression))
                    .setNoiseSuppressionLevel(NSLevel.parseValue(noiseSuppressionLevel))
                    .enableHighPassFilter(enableHighPassFilter)
                    /* Audio AGC Config */
                    .enableAGCControl(sm.getBoolean(Consts.SP_AUDIO_AGC_CONTROL_ENABLE, true))
                    .enableAGCLimiter(sm.getBoolean(Consts.SP_AUDIO_AGC_LIMITER_ENABLE, true))
                    .setAGCTargetdbov(sm.getInt(Consts.SP_AUDIO_AGC_TARGET_DBOV, -3))
                    .setAGCCompression(sm.getInt(Consts.SP_AUDIO_AGC_COMPRESSION, 9))
                    .enablePreAmplifier(sm.getBoolean(Consts.SP_AUDIO_PRE_AMPLIFIER_ENABLE, true))
                    .setPreAmplifierLevel(sm.getFloat(Consts.SP_AUDIO_PRE_AMPLIFIER_LEVEL, 1.0f));
            boolean isMusicMode = sm.getBoolean(SettingActivity.IS_AUDIO_MUSIC, false);
            RCRTCAudioStreamConfig audioStreamConfig = isMusicMode ?
                    audioConfigBuilder.buildMusicMode() : audioConfigBuilder.buildDefaultMode();
            FinLog.d(TAG + "", "Audio --> enter");
            RCRTCEngine.getInstance().getDefaultAudioStream().setAudioConfig(audioStreamConfig);
        }

        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        // 如果开启了镜像翻转 VideoFrame，则应关闭镜像预览功能，否则会造成2次翻转效果
        RCRTCEngine.getInstance().getDefaultVideoStream().setPreviewMirror(!sm.getBoolean(IS_MIRROR));
        /* 视频分辨率/码率 */
        String maxBitRate = sm.getString(SettingActivity.BIT_RATE_MAX, "");
        String minBitRate = sm.getString(SettingActivity.BIT_RATE_MIN);
        if (!TextUtils.isEmpty(maxBitRate)) {
            videoConfigBuilder.setMaxRate(Integer.parseInt(maxBitRate.substring(0, maxBitRate.length() - 4)));
        }
        if (!TextUtils.isEmpty(minBitRate)) {
            videoConfigBuilder.setMinRate(Integer.parseInt(minBitRate.substring(0, minBitRate.length() - 4)));
        }
        videoConfigBuilder.setVideoResolution(selectiveResolution(sm.getString(SettingActivity.RESOLUTION)))
                .setVideoFps(selectiveFrame(sm.getString(SettingActivity.FPS)));
        RCRTCEngine.getInstance().getDefaultVideoStream().
                enableTinyStream(sm.getIsSupportTiny(SettingActivity.IS_STREAM_TINY));
        RCRTCEngine.getInstance().getDefaultVideoStream().setCameraDisplayOrientation(
                sm.getInt(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY, 0));
        RCRTCEngine.getInstance().getDefaultVideoStream().setFrameOrientation(
                sm.getInt(Consts.CAPTURE_FRAME_ORIENTATION_KEY, -1));
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder.build());
    }

    private void initViews() {
        roomEditText = (EditText) findViewById(R.id.room_inputnumber);
        roomId = SessionManager.getInstance().getString(UserUtils.ROOMID_KEY);

        if (!TextUtils.isEmpty(roomId)) {
            roomEditText.setText(roomId);
        }
        edit_room_phone = (EditText) findViewById(R.id.room_phone);
        String phoneNum = SessionManager.getInstance().getString(UserUtils.PHONE);
        if (!TextUtils.isEmpty(phoneNum)) {
            edit_room_phone.setText(phoneNum);
        }

        edit_UserName = (EditText) findViewById(R.id.room_userName);
        edit_UserName.requestFocus();
        edit_UserName.setText(username);
        userNameEditText = (EditText) findViewById(R.id.tv_user_name);
        userNameEditText.setText(SessionManager.getInstance().getString(UserUtils.USERNAME_KEY));
        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setText(R.string.room_connect_button);
        if (TextUtils.isEmpty(edit_room_phone.getText().toString().trim())
                || TextUtils.isEmpty(roomId)) {
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
        room_cb_quiktest=findViewById(R.id.room_cb_quiktest);
        boolean quickTest=SessionManager.getInstance().getBoolean(QUICK_TEST_KEY);
        room_cb_quiktest.setChecked(quickTest);
        room_cb_quiktest.setOnCheckedChangeListener(this);
        room_cb_quiktest.setVisibility(BuildConfig.DEBUG ? View.VISIBLE:View.GONE);

        cbCamera.setOnCheckedChangeListener(this);
        cbObserver.setOnCheckedChangeListener(this);
        logoView = (ImageView) findViewById(R.id.img_logo);
        if (logoView != null) {
            if (ServerUtils.usePrivateCloud()) {
                logoView.setImageResource(R.drawable.ic_launcher_privatecloud);
            } else {
                logoView.setImageResource(R.drawable.ic_launcher);
            }
        }

        versionCodeView.setText(getResources().getString(R.string.blink_description_version) +
            BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "_Debug" : ""));
        versionCodeView.setTextColor(getResources().getColor(R.color.blink_text_green));
        versionCodeText = versionCodeView.getText().toString();
        ((TextView) findViewById(R.id.main_page_version))
                .setTextColor(getResources().getColor(R.color.blink_text_green));
        ((TextView) findViewById(R.id.room_number_description))
                .setTextColor(getResources().getColor(R.color.blink_blue));
        ((TextView) findViewById(R.id.blink_copyright))
                .setTextColor(getResources().getColor(R.color.blink_text_grey));
        mTvCountry = (TextView) findViewById(R.id.tv_country);
        mTvCountry.setOnClickListener(this);
        connectButton.setOnClickListener(this);

        roomEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    connectButton.setBackgroundResource(R.drawable.shape_corner_button_blue);
                    connectButton.setClickable(true);
                } else {
                    SessionManager.getInstance().remove(UserUtils.ROOMID_KEY);
                    connectButton.setBackgroundResource(R.drawable.shape_corner_button_blue_invalid);
                    connectButton.setClickable(false);
                }
            }
        });
        mTvRegion = (TextView) findViewById(R.id.tv_region);
        updateCountry();
        logoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeDuration = System.currentTimeMillis() - lastClickTime;
                if (timeDuration > 500) {
                    tapStep = 0;
                    lastClickTime = 0;
                } else {
                    tapStep++;
                    //                    if (tapStep == 6) {
                    try {
                        Intent intent = new Intent(MainPageActivity.this, AVSettingsActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //                    }
                }
                lastClickTime = System.currentTimeMillis();
            }
        });
        if (ServerUtils.usePrivateCloud() && mTvCountry != null) {
            mTvCountry.setVisibility(View.GONE);
        }
        //        initSDK();        // imInit 多次。会造成im中extension添加多次
        mLiveView = findViewById(R.id.live_button);
        mLiveView.setOnClickListener(this);
        RongIMClient.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus connectionStatus) {
                // 点击"开始会议"按钮时，IM为非CONNECTED时会主动connect，如果还是失败，自动化测试case失败
                // 监听IM连接状态，做1次自动加入房间的尝试，开发者可以忽略此修改
                if (ConnectionStatus.CONNECTED.equals(connectionStatus)) {
                    if ((isDebug && joinRoomWhenConnectedInAutoTest) ||
                        joinRoomWhenReconnected) {
                        joinRoomWhenConnectedInAutoTest = false;
                        joinRoomWhenReconnected = false;
                        FinLog.d(TAG, "RongLog IM connected, Join Room");
                        connectToRoom();
                    }
                } else if (connectionStatus ==
                        RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT) {
                    EventBus.getDefault().post(new KickedOfflineEvent());
                    showDialog();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_settings:
                startSetting();
                break;
            case R.id.connect_button:
                FinLog.d(TAG, "connect button clicked,trying to join rtc room,roomId = " + roomEditText.getText().toString());
                if (Utils.isFastDoubleClick()) {
                    return;
                }
                if (null == roomEditText
                        || TextUtils.isEmpty(roomEditText.getText().toString().trim())) {
                    String toastMsg = getResources().getString(R.string.input_roomId);
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                final String phoneNumber = edit_room_phone.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber)) {
                    String toastMsg = getResources().getString(R.string.input_room_phoneNum);
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phoneNumber.length() < 1) {
                    String toastMsg = getResources().getString(R.string.input_room_phoneNum_error);
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                String storedPhone = SessionManager.getInstance().getString(UserUtils.PHONE);
                if (ServerUtils.usePrivateCloud() && !phoneNumber.equals(storedPhone)) {
                    // 私有环境下，手机号发生变化，需要重新取token
                    SessionManager.getInstance().remove(ServerUtils.TOKEN_PRIVATE_CLOUD_KEY);
                }

                SessionManager.getInstance().put(UserUtils.ROOMID_KEY, roomEditText.getText().toString().trim());
                SessionManager.getInstance().put(UserUtils.USERNAME_KEY, userNameEditText.getText().toString().trim());

                if (!TextUtils.isEmpty(storedPhone) && !storedPhone.equals(phoneNumber)) {
                    // 登录手机号发生变化时，删除之前保存的userID 和 token
                    SessionManager.getInstance().remove(UserUtils.USER_ID);
                    SessionManager.getInstance().remove(storedPhone);
                }
                SessionManager.getInstance().put(UserUtils.PHONE, phoneNumber);

                if (ServerUtils.usePrivateCloud()) {
                    if (ServerUtils.getTokenConnection()) {
                        LoadDialog.show(MainPageActivity.this);
                        FinLog.i(TAG, "-- getTokenConnection --");
                        connectForXQ(mIsLive);
                        return;
                    } else {
                        String toastMsg = getResources().getString(R.string.private_clouconfiguration_cannot_be_empty);
                        Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (!SessionManager.getInstance().contains(phoneNumber)
                        || TextUtils.isEmpty(SessionManager.getInstance().getString(UserUtils.USER_ID))
                        || TextUtils.isEmpty(SessionManager.getInstance().getString(UserUtils.APP_KEY))) {
                    startVerifyActivity(phoneNumber);
                } else {
                    long kickTime = SessionManager.getInstance().getLong("KICK_TIME");
                    String kickedRoomId = SessionManager.getInstance().getString("KICK_ROOM_ID");
                    String roomId = roomEditText.getText().toString();
                    if (kickTime > 0
                            && (System.currentTimeMillis() - kickTime < KICK_SILENT_PERIOD
                            && roomId.equals(kickedRoomId))) {
                        Toast.makeText(this, R.string.member_operate_kicked, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FinLog.d(TAG, "mStatus : " + mStatus);
                    if (mStatus == STATE_JOINING) return;
                    //TODO Server 端已将 APP_KEY 与 IM Navi 导航地址绑定，以后将不再支持单独设置 Navi
                    // 由于导航地址必须在 SDK 初始化之前设置，所以将初始化 SDK 逻辑放在获取导航地址之后再去初始化 SDK
                    if (mStatus == STATE_IDLE  && !initSDK()){
                        showToast("初始化SDK失败,请检查您的 AppKey 设置是否正确");
                        return;
                    }

                    mStatus = STATE_JOINING;
                    String status = RongIMClient.getInstance().getCurrentConnectionStatus().name();
                    FinLog.d(TAG, "CurrentConnectionStatus : " + status);
                    if (RongIMClient.getInstance().getCurrentConnectionStatus()
                            == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                        connectToRoom();
                        return;
                    }
                    if (isDebug) {
                        connectButton.setBackgroundColor(Color.RED);
                    }
                    final boolean autoTest = SessionManager.getInstance().getBoolean(IS_AUTO_TEST);
                    if (autoTest) {
                        joinRoomWhenConnectedInAutoTest = true;
                    }
                    String token = SessionManager.getInstance().getString(phoneNumber);
                    FinLog.v(TAG, "token ：" + token);
                    RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onDatabaseOpened(DatabaseOpenStatus code) {}

                        public void onTokenIncorrect() {
                            mStatus = STATE_FAILED;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String toastMsg = "Token验证失败，请重新获取！";
                                    Toast.makeText(MainPageActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                                    startVerifyActivity(phoneNumber);
                                }
                            });
                        }

                        @Override
                        public void onSuccess(String s) {
                            SessionManager.getInstance().put(UserUtils.PHONE, phoneNumber);
                            //自动化测试时，通过监听IM setConnectionStatusListener加入房间
                            if (!autoTest) {
                                connectToRoom();
                            } else if (!isFinishing() && !isDestroyed()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainPageActivity.this);
                                builder.setMessage("加入房间失败: IM 连接状态异常").show();
                            }
                        }

                        @Override
                        public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                            mStatus = STATE_FAILED;
                            FinLog.e(TAG, "RongIMClient connect errorCode :" + errorCode);
                            if (errorCode == ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT) {
                                onTokenIncorrect();
                            } else if (errorCode == ConnectionErrorCode.RC_CONNECTION_EXIST) {
                                //IM CONNECTED 或者 CONNECTING 都可能报这个错误码，也就是已经主动调用了一次 connect，
                                // 第二次调用时会报 RC_CONNECTION_EXIST，IM SDK 内部会做自动重连
                                joinRoomWhenReconnected = true;
                                LoadDialog.show(MainPageActivity.this);
                                Handler handler = new Handler();
                                //IM 可能长时间连接不成功，增加一个计时器，2s 不成功，取消IM连接成功时的加入房间逻辑
                                //原因:用户在当前页面，若突然自动加入房间，对用户不友好
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (joinRoomWhenReconnected && RongIMClient.getInstance().getCurrentConnectionStatus()
                                            != RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                                            Toast.makeText(MainPageActivity.this, R.string.im_connect_failed, Toast.LENGTH_SHORT).show();
                                            LoadDialog.dismiss(MainPageActivity.this);
                                        }
                                        joinRoomWhenReconnected = false;
                                    }
                                }, 2000);
                            }
                        }
                    });
                }
                break;
            case R.id.tv_country:
                Intent intent = new Intent(MainPageActivity.this, CountryListActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT_COUNTRY);
                break;
            case R.id.live_button:
                //                connectForXQ(true);
                final String phoneNumber2 = edit_room_phone.getText().toString().trim();
                if (!SessionManager.getInstance().contains(phoneNumber2)) {
                    startVerifyActivity(phoneNumber2);
                } else if (RongIMClient.getInstance().getCurrentConnectionStatus()
                        == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                    jumpLive();
                } else {
                    String token = SessionManager.getInstance().getString(phoneNumber2);
                    FinLog.v(TAG, "token 存在 ：" + token);
                    RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onDatabaseOpened(DatabaseOpenStatus code) {}

                        public void onTokenIncorrect() {
                            mStatus = STATE_FAILED;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String toastMsg = "Token验证失败，请重新获取！";
                                    Toast.makeText(MainPageActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                                    startVerifyActivity(phoneNumber2);
                                }
                            });
                        }

                        @Override
                        public void onSuccess(String s) {
                            SessionManager.getInstance().put(UserUtils.PHONE, phoneNumber2);
                            jumpLive();
                        }

                        @Override
                        public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                            mStatus = STATE_FAILED;
                            FinLog.e(TAG, "RongIMClient connect errorCode :" + errorCode);
                            if (errorCode == ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT) {
                                onTokenIncorrect();
                            }else if(errorCode == ConnectionErrorCode.RC_CONNECTION_EXIST){
                                connectToRoom();
                            }
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCamerCheck();
        updateRoomType();

        String mediaServerUrl = SessionManager.getInstance().getString("MediaUrl");
        // 设置media server地址，内部自动化测试使用，开发者一般不需要关心
        if (!TextUtils.isEmpty(mediaServerUrl) && !ServerUtils.usePrivateCloud()) {
            RCRTCEngine.getInstance().setMediaServerUrl(mediaServerUrl);
        }

        isDebug = SessionManager.getInstance().getBoolean(IS_AUTO_TEST);
        if (isDebug) {
            connectButton.setBackgroundColor(R.drawable.shape_corner_button_blue);
        }

        String phoneNum = SessionManager.getInstance().getString(UserUtils.PHONE);
        if (!TextUtils.isEmpty(phoneNum)) {
            edit_room_phone.setText(phoneNum);
        }
    }

    private void updateRoomType() {
        mIsLive = SessionManager.getInstance().getBoolean(SettingActivity.IS_LIVE, false);
        mLiveView.setVisibility(mIsLive ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            case R.id.room_cb_quiktest:
                SessionManager.getInstance().put(QUICK_TEST_KEY,isChecked);
                break;
            default:
                break;
        }
    }

    private void startSetting() {
        if (Utils.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(MainPageActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    /**
     * {\"liveUrl\":\"AB9tZHNydjAxLWtzYmoucm9uZ2Nsb3VkLm5ldDoxMDgwABIxMjAuOTIuMjIuMTQ0Ojc3ODgAIEFBWXhNREF3TURBQUJYQXlNak16QUFWd01qSXpNdz09ABRwMjIzM19Sb25nQ2xvdWRSVENfMQAAAACxVjwFABRwMjIzM19Sb25nQ2xvdWRSVENfMQAAAAAvvMzKABRwMjIzM19Sb25nQ2xvdWRSVENfMAAAAAB8CxTh\"}"}
     */
    private void connectToRoom() {
        mStatus = STATE_JOINING;
        joinRoomWhenConnectedInAutoTest = false;
        if (RongIMClient.getInstance().getCurrentConnectionStatus()
                == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            LoadDialog.show(this);
            initOrUpdateRTCEngine();
            final String roomId = roomEditText.getText().toString();

            RCRTCRoomType roomType;
            if (mIsLive) {
                roomType = isVideoMute ? RCRTCRoomType.LIVE_AUDIO : RCRTCRoomType.LIVE_AUDIO_VIDEO;
            } else {
                roomType = RCRTCRoomType.MEETING;
            }
            RCRTCEngine.getInstance().joinRoom(roomId, roomType, new IRCRTCResultDataCallback<RCRTCRoom>() {
                    @Override
                    public void onSuccess(RCRTCRoom room) {
                        LoadDialog.dismiss(MainPageActivity.this);
                        String toastMsg = getResources().getString(R.string.join_room_success);
                        Toast.makeText(MainPageActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        int userCount = room.getRemoteUsers().size();
                        if (userCount >= OBSERVER_MUST && !isObserver) {
                            AlertDialog dialog = new AlertDialog.Builder(MainPageActivity.this)
                                .setMessage(getResources().getString(R.string.join_room_observer_prompt))
                                .setNegativeButton(getResources().getString(R.string.rtc_dialog_cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            quitRoom();
                                            dialog.dismiss();
                                        }
                                    })
                                .setPositiveButton(getResources().getString(R.string.rtc_dialog_ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            canOnlyPublishAudio = false;
                                            startCallActivity(true, true);
                                        }
                                    }).create();
                            dialog.setCancelable(false);
                            dialog.show();
                        } else if (userCount >= VIDEOMUTE_MUST && !isVideoMute && !isObserver) {
                            AlertDialog dialog = new AlertDialog.Builder(MainPageActivity.this)
                                .setMessage(getResources().getString(R.string.join_room_audio_only_prompt))
                                .setNegativeButton(getResources().getString(R.string.rtc_dialog_cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            quitRoom();
                                            dialog.dismiss();
                                        }
                                    })
                                .setPositiveButton(getResources().getString(R.string.rtc_dialog_ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            canOnlyPublishAudio = true;
                                            startCallActivity(true, false);
                                        }
                                    }).create();
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            canOnlyPublishAudio = false;
                            startCallActivity(isVideoMute, isObserver);
                        }
                    }

                    @Override
                    public void onFailed(RTCErrorCode errorCode) {
                        mStatus = STATE_FAILED;
                        LoadDialog.dismiss(MainPageActivity.this);
                        String toastMsg;
                        if (errorCode == RTCErrorCode.ServerUserBlocked) {
                            toastMsg = getResources().getString(R.string.rtc_dialog_forbidden_by_server);
                        } else {
                            toastMsg = getResources().getString(R.string.join_room_failed);
                        }
                        Toast.makeText(MainPageActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            mStatus = STATE_FAILED;
            String toastMsg = getResources().getString(R.string.im_connect_failed);
            Toast.makeText(MainPageActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void startCallActivity(boolean muteVideo, boolean observer) {
        if (mStatus == STATE_JOINED) return;
        mStatus = STATE_JOINED;
        boolean quickTest=SessionManager.getInstance().getBoolean(QUICK_TEST_KEY);
        Intent intent = null;
        if(quickTest){//使用快速测试界面进行音视频
            intent = new Intent(this, TestActivity.class);
        }else{//使用会议界面进行音视频
            intent = new Intent(this, CallActivity.class);
        }
        // 加入房间之前 置为默认状态
        SessionManager.getInstance().put("VideoModeKey", "smooth");
        //
        intent.putExtra(CallActivity.EXTRA_ROOMID, roomEditText.getText().toString());
        intent.putExtra(CallActivity.EXTRA_USER_NAME, userNameEditText.getText().toString());
        intent.putExtra(CallActivity.EXTRA_CAMERA, muteVideo);
        intent.putExtra(CallActivity.EXTRA_OBSERVER, observer);
        intent.putExtra(CallActivity.EXTRA_AUTO_TEST, SessionManager.getInstance().getBoolean(IS_AUTO_TEST));
        intent.putExtra(CallActivity.EXTRA_WATER, SessionManager.getInstance().getBoolean(IS_WATER));
        intent.putExtra(CallActivity.EXTRA_MIRROR, SessionManager.getInstance().getBoolean(IS_MIRROR));
        intent.putExtra(CallActivity.EXTRA_IS_LIVE, mIsLive);
        RCRTCRoom room = RCRTCEngine.getInstance().getRoom();
        int joinMode = RoomInfoMessage.JoinMode.AUDIO_VIDEO;
        if (muteVideo) {
            joinMode = RoomInfoMessage.JoinMode.AUDIO;
        }
        if (observer) {
            joinMode = RoomInfoMessage.JoinMode.OBSERVER;
        }
        String userId = room.getLocalUser().getUserId();
        String userName = userNameEditText.getText().toString();
        int remoteUserCount = room.getRemoteUsers() != null ? room.getRemoteUsers().size() : 0;
        intent.putExtra(CallActivity.EXTRA_IS_MASTER, remoteUserCount == 0);
        RoomInfoMessage roomInfoMessage = new RoomInfoMessage(
            userId, userName, joinMode, System.currentTimeMillis(), remoteUserCount == 0);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("userName", userName);
            jsonObject.put("joinMode", joinMode);
            jsonObject.put("joinTime", System.currentTimeMillis());
            jsonObject.put("master", remoteUserCount == 0 ? 1 : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        room.setRoomAttributeValue(jsonObject.toString(), userId, roomInfoMessage, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {

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
        String json = SessionManager.getInstance().getString(UserUtils.COUNTRY);
        CountryInfo info;
        if (TextUtils.isEmpty(json)) {
            info = CountryInfo.createDefault();
        } else {
            try {
                info = new Gson().fromJson(json, CountryInfo.class);
            } catch (Exception e) {
                info = CountryInfo.createDefault();
            }
        }
        mTvCountry.setText(
                getString(R.string.select_country_hint)
                        + " "
                        + (Utils.isZhLanguage() ? info.zh : info.en));
        mTvRegion.setText("+" + info.region);
    }

    /** 使用小乔环境 */
    private void connectForXQ(final boolean isLive) {
        String token = SessionManager.getInstance().getString(ServerUtils.TOKEN_PRIVATE_CLOUD_KEY);
        FinLog.i(TAG, "private_Cloud_tokne : " + token);
        if (TextUtils.isEmpty(token)) {
            getTokenForXQ(isLive);
            return;
        }
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            public void onTokenIncorrect() {
                getTokenForXQ(isLive);
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus code) {}

            @Override
            public void onSuccess(String s) {
                FinLog.d(TAG, "IM  connectForXQ success ");
                if (isLive) {
                    jumpLive();
                } else {
                    connectToRoom();
                }
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                Toast.makeText(MainPageActivity.this, "连接IM失败，请稍后重试", Toast.LENGTH_SHORT).show();
                if (errorCode == ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT) {
                    onTokenIncorrect();
                }
            }
        });
    }

    private void jumpLive() {
        Intent liveIntent = new Intent(MainPageActivity.this, LiveListActivity.class);
        startActivity(liveIntent);
    }

    private void getTokenForXQ(final boolean isLive) {
        StringBuilder params = new StringBuilder();
        params.append("userId=")
                .append(
                        edit_room_phone.getText().toString().trim()
                                + (DeviceUtils.getDeviceId(Utils.getContext()).length() > 4
                                        ? DeviceUtils.getDeviceId(Utils.getContext()).substring(0, 4)
                                        : DeviceUtils.getDeviceId(Utils.getContext())))
                .append("&")
                .append("name=")
                .append(userNameEditText.getText().toString());
        long timestamp = System.currentTimeMillis();
        int nonce = (int) (Math.random() * 10000);
        String signature = "";
        try {
            signature = sha1(ServerUtils.APP_SECRET + nonce + timestamp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            LoadDialog.dismiss(MainPageActivity.this);
            postShowToast("错误 :" + e.getMessage());
        }
        FinLog.e(TAG, "API_SERVER: " + ServerUtils.API_SERVER +
            " ,  " + "signature :" + signature + " ,  params : " + params.toString());
        Request request = new Request.Builder()
            .url(ServerUtils.API_SERVER + "/user/getToken.json")
            .method(RequestMethod.POST)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Timestamp", String.valueOf(timestamp))
            .addHeader("Nonce", String.valueOf(nonce))
            .addHeader("Signature", signature)
            .addHeader("App-Key", ServerUtils.APP_KEY)
            .body(params.toString())
            .build();
        HttpClient.getDefault().request(request, new HttpClient.ResultCallback() {
            @Override
            public void onResponse(String result) {
                FinLog.e(TAG, "result :" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.optInt("code") == 200) {
                        SessionManager.getInstance()
                            .put(ServerUtils.TOKEN_PRIVATE_CLOUD_KEY, jsonObject.optString("token"));
                        connectForXQ(isLive);
                    } else {
                        postShowToast("code not 200, code=" + jsonObject.optInt("code"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    postShowToast("请求错误: " + e.getMessage());
                    FinLog.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(int errorCode) {
                LoadDialog.dismiss(MainPageActivity.this);
                postShowToast("请求Token失败 onFailure: " + errorCode);
                FinLog.e(TAG, "请求Token失败 errorCode:" + errorCode);
            }
        });
    }

    public static String sha1(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(data.getBytes());
        StringBuffer buf = new StringBuffer();
        byte[] bits = md.digest();
        for (int i = 0; i < bits.length; i++) {
            int a = bits[i];
            if (a < 0) a += 256;
            if (a < 16) buf.append("0");
            buf.append(Integer.toHexString(a));
        }
        return buf.toString();
    }

    private void getTokenNew() {
        JSONObject loginInfo = new JSONObject();
        try {
            loginInfo.put("id", DeviceUtils.getDeviceId(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
            .url(UserUtils.getUrl(ServerUtils.getAppServer(), UserUtils.URL_GET_TOKEN_NEW))
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
                                    public void onDatabaseOpened(DatabaseOpenStatus code) {}

                                    public void onTokenIncorrect() {
                                        LoadDialog.dismiss(MainPageActivity.this);
                                        Toast.makeText(MainPageActivity.this,
                                            "token获取失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onSuccess(String s) {
                                        FinLog.d(TAG, "IM  connectForXQ success in getTokenNew");
                                        LoadDialog.dismiss(MainPageActivity.this);
                                        //
                                        //
                                        // SharedPreferences.Editor
                                        // editor = sp.edit();
                                        //
                                        //
                                        // editor.putString("tokenNew", token);
                                        //
                                        //
                                        // editor.commit();
                                    }

                                    @Override
                                    public void onError(
                                        RongIMClient.ConnectionErrorCode errorCode) {
                                        LoadDialog.dismiss(MainPageActivity.this);
                                        Toast.makeText(MainPageActivity.this,
                                            "连接IM失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                        FinLog.d(TAG,
                                            "IM  connectForXQ = " + errorCode.getValue());
                                        if (errorCode
                                            == ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT) {
                                            onTokenIncorrect();
                                        }
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
        });
    }

    private void checkPermissions() {
        unGrantedPermissions = new ArrayList();
        for (String permission : MANDATORY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        if (unGrantedPermissions.size() == 0) { // 已经获得了所有权限，开始加入聊天室
            initSDK();
        } else { // 部分权限未获得，重新请求获取权限
            String[] array = new String[unGrantedPermissions.size()];
            ActivityCompat.requestPermissions(this, unGrantedPermissions.toArray(array), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        unGrantedPermissions.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                unGrantedPermissions.add(permissions[i]);
        }
        if (unGrantedPermissions.size() > 0) {
            for (String permission : unGrantedPermissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    Toast.makeText(
                                    this,
                                    getString(R.string.PermissionStr)
                                            + permission
                                            + getString(R.string.plsopenit),
                                    Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else ActivityCompat.requestPermissions(this, new String[] {permission}, 0);
            }
        } else {
            initSDK();
        }
    }

    private void quitRoom() {
        mStatus = STATE_INIT;
        canOnlyPublishAudio = false;
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {

            }
        });
    }

    private boolean initSDK() {
        if (TextUtils.isEmpty(SessionManager.getInstance().getString(APP_KEY)))
            return false;
        if (mStatus < STATE_INIT) {
            mStatus = STATE_INIT;
            /*
             * 如果是连接到私有云需要在此配置服务器地址
             * 如果是公有云则不需要调用此方法
             */

            RongIMClient.setServerInfo(SessionManager.getInstance().getString(APP_KEY), UserUtils.FILE_SERVER);
            RongIMClient.init(getApplication(), ServerUtils.getAppKey(), false);
            // Jenkins 配置 Meida Server 地址
            if (!TextUtils.isEmpty(UserUtils.MEDIA_SERVER)
                    && UserUtils.MEDIA_SERVER.startsWith("http")) {
                SessionManager.getInstance().put("QuickTestMeidaServerUrl",UserUtils.MEDIA_SERVER);
                RCRTCEngine.getInstance().setMediaServerUrl(UserUtils.MEDIA_SERVER);
            }
        }
        return true;
    }

    /**
     * 构造分辨率对应的BlinkVideoProfile对象
     *
     * @param resolutionStr
     * @return
     */
    private RCRTCVideoResolution selectiveResolution(String resolutionStr) {
        RCRTCVideoResolution profile = null;
        if (resolutionStr == null || resolutionStr.equals("")) {
            return RCRTCVideoResolution.RESOLUTION_480_640;
        }
        String[] resolutionArray = resolutionStr.split("x");
        profile = RCRTCVideoResolution.parseVideoResolution(
            Integer.parseInt(resolutionArray[0]), Integer.parseInt(resolutionArray[1]));
        return profile;
    }

    private RCRTCVideoFps selectiveFrame(String frameStr) {
        frameStr = TextUtils.isEmpty(frameStr) ? "15" : frameStr;
        return RCRTCVideoFps.parseVideoFps(Integer.parseInt(frameStr));
    }

    private void startVerifyActivity(String phoneNumber) {
        SessionManager.getInstance().put(UserUtils.PHONE, phoneNumber);
        SessionManager.getInstance().put(isVideoMute_key, isVideoMute);
        SessionManager.getInstance().put(isObserver_key, isObserver);
        Intent intent = new Intent(MainPageActivity.this, VerifyActivity.class);
        startActivityForResult(intent, REQUEST_CODE_VERIFY);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBusComplaint(String val) {
        if (val.equals("ServerConfigActivity")) {
            RongIMClient.getInstance().switchAppKey(ServerUtils.getAppKey());
            RongIMClient.setServerInfo(ServerUtils.getNavServer(), UserUtils.FILE_SERVER);
            RongIMClient.init(getApplication(), ServerUtils.getAppKey(), false);
            showToast(getString(R.string.update_configuration_successful));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onKickEvent(KickEvent event) {
        SessionManager.getInstance().put("KICK_TIME", System.currentTimeMillis());
        SessionManager.getInstance().put("KICK_ROOM_ID", event.getRoomId());
        final PromptDialog dialog =
                PromptDialog.newInstance(this, getString(R.string.member_operate_kicked));
        dialog.setPromptButtonClickedListener(new PromptDialog.OnPromptButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked() {}

            @Override
            public void onNegativeButtonClicked() {}
        });
        dialog.disableCancel();
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LoadDialog.dismiss(MainPageActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().removeStickyEvent("ConfigActivity");
        EventBus.getDefault().unregister(this);
        RongIMClient.setConnectionStatusListener(null);
    }

    private void showDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPageActivity.this)
                    .setTitle(getText(R.string.rtc_dialog_kicked_offline))
                    .setNeutralButton(getText(R.string.rtc_dialog_ok), null);
                builder.create().show();
            }
        });
    }
}
