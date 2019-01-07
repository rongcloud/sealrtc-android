package cn.rongcloud.rtc;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import cn.rongcloud.rtc.base.RongRTCBaseActivity;

import cn.rongcloud.rtc.engine.broadcast.BluetoothUtil;
import cn.rongcloud.rtc.util.AppRTCUtils;
import cn.rongcloud.rtc.util.RongRTCPopupWindow;
import cn.rongcloud.rtc.engine.binstack.http.RongRTCHttpClient;
import cn.rongcloud.rtc.engine.binstack.http.QuicHttpCallback;
import cn.rongcloud.rtc.engine.binstack.json.module.StatusBean;
import cn.rongcloud.rtc.engine.binstack.json.module.StatusReport;
import cn.rongcloud.rtc.engine.binstack.json.module.StatusReportParser;
import cn.rongcloud.rtc.engine.binstack.util.RongRTCSessionManager;
import cn.rongcloud.rtc.engine.binstack.util.FinLog;
import cn.rongcloud.rtc.engine.context.RongRTCContext;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.entity.ResolutionInfo;
import cn.rongcloud.rtc.util.AssetsFilesUtil;
import cn.rongcloud.rtc.util.RongRTCTalkTypeUtil;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import static cn.rongcloud.rtc.SettingActivity.IS_GPUIMAGEFILTER;
import static cn.rongcloud.rtc.SettingActivity.IS_SRTP;
import static cn.rongcloud.rtc.SettingActivity.IS_STREAM_TINY;

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends RongRTCBaseActivity {

    private static final String appid_ = "x4vkb1qpxfrzk";//x4vkb1qpxfrzk
    private static String APPID = appid_;

    private AlertDialog ConfirmDialog = null;
    private String deviceId = "";

    public static final String EXTRA_ROOMID = "blinktalk.io.ROOMID";
    public static final String EXTRA_USER_NAME = "blinktalk.io.USER_NAME";
    public static final String EXTRA_SERVER_URL = "blinktalk.io.EXTRA_SERVER_URL";
    public static final String EXTRA_CAMERA = "blinktalk.io.EXTRA_CAMERA";
    public static final String EXTRA_OBSERVER = "blinktalk.io.EXTRA_OBSERVER";
    private static final String TAG = "CallActivity";
    private static String Path = Environment.getExternalStorageDirectory().toString() + File.separator;

    // List of mandatory application unGrantedPermissions.
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

    private AppRTCAudioManager audioManager = null;
    private boolean isVideoMute = true;
    Handler networkSpeedHandler;
    // Controls
    private String serverURL = "", channelID = "", iUserName = "";
    private VideoViewManager renderViewManager;
    private boolean isConnected = true;

    private TextView textViewRoomNumber;
    private TextView textViewTime;
    private TextView textViewNetSpeed;
    private Button buttonHangUp;
//    private CheckBox btnRotateScreen;
    //功能按钮所在的layout
//    private LinearLayout moreContainer;
    private LinearLayout waitingTips;
    private LinearLayout titleContainer,mcall_more_container;
    private WebView whiteboardView;
    private RelativeLayout mRelativeWebView;
    private boolean isGPUImageFliter = false;
    private Handler handler = new Handler();
    private DebugInfoAdapter debugInfoAdapter;
    private ListView debugInfoListView;
    private TextView biteRateSendView, biteRateRcvView, rttSendView;
    private ProgressDialog progressDialog;
//    private checkBoxDialog sideBar;
    private AppCompatCheckBox btnCloseCamera,btnMuteMic;
    private static RongRTCPopupWindow popupWindow;
    private LinearLayout call_reder_container,call_layout_title;
    private int sideBarWidth=0;
    private AppCompatCheckBox btnSwitchCamera,btnMuteSpeaker,btnWhiteBoard,btnRaiseHand,btnChangeResolution_up,btnChangeResolution_down;
    private ImageView iv_modeSelect;
    /**
     * UpgradeToNormal邀请观察者发言,将观察升级为正常用户=0, 摄像头:1 麦克风:2
     **/
    Map<Integer, ActionState> stateMap = new LinkedHashMap<>();
    /**
     * 存储用户是否开启分享
     **/
    private HashMap<String, Boolean> sharingMap = new HashMap<>();

    public static final String CR_720x1280 = "720x1280";
    public static final String CR_1080x1920 = "1088x1920";
    public static final String CR_480x720 = "480x720";
    public static final String CR_480x640 = "480x640";
    public static final String CR_368x640 = "368x640";
    public static final String CR_368x480 = "368x480";
    public static final String CR_240x320 = "240x320";
    public static final String CR_144x256 = "144x256";
    /** true  关闭麦克风,false 打开麦克风 */
    private boolean muteMicrophone=false;
    private ScrollView scrollView;
    private HorizontalScrollView horizontalScrollView;
    private RelativeLayout rel_sv;//sv父布局
    GPUImageBeautyFilter beautyFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(
                new UnhandledExceptionHandler(this));

        String userAppid = AppRTCUtils.getAppID();
        APPID = TextUtils.isEmpty(userAppid)?appid_:userAppid;

        FinLog.i(TAG, "user appid=" + APPID);
        sideBarWidth=dip2px(CallActivity.this,40)+75;

        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_call);

        // Get Intent parameters.
        final Intent intent = getIntent();
        channelID = intent.getStringExtra(EXTRA_ROOMID);
        iUserName = intent.getStringExtra(EXTRA_USER_NAME);
        serverURL = intent.getStringExtra(EXTRA_SERVER_URL);
        isVideoMute = intent.getBooleanExtra(EXTRA_CAMERA, false);
        //设置是否启用美颜模式
        isGPUImageFliter = SessionManager.getInstance(this).getBoolean(IS_GPUIMAGEFILTER);
        RongRTCContext.ConfigParameter.isObserver = intent.getBooleanExtra(EXTRA_OBSERVER, false);
        if (channelID == null || channelID.length() == 0) {
            Log.e(TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        setChangeResolutionMap();
        initAudioManager();

        initViews(intent);
        setCallbacks();
        checkPermissions();

        initeBoottombtn();
        initRemoteScrollView();

        RongRTCEngine.getInstance().registerHeadsetReceiver();
    }

    private void initAudioManager() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(this, new Runnable() {
                    // This method will be called each time the audio state (number and
                    // type of devices) has been changed.
                    @Override
                    public void run() {
                        onAudioManagerChangedState();
                    }
                }
        );
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Initializing the audio manager...");
        audioManager.init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(popupWindow!=null && popupWindow.isShowing()){
            popupWindow.dismiss();
            popupWindow=null;
        }
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            horizontalScreenViewInit();
        }else if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
            verticalScreenViewInit();
        }
        if (renderViewManager != null && null != unGrantedPermissions && unGrantedPermissions.size() == 0) {
            renderViewManager.rotateView();
            if (mRelativeWebView.getVisibility() == View.VISIBLE)
                loadWhiteBoard(null, true);
        }
    }
    /** 初始化底部按钮 默认竖屏 **/
    private void initeBoottombtn(){
        ViewGroup.MarginLayoutParams  layoutParams= (ViewGroup.MarginLayoutParams) btnCloseCamera.getLayoutParams();
        layoutParams.setMargins(dip2px(CallActivity.this,50), 0, 0, dip2px(CallActivity.this,16));
        btnCloseCamera.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams  mutelayoutParams= (ViewGroup.MarginLayoutParams) btnMuteMic.getLayoutParams();
        mutelayoutParams.setMargins(0, 0, dip2px(CallActivity.this,50), dip2px(CallActivity.this,16));
        btnMuteMic.setLayoutParams(mutelayoutParams);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 在升降级过程中，改变功能按钮的状态
     */
    private void toggleCameraMicViewStatus() {
        if (RongRTCContext.ConfigParameter.isObserver) {//降级之前，先恢复到正常状态：摄像头打开/前置/未静音
            if (btnSwitchCamera.isChecked())
                btnSwitchCamera.performClick();
            if (btnMuteMic.isChecked())
                btnMuteMic.performClick();
            if (btnCloseCamera.isChecked())
                btnCloseCamera.performClick();
        }
        btnSwitchCamera.setEnabled(RongRTCContext.ConfigParameter.isObserver ? false : true);
        btnCloseCamera.setEnabled(RongRTCContext.ConfigParameter.isObserver ? false : true);
        btnMuteMic.setEnabled(RongRTCContext.ConfigParameter.isObserver ? false : true);
        btnRaiseHand.setEnabled(RongRTCContext.ConfigParameter.isObserver ? true : false);
        iv_modeSelect.setEnabled(RongRTCContext.ConfigParameter.isObserver ? false : true);
    }

    private void initViews(Intent intent) {
        mcall_more_container= (LinearLayout) findViewById(R.id.call_more_container);
        iv_modeSelect= (ImageView) findViewById(R.id.btn_modeSelect);
        btnRaiseHand = (AppCompatCheckBox) findViewById(R.id.menu_request_to_normal);
        btnSwitchCamera = (AppCompatCheckBox) findViewById(R.id.menu_switch);

        btnMuteSpeaker = (AppCompatCheckBox) findViewById(R.id.menu_mute_speaker);
        btnWhiteBoard = (AppCompatCheckBox) findViewById(R.id.menu_whiteboard);

        btnChangeResolution_up= (AppCompatCheckBox) findViewById(R.id.menu_up);
        btnChangeResolution_down= (AppCompatCheckBox) findViewById(R.id.menu_down);

        if(BuildConfig.DEBUG && null!=btnChangeResolution_up){
            btnChangeResolution_up.setVisibility(View.GONE);
        }else{
            btnChangeResolution_up.setVisibility(View.GONE);
        }
        if(BuildConfig.DEBUG && null!=btnChangeResolution_down){
            btnChangeResolution_down.setVisibility(View.GONE);
        }else{
            btnChangeResolution_down.setVisibility(View.GONE);
        }
        //
        call_layout_title= (LinearLayout) findViewById(R.id.call_layout_title);
        call_reder_container= (LinearLayout) findViewById(R.id.call_reder_container);
        biteRateSendView = (TextView) findViewById(R.id.debug_info_bitrate_send);
        biteRateRcvView = (TextView) findViewById(R.id.debug_info_bitrate_rcv);
        rttSendView = (TextView) findViewById(R.id.debug_info_rtt_send);
        debugInfoListView = (ListView) findViewById(R.id.debug_info_list);
        debugInfoAdapter = new DebugInfoAdapter(this);
        debugInfoListView.setAdapter(debugInfoAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("白板加载中...");
        textViewRoomNumber = (TextView) findViewById(R.id.call_room_number);
        textViewTime = (TextView) findViewById(R.id.call_time);
        textViewNetSpeed = (TextView) findViewById(R.id.call_net_speed);
        buttonHangUp = (Button) findViewById(R.id.call_btn_hangup);
        titleContainer = (LinearLayout) findViewById(R.id.call_layout_title);
        scrollView= (ScrollView) findViewById(R.id.scrollView);
        horizontalScrollView= (HorizontalScrollView) findViewById(R.id.horizontalScrollView);

        rel_sv= (RelativeLayout) findViewById(R.id.rel_sv);

        iv_modeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ButtentSolp.check(view.getId(), 500)) {return;}
                if(!RongRTCContext.ConfigParameter.isObserver){
                    if(popupWindow!=null && popupWindow.isShowing()){
                        popupWindow.dismiss();
                        popupWindow=null;
                    }else{
                        showPopupWindow();
                    }
                }
            }
        });

        iv_modeSelect.setEnabled(RongRTCContext.ConfigParameter.isObserver ? false : true);
        btnRaiseHand.setEnabled(RongRTCContext.ConfigParameter.isObserver ? true : false);

        btnSwitchCamera.setEnabled(RongRTCContext.ConfigParameter.isObserver ? false : true);

        btnCloseCamera = (AppCompatCheckBox) findViewById(R.id.menu_close);
        btnCloseCamera.setChecked(isVideoMute);
        btnCloseCamera.setEnabled(RongRTCContext.ConfigParameter.isObserver ? false : true);

        btnMuteMic = (AppCompatCheckBox) findViewById(R.id.menu_mute_mic);
        btnMuteMic.setEnabled(RongRTCContext.ConfigParameter.isObserver ? false : true);
        waitingTips = (LinearLayout) findViewById(R.id.call_waiting_tips);
