package cn.rongcloud.rtc.call;

import static cn.rongcloud.rtc.util.RongRTCTalkTypeUtil.O_MICROPHONE;
import static cn.rongcloud.rtc.util.Utils.parseTimeSeconds;
import static io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus.NETWORK_UNAVAILABLE;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera.CameraInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.rongcloud.rtc.ActionState;
import cn.rongcloud.rtc.AudioMixActivity;
import cn.rongcloud.rtc.AudioMixFragment;
import cn.rongcloud.rtc.AudioEffectFragment;
import cn.rongcloud.rtc.BuildConfig;
import cn.rongcloud.rtc.DebugInfoAdapter;
import cn.rongcloud.rtc.LiveDataOperator;
import cn.rongcloud.rtc.LoadDialog;
import cn.rongcloud.rtc.McuConfigDialog;
import cn.rongcloud.rtc.MembersDialog;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.api.RCRTCAudioMixer;
import cn.rongcloud.rtc.SettingActivity;
import cn.rongcloud.rtc.VideoSizeListDialog;
import cn.rongcloud.rtc.VideoSizeListDialog.OnItemClickListener;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCLocalUser;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCAudioDataListener;
import cn.rongcloud.rtc.api.callback.IRCRTCOnStreamSendListener;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.callback.IRCRTCStatusReportListener;
import cn.rongcloud.rtc.api.callback.IRCRTCVideoOutputFrameListener;
import cn.rongcloud.rtc.api.stream.RCRTCCameraOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCFileVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.api.stream.RCRTCMicOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCParamsType.AudioScenario;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RCRTCResourceState;
import cn.rongcloud.rtc.base.RCRTCStream;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.call.VideoViewManager.RenderHolder;
import cn.rongcloud.rtc.callSettingsFragment.CallSettingsFragment;
import cn.rongcloud.rtc.core.CameraVideoCapturer.CameraSwitchHandler;
import cn.rongcloud.rtc.api.report.StatusBean;
import cn.rongcloud.rtc.api.report.StatusReport;
import cn.rongcloud.rtc.entity.KickEvent;
import cn.rongcloud.rtc.entity.KickedOfflineEvent;
import cn.rongcloud.rtc.entity.RongRTCDeviceType;
import cn.rongcloud.rtc.entity.UserInfo;
import cn.rongcloud.rtc.base.RCRTCAudioFrame;
import cn.rongcloud.rtc.base.RCRTCVideoFrame;
import cn.rongcloud.rtc.base.RCRTCVideoFrame.CaptureType;
import cn.rongcloud.rtc.faceunity.FURenderer;
import cn.rongcloud.rtc.faceunity.ui.FUMenuDialogFrag;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.util.http.HttpClient;
import cn.rongcloud.rtc.message.RoomInfoMessage;
import cn.rongcloud.rtc.message.RoomKickOffMessage;
import cn.rongcloud.rtc.message.WhiteBoardInfoMessage;
import cn.rongcloud.rtc.screen_cast.RongRTCScreenCastHelper;
import cn.rongcloud.rtc.usbcamera.UsbCameraCapturer;
import cn.rongcloud.rtc.usbcamera.UsbCameraCapturerImpl;
import cn.rongcloud.rtc.util.AssetsFilesUtil;
import cn.rongcloud.rtc.util.BluetoothUtil;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.HeadsetPlugReceiver;
import cn.rongcloud.rtc.util.MirrorImageHelper;
import cn.rongcloud.rtc.util.OnHeadsetPlugListener;
import cn.rongcloud.rtc.util.PromptDialog;
import cn.rongcloud.rtc.util.RongRTCPopupWindow;
import cn.rongcloud.rtc.util.RongRTCTalkTypeUtil;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import cn.rongcloud.rtc.watersign.TextureHelper;
import cn.rongcloud.rtc.watersign.WaterMarkFilter;
import cn.rongcloud.rtc.whiteboard.PencilColorPopupWindow;
import cn.rongcloud.rtc.whiteboard.WhiteBoardApi;
import cn.rongcloud.rtc.whiteboard.WhiteBoardRoomInfo;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;
import com.herewhite.sdk.domain.UrlInterrupter;
import io.rong.common.RLog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.MessageContent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
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
import java.util.TimerTask;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/** Activity for peer connection call setup, call waiting and call view. */
public class CallActivity extends RongRTCBaseActivity
        implements View.OnClickListener, OnHeadsetPlugListener {
    private static String TAG = "CallActivity";
    private boolean isShowAutoTest;
    private AlertDialog ConfirmDialog = null;
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 10101;

    public static final String EXTRA_ROOMID = "blinktalk.io.ROOMID";
    public static final String EXTRA_USER_NAME = "blinktalk.io.USER_NAME";
    public static final String EXTRA_CAMERA = "blinktalk.io.EXTRA_CAMERA";
    public static final String EXTRA_OBSERVER = "blinktalk.io.EXTRA_OBSERVER";
    public static final String EXTRA_ONLY_PUBLISH_AUDIO = "ONLY_PUBLISH_AUDIO";
    public static final String EXTRA_AUTO_TEST = "EXTRA_AUTO_TEST";
    public static final String EXTRA_WATER = "EXTRA_WATER";
    public static final String EXTRA_MIRROR = "EXTRA_MIRROR";
    public static final String EXTRA_IS_MASTER = "EXTRA_IS_MASTER";
    public static final String EXTRA_IS_LIVE = "EXTRA_IS_LIVE";

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
    private RCRTCVideoOutputStream screenOutputStream;
    private RongRTCScreenCastHelper screenCastHelper;

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
    //    功能按钮所在的layout
    //    private LinearLayout moreContainer;
    private LinearLayout waitingTips;
    private LinearLayout layoutNetworkStatusInfo;
    private TextView txtViewNetworkStatusInfo;
    private LinearLayout titleContainer;
    private RelativeLayout mcall_more_container;
    private Handler handler = new Handler();
    private DebugInfoAdapter debugInfoAdapter;
    private ListView debugInfoListView;
    private TextView biteRateSendView;
    private TextView biteRateRcvView;
    private TextView rttSendView;
    private RongRTCPopupWindow popupWindow;
    private LinearLayout call_reder_container;
    private int sideBarWidth = 0;
    private AppCompatCheckBox btnSwitchCamera;
    private AppCompatCheckBox btnMuteSpeaker;
    private AppCompatCheckBox btnCloseCamera;
    private AppCompatCheckBox btnMuteMic;
    private AppCompatCheckBox btnSwitchSpeechMusic;
    private AppCompatCheckBox btnRaiseHand;
    private AppCompatCheckBox btnChangeResolution_up;
    private AppCompatCheckBox btnChangeResolution_down;
    private ImageButton btnMembers;
    private ImageView iv_modeSelect;
    private List<MembersDialog.ItemModel> mMembers = new ArrayList<>();
    private Map<String, UserInfo> mMembersMap = new HashMap<>();
    private AppCompatCheckBox btnCustomStream;
    private AppCompatCheckBox btnCustomAudioStream;
    private AppCompatCheckBox btnCustomAudioVolume;
    private AppCompatCheckBox btnScreenCast;
    private AppCompatButton btnMenuSettings;
    private AppCompatCheckBox btnFuEnable;
    private AppCompatCheckBox btnEnableFocus;

    /** UpgradeToNormal邀请观察者发言,将观察升级为正常用户=0, 摄像头:1 麦克风:2 */
    Map<Integer, ActionState> stateMap = new LinkedHashMap<>();
    /** 存储用户是否开启分享 */
    private HashMap<String, Boolean> sharingMap = new HashMap<>();

    /** true 关闭麦克风,false 打开麦克风 */
    private boolean muteMicrophone = false;

    /** true 关闭扬声器； false 打开扬声器 */
    private boolean muteSpeaker = false;
    /** true 音乐模式,false 人声模式 */
    private boolean isMusic = false;

    private ScrollView scrollView;
    private HorizontalScrollView horizontalScrollView;
    private RelativeLayout rel_sv; // sv父布局
    private String myUserId;
    // 管理员uerId,默认第一个加入房间的用户为管理员
    private String adminUserId;
    private boolean kicked;

    private RCRTCRoom room;
    private RCRTCLocalUser localUser;

    private HeadsetPlugReceiver headsetPlugReceiver = null;
    private boolean HeadsetPlugReceiverState = false; // false：开启音视频之前已经连接上耳机
    private boolean mShowWaterMark;
    private WaterMarkFilter mWaterFilter;

    // 白板相关功能
    private WhiteboardView whiteboardView;
    private RelativeLayout whiteboardContainer;
    private ProgressDialog progressDialog;
    private WhiteBoardRoomInfo whiteBoardRoomInfo;
    private View whiteBoardAction;
    private PencilColorPopupWindow pencilColorPopupWindow;
    private Scene[] whiteBoardScenes;
    private String currentSceneName;
    private int currentSceneIndex;
    private Room whiteBoardRoom;
    private Button whiteBoardPagesPrevious;
    private Button whiteBoardPagesNext;
    private Button whiteBoardClose;
    private AppCompatCheckBox btnWhiteBoard;
    private UsbCameraCapturer mUsbCameraCapturer;
    private boolean screenCastEnable = true;
    private boolean customVideoEnabled = true;

    private List<StatusReport> statusReportList = new ArrayList<>();
    private int lossRateSum = 0;
    private SoundPool mSoundPool;
    private boolean playSound = true;
    private Timer networkObserverTimer = null;
    private int timerPeriod = 5 * 1000;
    private boolean mIsLive;
    private byte[] mDrawBackData = null;

    /**
     * 本地麦克风采集的和远端的pcm音频数据写到文件用于定位问题,写入文件地址为sdcard/webrtc/ 1.使用时 writePcmFileForDebug 设置为true 即可 2.
     * 此功能主要用于排查问题，强烈建议不能发布到生产环境
     */
    private boolean writePcmFileForDebug = false;

    private FURenderer mFURenderer;
    private Handler mGlHandler;
    private boolean mMirrorVideoFrame = true; // 是否镜像翻转采集数据
    private MirrorImageHelper mMirrorHelper;
    private FUMenuDialogFrag fuMenuView;
    private RCRTCFileVideoOutputStream fileVideoOutputStream;
    private VideoSizeListDialog mVideoSizeDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HeadsetPlugReceiver.setOnHeadsetPlugListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        headsetPlugReceiver = new HeadsetPlugReceiver(BluetoothUtil.hasBluetoothA2dpConnected());
        registerReceiver(headsetPlugReceiver, intentFilter);

        sideBarWidth = dip2px(CallActivity.this, 40) + 75;

        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
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
        isShowAutoTest = intent.getBooleanExtra(EXTRA_AUTO_TEST, false);
        canOnlyPublishAudio = intent.getBooleanExtra(EXTRA_ONLY_PUBLISH_AUDIO, false);
        mShowWaterMark = intent.getBooleanExtra(EXTRA_WATER, false);
        mMirrorVideoFrame = intent.getBooleanExtra(EXTRA_MIRROR, false);
        mIsLive = intent.getBooleanExtra(EXTRA_IS_LIVE, false);
        if (TextUtils.isEmpty(roomId)) {
            Log.e(TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        myUserId = RongIMClient.getInstance().getCurrentUserId();
        boolean admin = intent.getBooleanExtra(EXTRA_IS_MASTER, false);
        if (admin) {
            adminUserId = myUserId;
        }
        initAudioManager();
        initViews(intent);
        checkPermissions();
        initBottomBtn();
        initRemoteScrollView();
        if (room == null) {
            return;
        }
        room.getRoomAttributes(null, new IRCRTCResultDataCallback<Map<String, String>>() {
            @Override
            public void onSuccess(final Map<String, String> data) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        onGetRoomAttributesHandler(data);
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {

            }
        });

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (writePcmFileForDebug) {
            createDebugPcmFile();
        }

        // 为防止在使用时初始化造成花屏或卡顿，所以提前初始化美颜引擎
        initFURenderer();
        initAudioMixing();
    }

    private void onGetRoomAttributesHandler(Map<String, String> data) {
        try {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                FinLog.d(TAG, "[MemberList] onCreate ==>  " + entry);
                JSONObject jsonObject = new JSONObject(entry.getValue());
                if (entry.getKey().equals(WhiteBoardApi.WHITE_BOARD_KEY)) {
                    whiteBoardRoomInfo = new WhiteBoardRoomInfo(jsonObject.getString("uuid"), jsonObject.getString("roomToken"));
                    continue;
                }
                UserInfo userInfo = new UserInfo();
                userInfo.userName = jsonObject.getString("userName");
                userInfo.joinMode = jsonObject.getInt("joinMode");
                userInfo.userId = jsonObject.getString("userId");
                userInfo.timestamp = jsonObject.getLong("joinTime");
                boolean master = jsonObject.optInt("master") == 1;
                if (master) {
                    adminUserId = userInfo.userId;
                }
                if (room.getRemoteUser(userInfo.userId) == null && !TextUtils.equals(myUserId, userInfo.userId)) {
                    continue;
                }
                if (mMembersMap.containsKey(entry.getKey())) {
                    continue;
                }
                mMembersMap.put(entry.getKey(), userInfo);

                MembersDialog.ItemModel model = new MembersDialog.ItemModel();
                model.mode = mapMode(userInfo.joinMode);
                model.name = userInfo.userName;
                model.userId = userInfo.userId;
                model.joinTime = userInfo.timestamp;
                mMembers.add(model);

                List<RenderHolder> holders = renderViewManager.getViewHolderByUserId(entry.getKey());
                for (RenderHolder holder : holders) {
                    if (TextUtils.equals(entry.getKey(), myUserId)) {
                        holder.updateUserInfo(getResources().getString(R.string.room_actor_me));
                    } else {
                        holder.updateUserInfo(model.name);
                    }
                }
                setWaitingTipsVisiable(mMembers.size() <= 1);
            }
            FinLog.d(TAG, "[MemberList] getRoomAttributes ==>  MemberSize=" + mMembers.size());
            sortRoomMembers();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private IRCRTCAudioDataListener audioDataListener = new IRCRTCAudioDataListener() {
        @Override
        public byte[] onAudioFrame(RCRTCAudioFrame rtcAudioFrame) {
            if (writePcmFileForDebug) {
                byte[] bytes = rtcAudioFrame.getBytes().clone();
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                writePcmBuffer(byteBuffer, localFileChanel);
            }
            return rtcAudioFrame.getBytes();
        }
    };

    private IRCRTCVideoOutputFrameListener videoOutputFrameListener = new IRCRTCVideoOutputFrameListener() {
        @Override
        public RCRTCVideoFrame processVideoFrame(RCRTCVideoFrame videoFrame) {
            boolean isTexture = videoFrame.getCaptureType() == CaptureType.TEXTURE;
            // TODO 水印目前仅支持 Texture 类型
            if (mShowWaterMark && isTexture) {
                videoFrame.setTextureId(
                    onDrawWater(videoFrame.getWidth(), videoFrame.getHeight(), videoFrame.getTextureId()));
            }
            // 如果 fuMenuView 为空表示用户没有启用美颜，所以不需要处理
            if (mFURenderer != null && fuMenuView != null) {
                if (mGlHandler == null) {
                    mGlHandler = new Handler(Looper.myLooper());
                    mFURenderer.onSurfaceCreated();
                }
                boolean isFrontCamera = RCRTCEngine.getInstance().getDefaultVideoStream().isFrontCamera();
                if (isTexture) {
                    int newTextureId = mFURenderer.onDrawFrame(
                        videoFrame.getTextureId(), videoFrame.getWidth(), videoFrame.getHeight(), isFrontCamera);
                    videoFrame.setTextureId(newTextureId);
                } else {
                    byte[] data = videoFrame.getData();
                    // 为节省内存开销，复用 byte 数组
                    if (mDrawBackData == null || mDrawBackData.length != data.length) {
                        mDrawBackData = new byte[data.length];
                    }
                    mFURenderer.onDrawFrame(data, videoFrame.getWidth(), videoFrame.getHeight(),
                        mDrawBackData, videoFrame.getWidth(), videoFrame.getHeight(), !isFrontCamera);
                    videoFrame.setData(mDrawBackData);
                }
            }
            onMirrorVideoFrame(videoFrame);
            return videoFrame;
        }
    };

    private IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {
        @Override
        public void onRemoteUserPublishResource(final RCRTCRemoteUser remoteUser, List<RCRTCInputStream> streams) {
            FinLog.d(TAG, "--- onRemoteUserPublishResource ----- remoteUser: " + remoteUser);
            if (remoteUser == null) {
                return;
            }
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    alertRemotePublished(remoteUser);
                    updateResourceVideoView(remoteUser);
                }
            });

            room.getRoomAttributes(null, new IRCRTCResultDataCallback<Map<String, String>>() {
                @Override
                public void onSuccess(final Map<String, String> data) {
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 房间中有用户断线1分钟后重连，会直接发布资源，这是重新统计房间人员信息
                                if (mMembersMap == null || mMembersMap.size() == data.size()) {
                                    return;
                                }
                                mMembersMap.clear();
                                mMembers.clear();
                                for (Map.Entry<String, String> entry : data.entrySet()) {
                                    FinLog.d(TAG, "[MemberList] onRemoteUserPublishResource ==>  " + entry);
                                    JSONObject jsonObject = new JSONObject(entry.getValue());
                                    if (entry.getKey().equals(WhiteBoardApi.WHITE_BOARD_KEY)) {
                                        whiteBoardRoomInfo = new WhiteBoardRoomInfo(jsonObject.getString("uuid"), jsonObject.getString("roomToken"));
                                        continue;
                                    }
                                    UserInfo userInfo = new UserInfo();
                                    userInfo.userName = jsonObject.getString("userName");
                                    userInfo.joinMode = jsonObject.getInt("joinMode");
                                    userInfo.userId = jsonObject.getString("userId");
                                    userInfo.timestamp = jsonObject.getLong("joinTime");
                                    boolean master = jsonObject.optInt("master") == 1;
                                    if (master) {
                                        adminUserId = userInfo.userId;
                                    }

                                    if (room.getRemoteUser(userInfo.userId) == null && !TextUtils.equals(myUserId, userInfo.userId)) {
                                        continue;
                                    }
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
                                FinLog.d(TAG, "[MemberList] onRemoteUserPublishResource ==>  MemberSize=" + mMembers.size());
                                sortRoomMembers();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {}
            });
        }

        @Override
        public void onKickedByServer() {
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    final PromptDialog dialog = PromptDialog.newInstance(CallActivity.this, getString(R.string.rtc_dialog_kicked_by_server));
                    dialog.setPromptButtonClickedListener(new PromptDialog.OnPromptButtonClickedListener() {
                        @Override
                        public void onPositiveButtonClicked() {
                            finish();
                        }

                        @Override
                        public void onNegativeButtonClicked() {
                            finish();
                        }
                    });
                    dialog.disableCancel();
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });
        }

        @Override
        public void onVideoTrackAdd(final String userId, final String tag) {
            Log.i(TAG, "onVideoTrackAdd() userId: " + userId + " ,tag = " + tag);
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    if (isShowAutoTest) { // 自动化测试会有红点
                        renderViewManager.onTrackadd(userId, tag);
                    }
                    if (TextUtils.equals(tag, UserUtils.CUSTOM_FILE_TAG)) {
                        customVideoEnabled = false;
                    } else if (TextUtils.equals(tag, RongRTCScreenCastHelper.VIDEO_TAG)) {
                        screenCastEnable = false;
                        btnScreenCast.setEnabled(false);
                    }
                }
            });
        }

        @Override
        public void onReceiveMessage(final io.rong.imlib.model.Message message) {
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    MessageContent messageContent = message.getContent();
                    FinLog.i(TAG, "onReceiveMessage()->" + messageContent);
                    if (messageContent instanceof RoomInfoMessage) {
                        RoomInfoMessage roomInfoMessage = (RoomInfoMessage) messageContent;
                        FinLog.d(TAG, "[MemberList] onReceiveMessage ==>  " + new String(roomInfoMessage.encode()));
                        MembersDialog.ItemModel itemModel = new MembersDialog.ItemModel();
                        itemModel.name = roomInfoMessage.getUserName();
                        itemModel.mode = mapMode(roomInfoMessage.getJoinMode());
                        itemModel.userId = roomInfoMessage.getUserId();
                        itemModel.joinTime = roomInfoMessage.getTimeStamp();
                        if (!mMembersMap.containsKey(itemModel.userId)) {
                            String toastMsg = itemModel.name + " " + getResources().getString(R.string.rtc_join_room);
                            Toast.makeText(CallActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                            mMembers.add(0, itemModel);
                            sortRoomMembers();
                        } else {
                            for (MembersDialog.ItemModel member : mMembers) {
                                if (TextUtils.equals(member.userId, itemModel.userId)) {
                                    member.mode = itemModel.mode;
                                    break;
                                }
                            }
                            if (roomInfoMessage.isMaster() && !itemModel.userId.equals(adminUserId)) {
                                adminUserId = itemModel.userId;
                                if (itemModel.userId.equals(myUserId)) {
                                    String toastMsg = getResources().getString(R.string.member_operate_admin_me);
                                    Toast.makeText(CallActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                                } else {
                                    String toastMsg = itemModel.name + " " + getResources().getString(R.string.member_operate_admin_new);
                                    Toast.makeText(CallActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        UserInfo userInfo = new UserInfo();
                        userInfo.userId = roomInfoMessage.getUserId();
                        userInfo.userName = roomInfoMessage.getUserName();
                        userInfo.joinMode = roomInfoMessage.getJoinMode();
                        userInfo.timestamp = roomInfoMessage.getTimeStamp();
                        mMembersMap.put(roomInfoMessage.getUserId(), userInfo);

                        List<VideoViewManager.RenderHolder> holders = renderViewManager.getViewHolderByUserId(roomInfoMessage.getUserId());
                        for (VideoViewManager.RenderHolder holder : holders) {
                            if (!TextUtils.equals(roomInfoMessage.getUserId(), myUserId)) {
                                holder.updateUserInfo(roomInfoMessage.getUserName());
                            }
                            switch (roomInfoMessage.getJoinMode()) {
                                case RoomInfoMessage.JoinMode.AUDIO:
                                    holder.CameraSwitch(RongRTCTalkTypeUtil.C_CAMERA);
                                    break;
                                case RoomInfoMessage.JoinMode.AUDIO_VIDEO:
                                    holder.CameraSwitch(RongRTCTalkTypeUtil.O_CAMERA);
                                    break;
                                case RoomInfoMessage.JoinMode.OBSERVER:
                                    renderViewManager.removeVideoView(roomInfoMessage.getUserId());
                                    break;
                            }
                        }

                        updateMembersDialog();
                        if (mMembers.size() > 1) {
                            setWaitingTipsVisiable(false);
                        }
                    } else if (messageContent instanceof WhiteBoardInfoMessage) {
                        WhiteBoardInfoMessage whiteBoardInfoMessage = (WhiteBoardInfoMessage) messageContent;
                        whiteBoardRoomInfo = new WhiteBoardRoomInfo(whiteBoardInfoMessage.getUuid(), whiteBoardInfoMessage.getRoomToken());
                    } else if (messageContent instanceof RoomKickOffMessage) {
                        RoomKickOffMessage kickOffMessage = (RoomKickOffMessage) messageContent;
                        if (myUserId.equals(kickOffMessage.getUserId())) {
                            FinLog.i(TAG, "kickOffMessage-intendToLeave");
                            intendToLeave(false);
                            EventBus.getDefault().post(new KickEvent(roomId));
                        }
                    }
                }
            });
        }

        @Override
        public void onFirstRemoteVideoFrame(final String userId, final String tag) {
            Log.i(TAG, "onFirstFrameDraw() userId: " + userId + " ,tag = " + tag);
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    if (isShowAutoTest) {
                        renderViewManager.onFirstFrameDraw(userId, tag);
                    }
                }
            });
        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser remoteUser, RCRTCInputStream stream, boolean mute) {
            FinLog.d(TAG, "onRemoteUserAudioStreamMute remoteUser: " + remoteUser + " ,  mute :" + mute);
        }

        @Override
        public void onRemoteUserMuteVideo(final RCRTCRemoteUser remoteUser, final RCRTCInputStream stream, final boolean mute) {
            FinLog.d(TAG, "onRemoteUserVideoStreamEnabled remoteUser: " + remoteUser + "  , enable :" + mute);
            if (remoteUser == null || stream == null) {
                return;
            }
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    updateVideoView(remoteUser, stream, mute);
                }
            });

        }

        @Override
        public void onRemoteUserUnpublishResource(final RCRTCRemoteUser remoteUser, final List<RCRTCInputStream> streams) {
            FinLog.d(TAG, "onRemoteUserUnpublishResource remoteUser: " + remoteUser);
            if (streams == null) {
                return;
            }
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    for (RCRTCInputStream stream : streams) {
                        if (stream.getMediaType().equals(RCRTCMediaType.VIDEO)) {
                            renderViewManager.removeVideoView(false, remoteUser.getUserId(), stream.getTag());
                            if (TextUtils.equals(stream.getTag(), UserUtils.CUSTOM_FILE_TAG)) {
                                customVideoEnabled = true;
                            } else if (TextUtils.equals(stream.getTag(), RongRTCScreenCastHelper.VIDEO_TAG)) {
                                screenCastEnable = true;
                                btnScreenCast.setEnabled(true);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onUserJoined(RCRTCRemoteUser remoteUser) {
            FinLog.d(TAG, "onUserJoined  remoteUser :" + remoteUser.getUserId());
            // Toast.makeText(CallActivity.this, remoteUser.getUserId() + " " +
            // getResources().getString(R.string.rtc_join_room), Toast.LENGTH_SHORT).show();

            //        if (!mMembersMap.containsKey(remoteUser.getUserId())) {//为兼容2.0版本加入房间不会触发room
            // info更新的情况，生成默认的ItemModel加入集合
            //            MembersDialog.ItemModel itemModel = new MembersDialog.ItemModel();
            //            itemModel.name = "";
            //            itemModel.mode = "0";
            //            itemModel.userId = remoteUser.getUserId();
            //            mMembers.add(0, itemModel);
            //        }
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mMembers.size() > 1) {
                        setWaitingTipsVisiable(false);
                    }
                }
            });
            // renderViewManager.userJoin(remoteUser.getUserId(), remoteUser.getUserId(),
            // RongRTCTalkTypeUtil.O_CAMERA);
        }

        @Override
        public void onUserLeft(final RCRTCRemoteUser remoteUser) {
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CallActivity.this, getUserName(remoteUser.getUserId()) + " " + getResources().getString(R.string.rtc_quit_room), Toast.LENGTH_SHORT).show();
                    exitRoom(remoteUser.getUserId());
                    clearWhiteBoardInfoIfNeeded();
                    if (mMembers.size() <= 1) {
                        setWaitingTipsVisiable(true);
                    }
                }
            });
        }

        @Override
        public void onUserOffline(final RCRTCRemoteUser remoteUser) {
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CallActivity.this, getUserName(remoteUser.getUserId()) + " " + getResources().getString(R.string.rtc_user_offline), Toast.LENGTH_SHORT).show();
                    exitRoom(remoteUser.getUserId());
                    clearWhiteBoardInfoIfNeeded();
                    if (remoteUser.getUserId().equals(adminUserId)) {
                        adminUserId = null;
                    }
                    if (mMembers.size() <= 1) {
                        setWaitingTipsVisiable(true);
                    }
                }
            });
        }

        @Override
        public void onLeaveRoom(int reasonCode) {
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    final PromptDialog dialog = PromptDialog.newInstance(CallActivity.this, getString(R.string.rtc_status_im_error));
                    dialog.setPromptButtonClickedListener(new PromptDialog.OnPromptButtonClickedListener() {
                        @Override
                        public void onPositiveButtonClicked() {
                            finish();
                        }

                        @Override
                        public void onNegativeButtonClicked() {
                        }
                    });
                    dialog.disableCancel();
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });
        }
    };

    private IRCRTCStatusReportListener statusReportListener = new IRCRTCStatusReportListener() {
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
                        val = Integer.parseInt(entry.getValue().toString());
                    }
                    audiolevel(val, key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAudioInputLevel(String audioLevel) {
            if (localUser == null) return;
            int val = 0;
            try {
                val = TextUtils.isEmpty(audioLevel) ? 0 : Integer.parseInt(audioLevel);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (localUser.getDefaultAudioStream() != null && !TextUtils.isEmpty(localUser.getDefaultAudioStream().getStreamId())) {
                audiolevel(val, localUser.getDefaultAudioStream().getStreamId());
            }
        }

        @Override
        public void onConnectionStats(final StatusReport statusReport) {
            postUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mMembers != null && mMembers.size() > 1) {
                        updateNetworkSpeedInfo(statusReport);
                    } else {
                        initUIForWaitingStatus();
                    }
                    unstableNetworkToast(statusReport);
                    // 只有Debug模式下才显示详细的调试信息
                    if (renderViewManager == null || !BuildConfig.DEBUG) {
                        return;
                    }
                    parseToList(statusReport);
                    updateDebugInfo(statusReport);
                }
            });
        }
    };

    private void initAudioMixing() {
        AudioMixFragment.mixing = false;
        AudioMixFragment.mixMode = AudioMixFragment.MODE_PLAY_MIX;
        AudioMixFragment.audioPath = AudioMixFragment.DEFAULT_AUDIO_PATH;
        Arrays.fill(AudioEffectFragment.preloaded, false);
        AudioEffectFragment.loopCount = 1;
        RCRTCAudioMixer.getInstance().setMixingVolume(100);
        RCRTCAudioMixer.getInstance().setPlaybackVolume(100);
        RCRTCEngine.getInstance().getDefaultAudioStream().adjustRecordingVolume(100);
    }

    private void sortRoomMembers() {
        Collections.sort(
                mMembers,
                new Comparator<MembersDialog.ItemModel>() {
                    @Override
                    public int compare(MembersDialog.ItemModel o1, MembersDialog.ItemModel o2) {
                        return (int) (o1.joinTime - o2.joinTime);
                    }
                });
        // 如果第一的位置不是管理员，强制把管理员排到第一的位置
        if (mMembers.size() > 0 && !mMembers.get(0).userId.equals(adminUserId)) {
            MembersDialog.ItemModel adminItem = null;
            for (MembersDialog.ItemModel model : mMembers) {
                if (model.userId.equals(adminUserId)) {
                    adminItem = model;
                    break;
                }
            }
            if (adminItem != null) {
                mMembers.remove(adminItem);
                mMembers.add(0, adminItem);
            }
        }
    }

    @Override
    public void onBackPressed() {
        intendToLeave(true);
        super.onBackPressed();
    }

    private void initAudioManager() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager =
                AppRTCAudioManager.create(
                        this.getApplicationContext(),
                        new Runnable() {
                            // This method will be called each time the audio state (number and
                            // type of devices) has been changed.
                            @Override
                            public void run() {
                                onAudioManagerChangedState();
                            }
                        });
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
        if (renderViewManager != null
                && null != unGrantedPermissions
                && unGrantedPermissions.size() == 0) {
            renderViewManager.rotateView();
        }
        if (mWaterFilter != null) {
            boolean isFrontCamera = RCRTCEngine.getInstance().getDefaultVideoStream().isFrontCamera();
            mWaterFilter.angleChange(isFrontCamera);
        }
        if (mFURenderer != null) {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            mFURenderer.setDisPlayOrientation(wm.getDefaultDisplay().getRotation());
            //            mFURenderer.onCameraChange(RongRTCCapture.getInstance().isFrontCamera(),
            //                ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
            //                    .getDefaultDisplay().getRotation());
        }
    }

    /** 初始化底部按钮 默认竖屏 */
    private void initBottomBtn() {
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) btnCloseCamera.getLayoutParams();
        layoutParams.setMargins(dip2px(CallActivity.this, 50), 0, 0, dip2px(CallActivity.this, 16));
        btnCloseCamera.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams mutelayoutParams =
                (ViewGroup.MarginLayoutParams) btnMuteMic.getLayoutParams();
        mutelayoutParams.setMargins(
                0, 0, dip2px(CallActivity.this, 50), dip2px(CallActivity.this, 16));
        btnMuteMic.setLayoutParams(mutelayoutParams);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void initViews(Intent intent) {
        findViewById(R.id.btn_mcu).setVisibility(mIsLive ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_mcu).setOnClickListener(this);
        mcall_more_container = (RelativeLayout) findViewById(R.id.call_more_container);
        iv_modeSelect = (ImageView) findViewById(R.id.btn_modeSelect);
        btnRaiseHand = (AppCompatCheckBox) findViewById(R.id.menu_request_to_normal);
        btnSwitchCamera = (AppCompatCheckBox) findViewById(R.id.menu_switch);
        btnMuteSpeaker = (AppCompatCheckBox) findViewById(R.id.menu_mute_speaker);
        btnSwitchSpeechMusic = (AppCompatCheckBox) findViewById(R.id.menu_switch_speech_music);
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
        layoutNetworkStatusInfo = (LinearLayout) findViewById(R.id.layout_network_status_tips);
        txtViewNetworkStatusInfo = (TextView) findViewById(R.id.textView_network_status_tips);
        whiteboardContainer = (RelativeLayout) findViewById(R.id.call_whiteboard);
        whiteBoardAction = findViewById(R.id.white_board_action);
        btnMembers = (ImageButton) findViewById(R.id.menu_members);
        whiteBoardPagesPrevious = (Button) findViewById(R.id.white_board_pages_previous);
        whiteBoardPagesNext = (Button) findViewById(R.id.white_board_pages_next);
        whiteBoardClose = (Button) findViewById(R.id.white_board_close);
        btnCustomStream = (AppCompatCheckBox) findViewById(R.id.menu_custom_stream);
        btnCustomAudioStream = (AppCompatCheckBox) findViewById(R.id.menu_custom_audio);
        btnMenuSettings = (AppCompatButton) findViewById(R.id.menu_btn_call_menu_settings);
        btnScreenCast = findViewById(R.id.menu_screen);
        btnFuEnable = findViewById(R.id.menu_fu_enbale);
        btnEnableFocus = findViewById(R.id.menu_focus);
        btnChangeResolution_up.setVisibility(View.GONE);
        btnChangeResolution_down.setVisibility(View.GONE);

        debugInfoAdapter = new DebugInfoAdapter(this);
        debugInfoListView.setAdapter(debugInfoAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.white_board_loading));

        rel_sv = (RelativeLayout) findViewById(R.id.rel_sv);

        iv_modeSelect.setOnClickListener(
                new View.OnClickListener() {
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

        renderViewManager = new VideoViewManager();
        renderViewManager.setActivity(this);
        if (BuildConfig.DEBUG) {
            textViewNetSpeed.setVisibility(View.VISIBLE);
        } else {
            textViewNetSpeed.setVisibility(View.GONE);
        }

        textViewRoomNumber.setText(
                getText(R.string.room_number) + intent.getStringExtra(CallActivity.EXTRA_ROOMID));
        buttonHangUp.setOnClickListener(this);
        btnSwitchCamera.setOnClickListener(this);
        btnCloseCamera.setOnClickListener(this);
        btnMuteMic.setOnClickListener(this);
        btnSwitchSpeechMusic.setOnClickListener(this);
        btnMuteSpeaker.setOnClickListener(this);
        btnWhiteBoard.setOnClickListener(this);
        btnMembers.setOnClickListener(this);
        btnRaiseHand.setOnClickListener(this);
        btnChangeResolution_up.setOnClickListener(this);
        btnChangeResolution_down.setOnClickListener(this);
        btnCustomStream.setOnClickListener(this);
        btnCustomAudioStream.setOnClickListener(this);
        btnMenuSettings.setOnClickListener(this);
        btnScreenCast.setOnClickListener(this);
        btnFuEnable.setOnClickListener(this);
        btnEnableFocus.setOnClickListener(this);
        renderViewManager.setOnLocalVideoViewClickedListener(
                new VideoViewManager.OnLocalVideoViewClickedListener() {
                    @Override
                    public void onClick() {
                        toggleActionButtons(buttonHangUp.getVisibility() == View.VISIBLE);
                    }
                });

        if (isObserver) {
            btnMuteMic.setChecked(true);
            btnMuteMic.setEnabled(false);
            btnCloseCamera.setChecked(true);
            btnCloseCamera.setEnabled(false);
            btnCustomStream.setEnabled(false);
            btnCustomAudioStream.setEnabled(false);
            btnFuEnable.setEnabled(false);
            findViewById(R.id.btn_switch_videosize).setEnabled(false);
        }
        if (isVideoMute) {
            btnCloseCamera.setChecked(true);
            btnCloseCamera.setEnabled(false);
            btnFuEnable.setEnabled(false);
            findViewById(R.id.btn_switch_videosize).setEnabled(false);
        }

        findViewById(R.id.btn_switch_videosize).setOnClickListener(this);

        // setCallIdel();
    }

    /**
     * 准备离开当前房间
     *
     * @param initiative 是否主动退出，false为被踢的情况
     */
    private void intendToLeave(boolean initiative) {
        FinLog.i(TAG, "intendToLeave()-> " + initiative);
        callFUDestroyed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cancelScreenCast(true);
        }
        if (mIsLive) {
            unpublishLiveData();
        }
        if (null != sharingMap) {
            sharingMap.clear();
        }
        if (initiative) {
            selectAdmin();
        } else {
            kicked = true;
        }
        RCRTCAudioMixer.getInstance().stop();
        AudioMixFragment.mixing = false;
        // 当前用户是观察者 或 离开房间时还有其他用户存在，直接退出
        if (screenOutputStream == null) {
            disconnect();
        }
    }

    private void unpublishLiveData() {
        if (liveInfo != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(LiveDataOperator.ROOM_ID, liveInfo.getRoomId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            LiveDataOperator.getInstance().unpublish(jsonObject.toString(), null);
        }
    }

    /** 改变屏幕上除了视频通话之外的其他视图可见状态 */
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
        }
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
                        //
                        // RongRTCEngine.getInstance().muteMicrophone(true);
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        //
                        // RongRTCEngine.getInstance().muteMicrophone(false);
                        break;
                }
            }
            }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void setWaitingTipsVisiable(boolean visiable) {
        //        FinLog.v(TAG,"setWaitingTipsVisiable() visiable = "+visiable);
        if (visiable) {
            visiable = !(mMembers != null && mMembers.size() > 1);
        }
        int tmp = waitingTips.getVisibility();
        if (visiable) {
            if (tmp != View.VISIBLE) {
                handler.removeCallbacks(timeRun);
            }
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
        callFUDestroyed();
        clearState();
        if (isInRoom) {
            RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
        }
    }

    private void callFUDestroyed() {
        if (mGlHandler != null && mFURenderer != null) {
            mGlHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "onStop surfaceDestroyed");
                            mFURenderer.onSurfaceDestroyed();
                        }
                    });
        }
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
        if (networkObserverTimer != null) {
            networkObserverTimer.cancel();
            networkObserverTimer = null;
        }
        HeadsetPlugReceiver.setOnHeadsetPlugListener(null);
        if (headsetPlugReceiver != null) {
            unregisterReceiver(headsetPlugReceiver);
            headsetPlugReceiver = null;
        }
        HeadsetPlugReceiverState = false;
        if (room != null) {
            room.unregisterRoomListener();
        }
        RCRTCEngine.getInstance().unregisterStatusReportListener();
        if (isConnected) {
            RCRTCAudioMixer.getInstance().stop();
            if (room != null) {
                room.deleteRoomAttributes(Arrays.asList(myUserId), null, null);
                deleteRTCWhiteBoardAttribute();
            }
            RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    isInRoom = false;
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {

                }
            });
