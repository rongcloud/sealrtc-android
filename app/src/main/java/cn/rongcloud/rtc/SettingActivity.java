package cn.rongcloud.rtc;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoFps;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.device.privatecloud.ServerConfigActivity;
import cn.rongcloud.rtc.device.privatecloud.ServerUtils;
import cn.rongcloud.rtc.entity.CMPAddress;
import cn.rongcloud.rtc.util.AppRTCUtils;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Created by suancai on 2016/11/4. */
public class SettingActivity extends RongRTCBaseActivity
        implements CompoundButton.OnCheckedChangeListener {

    private LinearLayout testSettingOptionsContainer;

    private LinearLayout backButton;
    private static final String TAG = "SettingActivitytag";

    public static final String FPS = "FPS";
    public static final String CONNECTION_MODE = "CONNECTION_MODE";
    public static final String RESOLUTION = "RESOLUTION";
    public static final String CODECS = "CODECS";
    public static final String BIT_RATE_MIN = "BIT_RATE_MIN";
    public static final String BIT_RATE_MAX = "BIT_RATE_MAX";
    public static final String IS_OBSERVER = "IS_OBSERVER";
    public static final String IS_GPUIMAGEFILTER = "IS_GPUIMAGEFILTER";
    public static final String IS_SRTP = "IS_SRTP";
    public static final String IS_RONGRTC_CONNECTIONMODE = "IS_RONGRTC_CONNECTIONMODE";
    public static final String MEDIA_URL = "media_url";
    /** 保存的bool值 true：大小流开启，false 关闭* */
    public static final String IS_STREAM_TINY = "STREAMTINY";
    /** 自动化测试 */
    public static final String IS_AUTO_TEST = "AUTOTEST";
    /** 水印 */
    public static final String IS_WATER = "show_water_mark";
    /** 是否为直播模式 */
    public static final String IS_LIVE = "is_Live_RoomType";
    /**
     * 是否镜像翻转
     */
    public static final String IS_MIRROR = "is_mirror_videoframe";

    public static final String IS_AUDIO_ENCRYPTION = "AUDIO_ENCRYPTION";
    public static final String IS_VIDEO_ENCRYPTION = "VIDEO_ENCRYPTION";

    public static final String IS_STEREO = "is_stereo";
    public static final String IS_AUDIO_MUSIC = "is_musicMode";
    public static final String IS_AUDIO_PROCESS = "is_audio_process";
    public static final String AUDIO_BITRATE = "audio_bitrate";
    public static final String AUDIO_AGC_LIMITER = "agc_limiter";
    public static final String AUDIO_AGC_TARGET_DBOV = "agc_target_dbov";
    public static final String AUDIO_AGC_COMPRESSION = "agc_compression";
    public static final String AUDIO__NOISE_SUPPRESSION = "noise_suppression";
    public static final String AUDIO__NOISE_SUPPRESSION_LEVEL = "noise_suppression_level";
    public static final String AUDIO_ECHO_CANCEL = "echo_cancel";
    public static final String AUDIO_PREAMPLIFIER = "preamplifier";
    public static final String AUDIO_PREAMPLIFIER_LEVEL = "preamplifier_level";

    public static final String VD_480x640 = "480x640";
    private String[] list_fps = new String[] {"10", "15", "24", "30"};
    private String[] list_resolution;
    private String[] list_bitrate = new String[] {};
    private String[] list_connectionMode = new String[] {"Relay", "P2P"};
    private String[] list_format = new String[] {"H264", "VP8", "VP9"};
    private String[] list_observer,
            list_gpuImageFilter,
            list_connectionType,
            list_streamTiny,
            list_autotest,
            list_water,
        list_mirror,
        list_isLive,
        list_stereo,
        listAudioEncryption,
        listVideoEncryption,
            list_audio_process;

    private int defaultBitrateMinIndex = 0;
    private int defaultBitrateMaxIndex = 0;

    private static final int REQUEST_CODE_RESOLUTION = 12;
    private static final int REQUEST_CODE_FPS = 13;
    private static final int REQUEST_CODE_BITRATE_MAX = 14;
    private static final int REQUEST_CODE_MODE = 15;
    private static final int REQUEST_CODE_FORMAT = 16;
    private static final int REQUEST_CODE_BITRATE_MIN = 17;
    private static final int REQUEST_CODE_IS_OBSERVER = 18;
    private static final int REQUEST_CODE_IS_GPUIMAGEFILTER = 19;
    private static final int REQUEST_CODE_IS_SRTP = 20;
    private static final int REQUEST_CODE_IS_CONNECTIONTYPE = 21;
    private static final int REQUEST_CODE_IS_STREAM_TINY = 22;
    private static final int REQUEST_CODE_IS_AUTO_TEST = 23;
    private static final int REQUEST_CODE_IS_WATER = 24;
    private static final int REQUEST_CODE_IS_STEREO = 25;
    private static final int REQUEST_CODE_IS_AUDIO_PROCESS = 26;
    private static final int REQUEST_CODE_IS_LIVE = 27;
    private static final int REQUEST_CODE_IS_MIRROR = 28;
    private static final int REQUEST_CODE_IS_AUDIO_ENCRYPTION = 29;
    private static final int REQUEST_CODE_IS_VIDEO_ENCRYPTION = 30;

    private int tapStep = 0;
    private long lastClickTime = 0;
    private TextView settingOptionText1,
            settingOptionText2,
            settingOptionText3,
            settingOptionText4,
            settingOptionText5,
            settingOptionText6,
            settingOptionText7,
            //            settingOptionText8,
            settingOptionSRTP,
            settingOptionConnectionType,
            setting_option_streamTiny,
            setting_autotest,
            setting_water,
            settingOptionMediaUrl,
            setting_stereo,
            setting_audio_process,
            setting_islive,
                setting_userId,
                settingAudioEncryption,
                settingVideoEncryption,
                setting_mirrror;
    private LinearLayout settings_Modify;
    private LinearLayout linear_connection_settings;

    private SwitchCompat audioSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        initVideoProfile();
        initViews();

        setupListeners();

        SessionManager.getInstance()
                .put(
                        getResources().getString(R.string.key_use_av_setting),
                        true); // 进入setting页使用本地设置参数
    }

    private void initVideoProfile() {
        List<String> videoProfileList = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();

        for (RCRTCVideoResolution profile :
            RCRTCVideoResolution.values()) {
            stringBuffer.setLength(0);
            if (profile.getWidth() != 0) {
                stringBuffer.append(profile.getWidth());
                stringBuffer.append("x");
                stringBuffer.append(profile.getHeight());
                if (!videoProfileList.contains(stringBuffer.toString())) {
                    videoProfileList.add(stringBuffer.toString());
                }
            }
        }

        list_resolution = new String[videoProfileList.size()];
        videoProfileList.toArray(list_resolution);
    }

    private void initViews() {
        linear_connection_settings = (LinearLayout) findViewById(R.id.linear_connection_settings);
        if (linear_connection_settings != null) {
            linear_connection_settings.setVisibility(
                    ServerUtils.usePrivateCloud() ? View.VISIBLE : View.GONE);
        }
        settings_Modify = (LinearLayout) findViewById(R.id.settings_Modify);

        list_observer =
                new String[] {
                    getResources().getString(R.string.settings_text_observer_no),
                    getResources().getString(R.string.settings_text_observer_yes)
                };
        list_gpuImageFilter =
                new String[] {
                    getResources().getString(R.string.settings_text_gpufliter_no),
                    getResources().getString(R.string.settings_text_gpufliter_yes)
                };
        list_connectionType =
                new String[] {
                    getResources().getString(R.string.settings_text_connectionType_quic),
                    getResources().getString(R.string.settings_text_connectionType_tcp)
                };

        list_streamTiny =
                new String[] {
                    getResources().getString(R.string.settings_text_MediaStreamTiny_no),
                    getResources().getString(R.string.settings_text_MediaStreamTiny_yes)
                };
        setting_option_streamTiny = (TextView) findViewById(R.id.setting_option_10_txt);
        // true：大小流开启，false 关闭(默认)
        boolean streamTiny = SessionManager.getInstance().getIsSupportTiny(IS_STREAM_TINY);
        setting_option_streamTiny.setText(streamTiny ? list_streamTiny[1] : list_streamTiny[0]);

        // 默认关闭自动化测试
        list_autotest =
                new String[] {
                    getResources().getString(R.string.settings_text_MediaStreamTiny_no),
                    getResources().getString(R.string.settings_text_MediaStreamTiny_yes)
                };
        setting_autotest = (TextView) findViewById(R.id.tv_setting_option_autotest);
        setting_autotest.setText(
                SessionManager.getInstance().getBoolean(IS_AUTO_TEST)
                        ? list_streamTiny[1]
                        : list_streamTiny[0]);
        // release 版本自定义音视频加解密功能关闭状态
        if (BuildConfig.DEBUG) {
            findViewById(R.id.setting_audio_encryption).setVisibility(View.VISIBLE);
            findViewById(R.id.setting_video_encryption).setVisibility(View.VISIBLE);
        }
        //自定义音频流开关
        listAudioEncryption = new String[] {
            getResources().getString(R.string.settings_no),
            getResources().getString(R.string.settings_yes)
        };
        settingAudioEncryption = (TextView) findViewById(R.id.tv_setting_option_audio_encryption);
        settingAudioEncryption.setText(
            SessionManager.getInstance().getBoolean(IS_AUDIO_ENCRYPTION)
                ? listAudioEncryption[1]
                : listAudioEncryption[0]);

        //自定义视频流开关
        listVideoEncryption = new String[] {
            getResources().getString(R.string.settings_no),
            getResources().getString(R.string.settings_yes)
        };
        settingVideoEncryption = (TextView) findViewById(R.id.tv_setting_option_video_encryption);
        settingVideoEncryption.setText(
            SessionManager.getInstance().getBoolean(IS_VIDEO_ENCRYPTION)
                ? listVideoEncryption[1]
                : listVideoEncryption[0]);

        list_isLive =
                new String[] {
                    getResources().getString(R.string.settings_text_MediaStreamTiny_no),
                    getResources().getString(R.string.settings_text_MediaStreamTiny_yes)
                };
        setting_islive = (TextView) findViewById(R.id.tv_setting_option_islive);
        setting_islive.setText(
                SessionManager.getInstance().getBoolean(IS_LIVE) ? list_isLive[1] : list_isLive[0]);

        list_water =
                new String[] {
                    getResources().getString(R.string.settings_text_MediaStreamTiny_no),
                    getResources().getString(R.string.settings_text_MediaStreamTiny_yes)
                };
        setting_water = (TextView) findViewById(R.id.tv_setting_option_water);
        setting_water.setText(
                SessionManager.getInstance().getBoolean(IS_WATER) ? list_water[1] : list_water[0]);

        list_mirror =
            new String[]{
                getResources().getString(R.string.settings_text_MediaStreamTiny_no),
                getResources().getString(R.string.settings_text_MediaStreamTiny_yes)
            };
        setting_mirrror = (TextView) findViewById(R.id.tv_setting_option_mirror);
        setting_mirrror.setText(
            SessionManager.getInstance().getBoolean(IS_MIRROR) ? list_mirror[1] : list_mirror[0]);

        list_stereo =
                new String[] {
                    getResources().getString(R.string.settings_text_MediaStreamTiny_no),
                    getResources().getString(R.string.settings_text_MediaStreamTiny_yes)
                };
        setting_stereo = (TextView) findViewById(R.id.tv_setting_option_stereo);
        setting_stereo.setText(
                SessionManager.getInstance().getBoolean(IS_STEREO)
                        ? list_stereo[1]
                        : list_stereo[0]);

        list_audio_process =
                new String[] {
                    getResources().getString(R.string.settings_text_MediaStreamTiny_no),
                    getResources().getString(R.string.settings_text_MediaStreamTiny_yes)
                };
        setting_audio_process = (TextView) findViewById(R.id.tv_setting_option_audio_process);
        setting_audio_process.setText(
                SessionManager.getInstance().getBoolean(IS_AUDIO_PROCESS)
                        ? list_audio_process[1]
                        : list_audio_process[0]);

        settingOptionText1 = ((TextView) findViewById(R.id.setting_option_1_txt));
        String resolution = SessionManager.getInstance().getString(RESOLUTION);
        if (TextUtils.isEmpty(resolution))
            resolution = SessionManager.getInstance().put(RESOLUTION, VD_480x640);
        settingOptionText1.setText(resolution);
        reInitBitrates(settingOptionText1.getText().toString());

        settingOptionText2 = ((TextView) findViewById(R.id.setting_option_2_txt));
        String fps = SessionManager.getInstance().getString(FPS);
        if (TextUtils.isEmpty(fps)) {
            fps = SessionManager.getInstance().put(FPS, list_fps[1]);
        }
        settingOptionText2.setText(fps);

        settingOptionText3 = ((TextView) findViewById(R.id.setting_option_3_txt));
        String biterate =
                SessionManager.getInstance()
                        .getString(
                                BIT_RATE_MAX, getResources().getString(R.string.def_max_bitrate));
        if (TextUtils.isEmpty(biterate))
            biterate =
                    SessionManager.getInstance()
                            .put(BIT_RATE_MAX, list_bitrate[defaultBitrateMaxIndex]);
        settingOptionText3.setText(biterate);

        settingOptionText4 = ((TextView) findViewById(R.id.setting_option_4_txt));
        String connectionMode = SessionManager.getInstance().getString(CONNECTION_MODE);
        if (TextUtils.isEmpty(connectionMode))
            connectionMode =
                    SessionManager.getInstance().put(CONNECTION_MODE, list_connectionMode[0]);
        settingOptionText4.setText(connectionMode);

        settingOptionText5 = ((TextView) findViewById(R.id.setting_option_5_txt));
        String format = SessionManager.getInstance().getString(CODECS);
        if (TextUtils.isEmpty(format))
            format = SessionManager.getInstance().put(CODECS, list_format[0]);
        settingOptionText5.setText(format);

        settingOptionText6 = ((TextView) findViewById(R.id.setting_option_6_txt));
        String biterateMin =
                SessionManager.getInstance()
                        .getString(
                                BIT_RATE_MIN, getResources().getString(R.string.def_min_bitrate));
        if (TextUtils.isEmpty(biterateMin))
            biterateMin =
                    SessionManager.getInstance()
                            .put(BIT_RATE_MIN, list_bitrate[defaultBitrateMinIndex]);
        settingOptionText6.setText(biterateMin);

        settingOptionText7 = (TextView) findViewById(R.id.setting_option_7_txt);
        boolean isObserver = SessionManager.getInstance().getBoolean(IS_OBSERVER);
        String observerMode = list_observer[isObserver ? 1 : 0];
        settingOptionText7.setText(observerMode);

        //        settingOptionText8 = (TextView) findViewById(R.id.setting_option_8_txt);
        //        boolean isGpuImageFilter =
        // SessionManager.getInstance().getBoolean(IS_GPUIMAGEFILTER);
        //        String gpuImageFiliter = list_gpuImageFilter[isGpuImageFilter ? 1 : 0];
        //        settingOptionText8.setText(gpuImageFiliter);

        settingOptionSRTP = (TextView) findViewById(R.id.setting_option_9_txt);
        boolean isSrtp = SessionManager.getInstance().getBoolean(IS_SRTP);
        String srtpOption = list_gpuImageFilter[isSrtp ? 1 : 0];
        settingOptionSRTP.setText(srtpOption);

        backButton = (LinearLayout) findViewById(R.id.settings_back);
        testSettingOptionsContainer = (LinearLayout) findViewById(R.id.setting_test_list);

        // connection type
        settingOptionConnectionType =
                (TextView) findViewById(R.id.setting_option_connectiontype_txt);
        boolean isQuic = SessionManager.getInstance().getBoolean(IS_RONGRTC_CONNECTIONMODE);
        settingOptionConnectionType.setText(list_connectionType[isQuic ? 0 : 1]);

        settingOptionMediaUrl = (TextView) findViewById(R.id.tv_setting_option_media_url);

        setting_userId = (TextView) findViewById(R.id.tv_setting_option_userId);
        setting_userId.setText(SessionManager.getInstance().getString(UserUtils.USER_ID, ""));

        if (BuildConfig.DEBUG && !ServerUtils.usePrivateCloud()) {
            findViewById(R.id.setting_layout_mediaserver).setVisibility(View.VISIBLE);
        }

        audioSwitch = ((SwitchCompat) findViewById(R.id.s_call_settings_audio_params));
        audioSwitch.setChecked(
                SessionManager.getInstance()
                        .getBoolean(
                                IS_AUDIO_MUSIC,
                                getResources().getBoolean(R.bool.def_audio_music_mode)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String mediaServer = SessionManager.getInstance().getString("MediaUrl");
        if (!TextUtils.isEmpty(mediaServer)) {
            settingOptionMediaUrl.setText(mediaServer);
        }
    }

    private void setupListeners() {
        findViewById(R.id.setting_option_1)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_resolution,
                                list_resolution,
                                REQUEST_CODE_RESOLUTION));
        findViewById(R.id.setting_option_2)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_fps, list_fps, REQUEST_CODE_FPS));
        findViewById(R.id.setting_option_3)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_rate,
                                list_bitrate,
                                REQUEST_CODE_BITRATE_MAX));
        findViewById(R.id.setting_option_4)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_connection_mode,
                                list_connectionMode,
                                REQUEST_CODE_MODE));
        findViewById(R.id.setting_option_5)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_coding_mode,
                                list_format,
                                REQUEST_CODE_FORMAT));
        findViewById(R.id.setting_option_6)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_min_rate,
                                list_bitrate,
                                REQUEST_CODE_BITRATE_MIN));
        findViewById(R.id.setting_option_7)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_observer,
                                list_observer,
                                REQUEST_CODE_IS_OBSERVER));
        //        findViewById(R.id.setting_option_8)
        //                .setOnClickListener(
        //                        new OnOptionViewClickListener(
        //                                R.string.settings_text_gpufliter,
        //                                list_gpuImageFilter,
        //                                REQUEST_CODE_IS_GPUIMAGEFILTER));
        findViewById(R.id.setting_option_9)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_srtp,
                                list_gpuImageFilter,
                                REQUEST_CODE_IS_SRTP));
        findViewById(R.id.setting_option_connectiontype)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.settings_text_connection,
                                list_connectionType,
                                REQUEST_CODE_IS_CONNECTIONTYPE));
        findViewById(R.id.setting_option_privateclud_config)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ButtentSolp.check(v.getId(), 1500)) {
                                    FinLog.v("SettingActivity", getString(R.string.btnsolpstr));
                                    return;
                                }
                                Intent intent =
                                        new Intent(
                                                SettingActivity.this, ServerConfigActivity.class);
                                startActivity(intent);
                            }
                        });

        final EditText audioBitrateEditText =
                (EditText) findViewById(R.id.tv_setting_option_audio_bitrate);
        final EditText audioAgcLimiter =
                (EditText) findViewById(R.id.tv_setting_option_audio_agc_limiter);
        final EditText audioAgcTargetDBOV =
                (EditText) findViewById(R.id.tv_setting_option_audio_agc_target_dbov);
        final EditText audioAgcCompression =
                (EditText) findViewById(R.id.tv_setting_option_audio_agc_compression);

        final EditText audioNoiseSuppression =
                (EditText) findViewById(R.id.tv_setting_option_audio_noise_suppression);
        final EditText audioNoiseSuppressionLevel =
                (EditText) findViewById(R.id.tv_setting_option_audio_noise_suppression_level);
        final EditText audioEchoCancel =
                (EditText) findViewById(R.id.tv_setting_option_audio_echo_cancel);
        final EditText audioPreAmplifier =
                (EditText) findViewById(R.id.tv_setting_option_audio_pre_amplifier);
        final EditText audioPreAmplifierLevel =
                (EditText) findViewById(R.id.tv_setting_option_audio_pre_amplifier_level);

        backButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String audioBitrate = audioBitrateEditText.getText().toString();
                        Log.d(TAG, "audio bitrate option = " + audioBitrate);
                        SessionManager.getInstance().put(AUDIO_BITRATE, audioBitrate);

                        String limiter = audioAgcLimiter.getText().toString();
                        Log.d(TAG, "audio agc limiter = " + limiter);
                        SessionManager.getInstance().put(AUDIO_AGC_LIMITER, limiter);

                        String targetDBOV = audioAgcTargetDBOV.getText().toString();
                        Log.d(TAG, "audio agc target dbov = " + targetDBOV);
                        SessionManager.getInstance().put(AUDIO_AGC_TARGET_DBOV, targetDBOV);

                        String agcCompression = audioAgcCompression.getText().toString();
                        Log.d(TAG, "audio agc compresssion option = " + agcCompression);
                        SessionManager.getInstance().put(AUDIO_AGC_COMPRESSION, agcCompression);

                        String noiseSupppression = audioNoiseSuppression.getText().toString();
                        Log.d(TAG, "audio noiseSupppression option = " + noiseSupppression);
                        SessionManager.getInstance()
                                .put(AUDIO__NOISE_SUPPRESSION, noiseSupppression);

                        String noiseSupppressionLevel =
                                audioNoiseSuppressionLevel.getText().toString();
                        Log.d(
                                TAG,
                                "audio noiseSupppression Level option = " + noiseSupppressionLevel);
                        SessionManager.getInstance()
                                .put(AUDIO__NOISE_SUPPRESSION_LEVEL, noiseSupppressionLevel);

                        String echoCancel = audioEchoCancel.getText().toString();
                        Log.d(TAG, "audio echoCancel option = " + echoCancel);
                        SessionManager.getInstance().put(AUDIO_ECHO_CANCEL, echoCancel);

                        String preAmplifier = audioPreAmplifier.getText().toString();
                        Log.d(TAG, "audio preAmplifier = " + preAmplifier);
                        SessionManager.getInstance().put(AUDIO_PREAMPLIFIER, preAmplifier);

                        String preAmplifierLevel = audioPreAmplifierLevel.getText().toString();
                        Log.d(TAG, "audio preAmplifierLevel option = " + preAmplifierLevel);
                        SessionManager.getInstance()
                                .put(AUDIO_PREAMPLIFIER_LEVEL, preAmplifierLevel);
                        finish();
                    }
                });
        findViewById(R.id.settings_title_layout).setOnClickListener(new OnTitleViewClickListener());
        findViewById(R.id.setting_option_streamTiny)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.Opensizestream,
                                list_streamTiny,
                                REQUEST_CODE_IS_STREAM_TINY));
        findViewById(R.id.setting_option_autotest)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.autotest, list_autotest, REQUEST_CODE_IS_AUTO_TEST));
        findViewById(R.id.setting_option_water)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.watermark, list_water, REQUEST_CODE_IS_WATER));
        findViewById(R.id.setting_option_mirror)
            .setOnClickListener(
                new OnOptionViewClickListener(
                    R.string.settings_mirror, list_mirror, REQUEST_CODE_IS_MIRROR));
        findViewById(R.id.setting_option_islive)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.islive, list_isLive, REQUEST_CODE_IS_LIVE));
        findViewById(R.id.setting_option_stereo)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.stereo, list_stereo, REQUEST_CODE_IS_STEREO));
        findViewById(R.id.setting_option_audio_process)
                .setOnClickListener(
                        new OnOptionViewClickListener(
                                R.string.audio_process,
                                list_audio_process,
                                REQUEST_CODE_IS_AUDIO_PROCESS));

        findViewById(R.id.setting_option_media_url_)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(
                                        new Intent(
                                                SettingActivity.this, MediaServerActivity.class));
                            }
                        });
        setting_userId.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        copyToClipBoard(setting_userId.getText().toString());
                        return false;
                    }
                });

        audioSwitch.setOnCheckedChangeListener(this);
        findViewById(R.id.setting_option_audio_encryption)
            .setOnClickListener(
                new OnOptionViewClickListener(
                    R.string.settings_audio_encryption, listAudioEncryption, REQUEST_CODE_IS_AUDIO_ENCRYPTION));
        findViewById(R.id.setting_option_video_encryption)
            .setOnClickListener(
                new OnOptionViewClickListener(
                    R.string.settings_video_encryption, listVideoEncryption, REQUEST_CODE_IS_VIDEO_ENCRYPTION));
    }

    private void copyToClipBoard(String content) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("simple text", content);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, getText(R.string.settings_text_copy_userid), Toast.LENGTH_SHORT)
                .show();
    }

    private class OnOptionViewClickListener implements View.OnClickListener {
        String title;
        String[] datas;
        int requestCode;

        public OnOptionViewClickListener(int resId, String[] datas, int requestCode) {
            this.title = getResources().getString(resId);
            this.datas = datas;
            this.requestCode = requestCode;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(SettingActivity.this, OptionsPickActivity.class);
            intent.putExtra(OptionsPickActivity.BUNDLE_KEY_DATAS, datas);
            intent.putExtra(OptionsPickActivity.BUNDLE_KEY_TITLE, title);
            startActivityForResult(intent, requestCode);
        }
    }

    /** Click 5 times to show the hidden views */
    private class OnTitleViewClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            long timeDuration = System.currentTimeMillis() - lastClickTime;
            if (timeDuration > 500) {
                tapStep = 0;
                lastClickTime = 0;
            } else {
                tapStep++;
                if (tapStep == 4) {
                    if (BuildConfig.DEBUG) {
                        findViewById(R.id.setting_options_hidden).setVisibility(View.VISIBLE);
                    }
                }
            }

            lastClickTime = System.currentTimeMillis();
        }
    }

    private void reInitBitrates(String resolution) {
        String kbps = "Kbps";
        try {
            // 数组长度 間隔  最小
            int length = 0, parameters = 5, min = 0; //
            length = (6800 - min) / parameters;
            list_bitrate = new String[length + 1];
            //            list_bitrate_min = new String[length + 1];
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i <= length; i++) {
                stringBuffer.setLength(0);
                int bitrate = i * parameters + min;
                stringBuffer.append(bitrate);
                stringBuffer.append(kbps);
                list_bitrate[i] = stringBuffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        String result = data.getStringExtra(OptionsPickActivity.BUNDLE_KEY_RESULT);
        if (TextUtils.isEmpty(result)) return;
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                settingOptionText1.setText(result);
                SessionManager.getInstance().put(RESOLUTION, result);
                changeBitrateByResolution();
                break;
            case REQUEST_CODE_FPS:
                SessionManager.getInstance().put(FPS, result);
                settingOptionText2.setText(result);
                changeBitrateByResolution();
                break;
            case REQUEST_CODE_BITRATE_MAX:
                settingOptionText3.setText(result);
                SessionManager.getInstance().put(BIT_RATE_MAX, result);
                break;
            case REQUEST_CODE_MODE:
                settingOptionText4.setText(result);
                SessionManager.getInstance().put(CONNECTION_MODE, result);
                break;
            case REQUEST_CODE_FORMAT:
                settingOptionText5.setText(result);
                SessionManager.getInstance().put(CODECS, result);
                break;
            case REQUEST_CODE_BITRATE_MIN:
                settingOptionText6.setText(result);
                SessionManager.getInstance().put(BIT_RATE_MIN, result);
                break;
            case REQUEST_CODE_IS_OBSERVER:
                settingOptionText7.setText(result);
                SessionManager.getInstance().put(IS_OBSERVER, result.equals(list_observer[1]));
                break;
                //            case REQUEST_CODE_IS_GPUIMAGEFILTER:
                //                settingOptionText8.setText(result);
                //                SessionManager.getInstance()
                //                        .put(IS_GPUIMAGEFILTER,
                // result.equals(list_gpuImageFilter[1]));
                //                break;
            case REQUEST_CODE_IS_SRTP:
                settingOptionSRTP.setText(result);
                SessionManager.getInstance().put(IS_SRTP, result.equals(list_gpuImageFilter[1]));
                break;
            case REQUEST_CODE_IS_CONNECTIONTYPE:
                settingOptionConnectionType.setText(result);
                if (list_connectionType[0].equals(result)) {
                    //
                    // RongRTCEngine.getInstance().setRongRTCConnectionMode(true);
                } else if (list_connectionType[1].equals(result)) {
                    //
                    // RongRTCEngine.getInstance().setRongRTCConnectionMode(false);
                }
                //                SessionManager.getInstance(this).put(IS_RONGRTC_CONNECTIONMODE,
                // RongRTCContext.ConfigParameter.RongRTCConnectionMode ==
                // RongRTCEngine.RongRTCConnectionMode.QUIC ? true : false);
                //                if
                // (!SessionManager.getInstance(Utils.getContext()).contains(AppRTCUtils.CUSTOM_CMPKEY))
                //                    EventBus.getDefault().postSticky("1");
                break;
            case REQUEST_CODE_IS_STREAM_TINY:
                setting_option_streamTiny.setText(result);
                SessionManager.getInstance().put(IS_STREAM_TINY, list_streamTiny[1].equals(result));
                break;
            case REQUEST_CODE_IS_AUTO_TEST:
                setting_autotest.setText(result);
                SessionManager.getInstance().put(IS_AUTO_TEST, list_autotest[1].equals(result));
                break;
            case REQUEST_CODE_IS_WATER:
                setting_water.setText(result);
                SessionManager.getInstance().put(IS_WATER, list_water[1].equals(result));
                break;
            case REQUEST_CODE_IS_MIRROR:
                setting_mirrror.setText(result);
                SessionManager.getInstance().put(IS_MIRROR, list_mirror[1].equals(result));
                break;
            case REQUEST_CODE_IS_LIVE:
                setting_islive.setText(result);
                SessionManager.getInstance().put(IS_LIVE, list_isLive[1].equals(result));
                break;
            case REQUEST_CODE_IS_STEREO:
                setting_stereo.setText(result);
                SessionManager.getInstance().put(IS_STEREO, list_stereo[1].equals(result));
                Log.d(TAG, "stero option = " + list_stereo[1].equals(result));
                break;
            case REQUEST_CODE_IS_AUDIO_PROCESS:
                setting_audio_process.setText(result);
                SessionManager.getInstance()
                        .put(IS_AUDIO_PROCESS, list_audio_process[1].equals(result));
                Log.d(TAG, "audio process option = " + list_audio_process[1].equals(result));
                break;
            case REQUEST_CODE_IS_AUDIO_ENCRYPTION:
                settingAudioEncryption.setText(result);
                SessionManager.getInstance().put(IS_AUDIO_ENCRYPTION, listAudioEncryption[1].equals(result));
                break;
            case REQUEST_CODE_IS_VIDEO_ENCRYPTION:
                settingVideoEncryption.setText(result);
                SessionManager.getInstance().put(IS_VIDEO_ENCRYPTION, listVideoEncryption[1].equals(result));
                break;
            default:
                break;
        }
    }

    private void changeBitrateByResolution() {
        try {
            String resolution = SessionManager.getInstance().getString(RESOLUTION);
            String fps = SessionManager.getInstance().getString(FPS);
            RCRTCVideoResolution videoResolution =
                    RCRTCVideoResolution.getVideoResolution(resolution);
            RCRTCVideoFps videoFrame =
                    RCRTCVideoFps.getVideoFps(String.format("_%sf", fps));

            String cameraMaxBitRate =
                    String.format(
                            "%dKbps",
                            (int) (videoResolution.getMaxBitRate() * videoFrame.getMultiplier()));
            String cameraMinBitRate =
                    String.format(
                            "%dKbps",
                            (int) (videoResolution.getMinBitRate() * videoFrame.getMultiplier()));
            settingOptionText6.setText(cameraMinBitRate);
            settingOptionText3.setText(cameraMaxBitRate);
            SessionManager.getInstance().put(BIT_RATE_MIN, cameraMinBitRate);
            SessionManager.getInstance().put(BIT_RATE_MAX, cameraMaxBitRate);
            setupListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View dialogView = null;
    private Dialog dialog;
    private LayoutInflater inflater;
    private EditText edit_ServerURL, edit_cmpServer, edit_appid;
    private CMPAddress cmpAddress;
    private Button btn_ok, btn_cancele;

    /** 修改服务器CMP地址 */
    private void modifyServerAddress() {
        try {
            inflater = LayoutInflater.from(SettingActivity.this);
            dialogView = inflater.inflate(R.layout.activity_cmpaddress, null);
            RelativeLayout layout = (RelativeLayout) dialogView.findViewById(R.id.rel_data);
            edit_appid = (EditText) layout.findViewById(R.id.edit_appid);
            edit_ServerURL = (EditText) layout.findViewById(R.id.edit_ServerURL);
            edit_cmpServer = (EditText) layout.findViewById(R.id.edit_cmpServer);
            btn_ok = (Button) layout.findViewById(R.id.btn_ok);
            btn_ok.setOnClickListener(btnClickListener);
            btn_cancele = (Button) layout.findViewById(R.id.btn_cancel);
            btn_cancele.setOnClickListener(btnClickListener);
            String appid = AppRTCUtils.getAppID();
            if (!TextUtils.isEmpty(appid)) {
                edit_appid.setText("");
                edit_appid.setText(appid);
            }

            try {
                if (null != AppRTCUtils.getCMPAddress(AppRTCUtils.CUSTOM_CMPKEY)) {
                    cmpAddress = AppRTCUtils.getCMPAddress(AppRTCUtils.CUSTOM_CMPKEY);
                } else if (null != AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY)) {
                    cmpAddress = AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY);
                }
                if (!TextUtils.isEmpty(cmpAddress.getServerURL())) {
                    edit_ServerURL.setText("");
                    edit_ServerURL.setText(cmpAddress.getServerURL());
                }
                if (!TextUtils.isEmpty(cmpAddress.getCmpServer())) {
                    edit_cmpServer.setText("");
                    edit_cmpServer.setText(cmpAddress.getCmpServer());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            dialog = new Dialog(SettingActivity.this, R.style.loadingdata_dialog);
            dialog.setCancelable(false); // bu可以用“返回键”取消
            dialog.setContentView(
                    layout,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout); // 设置布局
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            if (!SettingActivity.this.isFinishing()) dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "ERROR!", Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener btnClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.btn_ok:
                            String cmp = "", tokenServer = "";
                            try {
                                if (null != edit_cmpServer) {
                                    cmp = edit_cmpServer.getText().toString().trim();
                                }
                                if (null != edit_ServerURL) {
                                    tokenServer = edit_ServerURL.getText().toString().trim();
                                }
                                if (!TextUtils.isEmpty(cmp)
                                        && !TextUtils.isEmpty(tokenServer)
                                        && !Utils.isQuicOrTcp(cmp)) {
                                    Toast.makeText(
                                                    SettingActivity.this,
                                                    R.string.serverCMPToast,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    return;
                                }
                                if (null != edit_ServerURL) {
                                    String appid = edit_appid.getText().toString().trim();
                                    boolean bool_appid = AppRTCUtils.setAppID(appid);
                                    CMPAddress address =
                                            AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY);

                                    if (!TextUtils.isEmpty(cmp)
                                            && !TextUtils.isEmpty(tokenServer)
                                            && null != address
                                            && !TextUtils.isEmpty(address.getServerURL())
                                            && address.getCmpServer().equals(cmp)
                                            && address.getServerURL().equals(tokenServer)) {
                                        //
                                        // FinLog.v(TAG,"用户继续保存的地址就是第一次进来默认选择的地址，就不用管，防止用户将默认地址保存成自定义的地址," +
                                        //                                        "并将自定义的地址删除
                                        // 已方便显示成默认地址，然后重新下载证书");
                                        SessionManager.getInstance()
                                                .remove(AppRTCUtils.CUSTOM_CMPKEY);
                                        reInitialization();
                                        saveSuccess(cmp);
                                    } else if (TextUtils.isEmpty(cmp)
                                            && TextUtils.isEmpty(tokenServer)) {
                                        FinLog.v(TAG, "同时为空 删除自定义的地址！");
                                        SessionManager.getInstance()
                                                .remove(AppRTCUtils.CUSTOM_CMPKEY);
                                        reInitialization();

                                        saveSuccess(cmp);
                                    } else {
                                        boolean bool =
                                                AppRTCUtils.setCMPAddress(cmp, tokenServer, 1);
                                        if (bool && bool_appid) {
                                            // RongRTCEngine.setVOIPServerAddress(cmp);

                                            saveSuccess(cmp);
                                        }
                                    }
                                } else {
                                    FinLog.v(TAG, "ERROR null!");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                FinLog.v(TAG, "ERROR " + e.getMessage());
                            }
                            break;
                        case R.id.btn_cancel:
                            if (!SettingActivity.this.isFinishing()
                                    && null != dialog
                                    && dialog.isShowing()) dialog.dismiss();
                            break;
                    }
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != dialog && dialog.isShowing()) dialog.dismiss();
    }

    private static String cerUrl = "";
    private static String cmpServer = "";

    private void reInitialization() {
        cerUrl = SessionManager.getInstance().getString(AppRTCUtils.CER_URL);
        CMPAddress cmpAddress = AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY);
        if (null != cmpAddress && !TextUtils.isEmpty(cmpAddress.getServerURL())) {
            cmpServer = cmpAddress.getCmpServer();
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            FinLog.v(
                                    "BinClient",
                                    "重新初始化 cmp 地址等==cerUrl=" + cerUrl + ",cmpServer=" + cmpServer);

                            // RongRTCEngine.setVOIPServerAddress(cmpServer);
                            // RongRTCEngine.init(getApplicationContext(), cmpServer, cmpServer);
                        }
                    });
        }
        new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    InputStream input = AppRTCUtils.downLoadFromUrl(cerUrl);

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    byte[] buffer = new byte[1024];
                                    int len;
                                    while ((len = input.read(buffer)) > -1) {
                                        baos.write(buffer, 0, len);
                                    }
                                    baos.flush();
                                    InputStream stream1 =
                                            new ByteArrayInputStream(baos.toByteArray());
                                    InputStream stream2 =
                                            new ByteArrayInputStream(baos.toByteArray());
                                    // RongRTCEngine.setCertificate(stream1, stream2);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .start();
    }

    private void saveSuccess(String cmpServer) {
        settingOptionConnectionType.setText(
                list_connectionType[Utils.connectionModeConfig(cmpServer) ? 0 : 1]);
        Toast.makeText(SettingActivity.this, R.string.saveSuccess, Toast.LENGTH_SHORT).show();
        if (!SettingActivity.this.isFinishing() && null != dialog && dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        switch (compoundButton.getId()) {
            case R.id.s_call_settings_audio_params:
                {
                    SessionManager.getInstance().put(IS_AUDIO_MUSIC, b);
                    break;
                }

            default:
                {
                    break;
                }
        }
    }
}