//        btnRotateScreen = (CheckBox) findViewById(R.id.menu_rotate_screen);
//        btnRotateScreen.setVisibility(View.GONE);
        mRelativeWebView = (RelativeLayout) findViewById(R.id.call_whiteboard);
        whiteboardView = new WebView(getApplicationContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        whiteboardView.setLayoutParams(params);
        mRelativeWebView.addView(whiteboardView);
//         btnWhiteBoard.setVisibility(View.GONE);
        WebSettings settings = whiteboardView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);//启用内置的缩放算法
        settings.setUseWideViewPort(true);
        if(Build.VERSION.SDK_INT>18){//host674
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        }else{
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        }
        settings.setLoadWithOverviewMode(true);
        settings.setBlockNetworkImage(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        renderViewManager = new VideoViewManager();
        renderViewManager.setActivity(this);
        if (BuildConfig.DEBUG) {
            textViewNetSpeed.setVisibility(View.VISIBLE);
        } else {
            textViewNetSpeed.setVisibility(View.GONE);
        }

        textViewRoomNumber.setText(getText(R.string.room_number) + intent.getStringExtra(CallActivity.EXTRA_ROOMID));
        buttonHangUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intendToLeave();
            }
        });
        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destoryPopupWindow();
                CheckBox checkBox = (CheckBox) v;
                onCameraSwitch();
            }
        });
        btnCloseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                onCameraClose(checkBox.isChecked());
            }
        });
        btnMuteMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                onToggleMic(checkBox.isChecked());
            }
        });
        btnMuteSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destoryPopupWindow();
                CheckBox checkBox = (CheckBox) view;
                onToggleSpeaker(checkBox.isChecked());
            }
        });
        btnWhiteBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destoryPopupWindow();
                toggleWhiteboard(((CheckBox) view).isChecked());
            }
        });
        btnRaiseHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destoryPopupWindow();
