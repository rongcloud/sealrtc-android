package cn.rongcloud.rtc;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import cn.rongcloud.rtc.config.RongCenterConfig;
import cn.rongcloud.rtc.entity.McuConfig;
import cn.rongcloud.rtc.media.RongMediaSignalClient;
import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.local.RongRTCLocalSourceManager;
import cn.rongcloud.rtc.user.RongRTCRemoteUser;
import cn.rongcloud.rtc.util.UserUtils;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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

    private RongRTCRoom mRTCRoom;
    private McuConfig mConfig;
    private String mServerDomain;

    public static void showDialog(Activity context, String configServer) {
        McuConfigDialog dialog = new McuConfigDialog();
        Bundle bundle = new Bundle();
        bundle.putString("server", configServer);
        dialog.setArguments(bundle);
        dialog.show(context.getFragmentManager(), TAG);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
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

        mRg.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.rb_mode_01:
                                mConfig.setMode(2);
                                break;
                            case R.id.rb_mode_02:
                                mConfig.setMode(3);
                                break;
                            case R.id.rb_mode_03:
                                mConfig.setMode(1);
                                break;
                        }
                    }
                });

        mRgEx.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        McuConfig.OutputBean.VideoBean.ExparamsBean exparams =
                                mConfig.getOutput().getVideo().getExparams();
                        switch (checkedId) {
                            case R.id.rb_exparams_01:
                                exparams.setRenderMode(1);
                                break;
                            case R.id.rb_exparams_02:
                                exparams.setRenderMode(2);
                                break;
                        }
                    }
                });

        view.findViewById(R.id.btn_sub).setOnClickListener(this);
        view.findViewById(R.id.iv_close).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRTCRoom = CenterManager.getInstance().getRongRTCRoom();
        mConfig = createMcuConfig(mRTCRoom);
        mServerDomain = getArguments().getString("server");
    }

    private McuConfig createMcuConfig(RongRTCRoom rongRTCRoom) {
        McuConfig config = new McuConfig();
        config.setMode(3);
        config.setHost_user_id(rongRTCRoom.getLocalUser().getUserId());

        // output
        McuConfig.OutputBean output = new McuConfig.OutputBean();
        McuConfig.OutputBean.VideoBean video = new McuConfig.OutputBean.VideoBean();
        McuConfig.OutputBean.VideoBean.NormalBean normal =
                new McuConfig.OutputBean.VideoBean.NormalBean();
        RongCenterConfig rongRTCConfig = RongRTCLocalSourceManager.getInstance().getRongRTCConfig();
        normal.setBitrate(rongRTCConfig.getMaxRate());
        normal.setWidth(Integer.parseInt(mEvW.getText().toString()));
        normal.setHeight(Integer.parseInt(mEvH.getText().toString()));
        normal.setFps(rongRTCConfig.getVideoFPS());
        video.setNormal(normal);

        McuConfig.OutputBean.VideoBean.ExparamsBean exparams =
                new McuConfig.OutputBean.VideoBean.ExparamsBean();
        exparams.setRenderMode(1);
        video.setExparams(exparams);

        output.setVideo(video);
        McuConfig.OutputBean.AudioBean audio = new McuConfig.OutputBean.AudioBean();
        audio.setBitrate(96);
        output.setAudio(audio);
        config.setOutput(output);

        McuConfig.InputBean input = new McuConfig.InputBean();
        ArrayList<McuConfig.InputBean.VideoBeanX> list = new ArrayList<>();
        McuConfig.InputBean.VideoBeanX iv = new McuConfig.InputBean.VideoBeanX();
        iv.setUser_id(rongRTCRoom.getLocalUser().getUserId());
        iv.setX(0);
        iv.setY(0);
        iv.setWidth(100);
        iv.setHeight(100);
        list.add(iv);
        Map<String, RongRTCRemoteUser> remoteUsers = rongRTCRoom.getRemoteUsers();
        if (remoteUsers != null) {
            int i = 1;
            for (RongRTCRemoteUser user : remoteUsers.values()) {
                McuConfig.InputBean.VideoBeanX vb = new McuConfig.InputBean.VideoBeanX();
                vb.setUser_id(user.getUserId());
                vb.setX(100 * i);
                vb.setY(100);
                vb.setWidth(100);
                vb.setHeight(100);
                list.add(vb);
            }
        }
        input.setVideo(list);
        config.setInput(input);
        return config;
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
        McuConfig.OutputBean.VideoBean.NormalBean normal =
                mConfig.getOutput().getVideo().getNormal();
        normal.setWidth(Integer.parseInt(mEvW.getText().toString()));
        normal.setHeight(Integer.parseInt(mEvH.getText().toString()));

        Gson gson =
                new GsonBuilder()
                        .setExclusionStrategies(
                                new ExclusionStrategy() {
                                    @Override
                                    public boolean shouldSkipField(FieldAttributes f) {

                                        if (TextUtils.equals(f.getName(), "tiny")
                                                || (f.getDeclaringClass()
                                                                == McuConfig.OutputBean.VideoBean
                                                                        .NormalBean.class
                                                        && TextUtils.equals(
                                                                f.getName(), "bitrate")))
                                            return true;
                                        return false;
                                    }

                                    @Override
                                    public boolean shouldSkipClass(Class<?> clazz) {
                                        return false;
                                    }
                                })
                        .create();
        String configJson = gson.toJson(mConfig);
        Log.d(TAG, "MCUConfig= " + configJson);

        Request request =
                new Request.Builder()
                        .url(mServerDomain + "/server/mcu/config")
                        .method(RequestMethod.POST)
                        .addHeader("RoomId", mRTCRoom.getRoomId())
                        .addHeader("UserId", mRTCRoom.getLocalUser().getUserId())
                        .addHeader("AppKey", UserUtils.APP_KEY)
                        .addHeader("SessionId", mRTCRoom.getSessionId())
                        .addHeader("Token", RongMediaSignalClient.getInstance().getRtcToken())
                        .body(configJson)
                        .build();
        HttpClient.getDefault()
                .request(
                        request,
                        new HttpClient.ResultCallback() {

                            @Override
                            public void onResponse(String result) {
                                Log.d(TAG, "onResponse: " + result);
                                if (getActivity() == null || getActivity().isFinishing()) {
                                    return;
                                }

                                getActivity()
                                        .runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(
                                                                        getActivity(),
                                                                        "Config设置成功",
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                        dismiss();
                                                    }
                                                });
                            }

                            @Override
                            public void onFailure(final int errorCode) {
                                Log.d(TAG, "onFailure: " + errorCode);
                                if (getActivity() == null || getActivity().isFinishing()) {
                                    return;
                                }

                                getActivity()
                                        .runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(
                                                                        getActivity(),
                                                                        "Config Failure: "
                                                                                + errorCode,
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                        dismiss();
                                                    }
                                                });
                            }

                            @Override
                            public void onError(final IOException exception) {
                                Log.d(TAG, "onError: " + exception);
                                if (getActivity() == null || getActivity().isFinishing()) {
                                    return;
                                }

                                getActivity()
                                        .runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(
                                                                        getActivity(),
                                                                        "Config Error: "
                                                                                + exception
                                                                                        .getMessage(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                        dismiss();
                                                    }
                                                });
                            }
                        });
    }
}
