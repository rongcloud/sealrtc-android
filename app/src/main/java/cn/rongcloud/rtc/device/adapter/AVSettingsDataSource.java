package cn.rongcloud.rtc.device.adapter;

import static android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_AGC_COMPRESSION_LEVEL;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_AGC_CONTROL_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_AGC_LIMITER_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_AGC_TARGET_DBOV;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_CHANNEL_STEREO_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_ECHO_CANCEL_FILTER_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_ECHO_CANCEL_MODE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_NOISE_HIGH_PASS_FILTER;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_NOISE_SUPPRESSION_LEVEL;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_NOISE_SUPPRESSION_MODE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_PRE_AMPLIFIER_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_PRE_AMPLIFIER_LEVEL;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_TRANSPORT_BIT_RATE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_AUDIO_SAMPLE_RATE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_AUDIO_SOURCE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_CAPTURE_TYPE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_DECODER_COLOR_FORMAT;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_DECODER_NAME;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_DECODER_TYPE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_ENCODER_COLOR_FORMAT;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_ENCODER_LEVEL;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_ENCODER_NAME;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_ENCODER_TYPE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_ENCODER_VIDEO_BITRATE_MODE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_FRAME_ORIENTATION;

import android.text.TextUtils;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.core.HardwareCodecHelper;
import cn.rongcloud.rtc.device.entity.AVConfigInfo;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import java.util.ArrayList;
import java.util.List;

public class AVSettingsDataSource {
    private static final String TAG = "AVSettingsDataSource";

    public enum SettingCategory {
        VideoEncoder,
        VideoDecoder,
        VideoCamera,
        AudioCapture,
        AudioAGC,
        AudioNS,
        AudioEC,
    }

    private List<AVConfigInfo> videoEncoderSettings = new ArrayList<>();
    private List<AVConfigInfo> videoDecoderSettings = new ArrayList<>();
    private List<AVConfigInfo> cameraSettings = new ArrayList<>();
    private List<AVConfigInfo> audioCaptureSettings = new ArrayList<>();
    private List<AVConfigInfo> audioAgcSettings = new ArrayList<>();
    private List<AVConfigInfo> audioNoiseSuppressSettings = new ArrayList<>();
    private List<AVConfigInfo> audioEchoCancelSettings = new ArrayList<>();

    private static AVSettingsDataSource instance;

    public static AVSettingsDataSource getInstance() {
        synchronized (AVSettingsDataSource.class) {
            if (instance == null) {
                instance = new AVSettingsDataSource();
            }
        }
        return instance;
    }

    private AVSettingsDataSource() {

        loadVideoEncoderConfig();
        loadVideoDecoderConfig();
        loadVideoCameraConfig();

        loadAudioCaptureConfig();
        loadAudioAgcConfig();
        loadAudioNSConfig();
        loadAudioECConfig();
    }

    private SettingCategory settingCategory = SettingCategory.VideoEncoder;

    public boolean isEncoderHardMode() {
        if (settingCategory == SettingCategory.VideoEncoder) {
            return TextUtils.equals(
                    videoEncoderSettings.get(0).getItemValue(),
                    Utils.getContext().getResources().getString(R.string.hw_encoder_str));
        }
        return false;
    }

    public boolean isDecoderHardMode() {
        if (settingCategory == SettingCategory.VideoDecoder) {
            return TextUtils.equals(
                    videoDecoderSettings.get(0).getItemValue(),
                    Utils.getContext().getResources().getString(R.string.hw_decoder_str));
        }
        return false;
    }

    public List<AVConfigInfo> getCurrentConfig() {
        if (this.settingCategory == SettingCategory.VideoEncoder) {
            return videoEncoderSettings;
        }  else if (this.settingCategory == SettingCategory.VideoCamera) {
            return cameraSettings;
        } else if (this.settingCategory == SettingCategory.AudioCapture) {
            return audioCaptureSettings;
        } else if (this.settingCategory == SettingCategory.AudioAGC) {
            return audioAgcSettings;
        } else if (this.settingCategory == SettingCategory.AudioNS) {
            return audioNoiseSuppressSettings;
        } else if (this.settingCategory == SettingCategory.AudioEC) {
            return audioEchoCancelSettings;
        }
        return videoEncoderSettings;
    }

