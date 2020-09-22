package cn.rongcloud.rtc.instrumentationtest;

import android.os.Bundle;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCLocalUser;
import cn.rongcloud.rtc.api.callback.IRCRTCOnStreamSendListener;
import cn.rongcloud.rtc.api.stream.RCRTCCameraOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCFileVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.api.stream.RCRTCOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig.Builder;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.device.privatecloud.ServerUtils;
import cn.rongcloud.rtc.instrumentationtest.OperationAdapter.OperationViewHolder;

/**
 * 集成测试配套页面，只用于集成测试
 */
public class RTCInstrumentationTestActivity extends BaseInstrumentationTestActivity {

    final int TYPE_PUB_AUDIO = 11;
    final int TYPE_PUB_VIDEO = 12;
    final int TYPE_PUB_DEFAULT = 13;
    final int TYPE_PUB_CUSTOM = 14;
    final int TYPE_UNPUB_AUDIO = 15;
    final int TYPE_UNPUB_VIDEO = 16;
    final int TYPE_UNPUB_DEFAULT = 17;
    final int TYPE_UNPUB_CUSTOM = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        test();
    }

    @Override
    protected void addInRoomOperation() {
        super.addInRoomOperation();
        addOperation("pub_audio", TYPE_PUB_AUDIO);
        addOperation("pub_video", TYPE_PUB_VIDEO);
        addOperation("pub_default", TYPE_PUB_DEFAULT);
        addOperation("PUB_CUSTOM_stream", TYPE_PUB_CUSTOM);
        addOperation("unpub_audio", TYPE_UNPUB_AUDIO);
        addOperation("unpub_video", TYPE_UNPUB_VIDEO);
        addOperation("unpub_default", TYPE_UNPUB_DEFAULT);
        addOperation("unpub_custom", TYPE_UNPUB_CUSTOM);
        mAdapter.notifyDataSetChanged();
    }

    private void test() {
        //TODO 仅供测试
        mEvAppKey.setText(ServerUtils.getAppKey());
        mEvToken.setText("HgfJkLUqvy/bcsdmfOPS74WR0+mOjEZHtyi+p4TkVi8n6m5akPz92g==@emx6.cn.rongnav.com;emx6.cn.rongcfg.com");
    }


    @Override
    public void onClickItem(final OperationModel model, OperationViewHolder viewHolder) {
        int type = model.getType();
        switch (type) {
            case TYPE_START_CAMERA:
                startCamera(model);
                break;
            case TYPE_STOP_CAMERA:
                stopCamera(model);
                break;
            case TYPE_START_MIC:
            case TYPE_STOP_MIC:
                RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(type == TYPE_STOP_MIC);
                model.setSuccess();
                break;
            case TYPE_PUB_AUDIO:
                publishStream(localUser.getDefaultAudioStream(), model);
                break;
            case TYPE_PUB_VIDEO:
                publishStream(localUser.getDefaultVideoStream(), model);
                break;
            case TYPE_PUB_DEFAULT:
                publishDefaultStreams(model);
                break;
            case TYPE_PUB_CUSTOM:
                publishCustomStream(model);
                break;
            case TYPE_UNPUB_AUDIO:
                unpublishedStream(localUser.getDefaultAudioStream(), model);
                break;
            case TYPE_UNPUB_VIDEO:
                unpublishedStream(localUser.getDefaultVideoStream(), model);
                break;
            case TYPE_UNPUB_DEFAULT:
                unpublishedDefaultStreams(model);
                break;
            case TYPE_UNPUB_CUSTOM:
                RCRTCOutputStream unPubstream = null;
                for (RCRTCOutputStream stream : localUser.getStreams()) {
                    if (stream != localUser.getDefaultVideoStream() && stream != localUser.getDefaultAudioStream()) {
                        unPubstream = stream;
                        break;
                    }
                }
                unpublishedStream(unPubstream, model);
                break;
        }
    }

    private void stopCamera(OperationModel model) {
        RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
        videoViewManager.removeVideoView(RCRTCEngine.getInstance().getDefaultVideoStream().getStreamId());
        model.setSuccess();
    }

    private void startCamera(final OperationModel model) {
        final RCRTCCameraOutputStream defaultVideoStream = RCRTCEngine.getInstance().getDefaultVideoStream();
        defaultVideoStream.startCamera(new RTCResultDataCallbackWrapper<Boolean>() {
            @Override
            protected void onUISuccess(Boolean data) {
                model.setSuccess();
                String streamId = defaultVideoStream.getStreamId();
                if (!videoViewManager.hasVideoView(streamId)) {
                    RCRTCVideoView videoView = new RCRTCVideoView(RTCInstrumentationTestActivity.this);
                    videoViewManager.addVideoView(streamId, videoView);
                    defaultVideoStream.setVideoView(videoView);
                }
            }

            @Override
            protected void onUIFailed(RTCErrorCode errorCode) {
                model.setFailed(errorCode);
            }
        });
    }

    private void publishCustomStream(final OperationModel data) {
        RCRTCVideoStreamConfig config = Builder.create().setVideoResolution(RCRTCVideoResolution.RESOLUTION_360_640).setVideoFps(RCRTCVideoFps.Fps_24).build();
        final RCRTCFileVideoOutputStream fileVideo = RCRTCEngine.getInstance().createFileVideoOutputStream("file:///android_asset/video_1.mp4", false, true,
            "FileVideo" + (int) (Math.random() * 100), config);
        fileVideo.setOnSendListener(new IRCRTCOnStreamSendListener() {
            @Override
            public void onStart(final RCRTCVideoOutputStream stream) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!videoViewManager.hasVideoView(stream.getStreamId())) {
                            RCRTCVideoView videoView = new RCRTCVideoView(RTCInstrumentationTestActivity.this);
                            stream.setVideoView(videoView);
                            videoViewManager.addVideoView(stream.getStreamId(), videoView);
                        }
                    }
                });
            }

            @Override
            public void onComplete(final RCRTCVideoOutputStream stream) {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        videoViewManager.removeVideoView(stream.getStreamId());
                    }
                });

            }

            @Override
            public void onFailed() {
                postUIThread(new Runnable() {
                    @Override
                    public void run() {
                        videoViewManager.removeVideoView(fileVideo.getStreamId());
                        data.setFailed(RTCErrorCode.valueOf(-1));
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        publishStream(fileVideo, data);
    }

    private void unpublishedDefaultStreams(OperationModel data) {
        room.getLocalUser().unpublishDefaultStreams(data.createCallback(this));
    }

    private void unpublishedStream(RCRTCOutputStream stream, OperationModel data) {
        room.getLocalUser().unpublishStream(stream, data.createCallback(this));
    }

    private void publishDefaultStreams(final OperationModel model) {
        RCRTCLocalUser localUser = room.getLocalUser();
        if (isLive) {
            localUser.publishDefaultLiveStreams(createDataCallback(model));
        } else {
            localUser.publishDefaultStreams(model.createCallback(this));
        }
    }

    private void publishStream(RCRTCOutputStream stream, final OperationModel model) {
        RCRTCLocalUser localUser = room.getLocalUser();
        if (isLive) {
            localUser.publishLiveStream(stream, createDataCallback(model));
        } else {
            localUser.publishStream(stream, model.createCallback(this));
        }
    }

    private RTCResultDataCallbackWrapper createDataCallback(final OperationModel model) {
        return new RTCResultDataCallbackWrapper<RCRTCLiveInfo>(this) {
            @Override
            protected void onUISuccess(RCRTCLiveInfo data) {
                mLiveInfo = data;
                model.setExtra(data.getLiveUrl());
                model.setSuccess();
            }

            @Override
            protected void onUIFailed(RTCErrorCode errorCode) {
                model.setFailed(errorCode);
            }
        };
    }


}