//                RongRTCEngine.getInstance().observerRequestBecomeNormalUser();
            }
        });
        renderViewManager.setOnLocalVideoViewClickedListener(new VideoViewManager.OnLocalVideoViewClickedListener() {
            @Override
            public void onClick() {
                toggleActionButtons(buttonHangUp.getVisibility() == View.VISIBLE);
            }
        });
        waitingTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleActionButtons(buttonHangUp.getVisibility() == View.VISIBLE);
            }
        });
        btnChangeResolution_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destoryPopupWindow();
                changeVideoSize("up");
            }
        });
        btnChangeResolution_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destoryPopupWindow();
                changeVideoSize("down");
            }
        });
        setCallIdel();
    }

    /**
     * 准备离开当前房间
     */
    private void intendToLeave() {
        if (null != sharingMap) {
            sharingMap.clear();
        }

        //当前用户是观察者 或 离开房间时还有其他用户存在，直接退出
        if (RongRTCContext.ConfigParameter.isObserver || RongRTCEngine.getInstance().hasConnectedUser())
            disconnect();
        else //非观察者离开房间时，房间只剩自己，这时候要先去判断是否有白板存在，然后提示用户
        {
            if (RongRTCEngine.getInstance().isWhiteBoardExist()) {//房间中有打开的白板，提示用户是否关闭
                FinLog.i(TAG, "还有人吗：" + RongRTCEngine.getInstance().hasConnectedUser() + ",,RongRTCEngine.getInstance().isWhiteBoardExist()=" + RongRTCEngine.getInstance().isWhiteBoardExist());
                showConfirmDialog(getResources().getString(R.string.meeting_control_destroy_whiteBoard), null, null, null, null);
            } else disconnect();
        }
    }

    /**
     * 改变屏幕上除了视频通话之外的其他视图可见状态
     */
    private void toggleActionButtons(boolean isHidden) {
        if (isHidden) {
            buttonHangUp.setVisibility(View.GONE);
            mcall_more_container.setVisibility(View.GONE);
            titleContainer.setVisibility(View.GONE);
            btnCloseCamera.setVisibility(View.GONE);
            btnMuteMic.setVisibility(View.GONE);
        } else {
            btnCloseCamera.setVisibility(View.VISIBLE);
            btnMuteMic.setVisibility(View.VISIBLE);
            buttonHangUp.setVisibility(View.VISIBLE);
            mcall_more_container.setVisibility(View.VISIBLE);
            titleContainer.setVisibility(View.VISIBLE);
            startTenSecondsTimer();
        }
    }

    private Timer tenSecondsTimer;

    /**
     * 启动一个持续10秒的计时器，用于隐藏除了视频以外的视图
     */
    private void startTenSecondsTimer() {
//        tenSecondsTimer = new Timer();
//        tenSecondsTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        toggleActionButtons(true);
//                        tenSecondsTimer = null;
//                    }
//                });
//            }
//        }, 10 * 1000);
    }

    private void setCallIdel() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (RongRTCEngine.getInstance() != null) {
                            RongRTCEngine.getInstance().muteMicrophone(true);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (RongRTCEngine.getInstance() != null) {
                            RongRTCEngine.getInstance().muteMicrophone(false);
                        }
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void setWaitingTipsVisiable(boolean visiable) {
        if (visiable) {
            waitingTips.setVisibility(View.VISIBLE);
        } else {
            waitingTips.setVisibility(View.GONE);
        }
    }

    // Activity interfaces
    @Override
    public void onPause() {
//        sm.unregisterListener(listener);
        super.onPause();
        RongRTCEngine.getInstance().stopCapture();
    }

    @Override
    public void onResume() {
//        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
        RongRTCEngine.getInstance().startCapture();
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearState();
    }

    private void destoryPopupWindow(){
        if(null!=popupWindow && popupWindow.isShowing()){
            popupWindow.dismiss();
            popupWindow=null;
        }
    }

    @Override
    protected void onDestroy() {
        destoryPopupWindow();
        if (isConnected) {
            if (RongRTCEngine.getInstance() != null)
                RongRTCEngine.getInstance().leaveChannel();
            if (renderViewManager != null)
//                renderViewManager.destroyViews();
                if (audioManager != null) {
                    audioManager.close();
                    audioManager = null;
                }
        }
        if (handler != null)
            handler.removeCallbacks(memoryRunnable);
        super.onDestroy();
        if (null != ConfirmDialog && ConfirmDialog.isShowing()) {
            ConfirmDialog.dismiss();
            ConfirmDialog = null;
        }
        if (null != sharingMap) {
            sharingMap.clear();
        }
        destroyWebView(whiteboardView);
        if (beautyFilter != null) {
            beautyFilter.destroy();
            beautyFilter = null;
        }
    }


    public void onCameraSwitch() {
        RongRTCEngine.getInstance().switchCamera();
    }

    /**
     * 摄像头开关
     *
     * @param closed true  关闭摄像头
     *               false 打开摄像头
     * @return
     */
    public boolean onCameraClose(boolean closed) {
        RongRTCEngine.getInstance().closeLocalVideo(closed);
        if (renderViewManager != null)
            renderViewManager.updateTalkType(getDeviceId(), closed ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
        this.isVideoMute = closed;
        return isVideoMute;
    }
//
//    @Override
//    public void onVideoScalingSwitch(ScalingType scalingType) {
////        this.scalingType = scalingType;
////        updateVideoView();
//    }

    public void onCaptureFormatChange(int width, int height, int framerate) {
    }

    public boolean onToggleMic(boolean mute) {
        muteMicrophone=mute;
        RongRTCEngine.getInstance().muteMicrophone(mute);
        return mute;
    }

    public boolean onToggleSpeaker(boolean mute) {
        try {
            audioManager.onToggleSpeaker(mute);
        } catch (Exception e) {
            e.printStackTrace();
            FinLog.i(TAG, "message=" + e.getMessage());
        }
        return mute;
    }

    private void rotateScreen(boolean isToLandscape) {
        if (isToLandscape)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void toggleWhiteboard(boolean open) {
        if (open) {
            RongRTCEngine.getInstance().requestWhiteBoardURL();
        } else {
            mRelativeWebView.setVisibility(View.GONE);
        }
//        renderViewManager.toggleLocalView(!open);
    }

    List<String> unGrantedPermissions;

    private void checkPermissions() {
        unGrantedPermissions = new ArrayList();
        for (String permission : MANDATORY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        if (unGrantedPermissions.size() == 0) {//已经获得了所有权限，开始加入聊天室
            startCall();
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
        for (String permission : unGrantedPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showToastLengthLong(getString(R.string.PermissionStr) + permission + getString(R.string.plsopenit));
                finish();
            } else ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
        }
        if (unGrantedPermissions.size() == 0) {
            AssetsFilesUtil.putAssetsToSDCard(getApplicationContext(), assetsFile, encryptFilePath);
            startCall();
        }
    }

    private String assetsFile = "EncryptData/00000001.bin";
    private String encryptFilePath=new StringBuffer().append(Environment.getExternalStorageDirectory().toString() + File.separator).append("Blink").append(File.separator).append("EncryptData").toString();
    RongRTCVideoView localSurface;

    private void startCall() {
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();

            //Set connection mode 这一段代码也只是在测试的时候放出
            String connetionMode = SessionManager.getInstance(this).getString(SettingActivity.CONNECTION_MODE);
            if (!TextUtils.isEmpty(connetionMode) && connetionMode.equals("P2P")) {
                RongRTCContext.ConfigParameter.connectionMode = (RongRTCContext.ConfigParameter.CONNECTION_MODE_P2P);
            } else {
                RongRTCContext.ConfigParameter.connectionMode = (RongRTCContext.ConfigParameter.CONNECTION_MODE_RELAY);
            }

            parameters.put(RongRTCEngine.ParameterKey.KEY_IS_AUDIO_ONLY, isVideoMute);
            //Set max and min bitrate
            String minBitRate = SessionManager.getInstance(this).getString(SettingActivity.BIT_RATE_MIN);
            if (!TextUtils.isEmpty(minBitRate) && minBitRate.length() > 4) {
                int bitRateIntvalue = Integer.valueOf(minBitRate.substring(0, minBitRate.length() - 4));
                FinLog.i(TAG,"BIT_RATE_MIN="+bitRateIntvalue);
                parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_MIN_RATE, bitRateIntvalue);
            }
            String maxBitRate = SessionManager.getInstance(this).getString(SettingActivity.BIT_RATE_MAX);
            if (!TextUtils.isEmpty(maxBitRate) && maxBitRate.length() > 4) {
                int bitRateIntvalue = Integer.valueOf(maxBitRate.substring(0, maxBitRate.length() - 4));
                FinLog.i(TAG,"BIT_RATE_MAX="+bitRateIntvalue);
                parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_MAX_RATE, bitRateIntvalue);
            }
            //set resolution
            String resolution = SessionManager.getInstance(this).getString(SettingActivity.RESOLUTION);
            String fps = SessionManager.getInstance(this).getString(SettingActivity.FPS);
            if (SettingActivity.RESOLUTION_LOW.equals(resolution)) {
                if ("15".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_15f);
                } else if ("24".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_24f);
                } else if ("30".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_30f);
                }
            } else if (SettingActivity.RESOLUTION_MEDIUM.equals(resolution)) {
                if ("15".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_1);
                } else if ("24".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_24f_1);
                } else if ("30".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_30f_1);
                }
            } else if (SettingActivity.RESOLUTION_HIGH.equals(resolution)) {
                if ("15".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_15f);
                } else if ("24".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_24f);
                } else if ("30".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_30f);
                }
            } else if (SettingActivity.RESOLUTION_SUPER.equals(resolution)) {
                if ("15".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_15f);
                } else if ("24".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_24f);
                } else if ("30".equals(fps)) {
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_PROFILE, RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_30f);
                }
            }
            //set codecs
            String codec = SessionManager.getInstance(this).getString(SettingActivity.CODECS);
            if (!TextUtils.isEmpty(codec)) {
                if ("VP8".equals(codec))
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_CODECS, RongRTCEngine.RongRTCVideoCodecs.VP8);
                else
                    parameters.put(RongRTCEngine.ParameterKey.KEY_VIDEO_CODECS, RongRTCEngine.RongRTCVideoCodecs.H264);
            }

            parameters.put(RongRTCEngine.ParameterKey.KEY_USER_TYPE, RongRTCContext.ConfigParameter.isObserver ? RongRTCEngine.UserType.RongRTC_User_Observer : RongRTCEngine.UserType.RongRTC_User_Normal);

            //设置是否使用SRTP
            parameters.put(RongRTCEngine.ParameterKey.KEY_IS_SRTP_USED, SessionManager.getInstance(this).getBoolean(IS_SRTP));

            parameters.put(RongRTCEngine.ParameterKey.KEY_TINYSTREAMENABLED,SessionManager.getInstance(this).getBoolean(IS_STREAM_TINY));

            RongRTCEngine.getInstance().setVideoParameters(parameters);
            RongRTCEngine.getInstance().enableSendLostReport(true);
