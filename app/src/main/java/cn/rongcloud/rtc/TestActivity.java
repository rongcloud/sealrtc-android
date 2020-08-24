package cn.rongcloud.rtc;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.rongcloud.rtc.api.RCRTCAudioMixer;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCLocalUser;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCOnStreamSendListener;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.callback.IRCRTCStatusReportListener;
import cn.rongcloud.rtc.api.report.StatusReport;
import cn.rongcloud.rtc.api.stream.RCRTCFileVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.utils.FinLog;
import io.rong.imlib.model.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huichao li
 */
public class TestActivity extends RongRTCBaseActivity implements View.OnClickListener, OnSubscribeListener {

    private static final String TAG = TestActivity.class.getSimpleName();
    private RelativeLayout localContainer, localCustomContainer;
    private TextView resultView;
    private Button audioButton, videoButton, audioAndVideoButton, customVideoButton, quitRoomButton, test_mediaServerButton;
    private Button cameraButton, micButton;
    private RCRTCRoom rtcRoom;
    boolean audioPublished, videoPublished, avPublished, customVideoPublished = false;
    private LinearLayout remoteVideoContainer;
    private ListView streamListView;
    private TestActivityAdapter streamAdapter;
    private ConcurrentHashMap<String, View> viewMap = new ConcurrentHashMap<String, View>();
    private final Object viewContainerLock = new Object();
    private RCRTCVideoView localVideoView;
    private RCRTCVideoView localCustomVideoView;
    RCRTCFileVideoOutputStream fileVideoOutputStream = null;
    private boolean startCamera = false;
    private boolean microphoneDisable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        localContainer = findViewById(R.id.test_local_container);
        localCustomContainer = findViewById(R.id.test_local_container_custom);
        resultView = findViewById(R.id.test_result);
        audioButton = findViewById(R.id.test_publish_audio);
        videoButton = findViewById(R.id.test_publish_video);
        audioAndVideoButton = findViewById(R.id.test_publish_audio_video);
        customVideoButton = findViewById(R.id.test_publish_custom_video);
        quitRoomButton = findViewById(R.id.test_quit);
        remoteVideoContainer = findViewById(R.id.test_remote_video_container);
        streamListView = findViewById(R.id.test_remote_resource_list);
        test_mediaServerButton = findViewById(R.id.test_mediaServer);
        test_mediaServerButton.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        cameraButton = findViewById(R.id.test_Camera);
        micButton = findViewById(R.id.test_Mic);

        setupListeners();