    public List<AVConfigInfo> getChangedConfig() {
        List<AVConfigInfo> changedConfigs = new ArrayList<>();
        changedConfigs.addAll(getCategoryChangedSettings(videoEncoderSettings));
        changedConfigs.addAll(getCategoryChangedSettings(videoDecoderSettings));
        changedConfigs.addAll(getCategoryChangedSettings(cameraSettings));

        changedConfigs.addAll(getCategoryChangedSettings(audioCaptureSettings));
        changedConfigs.addAll(getCategoryChangedSettings(audioAgcSettings));
        changedConfigs.addAll(getCategoryChangedSettings(audioNoiseSuppressSettings));
        changedConfigs.addAll(getCategoryChangedSettings(audioEchoCancelSettings));
        return changedConfigs;
    }

    private List<AVConfigInfo> getCategoryChangedSettings(List<AVConfigInfo> categorySettings) {
        List<AVConfigInfo> changedConfigs = new ArrayList<>();
        if (categorySettings == null || categorySettings.size() == 0) return changedConfigs;
        for (AVConfigInfo config : categorySettings) {
            if (config != null
                    && !TextUtils.isEmpty(config.getItemValueNew())
                    && !TextUtils.equals(config.getItemValueOld(), config.getItemValue())) {
                changedConfigs.add(config);
            }
        }

        return changedConfigs;
    }

    public List<AVConfigInfo> getVideoEncoderConfig() {
        this.settingCategory = SettingCategory.VideoEncoder;
        return videoEncoderSettings;
    }

    public List<AVConfigInfo> getVideoDecoderConfig() {
        this.settingCategory = SettingCategory.VideoDecoder;
        return videoDecoderSettings;
    }

    public List<AVConfigInfo> getVideoCameraConfig() {
        this.settingCategory = SettingCategory.VideoCamera;
        return cameraSettings;
    }

    public List<AVConfigInfo> getAudioCaptureConfig() {
        this.settingCategory = SettingCategory.AudioCapture;
        return audioCaptureSettings;
    }

    public List<AVConfigInfo> getAudioAgcConfig() {
        this.settingCategory = SettingCategory.AudioAGC;
        return audioAgcSettings;
    }

    public List<AVConfigInfo> getAudioNsConfig() {
        this.settingCategory = SettingCategory.AudioNS;
        return audioNoiseSuppressSettings;
    }

    public List<AVConfigInfo> getAudioEcConfig() {
        this.settingCategory = SettingCategory.AudioEC;
        return audioEchoCancelSettings;
    }

    public boolean saveConfig() {
        try {
            saveVideoEncoderConfig();
            saveVideoDecoderConfig();
            saveVideoCameraConfig();

            saveAudioCaptureConfig();
            saveAudioAgcConfig();
            saveAudioNSConfig();
            saveAudioECConfig();

            reloadConfig();
        } catch (Exception ex) {
            FinLog.e(TAG, ex.getMessage());
            return false;
        }
        return true;
    }

    public void reloadConfig() {
        this.videoEncoderSettings.clear();
        this.videoDecoderSettings.clear();
        this.cameraSettings.clear();
        this.audioCaptureSettings.clear();
        this.audioAgcSettings.clear();
        this.audioNoiseSuppressSettings.clear();
        this.audioEchoCancelSettings.clear();

        loadVideoEncoderConfig();
        loadVideoDecoderConfig();
        loadVideoCameraConfig();

        loadAudioCaptureConfig();
        loadAudioAgcConfig();
        loadAudioNSConfig();
        loadAudioECConfig();
    }

    public void resetAudioConfig() {
        resetAudioCaptureConfig();
        resetAudioAgcConfig();
        resetAudioNSConfig();
        resetAudioECConfig();

        loadAudioCaptureConfig();
        loadAudioAgcConfig();
        loadAudioNSConfig();
        loadAudioECConfig();
    }

    public void resetVideoConfig() {
        resetVideoEncoderConfig();
        resetVideoDecoderConfig();
        resetVideoCameraConfig();

        loadVideoEncoderConfig();
        loadVideoDecoderConfig();
        loadVideoCameraConfig();
    }