//            RongRTCEngine.getInstance().setExternalEncryptFilePath(encryptFilePath + File.separator + "00000001.bin");

//            String path = new StringBuffer().append(Path).append("Blink").append(File.separator).append("Log").append(File.separator).append(getDateString()).append(".log").toString();
//            RongRTCEngine.getInstance().setBlinkLog(path);

            renderViewManager.initViews(this, RongRTCContext.ConfigParameter.isObserver);

            if (!RongRTCContext.ConfigParameter.isObserver) {
                localSurface = RongRTCEngine.createVideoView(getApplicationContext());
                RongRTCEngine.getInstance().setLocalVideoView(localSurface);
                renderViewManager.userJoin(getDeviceId(), iUserName, isVideoMute ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
                renderViewManager.setVideoView(true, getDeviceId(), iUserName, localSurface, isVideoMute ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
            }
            logonToServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logonToServer() {
        try {
            deviceId = getDeviceId();
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = RongRTCSessionManager.getInstance().getString(RongRTCContext.RONGRTC_UUID);
            }
            FinLog.i("BinClient", "tcp方式请求token");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String token = RongRTCHttpClient.getInstance().doPost(serverURL, "uid=" + deviceId + "&appid=" + APPID);
                    if (TextUtils.isEmpty(token) || TextUtils.isEmpty(deviceId)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CallActivity.this, R.string.pleasetryagain, Toast.LENGTH_SHORT).show();
                                CallActivity.this.finish();
                            }
                        });
                        return;
                    }
                    RongRTCEngine.getInstance().joinChannel(deviceId, iUserName, token, channelID);
//                        String filePath = getAudioRecordFilePath();
//                        FinLog.i(TAG, "开始录音： " + filePath);
//                        RongRTCEngine.getInstance().startAudioRecording(filePath);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAudioRecordFilePath() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/blink/audio_recording/";
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return path + System.currentTimeMillis() + ".wav";
    }

    private String getDeviceId() {
        String deviceId = "";
        try {
            TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Activity.TELEPHONY_SERVICE);
            deviceId = TelephonyMgr.getDeviceId();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = RongRTCSessionManager.getInstance().getString(RongRTCContext.RONGRTC_UUID);
            }
            return deviceId;
        }
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.

    private void startCalculateNetSpeed() {
        if (networkSpeedHandler == null)
            networkSpeedHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        textViewNetSpeed.setText(getResources().getString(R.string.network_traffic_receive) + msg.getData().getLong("rcv") + "Kbps  " +
                                getResources().getString(R.string.network_traffic_send) + msg.getData().getLong("send") + "Kbps");
                        updateTimer();
                    }
                    super.handleMessage(msg);
                }
            };
