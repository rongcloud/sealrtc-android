package cn.rongcloud.rtc;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.callback.RongRTCDataResultCallBack;
import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.engine.report.StatusBean;
import cn.rongcloud.rtc.engine.report.StatusReport;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.entity.ResolutionInfo;
import cn.rongcloud.rtc.entity.RongRTCDeviceType;
import cn.rongcloud.rtc.entity.UserInfo;
import cn.rongcloud.rtc.events.RongRTCEventsListener;
import cn.rongcloud.rtc.events.RongRTCStatusReportListener;
import cn.rongcloud.rtc.events.RongRTCVideoFrameListener;
import cn.rongcloud.rtc.message.RoomInfoMessage;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.MediaType;
import cn.rongcloud.rtc.stream.ResourceState;
import cn.rongcloud.rtc.stream.local.RongRTCAVOutputStream;
import cn.rongcloud.rtc.stream.local.RongRTCCapture;
import cn.rongcloud.rtc.stream.remote.RongRTCAVInputStream;
import cn.rongcloud.rtc.user.RongRTCLocalUser;
import cn.rongcloud.rtc.user.RongRTCRemoteUser;
import cn.rongcloud.rtc.util.AssetsFilesUtil;
import cn.rongcloud.rtc.util.BluetoothUtil;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.HeadsetPlugReceiver;
import cn.rongcloud.rtc.util.OnHeadsetPlugListener;
import cn.rongcloud.rtc.util.RongRTCPopupWindow;
import cn.rongcloud.rtc.util.RongRTCTalkTypeUtil;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.MessageContent;