    public String getItemConfig(SettingCategory category, int requestCode) {
        AVConfigInfo item = getConfigInfo(category, requestCode);
        if (item == null) {
            return null;
        }
        return item.getItemValue();
    }

    /** 初始化编码器选项列表 */
    private void loadVideoEncoderConfig() {
        if (videoEncoderSettings.size() > 0) {
            return;
        }

        String hw_encoder = Utils.getContext().getResources().getString(R.string.hw_encoder_str);
        String sw_encoder = Utils.getContext().getResources().getString(R.string.soft_encoder_str);
        String encoder_leval_hight =
                Utils.getContext().getResources().getString(R.string.encoder_leval_hight);
        String encoder_level_base =
                Utils.getContext().getResources().getString(R.string.encoder_leval_baseline);
        String encoder_bit_rate_mode_cbr =
                Utils.getContext().getResources().getString(R.string.encoder_bit_rate_mode_cbr);

        boolean encoderType =
                SessionManager.getInstance().getBoolean(Consts.SP_ENCODER_TYPE_KEY, true);
        String enCoderName = HardwareCodecHelper.getDefaultH264Encoder();
        String enCoderColorName =
                SessionManager.getInstance().getString(Consts.SP_ENCODER_COLOR_FORMAT_ALIAS_KEY);
        int encoderColorValue =
                SessionManager.getInstance().getInt(Consts.SP_ENCODER_COLOR_FORMAT_VAL_KEY, 0);
        boolean encoderLevel_key =
                SessionManager.getInstance().getBoolean(Consts.SP_ENCODER_LEVEL_KEY, false);
        String encoderBitRateMode =
                SessionManager.getInstance()
                        .getString(
                                Consts.SP_ENCODER_BIT_RATE_MODE,
                                Utils.getContext()
                                        .getResources()
                                        .getString(R.string.def_encoder_bitrate_mode));

        if (videoEncoderSettings != null && videoEncoderSettings.size() > 0) {
            videoEncoderSettings.clear();
        }

        videoEncoderSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.encode_type_str),
                        REQUEST_CODE_ENCODER_TYPE,
                        encoderType ? hw_encoder : sw_encoder,
                        0));
        videoEncoderSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.encoder_name_str),
                        REQUEST_CODE_ENCODER_NAME,
                        enCoderName,
                        0));
        videoEncoderSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.encoder_color_format_str),
                        REQUEST_CODE_ENCODER_COLOR_FORMAT,
                        enCoderColorName,
                        encoderColorValue));
        videoEncoderSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.encoder_level),
                        REQUEST_CODE_ENCODER_LEVEL,
                        encoderLevel_key ? encoder_leval_hight : encoder_level_base,
                        1));
        videoEncoderSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.encoder_bit_rate_mode),
                        REQUEST_CODE_ENCODER_VIDEO_BITRATE_MODE,
                        encoderBitRateMode,
                        0));
    }

    private void saveVideoEncoderConfig() {
        String hw_encoder = Utils.getContext().getResources().getString(R.string.hw_encoder_str);
        String encoder_level_high =
                Utils.getContext().getResources().getString(R.string.encoder_leval_hight);

        AVConfigInfo configInfo =
                getConfigInfo(SettingCategory.VideoEncoder, REQUEST_CODE_ENCODER_TYPE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoEncoder, REQUEST_CODE_ENCODER_TYPE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_ENCODER_TYPE_KEY,
                            TextUtils.equals(hw_encoder, configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.VideoEncoder, REQUEST_CODE_ENCODER_NAME);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoEncoder, REQUEST_CODE_ENCODER_NAME");
        } else {
            SessionManager.getInstance().put(Consts.SP_ENCODER_NAME_KEY, configInfo.getItemValue());
        }

        configInfo = getConfigInfo(SettingCategory.VideoEncoder, REQUEST_CODE_ENCODER_COLOR_FORMAT);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoEncoder, REQUEST_CODE_ENCODER_COLOR_FORMAT");
        } else {
            SessionManager.getInstance()
                    .put(Consts.SP_ENCODER_COLOR_FORMAT_ALIAS_KEY, configInfo.getItemValue());
            SessionManager.getInstance()
                    .put(Consts.SP_ENCODER_COLOR_FORMAT_VAL_KEY, configInfo.getItemRealValue());
        }

        configInfo = getConfigInfo(SettingCategory.VideoEncoder, REQUEST_CODE_ENCODER_LEVEL);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoEncoder, REQUEST_CODE_ENCODER_LEVEL");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_ENCODER_LEVEL_KEY,
                            TextUtils.equals(encoder_level_high, configInfo.getItemValue()));
        }

        configInfo =
                getConfigInfo(
                        SettingCategory.VideoEncoder, REQUEST_CODE_ENCODER_VIDEO_BITRATE_MODE);
        if (configInfo == null) {
            FinLog.e(
                    TAG,
                    "configInfo = null. VideoEncoder, REQUEST_CODE_ENCODER_VIDEO_BITRATE_MODE");
        } else {
            SessionManager.getInstance()
                    .put(Consts.SP_ENCODER_BIT_RATE_MODE, configInfo.getItemValue());
        }
    }

    private void resetVideoEncoderConfig() {
        videoEncoderSettings.clear();
        SessionManager.getInstance().remove(Consts.SP_ENCODER_TYPE_KEY);
        SessionManager.getInstance().remove(Consts.SP_ENCODER_NAME_KEY);

        SessionManager.getInstance().remove(Consts.SP_ENCODER_COLOR_FORMAT_ALIAS_KEY);
        SessionManager.getInstance().remove(Consts.SP_ENCODER_COLOR_FORMAT_VAL_KEY);

        SessionManager.getInstance().remove(Consts.SP_ENCODER_LEVEL_KEY);
        SessionManager.getInstance().remove(Consts.SP_ENCODER_BIT_RATE_MODE);
    }

    private void loadVideoDecoderConfig() {
        if (videoDecoderSettings.size() > 0) {
            return;
        }

        String hw_decoder_str =
                Utils.getContext().getResources().getString(R.string.hw_decoder_str);
        String sw_decoder_str =
                Utils.getContext().getResources().getString(R.string.soft_decoder_str);

        boolean decoderType =
                SessionManager.getInstance().getBoolean(Consts.SP_DECODER_TYPE_KEY, true);
        String deCoderName = HardwareCodecHelper.getDefaultH264Decoder();
        String deCoderColorName =
                SessionManager.getInstance().getString(Consts.SP_DECODER_COLOR_FORMAT_ALIAS_KEY);
        int decoderColorSpace =
                SessionManager.getInstance().getInt(Consts.SP_DECODER_COLOR_FORMAT_VAL_KEY, 0);

        if (videoDecoderSettings != null && videoDecoderSettings.size() > 0) {
            videoDecoderSettings.clear();
        }

        videoDecoderSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.decoder_type_str),
                        REQUEST_CODE_DECODER_TYPE,
                        decoderType ? hw_decoder_str : sw_decoder_str,
                        0));
        videoDecoderSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.decoder_name_str),
                        REQUEST_CODE_DECODER_NAME,
                        deCoderName,
                        0));
        videoDecoderSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.decoder_color_format_str),
                        REQUEST_CODE_DECODER_COLOR_FORMAT,
                        deCoderColorName,
                        decoderColorSpace));
    }

    private void saveVideoDecoderConfig() {
        String hw_decoder_str =
                Utils.getContext().getResources().getString(R.string.hw_decoder_str);

        AVConfigInfo configInfo =
                getConfigInfo(SettingCategory.VideoDecoder, REQUEST_CODE_DECODER_TYPE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoDecoder, REQUEST_CODE_DECODER_TYPE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_DECODER_TYPE_KEY,
                            TextUtils.equals(hw_decoder_str, configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.VideoDecoder, REQUEST_CODE_DECODER_NAME);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoDecoder, REQUEST_CODE_DECODER_NAME");
        } else {
            SessionManager.getInstance().put(Consts.SP_DECODER_NAME_KEY, configInfo.getItemValue());
        }

        configInfo = getConfigInfo(SettingCategory.VideoDecoder, REQUEST_CODE_DECODER_COLOR_FORMAT);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoDecoder, REQUEST_CODE_DECODER_COLOR_FORMAT");
        } else {
            SessionManager.getInstance()
                    .put(Consts.SP_DECODER_COLOR_FORMAT_ALIAS_KEY, configInfo.getItemValue());
            SessionManager.getInstance()
                    .put(Consts.SP_DECODER_COLOR_FORMAT_VAL_KEY, configInfo.getItemRealValue());
        }
    }

    private void resetVideoDecoderConfig() {
        videoDecoderSettings.clear();
        SessionManager.getInstance().remove(Consts.SP_DECODER_TYPE_KEY);
        SessionManager.getInstance().remove(Consts.SP_DECODER_NAME_KEY);
        SessionManager.getInstance().remove(Consts.SP_DECODER_COLOR_FORMAT_ALIAS_KEY);
        SessionManager.getInstance().remove(Consts.SP_DECODER_COLOR_FORMAT_VAL_KEY);
    }

    private void loadVideoCameraConfig() {
        if (cameraSettings.size() > 0) {
            return;
        }

        int cameraDisplayOrientation =
                SessionManager.getInstance()
                        .getInt(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY, 0);
        int frameOrientation =
                SessionManager.getInstance().getInt(Consts.CAPTURE_FRAME_ORIENTATION_KEY, -1);
        String capture_type_texture =
                Utils.getContext().getResources().getString(R.string.capture_type_texture);
        String capture_type_yuv =
                Utils.getContext().getResources().getString(R.string.capture_type_yuv);

        if (cameraSettings != null && cameraSettings.size() > 0) {
            cameraSettings.clear();
        }

        boolean acquisitionMode =
                SessionManager.getInstance().getBoolean(Consts.ACQUISITION_MODE_KEY, true);
        cameraSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.camer_display_orientation),
                        REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION,
                        String.valueOf(cameraDisplayOrientation),
                        cameraDisplayOrientation));
        cameraSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.frame_orientation),
                        REQUEST_CODE_FRAME_ORIENTATION,
                        String.valueOf(frameOrientation),
                        frameOrientation));
        cameraSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.capture_type_str),
                        REQUEST_CODE_CAPTURE_TYPE,
                        acquisitionMode ? capture_type_texture : capture_type_yuv,
                        0));
    }

    private void saveVideoCameraConfig() {

        AVConfigInfo configInfo =
                getConfigInfo(SettingCategory.VideoCamera, REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION);
        if (configInfo == null) {
            FinLog.e(
                    TAG, "configInfo = null. VideoCamera, REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY,
                            Integer.parseInt(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.VideoCamera, REQUEST_CODE_FRAME_ORIENTATION);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoCamera, REQUEST_CODE_FRAME_ORIENTATION");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.CAPTURE_FRAME_ORIENTATION_KEY,
                            Integer.parseInt(configInfo.getItemValue()));
        }

        String capture_type_texture =
                Utils.getContext().getResources().getString(R.string.capture_type_texture);
        configInfo = getConfigInfo(SettingCategory.VideoCamera, REQUEST_CODE_CAPTURE_TYPE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. VideoCamera, REQUEST_CODE_CAPTURE_TYPE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.ACQUISITION_MODE_KEY,
                            TextUtils.equals(capture_type_texture, configInfo.getItemValue()));
        }
    }

    private void resetVideoCameraConfig() {
        cameraSettings.clear();
        SessionManager.getInstance().remove(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY);
        SessionManager.getInstance().remove(Consts.CAPTURE_FRAME_ORIENTATION_KEY);
        SessionManager.getInstance().remove(Consts.ACQUISITION_MODE_KEY);
    }

    private void loadAudioCaptureConfig() {
        if (audioCaptureSettings.size() > 0) {
            return;
        }

        //        boolean audioSampleUseAudioRecorder =
        // SessionManager.getInstance().getBoolean(Consts.SP_AUDIO_SAMPLE_USE_AUDIO_RECORDER, true);
        int audioSource =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_SOURCE, VOICE_COMMUNICATION);
        int audioSampleRate =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_SAMPLE_RATE, 48000);
        boolean audioSampleStereo =
                SessionManager.getInstance()
                        .getBoolean(Consts.SP_AUDIO_STEREO_ENABLE, false);
        int audioBitRate =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_TRANSPORT_BIT_RATE, 30);

        if (audioCaptureSettings != null && audioCaptureSettings.size() > 0) {
            audioCaptureSettings.clear();
        }

        //        audioCaptureSettings.add(new
        // AVConfigInfo(Utils.getContext().getResources().getString(R.string.audio_sample_use_audio_recorder), REQUEST_CODE_AUDIO_SAMPLE_USE_AUDIO_RECORDER, String.valueOf(audioSampleUseAudioRecorder)));
        audioCaptureSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.audio_audio_source),
                        REQUEST_CODE_AUDIO_SOURCE,
                        String.valueOf(audioSource),
                        audioSource));
        audioCaptureSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.audio_sample_rate),
                        REQUEST_CODE_AUDIO_SAMPLE_RATE,
                        String.valueOf(audioSampleRate)));
        audioCaptureSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.audio_channel_stereo),
                        REQUEST_AUDIO_CHANNEL_STEREO_ENABLE,
                        String.valueOf(audioSampleStereo)));
        audioCaptureSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_transport_bit_rate),
                        REQUEST_AUDIO_TRANSPORT_BIT_RATE,
                        String.valueOf(audioBitRate)));
    }

    private void saveAudioCaptureConfig() {
        //        AVConfigInfo configInfo = getConfigInfo(SettingCategory.AudioCapture,
        // REQUEST_CODE_AUDIO_SAMPLE_USE_AUDIO_RECORDER);
        //        if (configInfo == null) {
        //            FinLog.e(TAG, "configInfo = null. Audio,
        // REQUEST_CODE_AUDIO_SAMPLE_USE_AUDIO_RECORDER");
        //        } else {
        //            SessionManager.getInstance().put(Consts.SP_AUDIO_SAMPLE_USE_AUDIO_RECORDER,
        // Boolean.parseBoolean(configInfo.getItemValue()));
        //        }

        AVConfigInfo configInfo =
                getConfigInfo(SettingCategory.AudioCapture, REQUEST_CODE_AUDIO_SOURCE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_CODE_AUDIO_SOURCE");
        } else {
            SessionManager.getInstance()
                    .put(Consts.SP_AUDIO_SOURCE, Integer.parseInt(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioCapture, REQUEST_CODE_AUDIO_SAMPLE_RATE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_CODE_AUDIO_SAMPLE_RATE");
        } else {
            SessionManager.getInstance()
                    .put(Consts.SP_AUDIO_SAMPLE_RATE, Integer.parseInt(configInfo.getItemValue()));
        }

        configInfo =
                getConfigInfo(SettingCategory.AudioCapture, REQUEST_AUDIO_CHANNEL_STEREO_ENABLE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_CHANNEL_STEREO_ENABLE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_STEREO_ENABLE,
                            Boolean.parseBoolean(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioCapture, REQUEST_AUDIO_TRANSPORT_BIT_RATE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_TRANSPORT_BIT_RATE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_TRANSPORT_BIT_RATE,
                            Integer.parseInt(configInfo.getItemValue()));
        }
    }

    private void resetAudioCaptureConfig() {
        audioCaptureSettings.clear();
        SessionManager.getInstance().remove(Consts.SP_AUDIO_SOURCE);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_SAMPLE_RATE);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_STEREO_ENABLE);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_TRANSPORT_BIT_RATE);
    }

    private void loadAudioAgcConfig() {
        if (audioAgcSettings.size() > 0) {
            return;
        }

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

        if (audioAgcSettings != null && audioAgcSettings.size() > 0) {
            audioAgcSettings.clear();
        }

        audioAgcSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_agc_control_enable),
                        REQUEST_AUDIO_AGC_CONTROL_ENABLE,
                        String.valueOf(audioAgcEnable)));

        audioAgcSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_pre_amplifier_enable),
                        REQUEST_AUDIO_PRE_AMPLIFIER_ENABLE,
                        String.valueOf(audioPreAmplifierEnable)));
        audioAgcSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_pre_amplifier_level),
                        REQUEST_AUDIO_PRE_AMPLIFIER_LEVEL,
                        String.valueOf(audioPreAmplifierLevel)));

        audioAgcSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.audio_agc_targetDBOV),
                        REQUEST_AUDIO_AGC_TARGET_DBOV,
                        String.valueOf(audioAgcTargetDbov)));
        audioAgcSettings.add(
                new AVConfigInfo(
                        Utils.getContext().getResources().getString(R.string.audio_agc_compression),
                        REQUEST_AUDIO_AGC_COMPRESSION_LEVEL,
                        String.valueOf(audioAgcCompression)));
        //        audioAgcSettings.add(new
        // AVConfigInfo(Utils.getContext().getResources().getString(R.string.audio_agc_limiter_enable), REQUEST_AUDIO_AGC_LIMITER_ENABLE, String.valueOf(audioAgcLimiter)));
    }

    private void saveAudioAgcConfig() {
        AVConfigInfo configInfo =
                getConfigInfo(SettingCategory.AudioAGC, REQUEST_AUDIO_AGC_CONTROL_ENABLE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_AGC_CONTROL_ENABLE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_AGC_CONTROL_ENABLE,
                            Boolean.parseBoolean(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioAGC, REQUEST_AUDIO_AGC_LIMITER_ENABLE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_AGC_LIMITER_ENABLE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_AGC_LIMITER_ENABLE,
                            Boolean.parseBoolean(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioAGC, REQUEST_AUDIO_AGC_TARGET_DBOV);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_AGC_TARGET_DBOV");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_AGC_TARGET_DBOV,
                            Integer.parseInt(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioAGC, REQUEST_AUDIO_AGC_COMPRESSION_LEVEL);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_AGC_COMPRESSION_LEVEL");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_AGC_COMPRESSION,
                            Integer.parseInt(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioAGC, REQUEST_AUDIO_PRE_AMPLIFIER_ENABLE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_PRE_AMPLIFIER_ENABLE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_PRE_AMPLIFIER_ENABLE,
                            Boolean.parseBoolean(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioAGC, REQUEST_AUDIO_PRE_AMPLIFIER_LEVEL);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_PRE_AMPLIFIER_LEVEL");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_PRE_AMPLIFIER_LEVEL,
                            Float.parseFloat(configInfo.getItemValue()));
        }
    }

    private void resetAudioAgcConfig() {
        audioAgcSettings.clear();
        SessionManager.getInstance().remove(Consts.SP_AUDIO_AGC_CONTROL_ENABLE);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_AGC_LIMITER_ENABLE);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_AGC_TARGET_DBOV);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_AGC_COMPRESSION);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_PRE_AMPLIFIER_ENABLE);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_PRE_AMPLIFIER_LEVEL);
    }

    private void loadAudioNSConfig() {
        if (audioNoiseSuppressSettings.size() > 0) {
            return;
        }
        int audioNoiseSuppresionMode =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_NOISE_SUPPRESSION_MODE, 0);
        int audioNoiseSuppressionLevel =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_NOISE_SUPPRESSION_LEVEL, 1);
        boolean audioNoiseHighPassFilter =
                SessionManager.getInstance()
                        .getBoolean(Consts.SP_AUDIO_NOISE_HIGH_PASS_FILTER, true);
        if (audioNoiseSuppressSettings != null && audioNoiseSuppressSettings.size() > 0) {
            audioNoiseSuppressSettings.clear();
        }

        audioNoiseSuppressSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_noise_suppression),
                        REQUEST_AUDIO_NOISE_SUPPRESSION_MODE,
                        String.valueOf(audioNoiseSuppresionMode)));
        audioNoiseSuppressSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_noise_suppression_level),
                        REQUEST_AUDIO_NOISE_SUPPRESSION_LEVEL,
                        String.valueOf(audioNoiseSuppressionLevel)));
        audioNoiseSuppressSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_noise_high_pass_filter),
                        REQUEST_AUDIO_NOISE_HIGH_PASS_FILTER,
                        String.valueOf(audioNoiseHighPassFilter)));
    }

    private void saveAudioNSConfig() {
        AVConfigInfo configInfo =
                getConfigInfo(SettingCategory.AudioNS, REQUEST_AUDIO_NOISE_SUPPRESSION_MODE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_NOISE_SUPPRESSION_MODE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_NOISE_SUPPRESSION_MODE,
                            Integer.parseInt(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioNS, REQUEST_AUDIO_NOISE_SUPPRESSION_LEVEL);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_NOISE_SUPPRESSION_LEVEL");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_NOISE_SUPPRESSION_LEVEL,
                            Integer.parseInt(configInfo.getItemValue()));
        }

        configInfo = getConfigInfo(SettingCategory.AudioNS, REQUEST_AUDIO_NOISE_HIGH_PASS_FILTER);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_NOISE_HIGH_PASS_FILTER");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_NOISE_HIGH_PASS_FILTER,
                            Boolean.parseBoolean(configInfo.getItemValue()));
        }
    }

    private void resetAudioNSConfig() {
        audioNoiseSuppressSettings.clear();
        SessionManager.getInstance().remove(Consts.SP_AUDIO_NOISE_SUPPRESSION_MODE);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_NOISE_SUPPRESSION_LEVEL);
    }

    private void loadAudioECConfig() {
        if (audioEchoCancelSettings.size() > 0) {
            return;
        }

        int audioEchoCancelMode =
                SessionManager.getInstance().getInt(Consts.SP_AUDIO_ECHO_CANCEL_MODE, 0);
        boolean audioEchoCancelFilterEnable =
                SessionManager.getInstance()
                        .getBoolean(Consts.SP_AUDIO_ECHO_CANCEL_FILTER_ENABLE, false);

        if (audioEchoCancelSettings != null && audioEchoCancelSettings.size() > 0) {
            audioEchoCancelSettings.clear();
        }
        audioEchoCancelSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_echo_cancel_mode),
                        REQUEST_AUDIO_ECHO_CANCEL_MODE,
                        String.valueOf(audioEchoCancelMode)));
        audioEchoCancelSettings.add(
                new AVConfigInfo(
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_echo_cancel_filter),
                        REQUEST_AUDIO_ECHO_CANCEL_FILTER_ENABLE,
                        String.valueOf(audioEchoCancelFilterEnable)));
    }

    private void saveAudioECConfig() {
        AVConfigInfo configInfo =
                getConfigInfo(SettingCategory.AudioEC, REQUEST_AUDIO_ECHO_CANCEL_MODE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_ECHO_CANCEL_MODE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_ECHO_CANCEL_MODE,
                            Integer.parseInt(configInfo.getItemValue()));
        }

        configInfo =
                getConfigInfo(SettingCategory.AudioEC, REQUEST_AUDIO_ECHO_CANCEL_FILTER_ENABLE);
        if (configInfo == null) {
            FinLog.e(TAG, "configInfo = null. Audio, REQUEST_AUDIO_ECHO_CANCEL_FILTER_ENABLE");
        } else {
            SessionManager.getInstance()
                    .put(
                            Consts.SP_AUDIO_ECHO_CANCEL_FILTER_ENABLE,
                            Boolean.parseBoolean(configInfo.getItemValue()));
        }
    }

    private void resetAudioECConfig() {
        audioEchoCancelSettings.clear();
        SessionManager.getInstance().remove(Consts.SP_AUDIO_ECHO_CANCEL_MODE);
        SessionManager.getInstance().remove(Consts.SP_AUDIO_ECHO_CANCEL_FILTER_ENABLE);
    }

    private AVConfigInfo getConfigInfo(SettingCategory category, int requestCode) {
        List<AVConfigInfo> configInfos = null;
        AVConfigInfo findConfig = null;
        if (category == SettingCategory.VideoEncoder) {
            configInfos = videoEncoderSettings;
        } else if (category == SettingCategory.VideoDecoder) {
            configInfos = videoDecoderSettings;
        } else if (category == SettingCategory.VideoCamera) {
            configInfos = cameraSettings;
        } else if (category == SettingCategory.AudioCapture) {
            configInfos = audioCaptureSettings;
        } else if (category == SettingCategory.AudioAGC) {
            configInfos = audioAgcSettings;
        } else if (category == SettingCategory.AudioNS) {
            configInfos = audioNoiseSuppressSettings;
        } else if (category == SettingCategory.AudioEC) {
            configInfos = audioEchoCancelSettings;
        }

        if (configInfos == null || configInfos.size() == 0) {
            return null;
        }

        for (AVConfigInfo item : configInfos) {
            if (item.getRequestCode() == requestCode) {
                findConfig = item;
                break;
            }
        }
        return findConfig;
    }
}
