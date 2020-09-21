package cn.rongcloud.rtc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCLocalUser;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCStream;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.api.RCRTCMixConfig;
import cn.rongcloud.rtc.api.RCRTCMixConfig.CustomLayoutList.CustomLayout;
import cn.rongcloud.rtc.api.RCRTCMixConfig.MediaConfig.VideoConfig.VideoLayout;
import cn.rongcloud.rtc.api.RCRTCMixConfig.MixLayoutMode;
import cn.rongcloud.rtc.api.RCRTCMixConfig.VideoRenderMode;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/** Created by wangw on 2019-10-14. */
public class McuConfigDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "McuConfigDialog";
    private RadioButton mRbMode01;
    private RadioButton mRbMode02;
    private RadioButton mRbMode03;
    private RadioGroup mRg;
    private EditText mEvW;
    private EditText mEvH;
    private RadioButton mRbExparams01;
    private RadioButton mRbExparams02;
    private RadioGroup mRgEx;

    private RCRTCRoom mRTCRoom;
    private RCRTCMixConfig mConfig;
    private RCRTCLiveInfo mLiveInfo;

    public static void showDialog(Activity context, RCRTCLiveInfo liveInfo) {
        McuConfigDialog dialog = new McuConfigDialog(liveInfo);
        Bundle bundle = new Bundle();
        dialog.setArguments(bundle);
        dialog.show(context.getFragmentManager(), TAG);
    }

    public McuConfigDialog() {

    }

    @SuppressLint("ValidFragment")
    public McuConfigDialog(RCRTCLiveInfo mLiveInfo) {
        super();
        this.mLiveInfo = mLiveInfo;
    }

    @Nullable
    @Override
    public View onCreateView(
        LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_mcu_config, container, false);
        onFindViews(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null) return;
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void onFindViews(View view) {
        mRbMode01 = view.findViewById(R.id.rb_mode_01);
        mRbMode02 = view.findViewById(R.id.rb_mode_02);
        mRbMode03 = view.findViewById(R.id.rb_mode_03);
        mRg = view.findViewById(R.id.rg);
        mEvW = view.findViewById(R.id.ev_w);
        mEvH = view.findViewById(R.id.ev_h);
        mRbExparams01 = view.findViewById(R.id.rb_exparams_01);
        mRbExparams02 = view.findViewById(R.id.rb_exparams_02);
        mRgEx = view.findViewById(R.id.rg_ex);

        mRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_mode_01:
                        mConfig.setLayoutMode(MixLayoutMode.SUSPENSION);
                        break;
                    case R.id.rb_mode_02:
                        mConfig.setLayoutMode(MixLayoutMode.ADAPTIVE);
                        break;
                    case R.id.rb_mode_03:
                        mConfig.setLayoutMode(MixLayoutMode.CUSTOM);
                        break;
                }
            }
        });

        mRgEx.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                VideoRenderMode renderMode = VideoRenderMode.WHOLE;
                switch (checkedId) {
                    case R.id.rb_exparams_01:
                        renderMode = VideoRenderMode.CROP;
                        break;
                    case R.id.rb_exparams_02:
                        renderMode = VideoRenderMode.WHOLE;
                        break;
                }
                mConfig.getMediaConfig().getVideoConfig().getExtend().setRenderMode(renderMode);
            }
        });

        view.findViewById(R.id.btn_sub).setOnClickListener(this);
        view.findViewById(R.id.iv_close).setOnClickListener(this);
    }

    private void updateCustomMixLayout() {
        ArrayList<RCRTCStream> streams = new ArrayList<>();
        RCRTCRoom room = RCRTCEngine.getInstance().getRoom();
        RCRTCLocalUser localUser = room.getLocalUser();
        streams.addAll(localUser.getStreams());
        for (RCRTCRemoteUser user : room.getRemoteUsers()) {
            streams.addAll(user.getStreams());
        }
        VideoLayout videoLayout = mConfig.getMediaConfig().getVideoConfig().getVideoLayout();
        onUpdateCustomMixLayout(mConfig, localUser.getDefaultVideoStream(),
            streams, 100, 100, 0, 0, videoLayout.getWidth(), videoLayout.getHeight());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRTCRoom = RCRTCEngine.getInstance().getRoom();
        mConfig = createMcuConfig(mRTCRoom);
    }

    private RCRTCMixConfig createMcuConfig(RCRTCRoom rongRTCRoom) {
        mConfig = new RCRTCMixConfig();
        //合流布局模式
        mConfig.setLayoutMode(MixLayoutMode.SUSPENSION);
        RCRTCLocalUser localUser = rongRTCRoom.getLocalUser();
        //当做背景的Stream
        mConfig.setHostVideoStream(localUser.getDefaultVideoStream());
        // 合流布局输出配置
        RCRTCMixConfig.MediaConfig mediaConfig = new RCRTCMixConfig.MediaConfig();
        // 合流布局视频输出配置
        RCRTCMixConfig.MediaConfig.VideoConfig video = new RCRTCMixConfig.MediaConfig.VideoConfig();
        // 视频layout配置
        VideoLayout videolayout =
            new VideoLayout();

        RCRTCVideoStreamConfig videoConfig = RCRTCEngine.getInstance().getDefaultVideoStream().getVideoConfig();
        videolayout.setBitrate(videoConfig.getMaxRate());
        videolayout.setWidth(Integer.parseInt(mEvW.getText().toString()));
        videolayout.setHeight(Integer.parseInt(mEvH.getText().toString()));
        videolayout.setFps(videoConfig.getVideoFps().getFps());
        video.setVideoLayout(videolayout);

        //输出扩展配置
        video.setExtend(new RCRTCMixConfig.MediaConfig.VideoConfig.VideoExtend(VideoRenderMode.WHOLE));

        mediaConfig.setVideoConfig(video);

        // 音频输出配置
        //TODO 没有特殊配置，一般不需要设置
//        RongRTCMixConfig.MediaConfig.AudioConfig audio = new RongRTCMixConfig.MediaConfig.AudioConfig();
//        mediaConfig.setAudioConfig(audio);

        mConfig.setMediaConfig(mediaConfig);
        return mConfig;
    }

    /**
     * 更新自定义布局参数
     *
     * @param config
     * @param hostStream   当做背景的Stream
     * @param streams
     * @param videoWidth   小视频窗口的宽
     * @param videoHeight  小竖屏窗口的高
     * @param startX       起始X坐标
     * @param startY       起始Y坐标
     * @param screenWidth  输出屏幕的宽
     * @param screenHeight 输出屏幕的高
     */
    private void onUpdateCustomMixLayout(RCRTCMixConfig config, RCRTCStream hostStream,
        List<RCRTCStream> streams, int videoWidth, int videoHeight, int startX, int startY,
        int screenWidth, int screenHeight) {
        ArrayList<CustomLayout> list = new ArrayList<>();
        CustomLayout hLayout = new CustomLayout();
        hLayout.setVideoStream(hostStream);
        hLayout.setWidth(screenWidth);
        hLayout.setHeight(screenHeight);
        hLayout.setX(0);
        hLayout.setY(0);
        list.add(hLayout);
        for (RCRTCStream stream : streams) {
            CustomLayout vl = new CustomLayout();
            if (stream == hostStream || stream.getMediaType() != RCRTCMediaType.VIDEO) {
                continue;
            }
            vl.setVideoStream(stream);
            vl.setX(startX);
            vl.setY(startY + (videoHeight * list.size()));
            vl.setWidth(videoWidth);
            vl.setHeight(videoHeight);
            list.add(vl);
        }
        config.setCustomLayouts(list);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.btn_sub:
                onSubmit();
                break;
        }
    }

    private void onSubmit() {
        VideoLayout videoLayout =
            mConfig.getMediaConfig().getVideoConfig().getVideoLayout();
//        videoLayout.setWidth(Integer.parseInt(mEvW.getText().toString()));
//        videoLayout.setHeight(Integer.parseInt(mEvH.getText().toString()));

        RCRTCVideoStreamConfig videoConfig = RCRTCEngine.getInstance().getDefaultVideoStream().getVideoConfig();
        videoLayout.setWidth(videoConfig.getVideoResolution().getWidth());
        videoLayout.setHeight(videoConfig.getVideoResolution().getHeight());
        if (mConfig.getLayoutMode() == MixLayoutMode.CUSTOM) {
            updateCustomMixLayout();
        }
        mLiveInfo.setMixConfig(mConfig, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_LONG).show();
                dismiss();
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                Log.d(TAG, "onUiFailed: " + errorCode);
                Toast.makeText(getActivity(), "更新失败: " + errorCode, Toast.LENGTH_LONG).show();
            }
        });
    }
}