import static cn.rongcloud.rtc.SettingActivity.IS_GPUIMAGEFILTER;
import static cn.rongcloud.rtc.util.Utils.parseTimeSeconds;

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends RongRTCBaseActivity implements View.OnClickListener, OnHeadsetPlugListener, RongRTCEventsListener ,RongRTCStatusReportListener,RongRTCVideoFrameListener{
    private static String TAG = "CallActivity";
    private boolean isShowAutoTest;
    private AlertDialog ConfirmDialog = null;
    private String deviceId = "";

    public static final String EXTRA_ROOMID = "blinktalk.io.ROOMID";
    public static final String EXTRA_USER_NAME = "blinktalk.io.USER_NAME";
    public static final String EXTRA_CAMERA = "blinktalk.io.EXTRA_CAMERA";
    public static final String EXTRA_OBSERVER = "blinktalk.io.EXTRA_OBSERVER";
    public static final String EXTRA_ONLY_PUBLISH_AUDIO = "ONLY_PUBLISH_AUDIO";
    public static final String EXTRA_AUTO_TEST = "EXTRA_AUTO_TEST";
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
    private boolean isInRoom;

    private AppRTCAudioManager audioManager = null;
    private boolean isVideoMute;
    private boolean isObserver;
    private boolean canOnlyPublishAudio;
    Handler networkSpeedHandler;
    // Controls
    private String roomId = "", iUserName = "";
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
    private LinearLayout titleContainer;
    private LinearLayout mcall_more_container;
    private WebView whiteboardView;
    private RelativeLayout mRelativeWebView;
    private boolean isGPUImageFliter = false;
    private Handler handler = new Handler();
    private DebugInfoAdapter debugInfoAdapter;
    private ListView debugInfoListView;
    private TextView biteRateSendView;
    private TextView biteRateRcvView;
    private TextView rttSendView;
    private ProgressDialog progressDialog;
    private RongRTCPopupWindow popupWindow;
    private LinearLayout call_reder_container;
    private int sideBarWidth = 0;
    private AppCompatCheckBox btnSwitchCamera;
    private AppCompatCheckBox btnMuteSpeaker;
    private AppCompatCheckBox getBtnWhiteBoard;
    private AppCompatCheckBox btnCloseCamera;
    private AppCompatCheckBox btnMuteMic;
    private AppCompatCheckBox btnRaiseHand;
    private AppCompatCheckBox btnChangeResolution_up;
    private AppCompatCheckBox btnChangeResolution_down;
    private AppCompatCheckBox btnWhiteBoard;
    private ImageButton btnMembers;
    private ImageView iv_modeSelect;
    private List<MembersDialog.ItemModel> mMembers = new ArrayList<>();
    private Map<String, UserInfo> mMembersMap = new HashMap<>();

    /**
     * UpgradeToNormal邀请观察者发言,将观察升级为正常用户=0, 摄像头:1 麦克风:2
     **/
    Map<Integer, ActionState> stateMap = new LinkedHashMap<>();
    /**
     * 存储用户是否开启分享
     **/
    private HashMap<String, Boolean> sharingMap = new HashMap<>();

    /**
     * true  关闭麦克风,false 打开麦克风
     */
    private boolean muteMicrophone = false;
    private ScrollView scrollView;
    private HorizontalScrollView horizontalScrollView;
    private RelativeLayout rel_sv;//sv父布局
    GPUImageBeautyFilter beautyFilter;
    private String myUserId;

    private RongRTCRoom rongRTCRoom;
    private RongRTCLocalUser localUser;

    private HeadsetPlugReceiver headsetPlugReceiver = null;
    private boolean HeadsetPlugReceiverState = false;//false：开启音视频之前已经连接上耳机

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HeadsetPlugReceiver.setOnHeadsetPlugListener(this);
        if (BluetoothUtil.isSupportBluetooth()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            headsetPlugReceiver = new HeadsetPlugReceiver();
            registerReceiver(headsetPlugReceiver, intentFilter);
        }

        sideBarWidth = dip2px(CallActivity.this, 40) + 75;

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
        roomId = intent.getStringExtra(EXTRA_ROOMID);
        iUserName = intent.getStringExtra(EXTRA_USER_NAME);
        isVideoMute = intent.getBooleanExtra(EXTRA_CAMERA, false);
        isObserver = intent.getBooleanExtra(EXTRA_OBSERVER, false);
        isShowAutoTest= intent.getBooleanExtra(EXTRA_AUTO_TEST,false);
        canOnlyPublishAudio = intent.getBooleanExtra(EXTRA_ONLY_PUBLISH_AUDIO, false);
        //设置是否启用美颜模式
        isGPUImageFliter = SessionManager.getInstance(this).getBoolean(IS_GPUIMAGEFILTER);
        if (TextUtils.isEmpty(roomId)) {
            Log.e(TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        myUserId = RongIMClient.getInstance().getCurrentUserId();
//        iUserName = myUserId;
        initAudioManager();
        initViews(intent);
        checkPermissions();
        initBottomBtn();
        initRemoteScrollView();
        if (rongRTCRoom == null) {
            return;
        }
        rongRTCRoom.getRoomAttributes(null, new RongRTCDataResultCallBack<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> data) {
                try {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        JSONObject jsonObject = new JSONObject(entry.getValue());
                        UserInfo userInfo = new UserInfo();
                        userInfo.userName = jsonObject.getString("userName");
                        userInfo.joinMode = jsonObject.getInt("joinMode");
                        userInfo.userId = jsonObject.getString("userId");
                        userInfo.timestamp = jsonObject.getLong("joinTime");
                        mMembersMap.put(entry.getKey(), userInfo);

                        MembersDialog.ItemModel model = new MembersDialog.ItemModel();
                        model.mode = mapMode(userInfo.joinMode);
                        model.name = userInfo.userName;
                        model.userId = userInfo.userId;
                        model.joinTime = userInfo.timestamp;
                        mMembers.add(model);

                        List<VideoViewManager.RenderHolder> holders = renderViewManager.getViewHolderByUserId(entry.getKey());
                        for (VideoViewManager.RenderHolder holder : holders) {
                            if (TextUtils.equals(entry.getKey(), myUserId)) {
                                holder.updateUserInfo(getResources().getString(R.string.room_actor_me));
                            } else {
                                holder.updateUserInfo(model.name);
                            }
                        }
                        setWaitingTipsVisiable(mMembers.size() <= 1);
                    }

                    Collections.sort(mMembers, new Comparator<MembersDialog.ItemModel>() {
                        @Override
                        public int compare(MembersDialog.ItemModel o1, MembersDialog.ItemModel o2) {
                            return (int) (o2.joinTime - o1.joinTime);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {

            }
        });
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
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            horizontalScreenViewInit();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            verticalScreenViewInit();
        }
        if (renderViewManager != null && null != unGrantedPermissions && unGrantedPermissions.size() == 0) {
            renderViewManager.rotateView();
            if (mRelativeWebView.getVisibility() == View.VISIBLE)
                loadWhiteBoard(null, true);
        }
    }

    /**
     * 初始化底部按钮 默认竖屏
     **/
    private void initBottomBtn() {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) btnCloseCamera.getLayoutParams();
        layoutParams.setMargins(dip2px(CallActivity.this, 50), 0, 0, dip2px(CallActivity.this, 16));
        btnCloseCamera.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams mutelayoutParams = (ViewGroup.MarginLayoutParams) btnMuteMic.getLayoutParams();
        mutelayoutParams.setMargins(0, 0, dip2px(CallActivity.this, 50), dip2px(CallActivity.this, 16));
        btnMuteMic.setLayoutParams(mutelayoutParams);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    private void initViews(Intent intent) {
        mcall_more_container = (LinearLayout) findViewById(R.id.call_more_container);
        iv_modeSelect = (ImageView) findViewById(R.id.btn_modeSelect);
        btnRaiseHand = (AppCompatCheckBox) findViewById(R.id.menu_request_to_normal);
        btnSwitchCamera = (AppCompatCheckBox) findViewById(R.id.menu_switch);
        btnMuteSpeaker = (AppCompatCheckBox) findViewById(R.id.menu_mute_speaker);
        btnWhiteBoard = (AppCompatCheckBox) findViewById(R.id.menu_whiteboard);
        btnChangeResolution_up = (AppCompatCheckBox) findViewById(R.id.menu_up);
        btnChangeResolution_down = (AppCompatCheckBox) findViewById(R.id.menu_down);
        titleContainer = (LinearLayout) findViewById(R.id.call_layout_title);
        call_reder_container = (LinearLayout) findViewById(R.id.call_reder_container);
        biteRateSendView = (TextView) findViewById(R.id.debug_info_bitrate_send);
        biteRateRcvView = (TextView) findViewById(R.id.debug_info_bitrate_rcv);
        rttSendView = (TextView) findViewById(R.id.debug_info_rtt_send);
        debugInfoListView = (ListView) findViewById(R.id.debug_info_list);
        textViewRoomNumber = (TextView) findViewById(R.id.call_room_number);
        textViewTime = (TextView) findViewById(R.id.call_time);
        textViewNetSpeed = (TextView) findViewById(R.id.call_net_speed);
        buttonHangUp = (Button) findViewById(R.id.call_btn_hangup);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        btnCloseCamera = (AppCompatCheckBox) findViewById(R.id.menu_close);
        btnMuteMic = (AppCompatCheckBox) findViewById(R.id.menu_mute_mic);
        waitingTips = (LinearLayout) findViewById(R.id.call_waiting_tips);
        mRelativeWebView = (RelativeLayout) findViewById(R.id.call_whiteboard);
        btnMembers = (ImageButton) findViewById(R.id.menu_members);
        if (BuildConfig.DEBUG && null != btnChangeResolution_up) {
            btnChangeResolution_up.setVisibility(View.GONE);
        } else {
            btnChangeResolution_up.setVisibility(View.GONE);
        }
        if (BuildConfig.DEBUG && null != btnChangeResolution_down) {
            btnChangeResolution_down.setVisibility(View.GONE);
        } else {
            btnChangeResolution_down.setVisibility(View.GONE);
        }
        //

        debugInfoAdapter = new DebugInfoAdapter(this);
        debugInfoListView.setAdapter(debugInfoAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("白板加载中...");

        rel_sv = (RelativeLayout) findViewById(R.id.rel_sv);

        iv_modeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ButtentSolp.check(view.getId(), 500)) {
                    return;
                }
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                } else {
                    showPopupWindow();
                }
            }
        });
        toggleCameraMicViewStatus();
        whiteboardView = new WebView(getApplicationContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        whiteboardView.setLayoutParams(params);
        mRelativeWebView.addView(whiteboardView);
        WebSettings settings = whiteboardView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);//启用内置的缩放算法
        settings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT > 18) {//host674
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        } else {
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
        buttonHangUp.setOnClickListener(this);
        btnSwitchCamera.setOnClickListener(this);
        btnCloseCamera.setOnClickListener(this);
        btnMuteMic.setOnClickListener(this);
        btnMuteSpeaker.setOnClickListener(this);
        btnWhiteBoard.setOnClickListener(this);
        btnMembers.setOnClickListener(this);
        btnRaiseHand.setOnClickListener(this);
        waitingTips.setOnClickListener(this);
        btnChangeResolution_up.setOnClickListener(this);
        btnChangeResolution_down.setOnClickListener(this);
        renderViewManager.setOnLocalVideoViewClickedListener(new VideoViewManager.OnLocalVideoViewClickedListener() {
            @Override
            public void onClick() {
                toggleActionButtons(buttonHangUp.getVisibility() == View.VISIBLE);
            }
        });

        if (isObserver){
            btnMuteMic.setChecked(true);
            btnMuteMic.setEnabled(false);
            btnCloseCamera.setChecked(true);
            btnCloseCamera.setEnabled(false);
        }
        if (isVideoMute) {
            btnCloseCamera.setChecked(true);
            btnCloseCamera.setEnabled(false);
        }

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
        disconnect();
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
//                            RongRTCEngine.getInstance().muteMicrophone(true);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (RongRTCEngine.getInstance() != null) {
//                            RongRTCEngine.getInstance().muteMicrophone(false);
                        }
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void setWaitingTipsVisiable(boolean visiable) {
//        FinLog.i(TAG,"setWaitingTipsVisiable() visiable = "+visiable);
        if (visiable) {
            visiable = !(mMembers != null && mMembers.size() > 1);
        }
        int tmp = waitingTips.getVisibility();
        if (visiable) {
            if (tmp != View.VISIBLE)
                handler.removeCallbacks(timeRun);
            waitingTips.setVisibility(View.VISIBLE);
            initUIForWaitingStatus();
        } else {
            waitingTips.setVisibility(View.GONE);
            if (tmp == View.VISIBLE) {
                handler.postDelayed(timeRun, 1000);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearState();
    }

    private void destroyPopupWindow() {
        if (null != popupWindow && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyPopupWindow();
        if (headsetPlugReceiver != null) {
            unregisterReceiver(headsetPlugReceiver);
            headsetPlugReceiver = null;
        }
        HeadsetPlugReceiverState = false;
        if(rongRTCRoom!=null){
            rongRTCRoom.unRegisterEventsListener(this);
            rongRTCRoom.unRegisterStatusReportListener(this);
            rongRTCRoom.unRegisterVideoFrameListener(this);
        }
        if (isConnected) {
            if (RongRTCEngine.getInstance() != null)
                if (rongRTCRoom != null) {
                    rongRTCRoom.deleteRoomAttributes(Arrays.asList(myUserId), null, null);
                }
                RongRTCEngine.getInstance().quitRoom(roomId, new RongRTCResultUICallBack() {
                    @Override
                    public void onUiSuccess() {
                        isInRoom = false;
                    }

                    @Override
                    public void onUiFailed(RTCErrorCode errorCode) {
                    }
                });
            if (renderViewManager != null)
//                renderViewManager.destroyViews();
                if (audioManager != null) {
                    audioManager.close();
                    audioManager = null;
                }
        }
        if (handler != null) {
            handler.removeCallbacks(memoryRunnable);
            handler.removeCallbacks(timeRun);
        }
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
//        RongRTCEngine.getInstance().switchCamera();
    }

    /**
     * 摄像头开关
     *
     * @param closed true  关闭摄像头
     *               false 打开摄像头
     * @return
     * @isActive true：主動
     */
    public boolean onCameraClose(boolean closed) {
        Log.i(TAG, "onCameraClose closed = " + closed);
        this.isVideoMute = closed;
        RongRTCCapture.getInstance().muteLocalVideo(closed);
        if (renderViewManager != null) {
            renderViewManager.updateTalkType(myUserId, RongRTCCapture.getInstance().getTag(), closed ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
        }
        toggleCameraMicViewStatus();
        return isVideoMute;
    }

    public void onToggleMic(boolean mute) {
        muteMicrophone = mute;
        RongRTCCapture.getInstance().muteMicrophone(muteMicrophone);
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
    private String encryptFilePath = new StringBuffer().append(Environment.getExternalStorageDirectory().toString() + File.separator).append("Blink").append(File.separator).append("EncryptData").toString();
    RongRTCVideoView localSurface;

    private void startCall() {
        try {
            renderViewManager.initViews(this, isObserver);
            if (!isObserver) {
                localSurface = RongRTCEngine.getInstance().createVideoView(getApplicationContext());
                renderViewManager.userJoin(myUserId, RongRTCCapture.getInstance().getTag(), iUserName, isVideoMute ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
                renderViewManager.setVideoView(true, myUserId, RongRTCCapture.getInstance().getTag(), iUserName, localSurface, isVideoMute ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
            }

            rongRTCRoom = CenterManager.getInstance().getRongRTCRoom();
            rongRTCRoom.registerEventsListener(CallActivity.this);
            rongRTCRoom.registerStatusReportListener(CallActivity.this);
            rongRTCRoom.registerVideoFrameListener(CallActivity.this);
            localUser = rongRTCRoom.getLocalUser();
            renderViewManager.setRongRTCRoom(rongRTCRoom);
            RongRTCCapture.getInstance().setRongRTCVideoView(localSurface);//设置本地view
            RongRTCCapture.getInstance().muteLocalVideo(isVideoMute);
            RongRTCCapture.getInstance().startCameraCapture();
            publishResource();//发布资源
            addAllVideoView();
            subscribeAll();

            if (!HeadsetPlugReceiverState) {
                int type = -1;
                if (BluetoothUtil.hasBluetoothA2dpConnected()) {
                    type = 0;
                } else if (BluetoothUtil.isWiredHeadsetOn(CallActivity.this)) {
                    type = 1;
                }
                if (type != -1) {
                    onNotifyHeadsetState(true, type);
                }
            }
            isInRoom = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAllVideoView() {
        Map<String, RongRTCRemoteUser> map = rongRTCRoom.getRemoteUsers();
        if (map != null) {
            for (RongRTCRemoteUser remoteUser : map.values()) {
                addNewRemoteView(remoteUser);//准备view
            }
        }
    }

    private void publishResource() {
        if (isObserver) {
            return;
        }
        if (localUser == null) {
            Toast.makeText(CallActivity.this, "不在房间里", Toast.LENGTH_SHORT).show();
            return;
        }

        List<RongRTCAVOutputStream> localAvStreams = localUser.getLocalAvStreams();
        if (isVideoMute) {
            for (RongRTCAVOutputStream stream : localAvStreams) {
                if (stream.getMediaType() == MediaType.VIDEO) {
                    stream.setResourceState(ResourceState.DISABLED);
                }

            }
        }
        localUser.publishDefaultAVStream(new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                FinLog.i(TAG,"publish success()");
            }

            @Override
            public void onUiFailed(RTCErrorCode errorCode) {
                FinLog.i(TAG,"publish publish Failed()");
            }
        });
    }


    private int getMaxBitRate() {
        int bitRate = 500;
        String maxBitRate = SessionManager.getInstance(this).getString(SettingActivity.BIT_RATE_MAX);
        if (!TextUtils.isEmpty(maxBitRate) && maxBitRate.length() > 4) {
            bitRate = Integer.valueOf(maxBitRate.substring(0, maxBitRate.length() - 4));
            FinLog.i(TAG, "BIT_RATE_MAX=" + bitRate);
        }
        return bitRate;
    }

    private int getMinBitRate() {
        int bitRate = 100;
        String minBitRate = SessionManager.getInstance(this).getString(SettingActivity.BIT_RATE_MIN);
        if (!TextUtils.isEmpty(minBitRate) && minBitRate.length() > 4) {
            bitRate = Integer.valueOf(minBitRate.substring(0, minBitRate.length() - 4));
            FinLog.i(TAG, "BIT_RATE_MIN=" + bitRate);
        }
        return bitRate;
    }

    private RongRTCConfig.RongRTCVideoCodecs getVideoCodec() {
        //set codecs
        String codec = SessionManager.getInstance(this).getString(SettingActivity.CODECS);
        if (!TextUtils.isEmpty(codec)) {
            if ("VP8".equals(codec)) {
                return RongRTCConfig.RongRTCVideoCodecs.VP8;
            }
        }
        return RongRTCConfig.RongRTCVideoCodecs.H264;
    }

    private RongRTCConfig.RongRTCVideoProfile getVideoProfile() {
        //set resolution
        String resolution = SessionManager.getInstance(this).getString(SettingActivity.RESOLUTION);
        String fps = SessionManager.getInstance(this).getString(SettingActivity.FPS);
        RongRTCConfig.RongRTCVideoProfile videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_2;
        if (SettingActivity.RESOLUTION_LOW.equals(resolution)) {
            if ("15".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_15f;
            } else if ("24".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_24f;
            } else if ("30".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_30f;
            }
        } else if (SettingActivity.RESOLUTION_MEDIUM.equals(resolution)) {
            if ("15".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_1;
            } else if ("24".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_24f_1;
            } else if ("30".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_30f_1;
            }
        } else if (SettingActivity.RESOLUTION_HIGH.equals(resolution)) {
            if ("15".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_15f;
            } else if ("24".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_24f;
            } else if ("30".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_30f;
            }
        } else if (SettingActivity.RESOLUTION_SUPER.equals(resolution)) {
            if ("15".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_15f;
            } else if ("24".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_24f;
            } else if ("30".equals(fps)) {
                videoProfile = RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_1080P_30f;
            }
        }
        return videoProfile;
    }

    private String getAudioRecordFilePath() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/blink/audio_recording/";
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return path + System.currentTimeMillis() + ".wav";
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
                    }
                    super.handleMessage(msg);
                }
            };
//        startTenSecondsTimer();
    }


    private Runnable timeRun = new Runnable() {
        @Override
        public void run() {
            if (waitingTips != null && waitingTips.getVisibility() != View.VISIBLE) {
                updateTimer();
                handler.postDelayed(timeRun, 1000);
            }
        }
    };

    private int time = 0;

    private void updateTimer() {
        time++;
        textViewTime.setText(parseTimeSeconds(time));
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

    @Override
    public void onRemoteUserPublishResource(RongRTCRemoteUser remoteUser, List<RongRTCAVInputStream> publishResource) {
        FinLog.i(TAG, "onPublishResource remoteUser: " + remoteUser);
        if (remoteUser == null) return;
        alertRemotePublished(remoteUser);
        updateResourceVideoView(remoteUser);
    }

    @Override
    public void onRemoteUserAudioStreamMute(RongRTCRemoteUser remoteUser, RongRTCAVInputStream avInputStream, boolean mute) {

    }

    @Override
    public void onRemoteUserVideoStreamEnabled(RongRTCRemoteUser remoteUser, RongRTCAVInputStream avInputStream, boolean enable) {
        if (remoteUser == null || avInputStream == null) {
            return;
        }
        updateVideoView(remoteUser, avInputStream, enable);
    }

    @Override
    public void onRemoteUserUnPublishResource(RongRTCRemoteUser remoteUser, List<RongRTCAVInputStream> unPublishResource) {
        if (unPublishResource != null) {
            for (RongRTCAVInputStream stream : unPublishResource) {
                if (stream.getMediaType().equals(MediaType.VIDEO)) {
                    renderViewManager.removeVideoView(stream.getUserId(), stream.getTag());
                }
            }
        }
    }

    @Override
    public void onUserJoined(RongRTCRemoteUser remoteUser) {
        Toast.makeText(CallActivity.this, remoteUser.getUserId() + " " + getResources().getString(R.string.rtc_join_room), Toast.LENGTH_SHORT).show();
        if (mMembers.size() > 1) {
            setWaitingTipsVisiable(false);
        }
        //renderViewManager.userJoin(remoteUser.getUserId(), remoteUser.getUserId(), RongRTCTalkTypeUtil.O_CAMERA);
    }

    @Override
    public void onUserLeft(RongRTCRemoteUser remoteUser) {
        Toast.makeText(CallActivity.this, remoteUser.getUserId() + " " + getResources().getString(R.string.rtc_quit_room), Toast.LENGTH_SHORT).show();
        exitRoom(remoteUser.getUserId());
        if (mMembers.size() <= 1) {
            setWaitingTipsVisiable(true);
        }
    }

    @Override
    public void onUserOffline(RongRTCRemoteUser remoteUser) {
        Toast.makeText(CallActivity.this, remoteUser.getUserId() + " " + getResources().getString(R.string.rtc_user_offline), Toast.LENGTH_SHORT).show();
        exitRoom(remoteUser.getUserId());
    }

    @Override
    public void onVideoTrackAdd(String userId, String tag) {
        Log.i(TAG, "onVideoTrackAdd() userId: " + userId + " ,tag = " + tag);
        if(isShowAutoTest){ //自动化测试会有红点
            renderViewManager.onTrackadd(userId, tag);
        }
    }

    @Override
    public void onFirstFrameDraw(String userId, String tag) {
        Log.i(TAG, "onFirstFrameDraw() userId: " + userId + " ,tag = " + tag);
        if(isShowAutoTest){
            renderViewManager.onFirstFrameDraw(userId, tag);
        }
    }

    @Override
    public void onLeaveRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
        builder.setTitle("异常退出").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).show();
    }

    @Override
    public void onReceiveMessage(io.rong.imlib.model.Message message) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof RoomInfoMessage) {
            RoomInfoMessage roomInfoMessage = (RoomInfoMessage) messageContent;
            MembersDialog.ItemModel itemModel = new MembersDialog.ItemModel();
            itemModel.name = roomInfoMessage.getUserName();
            itemModel.mode = mapMode(roomInfoMessage.getJoinMode());
            itemModel.userId = roomInfoMessage.getUserId();
            if (!mMembersMap.containsKey(itemModel.userId)) {
                mMembers.add(0, itemModel);
            }

            UserInfo userInfo = new UserInfo();
            userInfo.userId = roomInfoMessage.getUserId();
            userInfo.userName = roomInfoMessage.getUserName();
            userInfo.joinMode = roomInfoMessage.getJoinMode();
            userInfo.timestamp = roomInfoMessage.getJoinMode();
            mMembersMap.put(roomInfoMessage.getUserId(), userInfo);

            List<VideoViewManager.RenderHolder> holders = renderViewManager.getViewHolderByUserId(roomInfoMessage.getUserId());
            for (VideoViewManager.RenderHolder holder : holders) {
                holder.updateUserInfo(roomInfoMessage.getUserName());
            }
            updateMembersDialog();
            if (mMembers.size() > 1) {
                setWaitingTipsVisiable(false);
            }
        }
    }

    private String mapMode(int mode) {
        if (mode == RoomInfoMessage.JoinMode.AUDIO) {
            return getString(R.string.mode_audio);
        } else if (mode == RoomInfoMessage.JoinMode.AUDIO_VIDEO) {
            return getString(R.string.mode_audio_video);
        } else if (mode == RoomInfoMessage.JoinMode.OBSERVER) {
            return getString(R.string.mode_observer);
        }
        return "";
    }

    @Override
    public void onAudioReceivedLevel(HashMap<String, String> audioLevel) {
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

    @Override
    public void onAudioInputLevel(String audioLevel) {
        if (localUser == null)
            return;
        int val = 0;
        try {
            val = TextUtils.isEmpty(audioLevel) ? 0 : Integer.valueOf(audioLevel);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        audiolevel(val, localUser.getDefaultAudioStream().getMediaId());
    }

    @Override
    public void onConnectionStats(final StatusReport statusReport) {
        if (mMembers != null && mMembers.size() > 1) {
            updateNetworkSpeedInfo(statusReport);
        } else {
            initUIForWaitingStatus();
        }

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


    private void updateResourceVideoView(RongRTCRemoteUser remoteUser) {
        for (RongRTCAVInputStream rongRTCAVOutputStream : remoteUser.getRemoteAVStreams()) {
            ResourceState state = rongRTCAVOutputStream.getResourceState();
            if (rongRTCAVOutputStream.getMediaType() == MediaType.VIDEO && renderViewManager != null) {
                FinLog.i(TAG, "updateResourceVideoView userId = " + remoteUser.getUserId() + " state = " + state);
                renderViewManager.updateTalkType(remoteUser.getUserId(),rongRTCAVOutputStream.getTag(), state == ResourceState.DISABLED ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
                rongRTCAVOutputStream.setVideoDisplayRenderer(rongRTCAVOutputStream.getResourceState() == ResourceState.NORMAL);
            }
        }
    }

    private void updateVideoView(RongRTCRemoteUser remoteUser, RongRTCAVInputStream rongRTCAVInputStream, boolean enable) {
        if (renderViewManager != null) {
            FinLog.i(TAG, "updateVideoView userId = " + remoteUser.getUserId() + " state = " + enable);
            renderViewManager.updateTalkType(remoteUser.getUserId(), rongRTCAVInputStream.getTag(), enable ? RongRTCTalkTypeUtil.O_CAMERA : RongRTCTalkTypeUtil.C_CAMERA);
            rongRTCAVInputStream.setVideoDisplayRenderer(enable);
        }
    }

    private void alertRemotePublished(final RongRTCRemoteUser remoteUser) {
        Log.i(TAG, "alertRemotePublished() start");
        addNewRemoteView(remoteUser);
        remoteUser.subscribeAvStream(remoteUser.getRemoteAVStreams(), new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {

            }

            @Override
            public void onUiFailed(RTCErrorCode errorCode) {

            }
        });

    }

    private void subscribeAll() {
        if (rongRTCRoom == null || rongRTCRoom.getRemoteUsers() == null) {
            return;
        }

        for (final RongRTCRemoteUser remoteUser : rongRTCRoom.getRemoteUsers().values()) {
            remoteUser.subscribeAvStream(remoteUser.getRemoteAVStreams(), new RongRTCResultUICallBack() {
                @Override
                public void onUiSuccess() {
                    updateResourceVideoView(remoteUser);
                }

                @Override
                public void onUiFailed(RTCErrorCode errorCode) {

                }
            });
        }
    }

    private void addNewRemoteView(RongRTCRemoteUser remoteUser) {
        List<RongRTCAVInputStream> videoStreamList = new ArrayList<>();
        List<RongRTCAVInputStream> remoteAVStreams = remoteUser.getRemoteAVStreams();
        RongRTCAVInputStream audioStream = null;
        for (RongRTCAVInputStream inputStream : remoteAVStreams) {
            if (inputStream.getMediaType() == MediaType.VIDEO) {
                videoStreamList.add(inputStream);
            }else if (inputStream.getMediaType() == MediaType.AUDIO){
                if (inputStream.getRongRTCVideoView() != null){
                    renderViewManager.removeVideoView(remoteUser.getUserId());
                }
                audioStream = inputStream;
            }
        }
        //只有音频流，没有视频流时增加占位
        if (videoStreamList.isEmpty() && audioStream != null){
            videoStreamList.add(audioStream);
        }

        if (videoStreamList.size() > 0) {
            for (RongRTCAVInputStream videoStream : videoStreamList) {
                if (videoStream != null && videoStream.getRongRTCVideoView() == null) {
                    FinLog.i(TAG, "addNewRemoteView");
                    if (!renderViewManager.hasConnectedUser()) {
                        startCalculateNetSpeed();
                    }
                    UserInfo userInfo = mMembersMap.get(remoteUser.getUserId());
                    String userName = "";
                    if (userInfo != null) {
                        userName = userInfo.userName;
                    }
                    renderViewManager.userJoin(remoteUser.getUserId(), videoStream.getTag(), userName, RongRTCTalkTypeUtil.O_CAMERA);
                    RongRTCVideoView remoteView = RongRTCEngine.getInstance().createVideoView(CallActivity.this.getApplicationContext());
                    renderViewManager.setVideoView(false, videoStream.getUserId(), videoStream.getTag(), remoteUser.getUserId(), remoteView, "");
                    videoStream.setRongRTCVideoView(remoteView);
                }
            }
        }
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
                if (null != btnWhiteBoard && btnWhiteBoard.isChecked() == true)
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
                    if (Build.VERSION.SDK_INT > 18) {//HOST-674
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
        ClipData data = ClipData.newPlainText("meeting", String.format(getResources().getString(R.string.meeting_control_invite_url), roomId, url));
        mClipboardManager.setPrimaryClip(data);
    }

    private void toastMessage(String message) {
        //Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialize the UI to "waiting user join" IMConnectionStatus
     */
    private void initUIForWaitingStatus() {
        if (time != 0) {
            textViewTime.setText(getResources().getText(R.string.connection_duration));
            textViewNetSpeed.setText(getResources().getText(R.string.network_traffic));
        }
        time = 0;
    }

    private void disconnect() {
        isConnected = false;
        if (rongRTCRoom != null) {
            rongRTCRoom.deleteRoomAttributes(Arrays.asList(myUserId), null, null);
        }
        RongRTCEngine.getInstance().quitRoom(roomId, new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                isInRoom = false;
                Toast.makeText(CallActivity.this, getResources().getString(R.string.quit_room_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUiFailed(RTCErrorCode errorCode) {
            }
        });
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
        FinLog.e(TAG, "max Memory:" + Long.toString(maxMemory / (1024 * 1024)));
        FinLog.e(TAG, "free Memory:" + rt.freeMemory() / (1024 * 1024) + "m");
        FinLog.e(TAG, "total Memory:" + rt.totalMemory() / (1024 * 1024) + "m");
        FinLog.e(TAG, "系统是否处于低Memory运行：" + info.lowMemory);
        FinLog.e(TAG, "当系统剩余Memory低于" + (info.threshold >> 10) / 1024 + "m时就看成低内存运行");
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

    private String blinkTalkType(boolean isOpen, RongRTCDeviceType dType) {
        String talkType = "";
        if (isOpen) {
            if (dType == RongRTCDeviceType.Camera) {
                talkType = RongRTCTalkTypeUtil.O_CAMERA;
            } else if (dType == RongRTCDeviceType.Microphone) {
                talkType = RongRTCTalkTypeUtil.O_MICROPHONE;
            } else if (dType == RongRTCDeviceType.CameraAndMicrophone) {
                talkType = RongRTCTalkTypeUtil.O_CM;
            }
        } else {//
            if (dType == RongRTCDeviceType.Camera) {
                talkType = RongRTCTalkTypeUtil.C_CAMERA;
            } else if (dType == RongRTCDeviceType.Microphone) {
                talkType = RongRTCTalkTypeUtil.C_MICROPHONE;
            } else if (dType == RongRTCDeviceType.CameraAndMicrophone) {
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
     * @param config return true:横屏
     */
    private boolean screenCofig(Configuration config) {
        boolean screen = false;//默认竖屏
        try {
            Configuration configuration = null;
            if (config == null) {
                configuration = this.getResources().getConfiguration();
            } else {
                configuration = config;
            }
            int ori = configuration.orientation;
            if (ori == configuration.ORIENTATION_LANDSCAPE) {
                screen = true;
            } else if (ori == configuration.ORIENTATION_PORTRAIT) {
                screen = false;
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
        String currentUserId = "";
        long minTimestamp = Long.MAX_VALUE;
        for (Map.Entry<String, UserInfo> entry : mMembersMap.entrySet()) {
            UserInfo userInfo = entry.getValue();
            if (userInfo.timestamp < minTimestamp && !TextUtils.equals(userInfo.userId, userId)) {
                minTimestamp = userInfo.timestamp;
                currentUserId = userInfo.userId;
            }
        }
        mMembersMap.remove(userId);
        for (int i = mMembers.size() - 1; i >= 0; --i) {
            MembersDialog.ItemModel model = mMembers.get(i);
            if (TextUtils.equals(model.userId, userId)) {
                mMembers.remove(i);
                break;
            }
        }
        updateMembersDialog();
        if (TextUtils.equals(currentUserId, myUserId)) {
            rongRTCRoom.deleteRoomAttributes(Arrays.asList(userId), null, null);
        }
    }

    /*--------------------------------------------------------------------------切换分辨率---------------------------------------------------------------------------*/

    private Map<String, ResolutionInfo> changeResolutionMap = null;
    private String[] resolution;

//    private void setChangeResolutionMap() {
//        ResolutionInfo info = null;
//        changeResolutionMap = new HashMap<>();
//        String key = "";
//        resolution = new String[]{CR_144x256, CR_240x320, CR_368x480, CR_368x640, CR_480x640, CR_480x720, CR_720x1280, CR_1080x1920};
//        try {
//            for (int i = 0; i < resolution.length; i++) {
//                key = resolution[i];
//                info = new ResolutionInfo(key, i);
//                changeResolutionMap.put(key, info);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    // String fpsStr = SessionManager.getInstance(this).getString(SettingActivity.FPS);

//    private void changeVideoSize(String action) {
//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append(RongRTCEngine.getInstance().getRTCConfig().getVideoWidth());
//        stringBuffer.append("x").append(RongRTCEngine.getInstance().getRTCConfig().getVideoHeight());
//        String resolutionStr = stringBuffer.toString();
//        int index = -1;
//
//        try {
//            if (changeResolutionMap.containsKey(resolutionStr)) {
//                index = changeResolutionMap.get(resolutionStr).getIndex();
//            }
//            if (action.equals("down")) {
//                if (index != 0) {
//                    String str = resolution[index - 1];
//                    RongRTCConfig.RongRTCVideoProfile profile = selectiveResolution(str, "15");
//                    RongRTCEngine.getInstance().changeVideoSize(profile);
//                } else {
//                    Toast.makeText(CallActivity.this, R.string.resolutionmunimum, Toast.LENGTH_SHORT).show();
//                }
//            } else if (action.equals("up")) {
//                if (index != 7) {
//                    String str = resolution[index + 1];
//                    RongRTCConfig.RongRTCVideoProfile profile = selectiveResolution(str, "30");
//                    RongRTCEngine.getInstance().changeVideoSize(profile);
//                } else {
//                    Toast.makeText(CallActivity.this, R.string.resolutionhighest, Toast.LENGTH_SHORT).show();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            FinLog.i(TAG, "error：" + e.getMessage());
//        }
//    }

    /*--------------------------------------------------------------------------AudioLevel---------------------------------------------------------------------------*/

    private void audiolevel(final int val, final String key) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != renderViewManager && null != renderViewManager.connetedRemoteRenders &&
                        renderViewManager.connetedRemoteRenders.containsKey(key)) {
                    if (val > 0) {
                        if (key.equals(RongIMClient.getInstance().getCurrentUserId()) && muteMicrophone) {
                            renderViewManager.connetedRemoteRenders.get(key).coverView.closeAudioLevel();
                        } else {
                            renderViewManager.connetedRemoteRenders.get(key).coverView.showAudioLevel();
                        }
                    } else {
                        renderViewManager.connetedRemoteRenders.get(key).coverView.closeAudioLevel();
                    }
                }
            }
        });
    }

    private void showPopupWindow() {
        if (null != popupWindow && popupWindow.isShowing()) {
            return;
        }
        boolean screenConfig = screenCofig(null);
        WindowManager wm = (WindowManager) this.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
//        int screenHeight=wm.getDefaultDisplay().getHeight();
        int xoff = screenWidth - sideBarWidth - dip2px(CallActivity.this, 80);
        int yoff = 0;
//        int base = screenHeight < screenWidth ? screenHeight : screenWidth;

        View view = LayoutInflater.from(CallActivity.this).inflate(R.layout.layout_viewing_pattern, null);
        final TextView tv_smooth = (TextView) view.findViewById(R.id.tv_smooth);
        final TextView tv_highresolution = (TextView) view.findViewById(R.id.tv_highresolution);
        if (SessionManager.getInstance(Utils.getContext()).contains("VideoModeKey")) {
            String videoMode = SessionManager.getInstance(Utils.getContext()).getString("VideoModeKey");
            if (!TextUtils.isEmpty(videoMode)) {
                if (videoMode.equals("smooth")) {
                    tv_smooth.setTextColor(getResources().getColor(R.color.blink_yellow));
                    tv_highresolution.setTextColor(Color.WHITE);
//                    sideBar.setVideoModeBtnText("流畅");
                } else if (videoMode.equals("highresolution")) {
                    tv_smooth.setTextColor(Color.WHITE);
//                    sideBar.setVideoModeBtnText("高清");
                    tv_highresolution.setTextColor(getResources().getColor(R.color.blink_yellow));
                }
            }
        }
        LinearLayout linear_smooth = (LinearLayout) view.findViewById(R.id.linear_smooth);
        LinearLayout linear_highresolution = (LinearLayout) view.findViewById(R.id.linear_highresolution);
        linear_smooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                RongRTCEngine.getInstance().setVideoMode(TEnumVideoMode.VideoModeSmooth);
                SessionManager.getInstance(Utils.getContext()).put("VideoModeKey", "smooth");
                tv_smooth.setTextColor(getResources().getColor(R.color.blink_yellow));
                tv_highresolution.setTextColor(Color.WHITE);
//                changeVideoSize("down");
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
        linear_highresolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                RongRTCEngine.getInstance().setVideoMode(TEnumVideoMode.VideoModeHighresolution);
                SessionManager.getInstance(Utils.getContext()).put("VideoModeKey", "highresolution");
                tv_smooth.setTextColor(Color.WHITE);
                tv_highresolution.setTextColor(getResources().getColor(R.color.blink_yellow));
//                changeVideoSize("up");
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
        if (popupWindow == null) {
            popupWindow = new RongRTCPopupWindow(view, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        }
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        yoff = dip2px(CallActivity.this, 92);//36+16+view.getH
        if (screenConfig) {
            xoff = sideBarWidth;
            popupWindow.showAtLocation(scrollView, Gravity.RIGHT, xoff, -yoff);
        } else {
            popupWindow.showAtLocation(iv_modeSelect, Gravity.LEFT, xoff, -yoff);
        }
    }

    /**
     * 第一次加入房间初始化远端的容器位置
     */
    private void initRemoteScrollView() {
        if (screenCofig(null)) {
            horizontalScreenViewInit();
        } else {
            verticalScreenViewInit();
        }
    }

    /**
     * 横屏View改变
     */
    private void horizontalScreenViewInit() {
        try {
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) rel_sv.getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW, 0);

            WindowManager wm = (WindowManager) this.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            int screenWidth = wm.getDefaultDisplay().getWidth();
            int screenHeight = wm.getDefaultDisplay().getHeight();
            int width = (screenHeight < screenWidth ? screenHeight : screenWidth) / 3;
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) btnCloseCamera.getLayoutParams();
            layoutParams.setMargins(width, 0, 0, dip2px(CallActivity.this, 16));
            btnCloseCamera.setLayoutParams(layoutParams);
            ViewGroup.MarginLayoutParams mutelayoutParams = (ViewGroup.MarginLayoutParams) btnMuteMic.getLayoutParams();
            mutelayoutParams.setMargins(0, 0, width, dip2px(CallActivity.this, 16));
            btnMuteMic.setLayoutParams(mutelayoutParams);
            //
            if (null != horizontalScrollView) {
                if (horizontalScrollView.getChildCount() > 0) {
                    horizontalScrollView.removeAllViews();
                }
                horizontalScrollView.setVisibility(View.GONE);
            }
            if (null != scrollView) {
                if (scrollView.getChildCount() > 0) {
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
    private void verticalScreenViewInit() {
        initBottomBtn();
        //
        RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) rel_sv.getLayoutParams();
        lp3.addRule(RelativeLayout.BELOW, titleContainer.getId());

        if (null != scrollView) {
            if (scrollView.getChildCount() > 0) {
                scrollView.removeAllViews();
            }
            scrollView.setVisibility(View.GONE);
        }
        if (null != horizontalScrollView) {
            if (horizontalScrollView.getChildCount() > 0) {
                horizontalScrollView.removeAllViews();
            }
            horizontalScrollView.addView(call_reder_container);
            horizontalScrollView.setVisibility(View.VISIBLE);
            call_reder_container.setOrientation(LinearLayout.HORIZONTAL);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_btn_hangup:
                intendToLeave();
                break;
            case R.id.menu_switch:
                RongRTCCapture.getInstance().switchCamera();
                break;
            case R.id.menu_close:
                CheckBox checkBox = (CheckBox) v;
                if (canOnlyPublishAudio || isObserver) {
                    checkBox.setChecked(true);
                } else {
                    onCameraClose(checkBox.isChecked());
                }
                break;
            case R.id.menu_mute_mic:
                checkBox = (CheckBox) v;
                onToggleMic(checkBox.isChecked());
                break;
            case R.id.menu_mute_speaker:
                destroyPopupWindow();
                checkBox = (CheckBox) v;
                onToggleSpeaker(checkBox.isChecked());
                break;
            case R.id.menu_whiteboard:
                destroyPopupWindow();
                break;
            case R.id.menu_request_to_normal:
                destroyPopupWindow();
                break;
            case R.id.call_waiting_tips:
                toggleActionButtons(buttonHangUp.getVisibility() == View.VISIBLE);
                break;
            case R.id.menu_up:
                destroyPopupWindow();
//                changeVideoSize("up");
                break;
            case R.id.menu_down:
                destroyPopupWindow();
//                changeVideoSize("down");
                break;
            case R.id.menu_members:
                showMembersDialog();
                break;
        }
    }

    private void showMembersDialog() {
        MembersDialog dialog = null;
        Fragment fragment = getFragmentManager().findFragmentByTag("MembersDialog");
        if (fragment == null) {
            dialog = new MembersDialog();
        } else {
            dialog = (MembersDialog) fragment;
        }
        dialog.update(mMembers);
        dialog.show(getFragmentManager(), "MembersDialog");
    }

    private void updateMembersDialog() {
        Fragment fragment = getFragmentManager().findFragmentByTag("MembersDialog");
        if (fragment != null) {
            MembersDialog dialog = (MembersDialog) fragment;
            dialog.update(mMembers);
        }
    }

    private void toggleCameraMicViewStatus() {
        Log.i(TAG, "toggleCameraMicViewStatus() isObserver = " + isObserver + " isVideoMute = " + isVideoMute);
        btnWhiteBoard.setVisibility(View.GONE); //暂时去掉白板
        iv_modeSelect.setVisibility(View.GONE);
        if (isObserver) {

            btnSwitchCamera.setVisibility(View.GONE);
            btnMuteSpeaker.setVisibility(View.VISIBLE);
            btnCloseCamera.setVisibility(View.GONE);
            btnMuteMic.setVisibility(View.GONE);
        } else {
            if (isVideoMute) {
                btnSwitchCamera.setEnabled(false);
                btnMuteSpeaker.setVisibility(View.VISIBLE);
                btnCloseCamera.setVisibility(View.VISIBLE);
                btnMuteSpeaker.setVisibility(View.VISIBLE);
            } else {
                btnSwitchCamera.setEnabled(true);
                btnSwitchCamera.setVisibility(View.VISIBLE);
                btnMuteSpeaker.setVisibility(View.VISIBLE);
                btnCloseCamera.setVisibility(View.VISIBLE);
                btnMuteSpeaker.setVisibility(View.VISIBLE);
            }
            btnCloseCamera.setChecked(isVideoMute);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isInRoom) {
            RongRTCCapture.getInstance().startCameraCapture();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isInRoom) {
            RongRTCCapture.getInstance().stopCameraCapture();
        }
    }

    @Override
    public void onNotifyHeadsetState(boolean connected, int type) {
        try {
            if (connected) {
                HeadsetPlugReceiverState = true;
                if (type == 0) {
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (am != null) {
                        if (am.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                            am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        }
                        am.startBluetoothSco();
                        am.setBluetoothScoOn(true);
                        am.setSpeakerphoneOn(false);
                    }
                }
                if (null != btnMuteSpeaker) {
                    btnMuteSpeaker.setBackgroundResource(R.drawable.img_capture_gray);
                    btnMuteSpeaker.setSelected(false);
                    btnMuteSpeaker.setEnabled(false);
                    btnMuteSpeaker.setClickable(false);
                }
            } else {
                if (type == 1 &&
                        BluetoothUtil.hasBluetoothA2dpConnected()) {
                    return;
                }
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (am != null) {
                    if (am.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    }
                    if (type == 0) {
                        am.stopBluetoothSco();
                        am.setBluetoothScoOn(false);
                        am.setSpeakerphoneOn(true);
                    }
                }
                if (null != btnMuteSpeaker) {
                    btnMuteSpeaker.setBackgroundResource(R.drawable.selector_checkbox_capture);
                    btnMuteSpeaker.setSelected(false);
                    btnMuteSpeaker.setEnabled(true);
                    btnMuteSpeaker.setClickable(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int processVideoFrame(int width, int height, int textureID) {
        if (isGPUImageFliter) {
            if (beautyFilter == null) {
                beautyFilter = new GPUImageBeautyFilter();
            }
            textureID = beautyFilter.draw(width, height, textureID);
            return textureID;
        }
        return textureID;
    }
}