        rtcRoom = RCRTCEngine.getInstance().getRoom();
        rtcRoom.registerRoomListener(roomEventsListener);
        RCRTCEngine.getInstance().registerStatusReportListener(statusReportListener);
        streamAdapter = new TestActivityAdapter(this, getRemoteUsers(), this);
        streamListView.setAdapter(streamAdapter);
        streamAdapter.notifyDataSetChanged();
        localVideoView = new RCRTCVideoView(TestActivity.this.getApplicationContext());
        localCustomVideoView = new RCRTCVideoView(TestActivity.this.getApplicationContext());
        fileVideoOutputStream = RCRTCEngine.getInstance().createFileVideoOutputStream("file:///android_asset/video_1.mp4", false, true, "FileVideo", RCRTCVideoStreamConfig.Builder.create().setVideoResolution(RCRTCVideoResolution.RESOLUTION_360_640).build());
        camera(false);
    }

    private List<RCRTCRemoteUser> getRemoteUsers() {
        if (rtcRoom == null || rtcRoom.getRemoteUsers() == null) {
            return null;
        }
        return rtcRoom.getRemoteUsers();
    }

    private void setupListeners() {
        audioButton.setOnClickListener(this);
        videoButton.setOnClickListener(this);
        audioAndVideoButton.setOnClickListener(this);
        customVideoButton.setOnClickListener(this);
        quitRoomButton.setOnClickListener(this);
        test_mediaServerButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        micButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == audioButton) {
            publishAudio(!audioPublished);
            audioPublished = !audioPublished;
        } else if (view == videoButton) {
            publishVideo(!videoPublished);
            videoPublished = !videoPublished;
        } else if (view == audioAndVideoButton) {
            publishAudioAndVideo(!avPublished);
            avPublished = !avPublished;
        } else if (view == customVideoButton) {
            publishCustomStream(!customVideoPublished);
            customVideoPublished = !customVideoPublished;
        } else if (view == quitRoomButton) {
            Toast.makeText(this, "正在退出...", Toast.LENGTH_SHORT).show();
            RCRTCAudioMixer.getInstance().stop();
            RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    TestActivity.this.finish();
                    Toast.makeText(TestActivity.this, "退出成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(final RTCErrorCode errorCode) {
                    TestActivity.this.finish();
                    Toast.makeText(TestActivity.this, "退出失败：" + errorCode.getReason(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (view == test_mediaServerButton) {
            showDialog();
        } else if (view == cameraButton) {
            if (startCamera) {
                Toast.makeText(this, "关闭摄像头", Toast.LENGTH_SHORT).show();
                RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
                camera(false);
            } else {
                Toast.makeText(this, "打开摄像头", Toast.LENGTH_SHORT).show();
                RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(-1, false, null);
                camera(true);
            }
        } else if (view == micButton) {
            if (microphoneDisable) {
                mic(false);
                Toast.makeText(this, "关闭Mic", Toast.LENGTH_SHORT).show();
                RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(microphoneDisable);
            } else {
                mic(true);
                Toast.makeText(this, "打开Mic", Toast.LENGTH_SHORT).show();
                RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(microphoneDisable);
            }
        }
    }

    private void mic(boolean val) {
        microphoneDisable = val;
        micButton.setText(microphoneDisable ? "打开Mic" : "关闭Mic");
    }

    private void camera(boolean val) {
        startCamera = val;
        cameraButton.setText(startCamera ? "关闭摄像头" : "打开摄像头");
    }

    private Dialog mDialog;
    private View dialogView;
    private Button btn_ok, btn_cancele;
    private EditText mMediaServer_et;

    private void showDialog() {
        LayoutInflater inflater = LayoutInflater.from(TestActivity.this);
        dialogView = inflater.inflate(R.layout.layout_activity_dialog, null);
        RelativeLayout layout = (RelativeLayout) dialogView.findViewById(R.id.rel_data);
        btn_ok = (Button) layout.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(btnClickListener);
        btn_cancele = (Button) layout.findViewById(R.id.btn_cancel);
        btn_cancele.setOnClickListener(btnClickListener);
        mMediaServer_et = layout.findViewById(R.id.edit_appid);

        String TestMediaUrl = SessionManager.getInstance().getString("QuickTestMeidaServerUrl");
        if (!TextUtils.isEmpty(TestMediaUrl)) {
            mMediaServer_et.setText(TestMediaUrl);
        }

        mDialog = new Dialog(TestActivity.this, R.style.loadingdata_dialog);
        mDialog.setCancelable(false);//bu可以用“返回键”取消
        mDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mDialog.setContentView(layout);// 设置布局
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        if (!TestActivity.this.isFinishing()) {
            mDialog.show();
        }
    }

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_ok) {
                if (mMediaServer_et != null) {
                    String server = mMediaServer_et.getText().toString().trim();
                    if (!TextUtils.isEmpty(server)) {
                        FinLog.e(TAG, "TestMediaUrl: " + server);
                        RCRTCEngine.getInstance().setMediaServerUrl(server);
                        SessionManager.getInstance().put("QuickTestMeidaServerUrl", server);
                        Toast.makeText(TestActivity.this, "设置成功", Toast.LENGTH_SHORT).show();

                        if (!TestActivity.this.isFinishing() && null != mDialog && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    }
                }
            } else if (v.getId() == R.id.btn_cancel) {
                if (!TestActivity.this.isFinishing() && null != mDialog && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        }
    };

    private void publishAudio(boolean isPublish) {
        RCRTCLocalUser localUser = rtcRoom.getLocalUser();
        if (localUser == null) {
            return;
        }
        if (isPublish) {
            mic(false);
            localUser.publishStream(localUser.getDefaultAudioStream(), new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    resultView.setText("音频发布成功");
                    audioButton.setText("取消音频");
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    resultView.setText("音频发布失败: " + errorCode);
                }
            });
        } else {
            mic(true);
            localUser.unpublishStream(localUser.getDefaultAudioStream(), new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    resultView.setText("音频取消发布成功");
                    audioButton.setText("音频");
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    resultView.setText("音频取消发布失败: " + errorCode);
                }
            });
        }
    }

    private void publishVideo(final boolean isPublish) {
        final RCRTCLocalUser localUser = rtcRoom.getLocalUser();
        if (localUser != null) {
            if (isPublish) {
                RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
                camera(true);
                localUser.publishStream(localUser.getDefaultVideoStream(), new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        resultView.setText("视频发布成功");
                        videoButton.setText("取消视频");
                        localUser.getDefaultVideoStream().setVideoView(localVideoView);
                        localContainer.removeAllViews();
                        localContainer.addView(localVideoView);
                    }

                    @Override
                    public void onFailed(RTCErrorCode errorCode) {
                        resultView.setText("视频发布失败: " + errorCode);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        localContainer.removeAllViews();
                    }
                });
                RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
                camera(false);
                localUser.unpublishStream(localUser.getDefaultVideoStream(), new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        resultView.setText("视频取消发布成功");
                        videoButton.setText("视频");
                    }

                    @Override
                    public void onFailed(RTCErrorCode errorCode) {
                        resultView.setText("视频取消发布失败: " + errorCode);
                    }
                });
            }
        }
    }

    private void publishAudioAndVideo(final boolean isPublish) {
        final RCRTCLocalUser localUser = rtcRoom.getLocalUser();
        if (localUser == null) {
            return;
        }
        if (isPublish) {
            RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
            camera(true);
            mic(false);
            localUser.publishDefaultStreams(new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    resultView.setText("发布音视频成功");
                    audioAndVideoButton.setText("取消音视频");
                    localUser.getDefaultVideoStream().setVideoView(localVideoView);
                    localContainer.removeAllViews();
                    localContainer.addView(localVideoView);
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    resultView.setText("发布音视频失败: " + errorCode);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    localContainer.removeAllViews();
                }
            });
            RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
            camera(false);
            mic(true);
            localUser.unpublishDefaultStreams(new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    resultView.setText("取消发布音视频成功");
                    audioAndVideoButton.setText("音视频");
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    resultView.setText("取消发布音视频失败: " + errorCode);
                }
            });
        }
    }

    private void publishCustomStream(final boolean isPubllish) {
        if (isPubllish) {
            fileVideoOutputStream.setOnSendListener(new IRCRTCOnStreamSendListener() {
                @Override
                public void onStart(final RCRTCVideoOutputStream stream) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            customVideoButton.setText("取消自定义");
                            resultView.setText("发布自定义视频成功");
                            stream.setVideoView(localCustomVideoView);
                            localCustomContainer.removeAllViews();
                            localCustomContainer.addView(localCustomVideoView);
                        }
                    });
                }

                @Override
                public void onComplete(final RCRTCVideoOutputStream stream) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            customVideoButton.setText("自定义");
                            resultView.setText("自定义视频发送完成");
                            localCustomContainer.removeAllViews();
                        }
                    });
                }

                @Override
                public void onFailed() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            customVideoButton.setText("取消自定义");
                            resultView.setText("发布自定义视频失败");
                        }
                    });
                }
            });
            rtcRoom.getLocalUser().publishStream(fileVideoOutputStream, new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                }
            });
        } else {
            rtcRoom.getLocalUser().unpublishStream(fileVideoOutputStream, new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    customVideoButton.setText("自定义");
                    resultView.setText("取消发布自定义视频成功");
                    localCustomContainer.removeAllViews();
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultView.setText("取消发布自定义视频失败");
                        }
                    });
                }
            });
        }
    }

    IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser remoteUser, List<RCRTCInputStream> publishResource) {
            streamAdapter.updateData(getRemoteUsers());
            streamAdapter.notifyDataSetChanged();
        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser remoteUser, RCRTCInputStream stream, boolean mute) {
        }

        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser remoteUser, RCRTCInputStream stream, boolean mute) {
        }

        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser remoteUser, List<RCRTCInputStream> unPublishResource) {
            streamAdapter.updateData(getRemoteUsers());
            streamAdapter.notifyDataSetChanged();
            for (RCRTCInputStream inputStream : unPublishResource) {
                if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                    removeVideoView(remoteUser.getUserId(), inputStream.getTag());
                }
            }
        }

        @Override
        public void onUserJoined(RCRTCRemoteUser remoteUser) {
        }

        @Override
        public void onUserLeft(RCRTCRemoteUser remoteUser) {
            streamAdapter.updateData(getRemoteUsers());
            streamAdapter.notifyDataSetChanged();
            removeVideoView(remoteUser.getUserId(), "");
        }

        @Override
        public void onUserOffline(RCRTCRemoteUser remoteUser) {
            streamAdapter.updateData(getRemoteUsers());
            streamAdapter.notifyDataSetChanged();
            removeVideoView(remoteUser.getUserId(), "");
        }

        @Override
        public void onVideoTrackAdd(String userId, String tag) {
        }

        @Override
        public void onLeaveRoom(int reasonCode) {
        }

        @Override
        public void onReceiveMessage(Message message) {
        }

        @Override
        public void onKickedByServer() {
        }
    };

    IRCRTCStatusReportListener statusReportListener = new IRCRTCStatusReportListener() {
        @Override
        public void onAudioReceivedLevel(HashMap<String, String> audioLevel) {
        }

        @Override
        public void onAudioInputLevel(String audioLevel) {
        }

        @Override
        public void onConnectionStats(final StatusReport statusReport) {
            if (streamAdapter != null) {
                streamAdapter.updateStatusReport(statusReport);
            }
        }
    };

    @Override
    public void onSubscribe(String userId, final RCRTCInputStream inputStream) {
        if (inputStream.getMediaType() != RCRTCMediaType.VIDEO) {
            return;
        }
        synchronized (viewContainerLock) {
            RCRTCVideoView videoView = new RCRTCVideoView(TestActivity.this.getApplicationContext());
            viewMap.put(userId + inputStream.getTag(), videoView);
            ((RCRTCVideoInputStream) inputStream).setVideoView(videoView);
            remoteVideoContainer.addView(videoView, 150, 150);
        }
    }

    @Override
    public void onUnsubscribe(String userId, final RCRTCInputStream inputStream) {
        if (inputStream.getMediaType() != RCRTCMediaType.VIDEO) {
            return;
        }
        if (viewMap.containsKey(userId + inputStream.getTag())) {
            TestActivity.this.removeVideoView(userId, inputStream.getTag());
        }
    }

    private void removeVideoView(String userId, String tag) {
        synchronized (viewContainerLock) {
            if (TextUtils.isEmpty(tag)) {
                // 用户离开，删除所有对应view
                List<Object> streamList = Arrays.asList(viewMap.keySet().toArray());
                List<String> streamIdList = new ArrayList<>();
                for (int i = 0; i < streamList.size(); i++) {
                    String streamId = (String) streamList.get(i);
                    if (streamId.contains(userId)) {
                        streamIdList.add(streamId);
                    }
                }
                for (String streamId : streamIdList) {
                    removeSingleView(streamId, "");
                }

            } else {
                removeSingleView(userId, tag);
            }
        }
    }

    private void removeSingleView(String userId, String tag) {
        int index = -1;
        for (int i = 0; i < remoteVideoContainer.getChildCount(); i++) {
            RCRTCVideoView videoView = (RCRTCVideoView) remoteVideoContainer.getChildAt(i);
            if (videoView == viewMap.get(userId + tag)) {
                index = i;
                break;
            }
        }
        viewMap.remove(userId + tag);
        if (index >= 0) {
            remoteVideoContainer.removeViewAt(index);
        }
    }
}