//            if (renderViewManager != null)
                //                renderViewManager.destroyViews();

        }

        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }

        if (handler != null) {
            handler.removeCallbacks(memoryRunnable);
            handler.removeCallbacks(timeRun);
        }
        handler = null;
        super.onDestroy();
        if (null != ConfirmDialog && ConfirmDialog.isShowing()) {
            ConfirmDialog.dismiss();
            ConfirmDialog = null;
        }
        if (null != sharingMap) {
            sharingMap.clear();
        }
        destroyWebView(whiteboardView);

        callFUDestroyed();
        mGlHandler = null;
        fuMenuView = null;
        mDrawBackData = null;

        if (mWaterFilter != null) {
            mWaterFilter.release();
        }
        if (mMirrorHelper != null) {
            mMirrorHelper.release();
        }
        mMirrorHelper = null;

        if (mUsbCameraCapturer != null) mUsbCameraCapturer.release();
        RCRTCMicOutputStream defaultAudioStream = RCRTCEngine.getInstance().getDefaultAudioStream();
        if (defaultAudioStream != null) {
            defaultAudioStream.setAudioDataListener(null);
        }

        RCRTCCameraOutputStream defaultVideoStream = RCRTCEngine.getInstance().getDefaultVideoStream();
        if (defaultVideoStream != null) {
            defaultVideoStream.setVideoFrameListener(null);
        }
        if (mSoundPool != null) {
            mSoundPool.release();
        }
        mSoundPool = null;
        localSurface = null;

        EventBus.getDefault().unregister(this);
        if (writePcmFileForDebug) {
            closePcmFile(localFileChanel, localFileStream);
            closePcmFile(remoteFileChanel, remoteFileStream);
        }
    }

    /**
     * 摄像头开关
     *
     * @param closed true 关闭摄像头 false 打开摄像头
     * @return
     * @isActive true：主動
     */
    public boolean onCameraClose(boolean closed) {
        Log.i(TAG, "onCameraClose closed = " + closed);
        this.isVideoMute = closed;
        if(closed){
            RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
        }else {
            RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(-1,!mMirrorVideoFrame,null);
        }
        if (renderViewManager != null) {
            String talkType = closed ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA;
            renderViewManager.updateTalkType(myUserId, RCRTCStream.RONG_TAG, talkType);
        }
        toggleCameraMicViewStatus();
        return isVideoMute;
    }

    public void onToggleMic(boolean mute) {
        muteMicrophone = mute;
        RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(muteMicrophone);
    }

    public void onToggleSwitchSpeechMusic(boolean isMusic) {
        this.isMusic = isMusic;
//        DevicesUtils.setPlayMode(
//                this.isMusic ? DevicesUtils.AudioPlayMode.MUSIC : DevicesUtils.AudioPlayMode.SPEEK);
//        int mode =
//                DevicesUtils.getAudioMode() == AudioManager.STREAM_MUSIC
//                        ? AudioManager.MODE_NORMAL
//                        : AudioManager.MODE_IN_COMMUNICATION;
//
//        if (audioManager != null && audioManager.getAudioManager() != null) {
//            Log.d(TAG, "second setMode =" + mode);
//            audioManager.getAudioManager().setMode(mode);
//        }
    }

    private void rotateScreen(boolean isToLandscape) {
        if (isToLandscape) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    List<String> unGrantedPermissions;

    private void checkPermissions() {
        unGrantedPermissions = new ArrayList();
        for (String permission : MANDATORY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        if (unGrantedPermissions.size() == 0) { // 已经获得了所有权限，开始加入聊天室
            startCall();
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
        for (String permission : unGrantedPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showToastLengthLong(getString(R.string.PermissionStr) + permission + getString(R.string.plsopenit));
                finish();
            } else ActivityCompat.requestPermissions(this, new String[] {permission}, 0);
        }
        if (unGrantedPermissions.size() == 0) {
            AssetsFilesUtil.putAssetsToSDCard(getApplicationContext(), assetsFile, encryptFilePath);
            startCall();
        }
    }

    private String assetsFile = "EncryptData/00000001.bin";
    private String encryptFilePath = new StringBuffer()
        .append(Environment.getExternalStorageDirectory().toString() + File.separator)
        .append("Blink")
        .append(File.separator)
        .append("EncryptData").toString();
    LocalVideoView localSurface;

    private void startCall() {
        try {
            renderViewManager.initViews(this, isObserver);
            if (!isObserver) {
                localSurface = new LocalVideoView(getApplicationContext());
                String talkType = isVideoMute ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA;
                renderViewManager.userJoin(myUserId, RCRTCStream.RONG_TAG, iUserName, talkType);
                renderViewManager.setVideoView(
                    true, myUserId, RCRTCStream.RONG_TAG, iUserName, localSurface, talkType);
                if (!isVideoMute) {
                    RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(localSurface); // 设置本地view
                    RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(-1,!mMirrorVideoFrame,null);
                }
            }

            room = RCRTCEngine.getInstance().getRoom();
            RCRTCEngine.getInstance().registerStatusReportListener(statusReportListener);
            room.registerRoomListener(roomEventsListener);
            localUser = room.getLocalUser();
            renderViewManager.setRongRTCRoom(room);

            RCRTCEngine.getInstance().getDefaultVideoStream().setVideoFrameListener(videoOutputFrameListener);
            RCRTCEngine.getInstance().getDefaultAudioStream().setAudioDataListener(audioDataListener);

            publishResource(); // 发布资源
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
        for (RCRTCRemoteUser remoteUser : room.getRemoteUsers()) {
            addNewRemoteView(remoteUser); // 准备view
        }
    }

    private RCRTCLiveInfo liveInfo;

    private void publishResource() {
        if (isObserver) {
            return;
        }
        if (localUser == null) {
            Toast.makeText(CallActivity.this, "不在房间里", Toast.LENGTH_SHORT).show();
            return;
        }

        if (RongIMClient.getInstance().getCurrentConnectionStatus() == NETWORK_UNAVAILABLE) {
            String toastMsg = getResources().getString(R.string.Thecurrentnetworkisnotavailable);
            Toast.makeText(CallActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            return;
        }

        final List<RCRTCOutputStream> localAvStreams = new ArrayList<>();
        localAvStreams.add(RCRTCEngine.getInstance().getDefaultAudioStream());
        if (!isVideoMute) {
            localAvStreams.add(RCRTCEngine.getInstance().getDefaultVideoStream());
        }
        if (!mIsLive) {
            localUser.publishStreams(localAvStreams, new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        FinLog.v(TAG, "publish success()");
                    }

                    @Override
                    public void onFailed(RTCErrorCode errorCode) {
                        FinLog.e(TAG, "publish publish Failed()");
                        // 50010 网络请求超时错误时，重试一次资源发布操作
                        if (errorCode.equals(RTCErrorCode.RongRTCCodeHttpTimeoutError)) {
                            localUser.publishStreams(localAvStreams, null);
                        }
                    }
            });
            return;
        }
        if (isVideoMute) {
            localUser.publishLiveStream(RCRTCEngine.getInstance().getDefaultAudioStream(), createLiveCallback());
        } else {
            localUser.publishDefaultLiveStreams(createLiveCallback());
        }
    }

    private IRCRTCResultDataCallback createLiveCallback() {
        return new IRCRTCResultDataCallback<RCRTCLiveInfo>() {
            @Override
            public void onSuccess(RCRTCLiveInfo data) {
                liveInfo = data;
                // TODO URL上传到服务器
                FinLog.d(TAG, "liveUrl::" + liveInfo.getLiveUrl());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(LiveDataOperator.ROOM_ID, liveInfo.getRoomId());
                    jsonObject.put(LiveDataOperator.ROOM_NAME, liveInfo.getUserId());
                    jsonObject.put(LiveDataOperator.LIVE_URL, liveInfo.getLiveUrl());
                    jsonObject.put(LiveDataOperator.PUB_ID, liveInfo.getUserId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LiveDataOperator.getInstance().publish(jsonObject.toString(), new LiveDataOperator.OnResultCallBack() {
                    @Override
                    public void onSuccess(final String result) {
                        postUIThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("直播房间上传成功！" + result);
                            }
                        });
                    }

                    @Override
                    public void onFailed(final String error) {
                        postUIThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("直播房间上传失败！" + error);
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailed(final RTCErrorCode errorCode) {
                FinLog.e(TAG, "publish publish Failed()");
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CallActivity.this, "发布资源失败 ：" + errorCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }

    private int getMaxBitRate() {
        int bitRate = 500;
        String maxBitRate = SessionManager.getInstance().getString(SettingActivity.BIT_RATE_MAX, getResources().getString(R.string.def_min_bitrate));
        if (!TextUtils.isEmpty(maxBitRate) && maxBitRate.length() > 4) {
            bitRate = Integer.valueOf(maxBitRate.substring(0, maxBitRate.length() - 4));
            FinLog.v(TAG, "BIT_RATE_MAX=" + bitRate);
        }
        return bitRate;
    }

    private int getMinBitRate() {
        int bitRate = 100;
        String minBitRate =
                SessionManager.getInstance()
                        .getString(
                                SettingActivity.BIT_RATE_MIN,
                                getResources().getString(R.string.def_min_bitrate));
        if (!TextUtils.isEmpty(minBitRate) && minBitRate.length() > 4) {
            bitRate = Integer.valueOf(minBitRate.substring(0, minBitRate.length() - 4));
            FinLog.v(TAG, "BIT_RATE_MIN=" + bitRate);
        }
        return bitRate;
    }

    private String getAudioRecordFilePath() {
        String path =
                Environment.getExternalStorageDirectory().getPath() + "/blink/audio_recording/";
        File file = new File(path);
        if (!file.exists()) file.mkdirs();
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
                        textViewNetSpeed.setText(getResources().getString(R.string.network_traffic_receive)
                            + msg.getData().getLong("rcv")
                            + "Kbps  "
                            + getResources()
                                                        .getString(R.string.network_traffic_send)
                            + msg.getData().getLong("send")
                            + "Kbps");
                    }
                    super.handleMessage(msg);
                }
        };
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

    private String getUserName(String userId) {
        if (TextUtils.isEmpty(userId)) return userId;
        UserInfo userInfo = mMembersMap.get(userId);
        if (userInfo == null) return userId;
        return userInfo.userName;
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

    /**
     * 根据丢包率信息，提示弱网
     *
     * @param statusReport
     */
    private void unstableNetworkToast(StatusReport statusReport) {
        if (statusReportList != null && statusReportList.size() < 10) {
            statusReportList.add(statusReport);
            return;
        }

        Map<String, Map<String, Integer>> userStreamLostRate = new HashMap<>();

        for (StatusReport item : statusReportList) {
            // 统计本地发送音频流丢包率
            for (Map.Entry<String, StatusBean> entry : item.statusAudioSends.entrySet()) {
                String localUid = entry.getValue().uid;
                String streamId = entry.getValue().id;
                if (!userStreamLostRate.containsKey(localUid)) {
                    userStreamLostRate.put(localUid, new HashMap<String, Integer>());
                }
                if (entry.getValue().packetLostRate > 30) {
                    if (!userStreamLostRate.get(localUid).containsKey(streamId)) {
                        userStreamLostRate.get(localUid).put(streamId, 0);
                    }
                    userStreamLostRate
                            .get(localUid)
                            .put(streamId, userStreamLostRate.get(localUid).get(streamId) + 1);
                }
            }

            // 统计本地发送视频流丢包率
            for (Map.Entry<String, StatusBean> entry : item.statusVideoSends.entrySet()) {
                String localUid = entry.getValue().uid;
                String streamId = entry.getValue().id;
                if (!userStreamLostRate.containsKey(localUid)) {
                    userStreamLostRate.put(localUid, new HashMap<String, Integer>());
                }
                if (entry.getValue().packetLostRate > 15) {
                    if (!userStreamLostRate.get(localUid).containsKey(streamId)) {
                        userStreamLostRate.get(localUid).put(streamId, 0);
                    }
                    userStreamLostRate
                            .get(localUid)
                            .put(streamId, userStreamLostRate.get(localUid).get(streamId) + 1);
                }
            }

            // 统计远端音频流丢包率
            for (Map.Entry<String, StatusBean> entry : item.statusAudioRcvs.entrySet()) {
                String remoteUid = entry.getValue().uid;
                String streamId = entry.getValue().id;
                if (!userStreamLostRate.containsKey(remoteUid)) {
                    userStreamLostRate.put(remoteUid, new HashMap<String, Integer>());
                }
                if (entry.getValue().packetLostRate > 30) {
                    if (!userStreamLostRate.get(remoteUid).containsKey(streamId)) {
                        userStreamLostRate.get(remoteUid).put(streamId, 0);
                    }
                    userStreamLostRate
                            .get(remoteUid)
                            .put(streamId, userStreamLostRate.get(remoteUid).get(streamId) + 1);
                }
            }

            // 统计远端视频流丢包率
            for (Map.Entry<String, StatusBean> entry : item.statusVideoRcvs.entrySet()) {
                String remoteUid = entry.getValue().uid;
                String streamId = entry.getValue().id;
                if (!userStreamLostRate.containsKey(remoteUid)) {
                    userStreamLostRate.put(remoteUid, new HashMap<String, Integer>());
                }
                if (entry.getValue().packetLostRate > 15) {
                    if (!userStreamLostRate.get(remoteUid).containsKey(streamId)) {
                        userStreamLostRate.get(remoteUid).put(streamId, 0);
                    }
                    userStreamLostRate
                            .get(remoteUid)
                            .put(streamId, userStreamLostRate.get(remoteUid).get(streamId) + 1);
                }
            }
        }
        statusReportList.clear();

        String networkToast = "";
        boolean shouldToast = false;
        for (Map.Entry<String, Map<String, Integer>> entry : userStreamLostRate.entrySet()) {
            String userId = entry.getKey();
            for (Map.Entry<String, Integer> streamEntry : entry.getValue().entrySet()) {
                if (streamEntry.getValue() > 5) {
                    if (mMembersMap != null
                            && mMembersMap.containsKey(userId)
                            && !networkToast.contains(mMembersMap.get(userId).userName)) {
                        if (shouldToast) {
                            networkToast += ", ";
                        }
                        networkToast += mMembersMap.get(userId).userName;
                        shouldToast = true;
                    }
                }
            }
        }

        networkToast = String.format(getString(R.string.network_tip), networkToast);

        final boolean finalShouldToast = shouldToast;
        final String finalNetworkToast = networkToast;

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (finalShouldToast) {
                            layoutNetworkStatusInfo.setVisibility(View.VISIBLE);
                            txtViewNetworkStatusInfo.setText(finalNetworkToast);
                            txtViewNetworkStatusInfo.setVisibility(View.VISIBLE);

                            handler.postDelayed(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            layoutNetworkStatusInfo.setVisibility(View.GONE);
                                            txtViewNetworkStatusInfo.setVisibility(View.GONE);
                                        }
                                    },
                                    3000);
                        } else {
                            layoutNetworkStatusInfo.setVisibility(View.GONE);
                            txtViewNetworkStatusInfo.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * 丟包>15%的情况下，会每 {@link #timerPeriod}检查一下当前丢包率
     *
     * @param lossRate
     */
    private void networkObserverTimer(final float lossRate) {
        if (lossRate < 15) {
            if (networkObserverTimer != null) {
                networkObserverTimer.cancel();
                networkObserverTimer = null;
            }
        } else {
            if (networkObserverTimer == null) {
                networkObserverTimer = new Timer("NetWorkObserverTimer");
                networkObserverTimer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                playTipsSound(lossRate);
                            }
                        },
                        0,
                        timerPeriod);
            }
        }
    }

    private void playTipsSound(final float lossRate) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (lossRate >= 15) {
                            FinLog.v(TAG, "loss rate > 15, play Sound !");
                            if (mSoundPool != null) {
                                mSoundPool.release();
                            }
                            if (playSound) {
                                playSound = false;
                                mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
                                mSoundPool.load(
                                        CallActivity.this, R.raw.voip_network_error_sound, 0);
                                mSoundPool.setOnLoadCompleteListener(
                                        new SoundPool.OnLoadCompleteListener() {
                                            @Override
                                            public void onLoadComplete(
                                                    SoundPool soundPool, int sampleId, int status) {
                                                soundPool.play(sampleId, 1F, 1F, 0, 0, 1F);
                                            }
                                        });
                                String toastInfo =
                                        CallActivity.this.getString(
                                                R.string.rtc_unstable_call_connection);
                                Toast.makeText(CallActivity.this, toastInfo, Toast.LENGTH_SHORT)
                                        .show();
                                playSound = true;
                            }
                        }
                    }
                });
    }

    private void updateResourceVideoView(RCRTCRemoteUser remoteUser) {
        for (RCRTCInputStream rongRTCAVOutputStream : remoteUser.getStreams()) {
            RCRTCResourceState state = rongRTCAVOutputStream.getResourceState();
            if (rongRTCAVOutputStream.getMediaType() == RCRTCMediaType.VIDEO && renderViewManager != null) {
                FinLog.v(TAG, "updateResourceVideoView userId = " + remoteUser.getUserId() + " state = " + state);
                renderViewManager.updateTalkType(remoteUser.getUserId(), rongRTCAVOutputStream.getTag(),
                        state == RCRTCResourceState.DISABLED ? RongRTCTalkTypeUtil.C_CAMERA : RongRTCTalkTypeUtil.O_CAMERA);
            }
        }
    }

    private void updateVideoView(RCRTCRemoteUser remoteUser, RCRTCInputStream rongRTCAVInputStream, boolean enable) {
        if (renderViewManager != null) {
            FinLog.v(TAG, "updateVideoView userId = " + remoteUser.getUserId() + " state = " + enable);
            renderViewManager.updateTalkType(remoteUser.getUserId(), rongRTCAVInputStream.getTag(),
                    enable ? RongRTCTalkTypeUtil.O_CAMERA : RongRTCTalkTypeUtil.C_CAMERA);
        }
    }

    private void alertRemotePublished(final RCRTCRemoteUser remoteUser) {
        Log.i(TAG, "alertRemotePublished() start");
        addNewRemoteView(remoteUser);
        localUser.subscribeStreams(remoteUser.getStreams(), new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                FinLog.d(TAG, "subscribeStreams userId = " + remoteUser.getUserId() + ", errorCode =" + errorCode.getValue());
                // 50010 网络请求超时错误时，重试一次订阅操作
                if (RTCErrorCode.RongRTCCodeHttpTimeoutError.equals(errorCode) && remoteUser.getStreams() != null &&
                    remoteUser.getStreams().size() > 0) {
                    localUser.subscribeStreams(remoteUser.getStreams(), null);
                }
            }
        });
    }

    private void subscribeAll() {
        for (final RCRTCRemoteUser remoteUser : room.getRemoteUsers()) {
            if (remoteUser.getStreams().size() == 0) {
                continue;
            }
            localUser.subscribeStreams(remoteUser.getStreams(), new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            updateResourceVideoView(remoteUser);
                        }
                    });
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    FinLog.d(TAG, "subscribeAll subscribeStreams userId = " + remoteUser.getUserId() + ", errorCode =" + errorCode.getValue());
                    // 50010 网络请求超时错误时，重试一次订阅操作
                    if (RTCErrorCode.RongRTCCodeHttpTimeoutError.equals(errorCode) && remoteUser.getStreams() != null &&
                        remoteUser.getStreams().size() > 0) {
                        localUser.subscribeStreams(remoteUser.getStreams(), null);
                    }
                }
            });
        }
    }

    private void addNewRemoteView(RCRTCRemoteUser remoteUser) {
        List<RCRTCVideoInputStream> videoStreamList = new ArrayList<>();
        List<RCRTCInputStream> remoteAVStreams = remoteUser.getStreams();
        RCRTCInputStream audioStream = null;
        // 标记对方是否发布了摄像头视频流
        boolean cameraOpened = false;
        for (RCRTCInputStream inputStream : remoteAVStreams) {
            if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                videoStreamList.add((RCRTCVideoInputStream) inputStream);
                if (TextUtils.equals(inputStream.getTag(), RCRTCStream.RONG_TAG)) {
                    cameraOpened = true;
                }
            } else if (inputStream.getMediaType() == RCRTCMediaType.AUDIO) {

                // 只处理默认音频流，如果是自定义音频流，不做UI展示
                if (RCRTCStream.RONG_TAG.equals(inputStream.getTag())) {
                    audioStream = inputStream;
                }
            }
        }
        // 只有音频流，没有视频流时增加占位
        if (videoStreamList.isEmpty() && audioStream != null) {
            videoStreamList.add(null);
        }
        for (RCRTCVideoInputStream videoStream : videoStreamList) {
            UserInfo userInfo = mMembersMap.get(remoteUser.getUserId());
            String talkType = videoStream == null
                    ? RongRTCTalkTypeUtil.C_CAMERA
                    : RongRTCTalkTypeUtil.O_CAMERA;
            String userName = userInfo != null ? userInfo.userName : "";
            if (videoStream != null && videoStream.getVideoView() == null) {
                FinLog.v(TAG, "addNewRemoteView");
                if (!renderViewManager.hasConnectedUser()) {
                    startCalculateNetSpeed();
                }
                renderViewManager.userJoin(remoteUser.getUserId(), videoStream.getTag(), userName, talkType);
                RCRTCVideoView remoteView = new RCRTCVideoView(this);
                renderViewManager.setVideoView(false, remoteUser.getUserId(),
                        videoStream.getTag(), remoteUser.getUserId(), remoteView, talkType);
                videoStream.setVideoView(remoteView);
            } else if (videoStream == null) {     //audio 占位
                renderViewManager.userJoin(remoteUser.getUserId(), RCRTCStream.RONG_TAG, userName, talkType);
                RCRTCVideoView remoteView = new RCRTCVideoView(this);
                renderViewManager.setVideoView(false, remoteUser.getUserId(), RCRTCStream.RONG_TAG, remoteUser.getUserId(), remoteView, talkType);
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
        for (Map.Entry<String, StatusBean> entry : statusReport.statusAudioSends.entrySet()) {
            statusBeanList.add(entry.getValue());
        }
        for (Map.Entry<String, StatusBean> entry : statusReport.statusAudioRcvs.entrySet()) {
            statusBeanList.add(entry.getValue());
        }
    }

    /** Initialize the UI to "waiting user join" IMConnectionStatus */
    private void initUIForWaitingStatus() {
        if (time != 0) {
            textViewTime.setText(getResources().getText(R.string.connection_duration));
            textViewNetSpeed.setText(getResources().getText(R.string.network_traffic));
        }
        time = 0;
    }

    private void disconnect() {
        isConnected = false;
        LoadDialog.show(CallActivity.this);
        if(room != null){
            room.deleteRoomAttributes(Collections.singletonList(myUserId), null, null);
        }
        deleteRTCWhiteBoardAttribute();
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        FinLog.i(TAG, "quitRoom()->onUiSuccess");
                        isInRoom = false;
                        if (!kicked) {
                            Toast.makeText(CallActivity.this, getResources().getString(R.string.quit_room_success), Toast.LENGTH_SHORT).show();
                        }
                        if (audioManager != null) {
                            audioManager.close();
                            audioManager = null;
                        }
                        LoadDialog.dismiss(CallActivity.this);
                        finish();
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                FinLog.i(TAG, "quitRoom()->onUiFailed : " + errorCode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (audioManager != null) {
                            audioManager.close();
                            audioManager = null;
                        }
                        LoadDialog.dismiss(CallActivity.this);
                        finish();
                    }
                });


            }
        });
    }

    private Runnable memoryRunnable = new Runnable() {
        @Override
        public void run() {
            getSystemMemory();
            if (handler != null) handler.postDelayed(memoryRunnable, 1000);
        }
    };

    /**
     * @param type true:不弹窗 false：弹窗
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
            if (stateMap.size() > 0) { // 之前有弹窗 保存key 不继续执行
                bean = new ActionState(type, hostUid, userid);
                stateMap.put(type, bean);
                state = true;
            } else { // 当前没有弹窗 保存 继续当前的执行（弹窗）
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
                talkType = O_MICROPHONE;
            } else if (dType == RongRTCDeviceType.CameraAndMicrophone) {
                talkType = RongRTCTalkTypeUtil.O_CM;
            }
        } else { //
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
        boolean screen = false; // 默认竖屏
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
        renderViewManager.removeVideoView(userId);
        if (!renderViewManager.hasConnectedUser()) { // 除我以为,无外人
            initUIForWaitingStatus();
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
    }
    /*--------------------------------------------------------------------------AudioLevel---------------------------------------------------------------------------*/

    private void audiolevel(final int val, final String key) {
        postUIThread(new Runnable() {
            @Override
            public void run() {
                if (null != renderViewManager && null != renderViewManager.connectedRemoteRenders && renderViewManager.getViewHolder(key) != null) {
                    VideoViewManager.RenderHolder renderHolder = renderViewManager.getViewHolder(key);
                    if (val > 0) {
                        if (key.equals(RongIMClient.getInstance().getCurrentUserId()) && muteMicrophone) {
                            renderHolder.coverView.closeAudioLevel();
                        } else {
                            renderHolder.coverView.showAudioLevel();
                        }
                    } else {
                        renderHolder.coverView.closeAudioLevel();
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
        WindowManager wm = (WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        //        int screenHeight=wm.getDefaultDisplay().getHeight();
        int xoff = screenWidth - sideBarWidth - dip2px(CallActivity.this, 80);
        int yoff = 0;
        //        int base = screenHeight < screenWidth ? screenHeight : screenWidth;

        View view = LayoutInflater.from(CallActivity.this).inflate(R.layout.layout_viewing_pattern, null);
        final TextView tv_smooth = (TextView) view.findViewById(R.id.tv_smooth);
        final TextView tv_highresolution = (TextView) view.findViewById(R.id.tv_highresolution);
        if (SessionManager.getInstance().contains("VideoModeKey")) {
            String videoMode = SessionManager.getInstance().getString("VideoModeKey");
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
                //
                // RongRTCEngine.getInstance().setVideoMode(TEnumVideoMode.VideoModeSmooth);
                SessionManager.getInstance().put("VideoModeKey", "smooth");
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
                //
                // RongRTCEngine.getInstance().setVideoMode(TEnumVideoMode.VideoModeHighresolution);
                SessionManager.getInstance().put("VideoModeKey", "highresolution");
                tv_smooth.setTextColor(Color.WHITE);
                tv_highresolution.setTextColor(getResources().getColor(R.color.blink_yellow));
                //                changeVideoSize("up");
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
        if (popupWindow == null) {
            popupWindow = new RongRTCPopupWindow(view,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        }
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        yoff = dip2px(CallActivity.this, 92); // 36+16+view.getH
        if (screenConfig) {
            xoff = sideBarWidth;
            popupWindow.showAtLocation(scrollView, Gravity.RIGHT, xoff, -yoff);
        } else {
            popupWindow.showAtLocation(iv_modeSelect, Gravity.LEFT, xoff, -yoff);
        }
    }

    /** 第一次加入房间初始化远端的容器位置 */
    private void initRemoteScrollView() {
        if (screenCofig(null)) {
            horizontalScreenViewInit();
        } else {
            verticalScreenViewInit();
        }
    }

    /** 横屏View改变 */
    private void horizontalScreenViewInit() {
        try {
            RelativeLayout.LayoutParams lp3 = (RelativeLayout.LayoutParams) rel_sv.getLayoutParams();
            lp3.addRule(RelativeLayout.BELOW, 0);

            WindowManager wm = (WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            int screenWidth = wm.getDefaultDisplay().getWidth();
            int screenHeight = wm.getDefaultDisplay().getHeight();
            int width = (screenHeight < screenWidth ? screenHeight : screenWidth) / 3;
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) btnCloseCamera.getLayoutParams();
            layoutParams.setMargins(width, 0, 0, dip2px(CallActivity.this, 16));
            btnCloseCamera.setLayoutParams(layoutParams);
            ViewGroup.MarginLayoutParams mutelayoutParams = (ViewGroup.MarginLayoutParams) btnMuteMic.getLayoutParams();
            mutelayoutParams.setMargins(0, 0, width, dip2px(CallActivity.this, 16));
            btnMuteMic.setLayoutParams(mutelayoutParams);
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

    /** 竖屏View改变 */
    private void verticalScreenViewInit() {
        initBottomBtn();
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
            case R.id.btn_mcu:
                if (liveInfo != null) {
                    McuConfigDialog.showDialog(this, liveInfo);
                } else {
                    showToast("liveRoom is Null");
                }

                break;
            case R.id.call_btn_hangup:
                FinLog.i(TAG, "intendToLeave()-> call_btn_hangup");
                intendToLeave(true);
                break;
            case R.id.menu_switch:
                RCRTCEngine.getInstance().getDefaultVideoStream().switchCamera(new CameraSwitchHandler() {
                    @Override
                    public void onCameraSwitchDone(boolean isFrontCamera) {
                        if (mWaterFilter != null) {
                            mWaterFilter.angleChange(isFrontCamera);
                        }
                        if (mFURenderer != null) {
                            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                            mFURenderer.onCameraChange(isFrontCamera, wm.getDefaultDisplay().getRotation());
                        }
                    }

                    @Override
                    public void onCameraSwitchError(String errorDescription) {

                    }
                });
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
                FinLog.i(TAG, "isMute : " + checkBox.isChecked());
                onToggleMic(checkBox.isChecked());
                break;
            case R.id.menu_switch_speech_music:
                checkBox = (CheckBox) v;
                Log.d(TAG, "setMode check " + checkBox.isChecked());
                onToggleSwitchSpeechMusic(checkBox.isChecked());
                break;
            case R.id.menu_mute_speaker:
                // 为防止频繁快速点击造成音频卡顿，增加点击间隔限制
                if (Utils.isFastDoubleClick()) {
                    showToast(R.string.rtc_processing);
                    return;
                }
                destroyPopupWindow();
                checkBox = (CheckBox) v;
                this.muteSpeaker = checkBox.isChecked();
                if (muteSpeaker) {
                    showToast(R.string.rtc_toast_switch_to_receiver);
                } else {
                    showToast(R.string.rtc_toast_switch_to_speaker);
                }
                RCRTCEngine.getInstance().enableSpeaker(!this.muteSpeaker);
                audioManager.onToggleSpeaker(!muteSpeaker);
                break;
            case R.id.menu_whiteboard:
                if (RongIMClient.getInstance().getCurrentConnectionStatus() == NETWORK_UNAVAILABLE) {
                    String toastMsg = getResources().getString(R.string.Thecurrentnetworkisnotavailable);
                    Toast.makeText(CallActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                destroyPopupWindow();
                checkBox = (CheckBox) v;
                if (!checkBox.isChecked()) {
                    return;
                }
                if (whiteboardView == null) {
                    whiteboardView = new WhiteboardView(this);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                    whiteboardContainer.addView(whiteboardView,layoutParams);
                }
                whiteboardView.requestFocus();
                if (whiteBoardRoomInfo != null) {
                    progressDialog.show();
                    updateCallActionsWithWhiteBoard(true);
                    joinWhiteBoardRoom(whiteBoardRoomInfo.getUuid(), whiteBoardRoomInfo.getRoomToken());
                } else {
                    if (isObserver) {
                        showToast(getString(R.string.white_board_room_not_exist));
                        return;
                    }
                    progressDialog.show();
                    updateCallActionsWithWhiteBoard(true);
                    WhiteBoardApi.createRoom(roomId, 100, new HttpClient.ResultCallback() {
                        @Override
                        public void onResponse(String result) {
                            if (TextUtils.isEmpty(result)) {
                                showToast("json is Null");
                                return;
                            }
                            JSONObject msg;
                            try {
                                msg = new JSONObject(result);
                                JSONObject room = msg.getJSONObject("msg").getJSONObject("room");
                                final String uuid = room.getString("uuid");
                                String roomToken = msg.getJSONObject("msg").getString("roomToken");
                                setRoomWhiteBoardInfo(uuid, roomToken);
                                joinWhiteBoardRoom(uuid, roomToken);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        }

                        @Override
                        public void onFailure(int errorCode) {
                            RLog.d(TAG, "here white create room errorCode:" + errorCode);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateCallActionsWithWhiteBoard(false);
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });
                }
                break;
            case R.id.menu_request_to_normal:
                destroyPopupWindow();
                break;
            case R.id.call_waiting_tips:
                //                toggleActionButtons(buttonHangUp.getVisibility() == View.VISIBLE);
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
            case R.id.menu_custom_stream:
                if (Utils.isFastDoubleClick()) {
                    showToast(R.string.rtc_processing);
                    return;
                }
                checkBox = (CheckBox) v;
                if (checkBox.isSelected()) {
                    unPublishCustomStream(checkBox);
                } else {
                    showVideoListMenu(v);
                }
                break;
            case R.id.menu_custom_audio:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(this, R.string.mix_audio_tips, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (localUser != null) {
                    localUser.unpublishStream(fileVideoOutputStream, null);
                }
                btnCustomAudioStream.setSelected(true);
                //                toggleActionButtons(true);
                startActivity(new Intent(CallActivity.this, AudioMixActivity.class));
                overridePendingTransition(R.anim.mix_slide_up, 0);
                break;
            case R.id.menu_btn_call_menu_settings:
                showSettingsDialog();
                break;
            case R.id.menu_screen:
                checkBox = (CheckBox) v;
                if (checkBox.isSelected()) {
                    cancelScreenCast(false);
                    checkBox.setSelected(false);
                } else {
                    checkBox.setSelected(true);
                    requestForScreenCast();
                }
                break;
            case R.id.menu_fu_enbale:
                if (fuMenuView == null) {
                    fuMenuView = FUMenuDialogFrag.newInstance(mFURenderer);
                    fuMenuView.setMenuDialogCallback(new FUMenuDialogFrag.FuMenuDialogCallback() {
                        @Override
                        public void onDismiss() {
                            btnFuEnable.setSelected(false);
                        }
                    });
                }
                btnFuEnable.setSelected(true);
                fuMenuView.show(getFragmentManager(), "FUMenuDialogView");
                break;
            case R.id.menu_focus:
                btnEnableFocus.setSelected(!btnEnableFocus.isSelected());
                localSurface.enableReceiveTouchEvent(btnEnableFocus.isSelected());
                ContainerLayout.enableGestureDetect = !btnEnableFocus.isSelected();
                break;
            case R.id.btn_switch_videosize:
                if (mVideoSizeDialog == null) {
                    mVideoSizeDialog = VideoSizeListDialog.newInstance(getApplicationContext());
                    mVideoSizeDialog.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(RCRTCVideoStreamConfig config) {
                                RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(config);
                        }
                    });
                }
                mVideoSizeDialog.show(getFragmentManager(), "VideoSizeListDialog");
                break;

            default:
                break;
        }
    }

    private void initFURenderer() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int displayOrientation = wm.getDefaultDisplay().getRotation();
        mFURenderer = new FURenderer.Builder(this)
            .maxFaces(4)
            .inputIsImage(false)
            // 默认前置摄像头
            .inputImageOrientation(FURenderer.disPlayOrientation2Angle(displayOrientation))
            //                .setLoadAiFaceLandmark75(!mIsMakeup)
            //                .setLoadAiFaceLandmark239(mIsMakeup)
            //                .setLoadAiHumanPose(isBodySlim)
            //                .setLoadAiHairSeg(isHairSeg)
            //                .setLoadAiBgSeg(selectEffectType ==
            // Effect.EFFECT_TYPE_BACKGROUND)
            //                .setLoadAiGesture(selectEffectType ==
            // Effect.EFFECT_TYPE_GESTURE)
            .inputTextureType(0) // FURenderer.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE)
            //                .setUseBeautifyBody(isBodySlim)
            //                .setNeedBeautyHair(isHairSeg)
            .setCameraFacing(CameraInfo.CAMERA_FACING_FRONT)
            //                .setOnTrackingStatusChangedListener(this)
            .build();
        //        mFURenderer.onSurfaceCreated();
        if (mMirrorVideoFrame) {
            mMirrorHelper = new MirrorImageHelper(this);
        }
    }

    private void showSettingsDialog() {
        CallSettingsFragment callSettingsFragment = new CallSettingsFragment();
        callSettingsFragment.setListener(new MyCallSettingFragmentListener(this));
        callSettingsFragment.show(getFragmentManager(), "menuSettings");
    }

    private void unPublishCustomStream(final CheckBox checkBox) {
        if (mUsbCameraCapturer != null) {
            mUsbCameraCapturer.unPublishVideoStream(new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            renderViewManager.removeVideoView(true, myUserId, UsbCameraCapturerImpl.STREAM_TAG);
                            mUsbCameraCapturer.release();
                            mUsbCameraCapturer = null;
                            checkBox.setSelected(false);
                        }
                    });

                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    showToast("cancel USBCamera Stream Failed: " + errorCode);
                }
            });
            return;
        }
        if (localUser != null) {
            localUser.unpublishStream(fileVideoOutputStream,new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            renderViewManager.removeVideoView(true, myUserId, fileVideoOutputStream.getTag());
                            checkBox.setSelected(false);
                        }
                    });

                }

                @Override
                public void onFailed(final RTCErrorCode errorCode) {
                    postUIThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CallActivity.this, "取消发布自定义视频失败:" + errorCode, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
        }
    }

    private void showVideoListMenu(View view) {
        final PopupWindow popupWindow = new PopupWindow();
        View contentView = getLayoutInflater().inflate(R.layout.layout_video_list, null);
        View video1 = contentView.findViewById(R.id.video_1);
        View video2 = contentView.findViewById(R.id.video_2);
        video1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishCustomStream("file:///android_asset/video_1.mp4");
                popupWindow.dismiss();
            }
        });
        video2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishCustomStream("file:///android_asset/video_2.mp4");
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.video_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishUSBCamera();
                popupWindow.dismiss();
            }
        });
        popupWindow.setContentView(contentView);
        showPopupWindowList(popupWindow, view);
    }

    private void showPopupWindowList(PopupWindow popupWindow, View view) {
        popupWindow.setOutsideTouchable(true);
        popupWindow.setWidth(getResources().getDimensionPixelSize(R.dimen.popup_width));
        popupWindow.setHeight(getResources().getDimensionPixelSize(R.dimen.popup_width));
        int[] location = new int[2];
        view.getLocationInWindow(location);
        int x = location[0] - popupWindow.getWidth();
        int y = location[1] + view.getHeight() / 2 - popupWindow.getHeight() / 2;
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
    }

    private void publishUSBCamera() {
        if (mUsbCameraCapturer == null) {
            mUsbCameraCapturer = new UsbCameraCapturerImpl(this, localUser, RCRTCVideoResolution.RESOLUTION_480_640);
            if (!((UsbCameraCapturerImpl) mUsbCameraCapturer).isUSBCameraConnected()) {
                mUsbCameraCapturer.release();
                mUsbCameraCapturer = null;
                Toast.makeText(this, R.string.usb_camera_not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
            RCRTCVideoView videoView = new RCRTCVideoView(this);
            mUsbCameraCapturer.setRongRTCVideoView(videoView);
            RCRTCOutputStream videoOutputStream = mUsbCameraCapturer.getVideoOutputStream();
            renderViewManager.userJoin(myUserId, videoOutputStream.getTag(), iUserName, RongRTCTalkTypeUtil.O_CAMERA);
            renderViewManager.setVideoView(true, myUserId,
                videoOutputStream.getTag(), iUserName, videoView, RongRTCTalkTypeUtil.O_CAMERA);
            mUsbCameraCapturer.startCapturer();
        }
        btnCustomStream.setSelected(true);
        mUsbCameraCapturer.publishVideoStream(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("publish USB Success");
                    }
                });
            }

            @Override
            public void onFailed(final RTCErrorCode errorCode) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        btnCustomStream.setSelected(false);
                        showToast("publish USB Failed: " + errorCode);
                    }
                });
            }
        });
    }

    private void publishCustomStream(String filePath) {
        if (!customVideoEnabled) {
            String toastInfo = getResources().getString(R.string.publish_disabled);
            Toast.makeText(this, toastInfo, Toast.LENGTH_SHORT).show();
            return;
        }
        btnCustomStream.setSelected(true);
        RCRTCAudioMixer.getInstance().stop();
        AudioMixFragment.mixing = false;
        btnCustomAudioStream.setSelected(false);
        if (localUser == null) {
            return;
        }

        fileVideoOutputStream = RCRTCEngine.getInstance().createFileVideoOutputStream(filePath, false, true,
            UserUtils.CUSTOM_FILE_TAG, RCRTCVideoStreamConfig.Builder.create().setVideoResolution(RCRTCVideoResolution.RESOLUTION_360_640).setVideoFps(RCRTCVideoFps.Fps_24).build());
        fileVideoOutputStream.setOnSendListener(new IRCRTCOnStreamSendListener() {
            @Override
            public void onStart(final RCRTCVideoOutputStream stream) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RCRTCVideoView videoView = new RCRTCVideoView(CallActivity.this);
                        stream.setVideoView(videoView);
                        renderViewManager.userJoin(myUserId, stream.getTag(), iUserName, RongRTCTalkTypeUtil.O_CAMERA);
                        renderViewManager.setVideoView(true, myUserId, stream.getTag(), iUserName, videoView, RongRTCTalkTypeUtil.O_CAMERA);
                    }
                });
            }

            @Override
            public void onComplete(final RCRTCVideoOutputStream stream) {
                btnCustomStream.post(new Runnable() {
                    @Override
                    public void run() {
                        //如果是 activity 退出过程中，不需要先取消发布资源
                        //在调用 leaveRoom 退出房间前也不需要先取消当前发布的资源
                        if (!isFinishing()) {
                            btnCustomStream.setSelected(false);
                            renderViewManager.removeVideoView(true, myUserId, stream.getTag());
                            localUser.unpublishStream(stream, null);
                        }
                    }
                });
            }

            @Override
            public void onFailed() {
                String toastInfo = getResources().getString(R.string.publish_failed);
                Toast.makeText(CallActivity.this, toastInfo, Toast.LENGTH_SHORT).show();
                btnCustomStream.post(new Runnable() {
                    @Override
                    public void run() {
                        btnCustomStream.setSelected(false);
                    }
                });
            }
        });
        localUser.publishStream(fileVideoOutputStream, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                //TODO ? 发布失败了肯定是网络异常导致，取消发布肯定也会失败的为什么还要取消发布？
//                localUser.unpublishStream(fileVideoOutputStream, null);
                // 50010 网络请求超时错误时，重试一次资源发布操作
                if (errorCode.equals(RTCErrorCode.RongRTCCodeHttpTimeoutError)) {
                    localUser.publishStream(fileVideoOutputStream, new IRCRTCResultCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailed(RTCErrorCode errorCode) {
                            publishCustomStreamFailed();
                        }
                    });
                } else {
                   publishCustomStreamFailed();
                }
            }
        });
    }

    private void publishCustomStreamFailed(){
        postUIThread(new Runnable() {
            @Override
            public void run() {
                btnCustomStream.setSelected(false);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void requestForScreenCast() {
        if (!screenCastEnable) {
            String toastInfo = getResources().getString(R.string.screen_cast_disabled);
            Toast.makeText(this, toastInfo, Toast.LENGTH_SHORT).show();
            return;
        }
        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(manager.createScreenCaptureIntent(), SCREEN_CAPTURE_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void cancelScreenCast(final boolean isHangup) {
        if (screenOutputStream == null || screenCastHelper == null) {
            return;
        }
        screenCastHelper.stop();
        screenCastHelper = null;
        localUser.unpublishStream(screenOutputStream, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        renderViewManager.removeVideoView(true, myUserId, screenOutputStream.getTag());
                        screenOutputStream = null;
                        if (isHangup) {
                            disconnect();
                        }
                    }
                });
            }

            @Override
            public void onFailed(final RTCErrorCode errorCode) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CallActivity.this, "取消发布屏幕共享失败:" + errorCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != SCREEN_CAPTURE_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return;
        }
        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        videoConfigBuilder.setVideoResolution(RCRTCVideoResolution.RESOLUTION_720_1280);
        videoConfigBuilder.setVideoFps(RCRTCVideoFps.Fps_10);
        screenOutputStream = RCRTCEngine.getInstance()
            .createVideoStream(RongRTCScreenCastHelper.VIDEO_TAG, videoConfigBuilder.build());
        screenCastHelper = new RongRTCScreenCastHelper();
        screenCastHelper.init(this, screenOutputStream, data, 720, 1280);

        RCRTCVideoView videoView = new RCRTCVideoView(this);
        screenOutputStream.setVideoView(videoView);
        renderViewManager.setVideoView(true, myUserId,
            screenOutputStream.getTag(), iUserName, videoView, RongRTCTalkTypeUtil.O_CAMERA);
        screenCastHelper.start();

        localUser.publishStream(screenOutputStream, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        renderViewManager.userJoin(myUserId, screenOutputStream.getTag(), iUserName, RongRTCTalkTypeUtil.O_CAMERA);
                    }
                });
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        String toastInfo = getResources().getString(R.string.publish_failed);
                        Toast.makeText(CallActivity.this, toastInfo, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showMembersDialog() {
        MembersDialog dialog;
        Fragment fragment = getFragmentManager().findFragmentByTag("MembersDialog");
        if (fragment == null) {
            dialog = new MembersDialog();
            dialog.setKickUserListener(new MembersDialog.onKickUserListener() {
                @Override
                public void onKick(String userId, String name) {
                    kickMember(userId, name);
                }
            });
        } else {
            dialog = (MembersDialog) fragment;
        }
        dialog.update(mMembers, adminUserId);
        dialog.show(getFragmentManager(), "MembersDialog");
    }

    private void kickMember(final String userId, final String name) {
        String message = String.format(getString(R.string.member_operate_kick), name);
        PromptDialog dialog = PromptDialog.newInstance(this, "", message);
        dialog.setPromptButtonClickedListener(new PromptDialog.OnPromptButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked() {
                if (isFinishing()) {
                    return;
                }
                RoomKickOffMessage kickOffMessage = new RoomKickOffMessage(userId);
                room.sendMessage(kickOffMessage, null);
            }

            @Override
            public void onNegativeButtonClicked() {}
        });
        dialog.show();
    }

    private void selectAdmin() {
        if (!TextUtils.equals(myUserId, adminUserId) || mMembersMap.size() <= 1) return;
        UserInfo userInfo = mMembersMap.get(mMembers.get(1).userId);
        if (userInfo == null) return;
        RoomInfoMessage roomInfoMessage = new RoomInfoMessage(
            userInfo.userId, userInfo.userName, userInfo.joinMode, userInfo.timestamp, true);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userInfo.userId);
            jsonObject.put("userName", userInfo.userName);
            jsonObject.put("joinMode", userInfo.joinMode);
            jsonObject.put("joinTime", userInfo.timestamp);
            jsonObject.put("master", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        room.setRoomAttributeValue(jsonObject.toString(), userInfo.userId, roomInfoMessage, null);
    }

    private void updateMembersDialog() {
        FinLog.d(TAG, "[MemberList] updateMembersDialog ==>  MemberSize=" + mMembers.size());
        Fragment fragment = getFragmentManager().findFragmentByTag("MembersDialog");
        if (fragment != null) {
            sortRoomMembers();
            MembersDialog dialog = (MembersDialog) fragment;
            dialog.update(mMembers, adminUserId);
        }
    }

    private void toggleCameraMicViewStatus() {
        Log.i(TAG, "toggleCameraMicViewStatus() isObserver = " + isObserver + " isVideoMute = " + isVideoMute);
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
    protected void onStart() {
        super.onStart();
        if (mGlHandler != null && mFURenderer != null) {
            mGlHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "onStart surfaceCreated");
                    mFURenderer.onSurfaceCreated();
                }
            });
        }
        if (isInRoom) {
            RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(-1,!mMirrorVideoFrame,null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnCustomAudioStream.setSelected(AudioMixFragment.alive);
    }

    private void startBluetoothSco() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            if (am.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                am.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }
            am.setSpeakerphoneOn(false);
            am.startBluetoothSco();
        }
    }

    @Override
    public void onNotifySCOAudioStateChange(int scoAudioState) {
        switch (scoAudioState) {
            case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (am != null) {
                    am.setBluetoothScoOn(true);
                }
                break;
            case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                Log.d("onNotifyHeadsetState",
                    "onNotifySCOAudioStateChange: " + headsetPlugReceiver.isBluetoothConnected());
                if (headsetPlugReceiver.isBluetoothConnected()) {
                    startBluetoothSco();
                }
                break;
        }
    }

    @Override
    public void onNotifyHeadsetState(boolean connected, int type) {
        try {
            if (connected) {
                HeadsetPlugReceiverState = true;
                if (type == 0) {
                    startBluetoothSco();
                }
                if (null != btnMuteSpeaker) {
                    btnMuteSpeaker.setBackgroundResource(R.drawable.img_capture_gray);
                    btnMuteSpeaker.setSelected(false);
                    btnMuteSpeaker.setEnabled(false);
                    btnMuteSpeaker.setClickable(false);
                    audioManager.onToggleSpeaker(false);
                }
            } else {
                if (type == 1 && BluetoothUtil.hasBluetoothA2dpConnected()) {
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
                        am.setSpeakerphoneOn(!muteSpeaker);
                    } else {
                        RCRTCEngine.getInstance().enableSpeaker(!this.muteSpeaker);
                    }
                    audioManager.onToggleSpeaker(!muteSpeaker);
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

    /**
     * 镜像翻转采集的视频数据
     *
     * @param rtcVideoFrame
     */
    private void onMirrorVideoFrame(RCRTCVideoFrame rtcVideoFrame) {
        boolean isFrontCamera = RCRTCEngine.getInstance().getDefaultVideoStream().isFrontCamera();
        if (!mMirrorVideoFrame || mMirrorHelper == null || !isFrontCamera) {
            return;
        }
        long start = System.nanoTime();
        if (rtcVideoFrame.getCaptureType() == CaptureType.TEXTURE) {
            int newTextureId = mMirrorHelper.onMirrorImage(
                rtcVideoFrame.getTextureId(), rtcVideoFrame.getWidth(), rtcVideoFrame.getHeight());
            rtcVideoFrame.setTextureId(newTextureId);
        } else {
            byte[] frameBytes = mMirrorHelper.onMirrorImage(
                rtcVideoFrame.getData(), rtcVideoFrame.getWidth(), rtcVideoFrame.getHeight());
            rtcVideoFrame.setData(frameBytes);
        }
        Log.d(TAG, "onMirrorVideoFrame: " + (System.nanoTime() - start) * 1.0 / 1000000);
    }

    private @Nullable FileOutputStream localFileStream;
    private @Nullable FileChannel localFileChanel;
    private @Nullable FileOutputStream remoteFileStream;
    private @Nullable FileChannel remoteFileChanel;

    private void createDebugPcmFile() {
        try {
            String roomId = "";
            roomId = room.getRoomId();
            String localName =
                    RongIMClient.getInstance().getCurrentUserId() + "_" + roomId + "_local.pcm";
            String remoteName =
                    RongIMClient.getInstance().getCurrentUserId() + "_" + roomId + "_remote.pcm";
            File parent_path = Environment.getExternalStorageDirectory();

            File dir = new File(parent_path.getAbsoluteFile(), "webrtc");
            dir.mkdir();

            File file = new File(dir.getAbsoluteFile(), localName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            localFileStream = new FileOutputStream(file);
            localFileChanel = localFileStream.getChannel();
            RLog.d(TAG, "create file success " + file.getAbsolutePath());
            file = new File(dir.getAbsoluteFile(), remoteName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            remoteFileStream = new FileOutputStream(file);
            remoteFileChanel = remoteFileStream.getChannel();
            RLog.d(TAG, "create file success " + file.getAbsolutePath());
        } catch (IOException e) {
            RLog.d(TAG, "create file failed");
        }
    }

    private void writePcmBuffer(ByteBuffer buffer, FileChannel channel) {
        try {
            if (channel != null) {
                channel.write(buffer);
                Log.d(TAG, "write buffer success.");
            }
        } catch (IOException e) {
            RLog.d(TAG, "write file failed");
        }
    }

    private void closePcmFile(FileChannel channel, FileOutputStream fos) {
        try {
            if (channel != null) channel.close();
            if (fos != null) {
                fos.flush();
                fos.close();
            }
            channel = null;
            fos = null;
        } catch (IOException e) {
            RLog.d(TAG, "close file failed");
        }
    }

    private int onDrawWater(int width, int height, int textureID) {
        boolean isFrontCamera = RCRTCEngine.getInstance().getDefaultVideoStream().isFrontCamera();
        if (mWaterFilter == null) {
            Bitmap logoBitmap = TextureHelper.loadBitmap(this, R.drawable.logo);
            mWaterFilter = new WaterMarkFilter(this, isFrontCamera, logoBitmap);
        }
        mWaterFilter.drawFrame(width, height, textureID, isFrontCamera);
        return mWaterFilter.getTextureID();
    }

    /*------------------------------------白板，集成第三方 here white 服务 start --------------------------*/
    private void joinWhiteBoardRoom(final String uuid, final String roomToken) {
        RLog.d(TAG, "here white uuid = " + uuid + " , roomToken = " + roomToken);
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1);
        WhiteSdk whiteSdk = new WhiteSdk(whiteboardView, CallActivity.this, configuration,
            // 如果需要拦截并重写 URL ,请实现如下方法, 但请注意不要每次都生成不同的 URL,尽量 cache 结果并请在必要的时候进行更新(如 更新私有
            // Token)
            new UrlInterrupter() {
                @Override
                public String urlInterrupter(String s) {
                    // 修改资源 URL
                    return s;
                }
        });
        whiteSdk.joinRoom(new RoomParams(uuid, roomToken), new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                RLog.d(TAG, "here white AbstractRoomCallbacks onPhaseChanged");
            }

            @Override
            public void onBeingAbleToCommitChange(boolean isAbleToCommit) {
                RLog.d(TAG, "here white AbstractRoomCallbacks onPhaseChanged");
            }

            @Override
            public void onRoomStateChanged(final RoomState modifyState) {
                RLog.d(TAG, "here white AbstractRoomCallbacks onRoomStateChanged");
                if (modifyState != null && modifyState.getSceneState() != null) {
                    updateSceneState(modifyState.getSceneState());
                }
                if (modifyState != null && modifyState.getZoomScale() != null) {
                    RLog.d(TAG, "here white AbstractRoomCallbacks zoom scale changed = " + modifyState.getZoomScale());
                }
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
                RLog.d(TAG, "here white AbstractRoomCallbacks onCatchErrorWhenAppendFrame userId = " +
                    userId + " ，error = " + error.getMessage());
            }

            @Override
            public void onDisconnectWithError(Exception e) {
                RLog.d(TAG, "here white joinRoom onDisconnectWithError = " + e.getMessage());
            }

            @Override
            public void onKickedWithReason(String reason) {
                RLog.d(TAG, "here white joinRoom onKickedWithReason reason = " + reason);
            }}, new Promise<Room>() {
                    @Override
                    public void then(final Room room) {
                        progressDialog.dismiss();
                        RLog.d(TAG, "here white joinRoom Promise  Room");
                        room.zoomChange(0.3);
                        if (isObserver) {
                            room.disableOperations(true);
                        }
                        whiteBoardRoom = room;
                        initPencilColor();
                        bindWhiteBoardActions(room);
                        room.getSceneState(new Promise<SceneState>() {
                            @Override
                            public void then(SceneState sceneState) {
                                RLog.d(TAG, "here white joinRoom and then getSceneState");
                                if (sceneState != null) {
                                    if (sceneState.getScenePath().equals(WhiteBoardApi.WHITE_BOARD_INIT_SCENE_PATH)) {
                                        currentSceneName = generateSceneName();
                                        whiteBoardScenes = new Scene[] {
                                            new Scene(currentSceneName, new PptPage("", 0d, 0d))};
                                        room.putScenes(WhiteBoardApi.WHITE_BOARD_SCENE_PATH, whiteBoardScenes, 0);
                                        room.setScenePath(
                                            WhiteBoardApi.WHITE_BOARD_SCENE_PATH + "/" + currentSceneName);
                                        whiteboardView.setVisibility(View.VISIBLE);
                                        currentSceneIndex = 0;
                                    } else {
                                        updateSceneState(sceneState);
                                        room.setScenePath(
                                            WhiteBoardApi.WHITE_BOARD_SCENE_PATH + "/" + currentSceneName);
                                    }
                                }
                            }

                            @Override
                            public void catchEx(SDKError sdkError) {
                                RLog.d(TAG,
                                    "here white joinRoom and then getSceneState error = " + sdkError.getMessage());
                            }
                        });
                    }

                    @Override
                    public void catchEx(SDKError t) {
                        RLog.d(TAG, "here white joinRoom Promise  catchEx = " + t.toString());
                        progressDialog.dismiss();
                        updateCallActionsWithWhiteBoard(false);
                        hidePencilColorPopupWindow();
                        showToast(R.string.white_board_service_error);
                    }
        });
    }

    private void updateSceneState(SceneState sceneState) {
        currentSceneIndex = sceneState.getIndex();
        whiteBoardScenes = sceneState.getScenes();
        String scenePath = sceneState.getScenePath();
        currentSceneName = scenePath.substring(scenePath.lastIndexOf("/") + 1);
        updateWhiteBoardPagesView();
    }

    private void bindWhiteBoardActions(final Room room) {
        // 画笔颜色
        whiteBoardAction.findViewById(R.id.white_action_pencil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePencilColorPopupWindow();
                pencilColorPopupWindow = new PencilColorPopupWindow(CallActivity.this, room);
                pencilColorPopupWindow.show(v);
            }
        });
        // 文字输入
        whiteBoardAction.findViewById(R.id.white_action_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemberState memberState = new MemberState();
                memberState.setCurrentApplianceName(Appliance.TEXT);
                room.setMemberState(memberState);
            }
        });
        // 橡皮擦
        whiteBoardAction.findViewById(R.id.white_action_eraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemberState memberState = new MemberState();
                memberState.setCurrentApplianceName(Appliance.ERASER);
                room.setMemberState(memberState);
            }
        });
        // 清空当前页
        whiteBoardAction.findViewById(R.id.white_action_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room != null) {
                    room.cleanScene(true);
                }
            }
        });
        // 新增白板页
        whiteBoardAction.findViewById(R.id.white_action_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sceneName = generateSceneName();
                Scene[] scenes = new Scene[] {new Scene(sceneName, new PptPage("", 0d, 0d))};
                room.putScenes(WhiteBoardApi.WHITE_BOARD_SCENE_PATH, scenes, Integer.MAX_VALUE);
                room.setScenePath(WhiteBoardApi.WHITE_BOARD_SCENE_PATH + "/" + sceneName);
                currentSceneName = sceneName;
            }
        });
        // 删除当前白板页
        whiteBoardAction.findViewById(R.id.white_action_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (whiteBoardScenes.length == 1) {
                    room.cleanScene(true);
                } else {
                    room.removeScenes(WhiteBoardApi.WHITE_BOARD_SCENE_PATH + "/" + currentSceneName);
                }
            }
        });
        // 上一页
        whiteBoardPagesPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSceneIndex > 0) {
                    String sceneName = whiteBoardScenes[--currentSceneIndex].getName();
                    room.setScenePath(WhiteBoardApi.WHITE_BOARD_SCENE_PATH + "/" + sceneName);
                }
                updateWhiteBoardPagesView();
            }
        });
        // 下一页
        whiteBoardPagesNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSceneIndex < whiteBoardScenes.length - 1) {
                    String sceneName = whiteBoardScenes[++currentSceneIndex].getName();
                    room.setScenePath(WhiteBoardApi.WHITE_BOARD_SCENE_PATH + "/" + sceneName);
                }
                updateWhiteBoardPagesView();
            }
        });
        // 关闭白板功能按钮
        whiteBoardClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCallActionsWithWhiteBoard(false);
                hidePencilColorPopupWindow();
                if (whiteBoardRoom != null) {
                    whiteBoardRoom.disconnect();
                    whiteBoardRoom = null;
                }
            }
        });
    }

    private void initPencilColor() {
        int color = getResources().getColor(R.color.white_board_pencil_color_red);
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[] {red, green, blue});
        memberState.setCurrentApplianceName(Appliance.PENCIL);
        whiteBoardRoom.setMemberState(memberState);
    }

    private void updateWhiteBoardPagesView() {
        if (currentSceneIndex == 0) {
            whiteBoardPagesPrevious.setEnabled(false);
        } else {
            whiteBoardPagesPrevious.setEnabled(true);
        }
        if (currentSceneIndex == whiteBoardScenes.length - 1) {
            whiteBoardPagesNext.setEnabled(false);
        } else {
            whiteBoardPagesNext.setEnabled(true);
        }
    }

    private String generateSceneName() {
        String userId = RongIMClient.getInstance().getCurrentUserId();
        return shortMD5(userId, System.currentTimeMillis() + "");
    }

    private void setRoomWhiteBoardInfo(final String uuid, final String roomToken) {
        whiteBoardRoomInfo = new WhiteBoardRoomInfo(uuid, roomToken);
        WhiteBoardInfoMessage whiteBoardInfoMessage = new WhiteBoardInfoMessage(uuid, roomToken);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("roomToken", roomToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        room.setRoomAttributeValue(jsonObject.toString(), WhiteBoardApi.WHITE_BOARD_KEY, whiteBoardInfoMessage,
            new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    RLog.d(TAG, "here white setRoomWhiteBoardInfo success");
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    RLog.d(TAG, "here white setRoomWhiteBoardInfo error = " + errorCode.getValue());
                }
            });
    }

    private void hidePencilColorPopupWindow() {
        if (pencilColorPopupWindow != null) {
            pencilColorPopupWindow.dismiss();
        }
    }

    private void updateCallActionsWithWhiteBoard(boolean hideCallAction) {
        if (hideCallAction) {
            buttonHangUp.setVisibility(View.GONE);
            mcall_more_container.setVisibility(View.GONE);
            titleContainer.setVisibility(View.GONE);
            btnCloseCamera.setVisibility(View.GONE);
            btnMuteMic.setVisibility(View.GONE);
            whiteboardContainer.setVisibility(View.VISIBLE);
            if (!isObserver) {
                whiteBoardAction.setVisibility(View.VISIBLE);
                whiteBoardPagesPrevious.setVisibility(View.VISIBLE);
                whiteBoardPagesNext.setVisibility(View.VISIBLE);
            } else {
                RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) whiteboardView.getLayoutParams();
                layoutParams.bottomMargin = 0;
                whiteboardView.setLayoutParams(layoutParams);
            }
            whiteBoardClose.setVisibility(View.VISIBLE);
        } else {
            btnCloseCamera.setVisibility(View.VISIBLE);
            btnMuteMic.setVisibility(View.VISIBLE);
            buttonHangUp.setVisibility(View.VISIBLE);
            mcall_more_container.setVisibility(View.VISIBLE);
            titleContainer.setVisibility(View.VISIBLE);
            whiteboardContainer.setVisibility(View.GONE);
            whiteBoardAction.setVisibility(View.GONE);
            whiteBoardPagesPrevious.setVisibility(View.GONE);
            whiteBoardPagesNext.setVisibility(View.GONE);
            whiteBoardClose.setVisibility(View.GONE);
        }
    }

    private void deleteRTCWhiteBoardAttribute() {
        // rtc room里最后一个用户清除白板属性
        if (whiteBoardRoomInfo != null && room !=null
            && (room.getRemoteUsers() == null
            || room.getRemoteUsers().size() == 0
            || allObserver())) {
            deleteWhiteBoardRoom();
            List<String> attributes = Collections.singletonList(WhiteBoardApi.WHITE_BOARD_KEY);
            room.deleteRoomAttributes(attributes, null, new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    RLog.d(TAG, "here white delete rtc room attributes success ");
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    RLog.d(TAG, "here white delete rtc room attributes error =  " + errorCode.getValue());
                }
            });
        }
    }

    private boolean allObserver() {
        if (mMembersMap != null) {
            for (UserInfo userInfo : mMembersMap.values()) {
                if (userInfo.joinMode != RoomInfoMessage.JoinMode.OBSERVER) {
                    return false;
                }
            }
        }
        return true;
    }

    private void clearWhiteBoardInfoIfNeeded() {
        if (isObserver && room != null) {
            if (room.getRemoteUsers() == null
                    || room.getRemoteUsers().size() == 0
                    || allObserver()) {
                whiteBoardRoomInfo = null;
            }
        }
    }

    private void deleteWhiteBoardRoom() {
        if (whiteBoardRoomInfo != null) {
            WhiteBoardApi.deleteRoom(whiteBoardRoomInfo.getUuid(), new HttpClient.ResultCallback() {
                @Override
                public void onResponse(String result) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    try {
                        JSONObject msg = new JSONObject(result);
                        int resultCode = msg.getInt("code");
                        if (resultCode == 200) {
                            RLog.d(TAG, "here white deleteRoom success ");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }

                @Override
                public void onFailure(int errorCode) {
                    RLog.d(TAG, "here white deleteRoom errorCode = " + errorCode);
                }
            });
        }
    }

    private String shortMD5(String... args) {
        try {
            StringBuilder builder = new StringBuilder();

            for (String arg : args) {
                builder.append(arg);
            }

            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(builder.toString().getBytes());
            byte[] mds = mdInst.digest();
            mds = Base64.encode(mds, Base64.NO_WRAP);
            String result = new String(mds);
            result = result.replace("=", "").replace("+", "-").replace("/", "_").replace("\n", "");
            return result;
        } catch (Exception e) {
            RLog.e(TAG, "shortMD5", e);
        }
        return "";
    }
    /*------------------------------------白板，集成第三方 here white 服务 end --------------------------*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onKickedOfflineEvent(KickedOfflineEvent offlineEvent) {
        Log.i(TAG, "kicked offline ");
        this.finish();
    }

    static class MyCallSettingFragmentListener implements CallSettingsFragment.CallSettingFragmentListener {

        CallActivity activity;

        public MyCallSettingFragmentListener(CallActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onSwitchAudioOptions(boolean isOn) {
            SessionManager.getInstance().put(SettingActivity.IS_AUDIO_MUSIC, isOn);
            AudioScenario audioScenario = isOn ? AudioScenario.MUSIC : AudioScenario.DEFAULT;
            RCRTCEngine.getInstance().getDefaultAudioStream().changeAudioScenario(audioScenario, null);
        }

        @Override
        public void onUploadClickEvents() {
            //            FwLog.upload();
            //            activity.showToast("日志上传中");
        }
    }
}