//        startTenSecondsTimer();
    }

    private int time = 0;

    private void updateTimer() {
        time++;
        textViewTime.setText(getFormatTime(time));
    }

    private String getFormatTime(int time) {
        if (time < 10)
            return "00:0" + time;
        else if (time < 60)
            return "00:" + time;
        else if (time % 60 < 10) {
            if (time / 60 < 10) {
                return "0" + time / 60 + ":0" + time % 60;
            } else {
                return time / 60 + ":0" + time % 60;
            }
        } else {
            if (time / 60 < 10) {
                return "0" + time / 60 + ":" + time % 60;
            } else {
                return time / 60 + ":" + time % 60;
            }
        }
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setCallbacks() {
        RongRTCEngine.getInstance().setRongRTCEngineEventHandler(new RongRTCEngineEventHandler() {

            @Override
            public void onNotifyHeadsetState(int HeadsetType, boolean state) {
                super.onNotifyHeadsetState(HeadsetType, state);
                Log.i(BluetoothUtil.TAG,""+(HeadsetType==0?"蓝牙耳机":"有线耳机")+",,"+(state?"连接上":"断开了"));
            }

            @Override
            public void onStartCaptureResult(int resultCode) {
                super.onStartCaptureResult(resultCode);
                FinLog.i("onStartCaptureResult", "onStartCapture  Result=" + resultCode + "\n threadName=" + Thread.currentThread().getName());
            }

            @Override
            public void onConnectionStateChanged(int connectionState) {
                FinLog.e("connectionState", "Connection State Code=" + connectionState + "\n threadName=" + Thread.currentThread().getName());
                switch (connectionState) {
                    case RongRTCResponseCode.Connection_RongRTCConnectionFactory_InitFailed:
                    case RongRTCResponseCode.Connection_Socket_InitFailed:
                    case RongRTCResponseCode.Connection_JoinFailed:
                    case RongRTCResponseCode.Connection_DNSFailed:
                    case RongRTCResponseCode.Connection_Disconnected:
                    case RongRTCResponseCode.Connection_KeepAliveFailed:
                    case RongRTCResponseCode.Connection_InsufficientPermissions:
                    case RongRTCResponseCode.FAILED_TO_CONNECT_USING_QUIC:
                        showToastLengthLong("onConnectionStateChanged error code=" + connectionState);
                        disconnect();
                        break;
                    case RongRTCResponseCode.Connection_JoinComplete:
                        break;
                }
            }

            /**
             * 自己是否成功离开某一聊天室, leaveChannel() 方法的结果反馈。
             *
             * @param success 是否成功
             */
            @Override
            public void onLeaveComplete(boolean success) {
            }

            @Override
            public void onUserJoined(String userId, String userName, RongRTCEngine.UserType type, long talkType, int screenSharingStatus) {
                FinLog.i("userJoined", "----onUserJoined-------\n userId=" + userId + "," + "type == RongRTCEngine.UserType.Blink_User_Observer=" + (type == RongRTCEngine.UserType.RongRTC_User_Observer) + "：Name=" + userName + ",talkType=" + talkType + ",screenSharingStatus=" + screenSharingStatus + "\n threadName=" + Thread.currentThread().getName());
                if (type == RongRTCEngine.UserType.RongRTC_User_Observer) {
                    return;
                }
                renderViewManager.userJoin(userId, userName, userJoinTaikType(talkType));
            }

            @Override
            public void onNotifyUserVideoCreated(String userId, String userName, RongRTCEngine.UserType type, long talkType, int screenSharingStatus) {
                FinLog.i("userJoined", "----onNotifyUserVideoCreated-------\n userId=" + userId + "," + "type == " + (type == RongRTCEngine.UserType.RongRTC_User_Observer) + "：Name=" + userName + ",talkType=" + talkType + ",screenSharingStatus=" + screenSharingStatus + "\n threadName=" + Thread.currentThread().getName());
                if (!userId.equals(getDeviceId())) {
                    boolean sharing_status = screenSharingStatus == 1 ? true : false;
                    sharingMap.put(userId, sharing_status);
                    if (RongRTCContext.ConfigParameter.isObserver && sharing_status) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                showToastLengthLong(getResources().getString(R.string.meeting_control_OpenWiteBoard));
                            }
                        }, 1100);
                    }
                }
                if (!renderViewManager.hasConnectedUser())
                    startCalculateNetSpeed();

                RongRTCVideoView remoteView = RongRTCEngine.createVideoView(CallActivity.this.getApplicationContext());
                RongRTCEngine.getInstance().setRemoteVideoView(remoteView, userId);
                renderViewManager.setVideoView(false, userId, userName, remoteView, userJoinTaikType(talkType));
            }

            /**
             * 某一用户从当前所在的聊天室退出
             * 房间中只有两个人,不存在流退不退出 直接left  ，人多的时候会执行left videoDestory;
             * @param userId 退出聊天室的用户ID
             */
            @Override
            public void onUserLeft(String userId) {
                FinLog.i("userLeft", "onUserLeft---\nuserid=" + userId + ",,connectedRemote size=" + renderViewManager.connetedRemoteRenders.size());
                exitRoom(userId);
            }

            @Override
            public void OnNotifyUserVideoDestroyed(String userId) {
                FinLog.i("userLeft", "OnNotifyUserVideoDestroyed----\nuserId=" + userId);
                //用户被降级会回调
                exitRoom(userId);
            }

            /**
             * 自己已在聊天室中且聊天室中至少还有一个远程用户, requestWhiteBoardURL() 请求白板页面的HTTP URL之后的回调
             *
             * @param url 白板页面的url
             */
            @Override
            public void onWhiteBoardURL(String url) {
                loadWhiteBoard(url, false);
            }

            @Override
            public void onNetworkSentLost(int lossRate) {
//                FinLog.e("lossRate = " + lossRate);
            }

            @Override
            public void onNetworkReceiveLost(int lossRate) {

            }

            @Override
            public void onAudioInputLevel(String audioLevel) {
                super.onAudioInputLevel(audioLevel);
                try {
                    if (!TextUtils.isEmpty(audioLevel)) {
                        int val = Integer.valueOf(audioLevel);
                        audiolevel(val, RongRTCContext.ConfigParameter.userID);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAudioReceivedLevel(HashMap<String, String> audioLevel) {
                super.onAudioReceivedLevel(audioLevel);
                try {
                    Iterator iter = audioLevel.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = "";
                        int val = 0;
                        if (null != entry.getKey()) {
                            key = entry.getKey().toString();
                        }
                        if (null != entry.getValue()) {
                            val = Integer.valueOf(entry.getValue().toString());
                        }
                        audiolevel(val, key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onControlAudioVideoDevice(int code) {
            }

            @Override
            public void onNotifyControlAudioVideoDevice(String userId, RongRTCEngine.RongRTCDeviceType type, boolean isOpen) {
//                if (renderViewManager != null && (type == RongRTCEngine.BlinkDeviceType.Camera || type == RongRTCEngine.BlinkDeviceType.CameraAndMicrophone))
//                    renderViewManager.updateTalkType(userId, isOpen ? 1 : 0);
                deviceCover(userId, isOpen, type);
            }

            @Override
            public void onNotifyCreateWhiteBoard(String userId) {
                toastMessage(userId + getString(R.string.Createdawhiteboard));
            }

            @Override
            public void onNotifySharingScreen(String userId, boolean isOpen) {
                FinLog.i(TAG, userId + (isOpen ? "打开了屏幕共享" : "关闭了屏幕共享"));
                sharingMap.put(userId, isOpen);
                if (isOpen && renderViewManager.isBig(userId) && isSharing(userId)) {
//                    showToastLengthLong(getResources().getString(R.string.meeting_control_OpenWiteBoard));
                }
            }
        });

//        RongRTCEngine.getInstance().setChannelManageEventHandler(new RongRTCEngineChannelManageEventHandler() {
//
//            @Override
//            public void onObserverRequestBecomeNormalUser(int code) {
//                if (code == 0)
//                    toastMessage(getString(R.string.Requestsentsuccessfully));
//                else toastMessage(getString(R.string.Requestfailedtosend) + code);
//
//            }
//
//            @Override
//            public void onUpgradeObserverToNormalUser(int code) {
//            }
//
//            @Override
//            public void onDegradeNormalUserToObserver(int code) {
//            }
//
//            @Override
//            public void onRemoveUser(int code) {
//            }
//
//            @Override
//            public void onHostControlUserDevice(String userId, RongRTCEngine.RongRTCDeviceType dType, int code) {
//            }
//
//            @Override
//            public void onGetInviteURL(String url, int code) {
//                if (code == 0)
//                    copyInviteUrlToClipboard(url);
//                else toastMessage(getString(R.string.Failedtogetinvitationlink));
//            }
//
//            @Override
//            public void onNormalUserRequestHostAuthority(int code) {
//                if (code == 0)
//                    toastMessage(getString(R.string.Youbecomethehostofthemeeting));
//            }
//
//            @Override
//            public void onNotifyNormalUserRequestHostAuthority(String userId) {
//                toastMessage(getString(R.string.userstr) + userId + getString(R.string.becomesthemeetinghost));
//            }
//
//            @Override
//            public void onNotifyDegradeNormalUserToObserver(String hostUid, String userId) {
//                if (userId.equals(getDeviceId())) {
//                    changeToObserverOrNormal(true);
//                    toastMessage(getString(R.string.downgradedtoanobserver));
//                    RongRTCEngine.getInstance().answerDegradeNormalUserToObserver(hostUid, true);
//                } else {
//                    toastMessage(userId + getString(R.string.userdowngradedtoanobserver));
//                    sharingMap.put(userId, false);
//                }
//            }
//
//            @Override
//            public void onNotifyUpgradeObserverToNormalUser(String hostUid, String userId) {
//                if (addActionState(0, hostUid, userId)) {
//                    return;
//                }
//                showConfirmDialog(getResources().getString(R.string.meeting_control_inviteToUpgrade), hostUid, userId, RongRTCEngine.RongRTCActionType.UpgradeToNormal, null);
//            }
//
//            @Override
//            public void onNotifyRemoveUser(String userId) {
//                toastMessage(getString(R.string.removedfromthecoversation));
//                disconnect();
//            }
//
//            @Override
//            public void onNotifyObserverRequestBecomeNormalUser(String userId) {
//                showConfirmDialog(getString(R.string.userstr) + userId + "请求发言", "", userId, RongRTCEngine.RongRTCActionType.RequestUpgradeToNormal, null);
//            }
//
//            @Override
//            public void onNotifyHostControlUserDevice(String userId, String hostId, RongRTCEngine.RongRTCDeviceType type, boolean isOpen) {
//                if (isOpen) {
//                    if (addActionState(type.getValue(), hostId, userId)) {
//                        return;
//                    }
//                    String deviceType = "";
//                    if (type == RongRTCEngine.RongRTCDeviceType.Camera)
//                        deviceType = getResources().getString(R.string.meeting_control_inviteToOpen_camera);
//                    if (type == RongRTCEngine.RongRTCDeviceType.Microphone)
//                        deviceType = getResources().getString(R.string.meeting_control_inviteToOpen_microphone);
//                    showConfirmDialog(deviceType, hostId, userId, RongRTCEngine.RongRTCActionType.InviteToOpen, type);
//                } else {
//                    if (userId.equals(getDeviceId())) {
//                        if (type == RongRTCEngine.RongRTCDeviceType.Camera && !btnCloseCamera.isChecked())
//                            btnCloseCamera.performClick();
//                        if (type == RongRTCEngine.RongRTCDeviceType.Microphone && !btnMuteMic.isChecked())
//                            btnMuteMic.performClick();
//                        if (type == RongRTCEngine.RongRTCDeviceType.CameraAndMicrophone) {
//                            if (!btnCloseCamera.isChecked())
//                                btnCloseCamera.performClick();
//                            if (!btnCloseCamera.isChecked())
//                                btnMuteMic.performClick();
//                        }
//                        RongRTCEngine.getInstance().answerHostControlUserDevice(hostId, type, isOpen, true);
//                    }
//                }
//            }
//
//            @Override
//            public void onNotifyAnswerUpgradeObserverToNormalUser(String userId, boolean isAccept) {
//                String statusString = "";
//                if (isAccept)
//                    statusString = getString(R.string.Agree);
//                else
//                    statusString = getString(R.string.Refuse);
//                toastMessage(getString(R.string.userstr) + userId + statusString + getString(R.string.Upgradetonormaluser));
//            }
//
//            @Override
//            public void onNotifyAnswerObserverRequestBecomeNormalUser(String userId, long status) {
//                if (status == RongRTCEngine.RongRTCAnswerActionType.Busy.getValue()) {
//                    showToastLengthLong(getString(R.string.plscalllater));
//                } else if (status == RongRTCEngine.RongRTCAnswerActionType.Accept.getValue()) {
////                    Toast.makeText(CallActivity.this, "主持人同意", Toast.LENGTH_SHORT).show();
//                    if (userId.equals(getDeviceId())) {
//                        changeToObserverOrNormal(false);
//                    } else {
////                        FinLog.i("","主持人同意了"+userId+"成为正常用户");
//                    }
//                } else if (status == RongRTCEngine.RongRTCAnswerActionType.Deny.getValue()) {
////                    Toast.makeText(CallActivity.this, "主持人拒绝", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            /**
//             * 主持人将其他与会人降级成为观察者时
//             * @param userId   用户ID
//             * @param isAccept 是否接受 true：被降级的与会人默认同意
//             */
//            @Override
//            public void onNotifyAnswerDegradeNormalUserToObserver(String userId, boolean isAccept) {
//                sharingMap.put(userId, false);
//            }
//
//            /**
//             * @param userId   用户ID 3947CD61-BBFE-4623-8BA7-D5FD5D7E9162
//             * @param isOpen   操作类型 false
//             * @param dType    设备类型 1:摄像头 2 麦克风 3 摄像头+麦克风 -1无效
//             * @param isAccept 是否接受 true
//             */
//            @Override
//            public void onNotifyAnswerHostControlUserDevice(String userId, boolean isOpen, RongRTCEngine.RongRTCDeviceType dType, boolean isAccept) {
////                toastMessage("用户:" + userId + (isAccept ? " 同意" : " 拒绝") + "了你的请求:" + (isOpen ? " 打开" : " 关闭") + dType.name());
////                int talkType=-2; //0-只有音频；1-视频；2-音频+视频；3-无 // 0 or 3摄像头被关闭
//                deviceCover(userId, isOpen, dType);
//            }
//        });

        RongRTCEngine.getInstance().setVideoFrameListener(new RongRTCEngineVideoFrameListener() {
            @Override
            public int processVideoFrame(int width, int height, int oesTextureId) {
                if (isGPUImageFliter) {
                    if (beautyFilter == null)
                        beautyFilter = new GPUImageBeautyFilter();
                    oesTextureId = beautyFilter.draw(width, height, oesTextureId);
                }
                return oesTextureId;
            }
        });

        StatusReportParser.debugCallbacks = new StatusReportParser.RongRTCDebugCallbacks() {
            @Override
            public void onConnectionStats(final StatusReport statusReport) {
                updateNetworkSpeedInfo(statusReport);
                //只有Debug模式下才显示详细的调试信息
                if (renderViewManager == null || !BuildConfig.DEBUG)
                    return;
                parseToList(statusReport);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDebugInfo(statusReport);
                    }
                });
            }
        };
    }

    private void updateNetworkSpeedInfo(StatusReport statusReport) {
        if (networkSpeedHandler != null) {
            Message message = new Message();
            Bundle bundle = new Bundle();
            message.what = 1;
            bundle.putLong("send", statusReport.bitRateSend);
            bundle.putLong("rcv", statusReport.bitRateRcv);
            message.setData(bundle);
            networkSpeedHandler.sendMessage(message);
        }
    }

    private void updateDebugInfo(StatusReport statusReport) {
        biteRateSendView.setText(statusReport.bitRateSend + "");
        biteRateRcvView.setText(statusReport.bitRateRcv + "");
        rttSendView.setText(statusReport.rtt + "");
        debugInfoAdapter.setStatusBeanList(statusBeanList);
        debugInfoAdapter.notifyDataSetChanged();
    }

    List<StatusBean> statusBeanList = new ArrayList<>();

    private void parseToList(StatusReport statusReport) {
        statusBeanList.clear();
        for (Map.Entry<String, StatusBean> entry : statusReport.statusVideoRcvs.entrySet()) {
            statusBeanList.add(entry.getValue());
        }
        for (Map.Entry<String, StatusBean> entry : statusReport.statusVideoSends.entrySet()) {
            statusBeanList.add(entry.getValue());
        }
        if (null != statusReport.statusAudioSend) {
            statusBeanList.add(statusReport.statusAudioSend);
        }
        for (Map.Entry<String, StatusBean> entry : statusReport.statusAudioRcvs.entrySet()) {
            statusBeanList.add(entry.getValue());
        }
    }

    private void loadWhiteBoard(String url, boolean isReload) {
        try {
            if (isReload) {
                whiteboardView.reload();
                progressDialog.show();
                return;
            }

            if (TextUtils.isEmpty(url)) {
                //重置白板按钮的状态
                if(null!=btnWhiteBoard && btnWhiteBoard.isChecked()==true)
                    btnWhiteBoard.setChecked(false);
    //            btnWhiteBoard.performClick();
                showToastLengthLong(getResources().getString(R.string.meeting_control_no_whiteBoard));
                return;
            }
            whiteboardView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if(Build.VERSION.SDK_INT>18){//HOST-674
                        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    //                super.onReceivedSslError(view, handler, error);
                    try {
                        FinLog.i(TAG, "Ignore the certificate error.");
                        handler.proceed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            progressDialog.show();
//            showToastLengthLong(getResources().getString(R.string.meeting_control_OpenWiteBoard));
            mRelativeWebView.setVisibility(View.VISIBLE);
            FinLog.i(TAG, url);
            whiteboardView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            whiteboardView.loadUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyInviteUrlToClipboard(String url) {
        toastMessage(getResources().getString(R.string.meeting_control_invite_tips));
        ClipboardManager mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("meeting", String.format(getResources().getString(R.string.meeting_control_invite_url), channelID, url));
        mClipboardManager.setPrimaryClip(data);
    }

    private void toastMessage(String message) {
        //Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void showConfirmDialog(final String message, final String hostUid, final String managedUid, final RongRTCEngine.RongRTCActionType action, final RongRTCEngine.RongRTCDeviceType type) {
        TextView msg = new TextView(this);
        msg.setText(message);
        msg.setPadding(10, 10, 10, 10);
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(18);
        ConfirmDialog = new AlertDialog.Builder(this).setView(msg)
                .setPositiveButton(getResources().getString(R.string.settings_text_observer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (action == null) {
                            disconnect();
                        } else if (action == RongRTCEngine.RongRTCActionType.InviteToOpen) {
                            removeActionState(type.getValue());
                            if (type == RongRTCEngine.RongRTCDeviceType.Camera && btnCloseCamera.isChecked())
                                btnCloseCamera.performClick();
                            if (type == RongRTCEngine.RongRTCDeviceType.Microphone && btnMuteMic.isChecked())
                                btnMuteMic.performClick();
                            if (type == RongRTCEngine.RongRTCDeviceType.CameraAndMicrophone) {
                                if (btnCloseCamera.isChecked())
                                    btnCloseCamera.performClick();
                                if (btnMuteMic.isChecked())
                                    btnMuteMic.performClick();
                            }
//                            RongRTCEngine.getInstance().answerHostControlUserDevice(hostUid, type, true, true);
                        } else if (action == RongRTCEngine.RongRTCActionType.RequestUpgradeToNormal) {
//                            RongRTCEngine.getInstance().answerObserverRequestBecomeNormalUser(managedUid, true);
                        } else if (action == RongRTCEngine.RongRTCActionType.UpgradeToNormal) {
                            removeActionState(0);
//                            RongRTCEngine.getInstance().answerUpgradeObserverToNormalUser(hostUid, true);
                            changeToObserverOrNormal(false);
                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.settings_text_observer_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (action == RongRTCEngine.RongRTCActionType.InviteToOpen) {
                            removeActionState(type.getValue());
//                            RongRTCEngine.getInstance().answerHostControlUserDevice(hostUid, type, true, false);
                        } else if (action == RongRTCEngine.RongRTCActionType.RequestUpgradeToNormal) {
//                            RongRTCEngine.getInstance().answerObserverRequestBecomeNormalUser(managedUid, false);
                        } else if (action == RongRTCEngine.RongRTCActionType.UpgradeToNormal) {
                            removeActionState(0);
//                            RongRTCEngine.getInstance().answerUpgradeObserverToNormalUser(hostUid, false);
                        }
                    }
                })
                .create();
        ConfirmDialog.setCanceledOnTouchOutside(false);
        ConfirmDialog.setCancelable(false);
        ConfirmDialog.show();
    }

    /**
     * 这是本地用户升降级回调
     *
     * @param toObserver false：观察者升级成正常用户
     */
    private void changeToObserverOrNormal(boolean toObserver) {
        if (toObserver) {
            RongRTCContext.ConfigParameter.isObserver = true;
            renderViewManager.isObserver = true;
            //把自己的本地视图删除
            renderViewManager.removeVideoView(getDeviceId());

        } else {
            RongRTCContext.ConfigParameter.isObserver = false;
            renderViewManager.isObserver = false;
            if (localSurface == null) {
                localSurface = RongRTCEngine.createVideoView(getApplicationContext());
                RongRTCEngine.getInstance().setLocalVideoView(localSurface);
            }
            FinLog.i(TAG, "用户：" + iUserName + " 升级成正常用户！");
            isVideoMute = false;
            renderViewManager.setVideoView(true, getDeviceId(), iUserName, localSurface, isVideoMute ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
//            RongRTCEngine.getInstance().upgradeToNormalUser();//升级成正常用户
        }
        toggleCameraMicViewStatus();
    }

    /**
     * Initialize the UI to "waiting user join" status
     */
    private void initUIForWaitingStatus() {
        time = 0;
        textViewTime.setText(getResources().getText(R.string.connection_duration));
        textViewNetSpeed.setText(getResources().getText(R.string.network_traffic));
    }

    private void disconnect() {
        isConnected = false;
//        RongRTCEngine.getInstance().stopAudioRecording();
        RongRTCEngine.getInstance().leaveChannel();
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }
        finish();
    }

    private Runnable memoryRunnable = new Runnable() {
        @Override
        public void run() {
            getSystemMemory();
            if (handler != null)
                handler.postDelayed(memoryRunnable, 1000);
        }
    };

    /**
     * @param type true:不弹窗  false：弹窗
     * @return
     */
    private boolean addActionState(int type, String hostUid, String userid) {
        if (null == stateMap) {
            stateMap = new LinkedHashMap<>();
        }
        boolean state = false;
        if (stateMap.containsKey(type)) {
            state = true;
        } else {
            ActionState bean = null;
            if (stateMap.size() > 0) {//之前有弹窗 保存key 不继续执行
                bean = new ActionState(type, hostUid, userid);
                stateMap.put(type, bean);
                state = true;
            } else {  //当前没有弹窗 保存 继续当前的执行（弹窗）
                bean = new ActionState(type, hostUid, userid);
                stateMap.put(type, bean);
                state = false;
            }
        }
        return state;
    }

    //将观察升级为正常用户=0, 摄像头:1 麦克风:2
    private void removeActionState(int keyType) {
        stateMap.remove((Integer) keyType);
        for (Map.Entry<Integer, ActionState> val : stateMap.entrySet()) {
            ActionState state = val.getValue();
            if (state.getType() == 0) {
                showConfirmDialog(getResources().getString(R.string.meeting_control_inviteToUpgrade), state.getHostUid(), state.getUserid(), RongRTCEngine.RongRTCActionType.UpgradeToNormal, null);
            } else {
                InviteToOpen(state.getType(), state.getUserid(), state.getHostUid());
            }
            return;
        }
    }

    private void InviteToOpen(int type, String userId, String hostId) {
        String deviceType = "";
        if (type == RongRTCEngine.RongRTCDeviceType.Camera.getValue())
            deviceType = getResources().getString(R.string.meeting_control_inviteToOpen_camera);
        if (type == RongRTCEngine.RongRTCDeviceType.Microphone.getValue())
            deviceType = getResources().getString(R.string.meeting_control_inviteToOpen_microphone);
        showConfirmDialog(deviceType, hostId, userId, RongRTCEngine.RongRTCActionType.InviteToOpen, type == RongRTCEngine.RongRTCDeviceType.Camera.getValue() ? RongRTCEngine.RongRTCDeviceType.Camera : RongRTCEngine.RongRTCDeviceType.Microphone);
    }

    private void clearState() {
        if (null != stateMap && stateMap.size() > 0) {
            stateMap.clear();
        }
    }

    private void getSystemMemory() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        FinLog.e("max Memory:" + Long.toString(maxMemory / (1024 * 1024)));
        FinLog.e("free Memory:" + rt.freeMemory() / (1024 * 1024) + "m");
        FinLog.e("total Memory:" + rt.totalMemory() / (1024 * 1024) + "m");
        FinLog.e("系统是否处于低Memory运行：" + info.lowMemory);
        FinLog.e("当系统剩余Memory低于" + (info.threshold >> 10) / 1024 + "m时就看成低内存运行");
    }

    public void destroyWebView(WebView mWebView) {
        if (mWebView != null) {
            try {
                ViewParent parent = mWebView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(mWebView);
                }
                mWebView.stopLoading();
                mWebView.getSettings().setJavaScriptEnabled(false);
                mWebView.clearHistory();
                mWebView.clearView();
                mWebView.removeAllViews();

                mWebView.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 主持人、与会人 关闭or打开设备，显示封面视图设置
     *
     * @param userId
     * @param isOpen 打开/关闭
     * @param dType  0x01：摄像头 0x02：麦克风 0x03：摄像头+麦克风
     */
    private void deviceCover(String userId, boolean isOpen, RongRTCEngine.RongRTCDeviceType dType) {
//        int talkType = 3; //0-只有音频；1-视频；2-音频+视频；3-无 // 0 or 3摄像头被关闭
        renderViewManager.updateTalkType(userId, blinkTalkType(isOpen, dType));
    }

    private String blinkTalkType(boolean isOpen, RongRTCEngine.RongRTCDeviceType dType) {
        String talkType = "";
        if (isOpen) {
            if (dType == RongRTCEngine.RongRTCDeviceType.Camera) {
                talkType = RongRTCTalkTypeUtil.O_CAMERA;
            } else if (dType == RongRTCEngine.RongRTCDeviceType.Microphone) {
                talkType = RongRTCTalkTypeUtil.O_MICROPHONE;
            } else if (dType == RongRTCEngine.RongRTCDeviceType.CameraAndMicrophone) {
                talkType = RongRTCTalkTypeUtil.O_CM;
            }
        } else {//
            if (dType == RongRTCEngine.RongRTCDeviceType.Camera) {
                talkType = RongRTCTalkTypeUtil.C_CAMERA;
            } else if (dType == RongRTCEngine.RongRTCDeviceType.Microphone) {
                talkType = RongRTCTalkTypeUtil.C_MICROPHONE;
            } else if (dType == RongRTCEngine.RongRTCDeviceType.CameraAndMicrophone) {
                talkType = RongRTCTalkTypeUtil.C_CM;
            }
        }
        return talkType;
    }

    private void showToastLengthLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 横竖屏检测
     *
     * @param config
     * return true:横屏
     */
    private boolean screenCofig(Configuration config) {
        boolean screen=false;//默认竖屏
        try {
            Configuration configuration = null;
            if (config == null) {
                configuration = this.getResources().getConfiguration();
            } else {
                configuration = config;
            }
            int ori = configuration.orientation;
            if (ori == configuration.ORIENTATION_LANDSCAPE) {
                screen=true;
            } else if (ori == configuration.ORIENTATION_PORTRAIT) {
                screen=false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screen;
    }

    public Boolean isSharing(String userid) {
        if (sharingMap.size() == 0) {
            return false;
        }
        if (sharingMap.containsKey(userid)) {
            return sharingMap.get(userid);
        } else {
            return false;
        }
    }

    private void exitRoom(String userId) {
        sharingMap.remove(userId);
        renderViewManager.delSelect(userId);
        //
        renderViewManager.removeVideoView(userId);
        if (!renderViewManager.hasConnectedUser()) {//除我以为,无外人
            initUIForWaitingStatus();
        }
    }

    /**
     * userjoin onNotifyUserVideoCreated
     *
     * @param talkType
     * @return
     */
    public String userJoinTaikType(long talkType) {
        String talk = "";
        if (talkType == 0) {
            talk = RongRTCTalkTypeUtil.C_CAMERA;
        } else if (talkType == 1) {
            talk = RongRTCTalkTypeUtil.O_CM;
        } else if (talkType == 2) {
            talk = RongRTCTalkTypeUtil.C_MICROPHONE;
        } else if (talkType == 3) {
            talk = RongRTCTalkTypeUtil.C_CM;
        }
        return talk;
    }
    /*--------------------------------------------------------------------------切换分辨率---------------------------------------------------------------------------*/

    /**
     * 构造分辨率对应的BlinkVideoProfile对象
     *
     * @param resolutionStr
     * @return
     */
    private RongRTCEngine.RongRTCVideoProfile selectiveResolution(String resolutionStr) {
        RongRTCEngine.RongRTCVideoProfile profile = null;
        String fpsStr = SessionManager.getInstance(this).getString(SettingActivity.FPS);
        if (TextUtils.isEmpty(fpsStr)) {
            fpsStr = "15";
        }
        if (CR_144x256.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_144P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_144P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_144P_30f;
            }
        } else if (CR_240x320.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_30f;
            }
        } else if (CR_368x480.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_15f_1;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_24f_1;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_30f_1;
            }
        } else if (CR_368x640.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_15f_2;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_24f_2;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_30f_2;
            }
        } else if (CR_480x640.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_1;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_24f_1;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_30f_1;
            }
        } else if (CR_480x720.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_2;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_24f_2;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_30f_2;
            }
        } else if (CR_720x1280.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_30f;
            }
        } else if (CR_1080x1920.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_30f;
            }
        } else if (CR_720x1280.equals(resolutionStr)) {
            if ("15".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_15f;
            } else if ("24".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_24f;
            } else if ("30".equals(fpsStr)) {
                profile = RongRTCEngine.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_30f;
            }
        }
        return profile;
    }

    private Map<String, ResolutionInfo> changeResolutionMap = null;
    private String[] resolution;

    private void setChangeResolutionMap() {
        ResolutionInfo info = null;
        changeResolutionMap = new HashMap<>();
        String key = "";
        resolution = new String[]{CR_144x256, CR_240x320, CR_368x480, CR_368x640, CR_480x640, CR_480x720, CR_720x1280, CR_1080x1920};
        try {
            for (int i = 0; i < resolution.length; i++) {
                key = resolution[i];
                info = new ResolutionInfo(key, i);
                changeResolutionMap.put(key, info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeVideoSize(String action) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(RongRTCContext.ConfigParameter.videoWidth);
        stringBuffer.append("x").append(RongRTCContext.ConfigParameter.videoHeight);
        String resolutionStr = stringBuffer.toString();
        int index = -1;

        try {
            if (changeResolutionMap.containsKey(resolutionStr)) {
                index = changeResolutionMap.get(resolutionStr).getIndex();
            }
            if (action.equals("down")) {
                if (index != 0) {
                    String str = resolution[index - 1];
                    RongRTCEngine.RongRTCVideoProfile profile = selectiveResolution(str);
                    RongRTCEngine.getInstance().changeVideoSize(profile);
                } else {
                    Toast.makeText(CallActivity.this, R.string.resolutionmunimum, Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals("up")) {
                if (index != 7) {
                    String str = resolution[index + 1];
                    RongRTCEngine.RongRTCVideoProfile profile = selectiveResolution(str);
                    RongRTCEngine.getInstance().changeVideoSize(profile);
                } else {
                    Toast.makeText(CallActivity.this, R.string.resolutionhighest, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FinLog.i(TAG, "error：" + e.getMessage());
        }
    }

    /*--------------------------------------------------------------------------AudioLevel---------------------------------------------------------------------------*/

    private void audiolevel(final int val, final String key) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != renderViewManager && null != renderViewManager.connetedRemoteRenders &&
                        renderViewManager.connetedRemoteRenders.containsKey(key)) {
                    if (val > 0) {
                        if(key.equals(RongRTCContext.ConfigParameter.userID) && muteMicrophone){
                            renderViewManager.connetedRemoteRenders.get(key).coverView.closeAudioLevel();
                        }else{
                            renderViewManager.connetedRemoteRenders.get(key).coverView.showAudioLevel();
                        }
                    } else {
                        renderViewManager.connetedRemoteRenders.get(key).coverView.closeAudioLevel();
                    }
                }
            }
        });
    }

    private void showPopupWindow(){
        if(null!=popupWindow && popupWindow.isShowing()){
            return;
        }
        boolean screenConfig=screenCofig(null);
        WindowManager wm = (WindowManager) this.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
//        int screenHeight=wm.getDefaultDisplay().getHeight();
        int xoff=screenWidth-sideBarWidth-dip2px(CallActivity.this,80);
        int yoff=0;
//        int base = screenHeight < screenWidth ? screenHeight : screenWidth;

        View view = LayoutInflater.from(CallActivity.this).inflate(R.layout.layout_viewing_pattern, null);
        final TextView tv_smooth= (TextView) view.findViewById(R.id.tv_smooth);
        final TextView tv_highresolution= (TextView) view.findViewById(R.id.tv_highresolution);
        if(SessionManager.getInstance(Utils.getContext()).contains("VideoModeKey")){
            String videoMode= SessionManager.getInstance(Utils.getContext()).getString("VideoModeKey");
            if(!TextUtils.isEmpty(videoMode)){
                if(videoMode.equals("smooth")){
                    tv_smooth.setTextColor(getResources().getColor(R.color.blink_yellow));
                    tv_highresolution.setTextColor(Color.WHITE);
//                    sideBar.setVideoModeBtnText("流畅");
                }else  if(videoMode.equals("highresolution")){
                    tv_smooth.setTextColor(Color.WHITE);
//                    sideBar.setVideoModeBtnText("高清");
                    tv_highresolution.setTextColor(getResources().getColor(R.color.blink_yellow));
                }
            }
        }
        LinearLayout linear_smooth= (LinearLayout) view.findViewById(R.id.linear_smooth);
        LinearLayout linear_highresolution= (LinearLayout) view.findViewById(R.id.linear_highresolution);
        linear_smooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongRTCEngine.getInstance().setVideoMode(RongRTCEngine.TEnumVideoMode.VideoModeSmooth);
                SessionManager.getInstance(Utils.getContext()).put("VideoModeKey","smooth");
                tv_smooth.setTextColor(getResources().getColor(R.color.blink_yellow));
//                sideBar.setVideoModeBtnText("流畅");
                tv_highresolution.setTextColor(Color.WHITE);
                if(popupWindow!=null && popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
            }
        });
        linear_highresolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongRTCEngine.getInstance().setVideoMode(RongRTCEngine.TEnumVideoMode.VideoModeHighresolution);
                SessionManager.getInstance(Utils.getContext()).put("VideoModeKey","highresolution");
                tv_smooth.setTextColor(Color.WHITE);
//                sideBar.setVideoModeBtnText("高清");
                tv_highresolution.setTextColor(getResources().getColor(R.color.blink_yellow));
                if(popupWindow!=null && popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
            }
        });
        if(popupWindow==null){
            popupWindow = new RongRTCPopupWindow(view, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        }
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        yoff=dip2px(CallActivity.this,92);//36+16+view.getH
        if(screenConfig){
            xoff=sideBarWidth;
            popupWindow.showAtLocation(scrollView,Gravity.RIGHT,xoff,-yoff);
        }else{
            popupWindow.showAtLocation(iv_modeSelect,Gravity.LEFT,xoff,-yoff);
        }
    }

    /**
     * 第一次加入房间初始化远端的容器位置
     */
    private void initRemoteScrollView(){
        if(screenCofig(null)){
            horizontalScreenViewInit();
        }else{
            verticalScreenViewInit();
        }
    }

    /**
     * 横屏View改变
     */
    private void horizontalScreenViewInit(){
        try {
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) rel_sv.getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW,0);

            WindowManager wm = (WindowManager) this.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            int screenWidth = wm.getDefaultDisplay().getWidth();
            int screenHeight=wm.getDefaultDisplay().getHeight();
            int width = (screenHeight < screenWidth ? screenHeight : screenWidth)/3;
            ViewGroup.MarginLayoutParams  layoutParams= (ViewGroup.MarginLayoutParams) btnCloseCamera.getLayoutParams();
            layoutParams.setMargins(width, 0, 0, dip2px(CallActivity.this,16));
            btnCloseCamera.setLayoutParams(layoutParams);
            ViewGroup.MarginLayoutParams  mutelayoutParams= (ViewGroup.MarginLayoutParams) btnMuteMic.getLayoutParams();
            mutelayoutParams.setMargins(0, 0, width, dip2px(CallActivity.this,16));
            btnMuteMic.setLayoutParams(mutelayoutParams);
            //
            if(null!=horizontalScrollView){
                if(horizontalScrollView.getChildCount()>0){
                    horizontalScrollView.removeAllViews();
                }
                horizontalScrollView.setVisibility(View.GONE);
            }
            if(null!=scrollView){
                if(scrollView.getChildCount()>0){
                    scrollView.removeAllViews();
                }
                scrollView.setVisibility(View.VISIBLE);
                call_reder_container.setOrientation(LinearLayout.VERTICAL);
                scrollView.addView(call_reder_container);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 竖屏View改变
     */
    private void verticalScreenViewInit(){
        initeBoottombtn();
        //
        RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) rel_sv.getLayoutParams();
        lp3.addRule(RelativeLayout.BELOW,call_layout_title.getId());

        if(null!=scrollView){
            if(scrollView.getChildCount()>0){
                scrollView.removeAllViews();
            }
            scrollView.setVisibility(View.GONE);
        }
        if(null!=horizontalScrollView){
            if(horizontalScrollView.getChildCount()>0){
                horizontalScrollView.removeAllViews();
            }
            horizontalScrollView.addView(call_reder_container);
            horizontalScrollView.setVisibility(View.VISIBLE);
            call_reder_container.setOrientation(LinearLayout.HORIZONTAL);
        }
    }
}
