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

import static android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION;
import static cn.rongcloud.rtc.SettingActivity.IS_AUTO_TEST;
import static cn.rongcloud.rtc.SettingActivity.IS_MIRROR;
import static cn.rongcloud.rtc.SettingActivity.IS_WATER;
import static cn.rongcloud.rtc.util.UserUtils.OBSERVER_MUST;
import static cn.rongcloud.rtc.util.UserUtils.VIDEOMUTE_MUST;
import static cn.rongcloud.rtc.util.UserUtils.isObserver_key;
import static cn.rongcloud.rtc.util.UserUtils.isVideoMute_key;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
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
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.callback.JoinRoomUICallBack;
import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.core.rongRTC.DevicesUtils;
import cn.rongcloud.rtc.device.AVSettingsActivity;
import cn.rongcloud.rtc.device.privatecloud.ServerUtils;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.entity.CountryInfo;
import cn.rongcloud.rtc.entity.KickEvent;
import cn.rongcloud.rtc.entity.KickedOfflineEvent;
import cn.rongcloud.rtc.media.RongMediaSignalClient;
import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.message.RoomInfoMessage;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.room.RongRTCRoomConfig;
import cn.rongcloud.rtc.stream.local.RongRTCCapture;
import cn.rongcloud.rtc.updateapk.UpDateApkHelper;
import cn.rongcloud.rtc.util.PromptDialog;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import com.google.gson.Gson;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.common.DeviceUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/** Handles the initial setup where the user selects which room to join. */
public class MainPageActivity extends RongRTCBaseActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    //    public static final boolean ISLIVE = false;

    private static final int CHECK_BUTTON_DELATY = 1100;
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1200;
    private static final int REQUEST_CODE_VERIFY = 1300;
    private static final int STATE_IDLE = 0; // 未初始化
    private static final int STATE_INIT = 1; // 已初始化
    private static final int STATE_JOINING = 2; // 加入中
    private static final int STATE_JOINED = 3; // 已加入
    private static final int STATE_FAILED = -1; // 加入失败
    private static final String TAG = "MainPageActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static final long KICK_SILENT_PERIOD = 5 * 60 * 1000L;
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
    private RongRTCConfig.Builder configBuilder;
    private boolean isDebug;
    private boolean joinRoomWhenConnectedInAutoTest;
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
    private int tapStep = 0;
    private long lastClickTime = 0;
    private View mLiveView;
    private boolean mIsLive = false;

    private boolean TokenIncorrectMark = true; // 记录私有云环境请求token失败后，仅重试一次

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

    private void initViews() {
        roomEditText = (EditText) findViewById(R.id.room_inputnumber);
        roomId = SessionManager.getInstance().getString(UserUtils.ROOMID_KEY);

        if (!TextUtils.isEmpty(roomId)) {
            roomEditText.setText(roomId);
        }

        //        if (!TextUtils.isEmpty(roomId)) {
        //            roomEditText.setEnabled(false);
        //            roomEditText.setText(roomId);
        //        }
        //        roomEditText.requestFocus();

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

        versionCodeView.setText(
                getResources().getString(R.string.blink_description_version)
                        + BuildConfig.VERSION_NAME
                        + (BuildConfig.DEBUG ? "_Debug" : ""));
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

        roomEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() > 0) {
                            connectButton.setBackgroundResource(
                                    R.drawable.shape_corner_button_blue);
                            connectButton.setClickable(true);
                        } else {
                            SessionManager.getInstance().remove(UserUtils.ROOMID_KEY);
                            connectButton.setBackgroundResource(
                                    R.drawable.shape_corner_button_blue_invalid);
                            connectButton.setClickable(false);
                        }
                    }
                });
        mTvRegion = (TextView) findViewById(R.id.tv_region);
        updateCountry();
        logoView.setOnClickListener(
                new View.OnClickListener() {
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
                                Intent intent =
                                        new Intent(MainPageActivity.this, AVSettingsActivity.class);
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
        RongIMClient.setConnectionStatusListener(
                new RongIMClient.ConnectionStatusListener() {
                    @Override
                    public void onChanged(ConnectionStatus connectionStatus) {
                        // 点击"开始会议"按钮时，IM为非CONNECTED时会主动connect，如果还是失败，自动化测试case失败
                        // 监听IM连接状态，做1次自动加入房间的尝试，开发者可以忽略此修改
                        if (ConnectionStatus.CONNECTED.equals(connectionStatus)) {
                            if (isDebug && joinRoomWhenConnectedInAutoTest) {
                                joinRoomWhenConnectedInAutoTest = false;
                                FinLog.d(TAG, "RongLog IM connected, Join Room");
                                connectToRoom();
                            }
                        } else if (connectionStatus
                                == RongIMClient.ConnectionStatusListener.ConnectionStatus
                                        .KICKED_OFFLINE_BY_OTHER_CLIENT) {
                            EventBus.getDefault().post(new KickedOfflineEvent());
                            showDialog();
                        }
                    }
                });
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
                if (null == roomEditText
                        || TextUtils.isEmpty(roomEditText.getText().toString().trim())) {
                    Toast.makeText(
                                    this,
                                    getResources().getString(R.string.input_roomId),
                                    Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                final String phoneNumber = edit_room_phone.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(
                                    this,
                                    getResources().getString(R.string.input_room_phoneNum),
                                    Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (phoneNumber.length() < 1) {
                    Toast.makeText(
                                    this,
                                    getResources().getString(R.string.input_room_phoneNum_error),
                                    Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                String storedPhone = SessionManager.getInstance().getString(UserUtils.PHONE);
                if (ServerUtils.usePrivateCloud() && !phoneNumber.equals(storedPhone)) {
                    // 私有环境下，手机号发生变化，需要重新取token
                    SessionManager.getInstance().remove(ServerUtils.TOKEN_PRIVATE_CLOUD_KEY);
                }

                SessionManager.getInstance()
                        .put(UserUtils.ROOMID_KEY, roomEditText.getText().toString().trim());
                SessionManager.getInstance()
                        .put(UserUtils.USERNAME_KEY, userNameEditText.getText().toString().trim());

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
                        Toast.makeText(
                                        this,
                                        getResources()
                                                .getString(
                                                        R.string
                                                                .private_clouconfiguration_cannot_be_empty),
                                        Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }

                if (!SessionManager.getInstance().contains(phoneNumber)
                        || TextUtils.isEmpty(
                                SessionManager.getInstance().getString(UserUtils.USER_ID))) {
                    startVerifyActivity(phoneNumber);
                } else {
                    long kickTime = SessionManager.getInstance().getLong("KICK_TIME");
                    String kickedRoomId = SessionManager.getInstance().getString("KICK_ROOM_ID");
                    String roomId = roomEditText.getText().toString();
                    if (kickTime > 0
                            && (System.currentTimeMillis() - kickTime < KICK_SILENT_PERIOD
                                    && roomId.equals(kickedRoomId))) {
                        Toast.makeText(this, R.string.member_operate_kicked, Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    if (mStatus == STATE_JOINING) return;
                    mStatus = STATE_JOINING;
                    FinLog.v(
                            TAG,
                            "CurrentConnectionStatu : "
                                    + RongIMClient.getInstance()
                                            .getCurrentConnectionStatus()
                                            .name());
                    if (RongIMClient.getInstance().getCurrentConnectionStatus()
                            == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                        connectToRoom();
                        return;
                    } else {
                        if (isDebug) {
                            connectButton.setBackgroundColor(Color.RED);
                        }
                    }
                    final boolean autoTest = SessionManager.getInstance().getBoolean(IS_AUTO_TEST);
                    if (autoTest) {
                        joinRoomWhenConnectedInAutoTest = true;
                    }
                    String token = SessionManager.getInstance().getString(phoneNumber);
                    FinLog.v(TAG, "token 存在 ：" + token);
                    RongIMClient.connect(
                            token,
                            new RongIMClient.ConnectCallback() {
                                @Override
                                public void onTokenIncorrect() {
                                    mStatus = STATE_FAILED;
                                    runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                                    MainPageActivity.this,
                                                                    "Token验证失败，请重新获取！",
                                                                    Toast.LENGTH_SHORT)
                                                            .show();
                                                    startVerifyActivity(phoneNumber);
                                                }
                                            });
                                }

                                @Override
                                public void onSuccess(String s) {
                                    SessionManager.getInstance().put(UserUtils.PHONE, phoneNumber);
                                    if (!autoTest) {
                                        connectToRoom();
                                    }
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    mStatus = STATE_FAILED;
                                    FinLog.e(
                                            TAG,
                                            "RongIMClient connectForXQ errorcode :" + errorCode);
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
                    RongIMClient.connect(
                            token,
                            new RongIMClient.ConnectCallback() {
                                @Override
                                public void onTokenIncorrect() {
                                    mStatus = STATE_FAILED;
                                    runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                                    MainPageActivity.this,
                                                                    "Token验证失败，请重新获取！",
                                                                    Toast.LENGTH_SHORT)
                                                            .show();
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
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    mStatus = STATE_FAILED;
                                    FinLog.e(
                                            TAG,
                                            "RongIMClient connectForXQ errorcode :" + errorCode);
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
        updateConfiguration();
        updateRoomType();

        String mediaServerUrl = SessionManager.getInstance().getString("MediaUrl");
        // 设置media server地址，内部自动化测试使用，开发者一般不需要关心
        if (!TextUtils.isEmpty(mediaServerUrl) && !ServerUtils.usePrivateCloud()) {
            RongRTCEngine.getInstance().setMediaServerUrl(mediaServerUrl);
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

    private void updateConfiguration() {

        if (!SessionManager.getInstance()
                .getBoolean(getResources().getString(R.string.key_use_av_setting), false)) {
            return; // 没有进入av setting 界面，不使用本地配置。
        }

        //如果开启了镜像翻转 VideoFrame，则应关闭镜像预览功能，否则会造成2次翻转效果
        RongRTCCapture.getInstance()
            .setPreviewMirror(!SessionManager.getInstance().getBoolean(IS_MIRROR));

        configBuilder = new RongRTCConfig.Builder();
        boolean isEncoderHardMode =
                SessionManager.getInstance().getBoolean(Consts.SP_ENCODER_TYPE_KEY, true);
        String hardEncoderName = SessionManager.getInstance().getString(Consts.SP_ENCODER_NAME_KEY);
        int encoderColorValue =
                SessionManager.getInstance().getInt(Consts.SP_ENCODER_COLOR_FORMAT_VAL_KEY, 0);
        boolean encoderLevel_key =
                SessionManager.getInstance().getBoolean(Consts.SP_ENCODER_LEVEL_KEY, true);
        configBuilder
                .enableHardWareEncode(isEncoderHardMode)
                .setHardWareEncodeColor(encoderColorValue)
                .enableHardWareEncodeHighProfile(encoderLevel_key);
        DevicesUtils.setEnCodeColor(encoderColorValue);
        DevicesUtils.setHighProfile(encoderLevel_key);

        String encoder_bit_rate_mode_vbr =
                Utils.getContext().getResources().getString(R.string.encoder_bit_rate_mode_vbr);
        String encoder_bit_rate_mode_cq =
                Utils.getContext().getResources().getString(R.string.encoder_bit_rate_mode_cq);
        String encoder_bit_rate_mode_cbr =
                Utils.getContext().getResources().getString(R.string.encoder_bit_rate_mode_cbr);

        String encoderBitRateMode =
                SessionManager.getInstance()
                        .getString(
                                Consts.SP_ENCODER_BIT_RATE_MODE,
                                getResources().getString(R.string.def_encoder_bitrate_mode));
        if (TextUtils.equals(encoderBitRateMode, encoder_bit_rate_mode_vbr)) {
            configBuilder.setHardWareEncodeBitrateMode(RongRTCConfig.VideoBitrateMode.VBR);
            DevicesUtils.setEnCodeBitRateMode(RongRTCConfig.VideoBitrateMode.VBR);
        } else if (TextUtils.equals(encoderBitRateMode, encoder_bit_rate_mode_cbr)) {
            configBuilder.setHardWareEncodeBitrateMode(RongRTCConfig.VideoBitrateMode.CBR);
            DevicesUtils.setEnCodeBitRateMode(RongRTCConfig.VideoBitrateMode.CBR);
        } else if (TextUtils.equals(encoderBitRateMode, encoder_bit_rate_mode_cq)) {
            configBuilder.setHardWareEncodeBitrateMode(RongRTCConfig.VideoBitrateMode.CQ);
            DevicesUtils.setEnCodeBitRateMode(RongRTCConfig.VideoBitrateMode.CQ);
        }

        boolean isDecoderHardMode =
                SessionManager.getInstance().getBoolean(Consts.SP_DECODER_TYPE_KEY, true);
        String hardDecoderName = SessionManager.getInstance().getString(Consts.SP_DECODER_NAME_KEY);
        int decoderColorSpace =
                SessionManager.getInstance().getInt(Consts.SP_DECODER_COLOR_FORMAT_VAL_KEY, 0);
        configBuilder
                .enableHardWareDecode(isDecoderHardMode)
                .setHardWareDecodeColor(decoderColorSpace);
        DevicesUtils.setDeCodeColor(decoderColorSpace);

        // audio capture config
        int audioSource =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_SOURCE, VOICE_COMMUNICATION);
        int audioSampleRate =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_SAMPLE_RATE, 48000);
        boolean audioSampleStereo =
                SessionManager.getInstance()
                        .getBoolean(Consts.SP_AUDIO_CHANNEL_STEREO_ENABLE, false);
        int audioBitRate =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_TRANSPORT_BIT_RATE, 30);
        configBuilder
                .setAudioSource(audioSource)
                .setAudioSampleRate(audioSampleRate)
                .enableStereo(audioSampleStereo)
                .setAudioBitrate(audioBitRate);

        // audio agc config
        boolean audioAgcEnable =
                SessionManager.getInstance().getBoolean(Consts.SP_AUDIO_AGC_CONTROL_ENABLE, true);
        boolean audioAgcLimiter =
                SessionManager.getInstance().getBoolean(Consts.SP_AUDIO_AGC_LIMITER_ENABLE, true);
        int audioAgcTargetDbov =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_AGC_TARGET_DBOV, -3);
        int audioAgcCompression =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_AGC_COMPRESSION, 9);
        boolean audioPreAmplifierEnable =
                SessionManager.getInstance().getBoolean(Consts.SP_AUDIO_PRE_AMPLIFIER_ENABLE, true);
        float audioPreAmplifierLevel =
                SessionManager.getInstance().getFloat(Consts.SP_AUDIO_PRE_AMPLIFIER_LEVEL, 1.0f);
        configBuilder
                .enableAGCControl(audioAgcEnable)
                .enableAGCLimiter(audioAgcLimiter)
                .setAGCTargetdbov(audioAgcTargetDbov)
                .setAGCCompression(audioAgcCompression)
                .enablePreAmplifier(audioPreAmplifierEnable)
                .setPreAmplifierLevel(audioPreAmplifierLevel);

        // audio noise suppression
        int audioNoiseSuppressionMode =
                SessionManager.getInstance()
                        .getInt(
                                Consts.SP_AUDIO_NOISE_SUPPRESSION_MODE,
                                getResources().getInteger(R.integer.def_ns_model));
        int audioNoiseSuppressionLevel =
                SessionManager.getInstance()
                        .getInt(
                                Consts.SP_AUDIO_NOISE_SUPPRESSION_LEVEL,
                                getResources().getInteger(R.integer.def_ns_level));
        boolean audioNoiseHighPassFilter =
                SessionManager.getInstance()
                        .getBoolean(Consts.SP_AUDIO_NOISE_HIGH_PASS_FILTER, true);
        configBuilder
                .setNoiseSuppression(RongRTCConfig.NSMode.parseValue(audioNoiseSuppressionMode))
                .setNoiseSuppressionLevel(
                        RongRTCConfig.NSLevel.parseValue(audioNoiseSuppressionLevel))
                .enableHighPassFilter(audioNoiseHighPassFilter);

        // audio echo cancel
        int audioEchoCancelMode =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_ECHO_CANCEL_MODE, 0);
        boolean audioEchoCancelFilterEnable =
                SessionManager.getInstance()
                        .getBoolean(
                                Consts.SP_AUDIO_ECHO_CANCEL_FILTER_ENABLE,
                                getResources().getBoolean(R.bool.def_echo_filter_enable));
        configBuilder
                .setEchoCancel(RongRTCConfig.AECMode.parseValue(audioEchoCancelMode))
                .enableEchoFilter(audioEchoCancelFilterEnable);

        // Set max and min bitrate
        String minBitRate = SessionManager.getInstance().getString(SettingActivity.BIT_RATE_MIN);
        if (!TextUtils.isEmpty(minBitRate) && minBitRate.length() > 4) {
            int bitRateIntvalue = Integer.valueOf(minBitRate.substring(0, minBitRate.length() - 4));
            FinLog.v(TAG, "BIT_RATE_MIN=" + bitRateIntvalue);
            configBuilder.setMinRate(bitRateIntvalue);
        }
        String maxBitRate = SessionManager.getInstance().getString(SettingActivity.BIT_RATE_MAX);
        if (!TextUtils.isEmpty(maxBitRate) && maxBitRate.length() > 4) {
            int bitRateIntvalue = Integer.valueOf(maxBitRate.substring(0, maxBitRate.length() - 4));
            FinLog.v(TAG, "BIT_RATE_MAX=" + bitRateIntvalue);
            configBuilder.setMaxRate(bitRateIntvalue);
        }
        // set resolution
        String resolution = SessionManager.getInstance().getString(SettingActivity.RESOLUTION);
        String fps = SessionManager.getInstance().getString(SettingActivity.FPS);
        RongRTCConfig.RongRTCVideoResolution videoProfile = selectiveResolution(resolution);
        RongRTCConfig.RongRTCVideoFps videoFrame = selectiveFrame((fps));
        configBuilder.setVideoResolution(videoProfile).setVideoFps(videoFrame);
        String codec = SessionManager.getInstance().getString(SettingActivity.CODECS);
        //        if (!TextUtils.isEmpty(codec)) {
        //            if ("VP8".equals(codec)) {
        //                configBuilder.videoCodecs(RongRTCConfig.RongRTCVideoCodecs.VP8);
        //            } else {
        //                configBuilder.videoCodecs(RongRTCConfig.RongRTCVideoCodecs.H264);
        //            }
        //        }
        boolean enableTinyStream =
                SessionManager.getInstance().getIsSupportTiny(SettingActivity.IS_STREAM_TINY);
        configBuilder.enableTinyStream(enableTinyStream);
        //
        // configBuilder.enableStereo(SessionManager.getInstance().getBoolean(SettingActivity.IS_STEREO));
        //
        // configBuilder.setAudioBitrate(SessionManager.getInstance().getString(SettingActivity.AUDIO_BITRATE));
        //
        // configBuilder.setAudioAgcLimiter(SessionManager.getInstance().getString(SettingActivity.AUDIO_AGC_LIMITER));
        //
        // configBuilder.setAudioAgcTargetDBOV(SessionManager.getInstance().getString(SettingActivity.AUDIO_AGC_TARGET_DBOV));
        //
        // configBuilder.setAudioAgcCompression(SessionManager.getInstance().getString(SettingActivity.AUDIO_AGC_COMPRESSION));
        //
        // configBuilder.setNoiseSuppression(SessionManager.getInstance().getString(SettingActivity.AUDIO__NOISE_SUPPRESSION));
        //
        // configBuilder.setNoiseSuppressionLevel(SessionManager.getInstance().getString(SettingActivity.AUDIO__NOISE_SUPPRESSION_LEVEL));
        //
        // configBuilder.setEchoCancelMode(SessionManager.getInstance().getString(SettingActivity.AUDIO_ECHO_CANCEL));
        //
        // configBuilder.enablePreAmplifier(SessionManager.getInstance().getString(SettingActivity.AUDIO_PREAMPLIFIER));
        //
        // configBuilder.setPreAmplifierLevel(SessionManager.getInstance().getString(SettingActivity.AUDIO_PREAMPLIFIER_LEVEL));

        // 在device设置界面设置的屏幕旋转角度 在此在设置一次，防止被覆盖
        int cameraDisplayOrientation =
                SessionManager.getInstance()
                        .getInt(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY, 0);
        int frameOrientation =
                SessionManager.getInstance().getInt(Consts.CAPTURE_FRAME_ORIENTATION_KEY, -1);
        boolean acquisitionMode =
                SessionManager.getInstance().getBoolean(Consts.ACQUISITION_MODE_KEY, true);

        configBuilder.setCameraDisplayOrientation(cameraDisplayOrientation);
        configBuilder.setFrameOrientation(frameOrientation);
        configBuilder.enableVideoTexture(acquisitionMode);

        boolean isAudioMode =
                SessionManager.getInstance()
                        .getBoolean(
                                SettingActivity.IS_AUDIO_MUSIC,
                                getResources().getBoolean(R.bool.def_audio_music_mode));
        if (isAudioMode) {
            configBuilder.buildMusicMode();
            RongRTCCapture.getInstance().changeAudioScenario(RongRTCConfig.AudioScenario.MUSIC);
        } else {
            configBuilder.buildDefaultMode();
            RongRTCCapture.getInstance().changeAudioScenario(RongRTCConfig.AudioScenario.DEFAULT);
        }

        FinLog.v(TAG, "enableTinyStream: " + enableTinyStream);
        FinLog.v(
                TAG,
                "audio option stereo: "
                        + SessionManager.getInstance().getBoolean(SettingActivity.IS_STEREO));
        FinLog.v(
                TAG,
                "audio option audioProcess: "
                        + SessionManager.getInstance()
                                .getBoolean(SettingActivity.IS_AUDIO_PROCESS));
        FinLog.v(
                TAG,
                "audio option audio bitrate: "
                        + SessionManager.getInstance().getString(SettingActivity.AUDIO_BITRATE));
        FinLog.v(
                TAG,
                "audio option audio agc limiter: "
                        + SessionManager.getInstance()
                                .getString(SettingActivity.AUDIO_AGC_LIMITER));
        FinLog.v(
                TAG,
                "audio option audio agc target dbov: "
                        + SessionManager.getInstance()
                                .getString(SettingActivity.AUDIO_AGC_TARGET_DBOV));
        FinLog.v(
                TAG,
                "audio option audio agc compression: "
                        + SessionManager.getInstance()
                                .getString(SettingActivity.AUDIO_AGC_COMPRESSION));
        FinLog.v(
                TAG,
                "audio option audio noise suppression: "
                        + SessionManager.getInstance()
                                .getString(SettingActivity.AUDIO__NOISE_SUPPRESSION));
        FinLog.v(
                TAG,
                "audio option audio noise suppression level: "
                        + SessionManager.getInstance()
                                .getString(SettingActivity.AUDIO__NOISE_SUPPRESSION_LEVEL));
        FinLog.v(
                TAG,
                "audio option audio echo cancel: "
                        + SessionManager.getInstance()
                                .getString(SettingActivity.AUDIO_ECHO_CANCEL));
        FinLog.v(
                TAG,
                "audio option audio preAmplifier : "
                        + SessionManager.getInstance()
                                .getString(SettingActivity.AUDIO_PREAMPLIFIER));
        FinLog.v(
                TAG,
                "audio option audio preAmplifier level: "
                        + SessionManager.getInstance()
                                .getString(SettingActivity.AUDIO_PREAMPLIFIER_LEVEL));

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
        updateConfiguration();
        joinRoomWhenConnectedInAutoTest = false;
        if (RongIMClient.getInstance().getCurrentConnectionStatus()
                == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            LoadDialog.show(this);
            final String roomId = roomEditText.getText().toString();
            RongRTCRoomConfig config =
                    new RongRTCRoomConfig.Builder()
                            .setRoomType(
                                    mIsLive
                                            ? RongRTCRoomConfig.RoomType.LIVE
                                            : RongRTCRoomConfig.RoomType.NORMAL)
                            .setLiveType(
                                    isVideoMute
                                            ? RongRTCRoomConfig.LiveType.AUDIO
                                            : RongRTCRoomConfig.LiveType.AUDIO_VIDEO) // 直播模式为音视频模式
                            .build();
            RongRTCEngine.getInstance()
                    .joinRoom(
                            roomId,
                            config,
                            new JoinRoomUICallBack() {
                                @Override
                                protected void onUiSuccess(RongRTCRoom rtcRoom) {
                                    LoadDialog.dismiss(MainPageActivity.this);
                                    Toast.makeText(
                                                    MainPageActivity.this,
                                                    getResources()
                                                            .getString(R.string.join_room_success),
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    int userCount = rtcRoom.getRemoteUsers().size();
                                    if (userCount >= OBSERVER_MUST && !isObserver) {
                                        AlertDialog dialog =
                                                new AlertDialog.Builder(MainPageActivity.this)
                                                        .setMessage(
                                                                getResources()
                                                                        .getString(
                                                                                R.string
                                                                                        .join_room_observer_prompt))
                                                        .setNegativeButton(
                                                                getResources()
                                                                        .getString(
                                                                                R.string
                                                                                        .rtc_dialog_cancel),
                                                                new DialogInterface
                                                                        .OnClickListener() {
                                                                    @Override
                                                                    public void onClick(
                                                                            DialogInterface dialog,
                                                                            int which) {
                                                                        quitRoom(roomId);
                                                                        dialog.dismiss();
                                                                    }
                                                                })
                                                        .setPositiveButton(
                                                                getResources()
                                                                        .getString(
                                                                                R.string
                                                                                        .rtc_dialog_ok),
                                                                new DialogInterface
                                                                        .OnClickListener() {
                                                                    @Override
                                                                    public void onClick(
                                                                            DialogInterface dialog,
                                                                            int which) {
                                                                        dialog.dismiss();
                                                                        canOnlyPublishAudio = false;
                                                                        startCallActivity(
                                                                                true, true);
                                                                    }
                                                                })
                                                        .create();
                                        dialog.setCancelable(false);
                                        dialog.show();
                                    } else if (userCount >= VIDEOMUTE_MUST
                                            && !isVideoMute
                                            && !isObserver) {
                                        AlertDialog dialog =
                                                new AlertDialog.Builder(MainPageActivity.this)
                                                        .setMessage(
                                                                getResources()
                                                                        .getString(
                                                                                R.string
                                                                                        .join_room_audio_only_prompt))
                                                        .setNegativeButton(
                                                                getResources()
                                                                        .getString(
                                                                                R.string
                                                                                        .rtc_dialog_cancel),
                                                                new DialogInterface
                                                                        .OnClickListener() {
                                                                    @Override
                                                                    public void onClick(
                                                                            DialogInterface dialog,
                                                                            int which) {
                                                                        quitRoom(roomId);
                                                                        dialog.dismiss();
                                                                    }
                                                                })
                                                        .setPositiveButton(
                                                                getResources()
                                                                        .getString(
                                                                                R.string
                                                                                        .rtc_dialog_ok),
                                                                new DialogInterface
                                                                        .OnClickListener() {
                                                                    @Override
                                                                    public void onClick(
                                                                            DialogInterface dialog,
                                                                            int which) {
                                                                        dialog.dismiss();
                                                                        canOnlyPublishAudio = true;
                                                                        startCallActivity(
                                                                                true, false);
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
                                    if (errorCode == RTCErrorCode.ServerUserBlocked) {
                                        Toast.makeText(
                                                        MainPageActivity.this,
                                                        getResources()
                                                                .getString(
                                                                        R.string
                                                                                .rtc_dialog_forbidden_by_server),
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Toast.makeText(
                                                        MainPageActivity.this,
                                                        getResources()
                                                                .getString(
                                                                        R.string.join_room_failed),
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            });
        } else {
            mStatus = STATE_FAILED;
            Toast.makeText(
                            MainPageActivity.this,
                            getResources().getString(R.string.im_connect_failed),
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void startCallActivity(boolean muteVideo, boolean observer) {
        if (mStatus == STATE_JOINED) return;
        mStatus = STATE_JOINED;
        Intent intent = new Intent(this, CallActivity.class);
        // 加入房间之前 置为默认状态
        SessionManager.getInstance().put("VideoModeKey", "smooth");
        //
        intent.putExtra(CallActivity.EXTRA_ROOMID, roomEditText.getText().toString());
        intent.putExtra(CallActivity.EXTRA_USER_NAME, userNameEditText.getText().toString());
        intent.putExtra(CallActivity.EXTRA_CAMERA, muteVideo);
        intent.putExtra(CallActivity.EXTRA_OBSERVER, observer);
        intent.putExtra(
                CallActivity.EXTRA_AUTO_TEST,
                SessionManager.getInstance().getBoolean(IS_AUTO_TEST));
        intent.putExtra(
                CallActivity.EXTRA_WATER, SessionManager.getInstance().getBoolean(IS_WATER));
        intent.putExtra(
            CallActivity.EXTRA_MIRROR, SessionManager.getInstance().getBoolean(IS_MIRROR));
        intent.putExtra(CallActivity.EXTRA_IS_LIVE, mIsLive);
        RongRTCRoom rongRTCRoom = CenterManager.getInstance().getRongRTCRoom();
        int joinMode = RoomInfoMessage.JoinMode.AUDIO_VIDEO;
        if (muteVideo) {
            joinMode = RoomInfoMessage.JoinMode.AUDIO;
        }
        if (observer) {
            joinMode = RoomInfoMessage.JoinMode.OBSERVER;
        }
        String userId = rongRTCRoom.getLocalUser().getUserId();
        String userName = userNameEditText.getText().toString();
        int remoteUserCount = 0;
        if (rongRTCRoom.getRemoteUsers() != null) {
            remoteUserCount = rongRTCRoom.getRemoteUsers().size();
        }
        intent.putExtra(CallActivity.EXTRA_IS_MASTER, remoteUserCount == 0);
        RoomInfoMessage roomInfoMessage =
                new RoomInfoMessage(
                        userId,
                        userName,
                        joinMode,
                        System.currentTimeMillis(),
                        remoteUserCount == 0);
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
        rongRTCRoom.setRoomAttributeValue(
                jsonObject.toString(),
                userId,
                roomInfoMessage,
                new RongRTCResultUICallBack() {
                    @Override
                    public void onUiSuccess() {}

                    @Override
                    public void onUiFailed(RTCErrorCode errorCode) {}
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
        if (!TextUtils.isEmpty(token)) {
            RongIMClient.connect(
                    token,
                    new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {
                            getTokenForXQ(isLive);
                        }

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
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            Toast.makeText(
                                            MainPageActivity.this,
                                            "连接IM失败，请稍后重试",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        } else {
            getTokenForXQ(isLive);
        }
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
                                        ? DeviceUtils.getDeviceId(Utils.getContext())
                                                .substring(0, 4)
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
        FinLog.e(
                TAG,
                "API_SERVER: "
                        + ServerUtils.API_SERVER
                        + " ,  "
                        + "signature :"
                        + signature
                        + " ,  params : "
                        + params.toString());
        Request request =
                new Request.Builder()
                        .url(ServerUtils.API_SERVER + "/user/getToken.json")
                        .method(RequestMethod.POST)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("Timestamp", String.valueOf(timestamp))
                        .addHeader("Nonce", String.valueOf(nonce))
                        .addHeader("Signature", signature)
                        .addHeader("App-Key", ServerUtils.APP_KEY)
                        .body(params.toString())
                        .build();
        HttpClient.getDefault()
                .request(
                        request,
                        new HttpClient.ResultCallback() {

                            @Override
                            public void onResponse(String result) {
                                FinLog.e(TAG, "result :" + result);
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    if (jsonObject.optInt("code") == 200) {
                                        SessionManager.getInstance()
                                                .put(
                                                        ServerUtils.TOKEN_PRIVATE_CLOUD_KEY,
                                                        jsonObject.optString("token"));
                                        connectForXQ(isLive);
                                    } else {
                                        postShowToast(
                                                "code not 200, code=" + jsonObject.optInt("code"));
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

                            @Override
                            public void onError(IOException exception) {
                                LoadDialog.dismiss(MainPageActivity.this);
                                exception.printStackTrace();
                                postShowToast("请求Token失败. exception: " + exception.getMessage());
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
        Request request =
                new Request.Builder()
                        .url(
                                UserUtils.getUrl(
                                        ServerUtils.getAppServer(), UserUtils.URL_GET_TOKEN_NEW))
                        .method(RequestMethod.POST)
                        .body(loginInfo.toString())
                        .build();
        HttpClient.getDefault()
                .request(
                        request,
                        new HttpClient.ResultCallback() {
                            @Override
                            public void onResponse(String result) {
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    int code = jsonObject.getInt("code");
                                    if (code == 200) {
                                        jsonObject = jsonObject.getJSONObject("result");
                                        final String token = jsonObject.getString("token");
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        RongIMClient.connect(
                                                                token,
                                                                new RongIMClient.ConnectCallback() {
                                                                    @Override
                                                                    public void onTokenIncorrect() {
                                                                        LoadDialog.dismiss(
                                                                                MainPageActivity
                                                                                        .this);
                                                                        Toast.makeText(
                                                                                        MainPageActivity
                                                                                                .this,
                                                                                        "token获取失败，请稍后重试",
                                                                                        Toast
                                                                                                .LENGTH_SHORT)
                                                                                .show();
                                                                    }

                                                                    @Override
                                                                    public void onSuccess(
                                                                            String s) {
                                                                        FinLog.d(
                                                                                TAG,
                                                                                "IM  connectForXQ success in getTokenNew");
                                                                        LoadDialog.dismiss(
                                                                                MainPageActivity
                                                                                        .this);
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
                                                                            RongIMClient.ErrorCode
                                                                                    errorCode) {
                                                                        LoadDialog.dismiss(
                                                                                MainPageActivity
                                                                                        .this);
                                                                        Toast.makeText(
                                                                                        MainPageActivity
                                                                                                .this,
                                                                                        "连接IM失败，请稍后重试",
                                                                                        Toast
                                                                                                .LENGTH_SHORT)
                                                                                .show();
                                                                        FinLog.d(
                                                                                TAG,
                                                                                "IM  connectForXQ = "
                                                                                        + errorCode
                                                                                                .getValue());
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

    private void quitRoom(String roomId) {
        mStatus = STATE_INIT;
        canOnlyPublishAudio = false;
        RongRTCEngine.getInstance()
                .quitRoom(
                        roomId,
                        new RongRTCResultUICallBack() {

                            @Override
                            public void onUiSuccess() {}

                            @Override
                            public void onUiFailed(RTCErrorCode errorCode) {}
                        });
    }

    private void initSDK() {
        if (mStatus < STATE_INIT) {
            mStatus = STATE_INIT;
            /*
             * 如果是连接到私有云需要在此配置服务器地址
             * 如果是公有云则不需要调用此方法
             */
            RongIMClient.setServerInfo(ServerUtils.getNavServer(), UserUtils.FILE_SERVER);
            RongIMClient.init(getApplication(), ServerUtils.getAppKey(), false);
            /*
             * 设置建立 Https 连接时，是否使用自签证书。
             * 公有云用户无需调用此方法，私有云用户使用自签证书时调用此方法设置
             */
            /*configBuilder = new RongRTCConfig.Builder();
            configBuilder.enableHttpsSelfCertificate(true);*/
            // Jenkins 配置 Meida Server 地址
            /*if (!TextUtils.isEmpty(UserUtils.MEDIA_SERVER)
                    && UserUtils.MEDIA_SERVER.startsWith("http")) {
                RongMediaSignalClient.setMediaServerUrl(UserUtils.MEDIA_SERVER);
            }*/
        }
    }

    /**
     * 构造分辨率对应的BlinkVideoProfile对象
     *
     * @param resolutionStr
     * @return
     */
    private RongRTCConfig.RongRTCVideoResolution selectiveResolution(String resolutionStr) {
        RongRTCConfig.RongRTCVideoResolution profile = null;

        if (resolutionStr == null || resolutionStr.equals("")) {
            return RongRTCConfig.RongRTCVideoResolution.RESOLUTION_480_640;
        }

        String[] resolutionArray = resolutionStr.split("x");

        profile =
                RongRTCConfig.RongRTCVideoResolution.parseVideoResolution(
                        Integer.valueOf(resolutionArray[0]), Integer.valueOf(resolutionArray[1]));
        return profile;
    }

    private RongRTCConfig.RongRTCVideoFps selectiveFrame(String frameStr) {
        RongRTCConfig.RongRTCVideoFps profile = null;
        if (TextUtils.isEmpty(frameStr)) {
            frameStr = "15";
        }

        profile = RongRTCConfig.RongRTCVideoFps.parseVideoFps(Integer.valueOf(frameStr));
        return profile;
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
        dialog.setPromptButtonClickedListener(
                new PromptDialog.OnPromptButtonClickedListener() {
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
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(MainPageActivity.this)
                                        .setTitle(getText(R.string.rtc_dialog_kicked_offline))
                                        .setNeutralButton(
                                                getText(R.string.rtc_dialog_ok),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialogInterface,
                                                            int i) {}
                                                });
                        builder.create().show();
                    }
                });
    }
}
