package cn.rongcloud.rtc.device.utils;

public class Consts {
    public static final String SP_ENCODER_COLOR_FORMAT_ALIAS_KEY =
            "SP_ENCODER_COLOR_FORMAT_ALIAS_KEY"; // 存储编码颜色空间别名
    public static final String SP_DECODER_COLOR_FORMAT_ALIAS_KEY =
            "DECODER_COLORFORMATKEY"; // string
    public static final String SP_DECODER_COLOR_FORMAT_VAL_KEY =
            "DECODER_COLORFORMAT_VAL_KEY"; // int
    public static final String SP_ENCODER_COLOR_FORMAT_VAL_KEY =
            "ENCODER_COLORFORMAT_VAL_KEY"; // int

    public static final String SP_ENCODER_NAME_KEY = "SP_ENCODER_NAME_KEY";
    public static final String SP_DECODER_NAME_KEY = "SP_DECODER_NAME_KEY";
    public static final String SP_ENCODER_TYPE_KEY = "SP_ENCODER_TYPE_KEY"; // true 硬编
    public static final String SP_DECODER_TYPE_KEY = "SP_DECODER_TYPE_KEY";
    public static final String SP_ENCODER_LEVEL_KEY = "SP_ENCODER_LEVEL_KEY"; // true highProfile
    public static final String SP_ENCODER_BIT_RATE_MODE = "SP_ENCODER_BIT_RATE_MODE";

    public static final String ACQUISITION_MODE_KEY = "ACQUISITIONMODE_KEY"; // true texture
    public static final String CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY =
            "CAPTURE_CAMERADISPLAYORIENTATION_KEY";
    public static final String CAPTURE_FRAME_ORIENTATION_KEY = "CAPTURE_FRAMEORIENTATION_KEY";

    public static final String SP_AUDIO_SAMPLE_USE_AUDIO_RECORDER =
            "SP_AUDIO_SAMPLE_USE_AUDIO_RECORDER";
    public static final String SP_AUDIO_SOURCE = "SP_AUDIO_SOURCE";
    public static final String SP_AUDIO_SAMPLE_RATE = "SP_AUDIO_SAMPLE_RATE";
    public static final String SP_AUDIO_STEREO_ENABLE = "SP_AUDIO_STEREO_ENABLE";

    public static final String SP_AUDIO_TRANSPORT_BIT_RATE = "SP_AUDIO_TRANSPORT_BIT_RATE";

    public static final String SP_AUDIO_AGC_CONTROL_ENABLE = "SP_AUDIO_AGC_CONTROL_ENABLE";
    public static final String SP_AUDIO_AGC_LIMITER_ENABLE = "SP_AUDIO_AGC_LIMITER_ENABLE";
    public static final String SP_AUDIO_AGC_TARGET_DBOV = "SP_AUDIO_AGC_TARGET_DBOV";
    public static final String SP_AUDIO_AGC_COMPRESSION = "SP_AUDIO_AGC_COMPRESSION";
    public static final String SP_AUDIO_PRE_AMPLIFIER_ENABLE = "SP_AUDIO_PRE_AMPLIFIER_ENABLE";
    public static final String SP_AUDIO_PRE_AMPLIFIER_LEVEL = "SP_AUDIO_PRE_AMPLIFIER_LEVEL";

    public static final String SP_AUDIO_NOISE_SUPPRESSION_MODE = "SP_AUDIO_NOISE_SUPPRESSION_MODE";
    public static final String SP_AUDIO_NOISE_SUPPRESSION_LEVEL =
            "SP_AUDIO_NOISE_SUPPRESSION_LEVEL";
    public static final String SP_AUDIO_NOISE_HIGH_PASS_FILTER = "SP_AUDIO_NOISE_HIGH_PASS_FILTER";
    public static final String SP_AUDIO_ECHO_CANCEL_MODE = "SP_AEC_MODE";
    public static final String SP_AUDIO_ECHO_CANCEL_FILTER_ENABLE =
            "SP_AUDIO_ECHO_CANCEL_FILTER_ENABLE";

    public static final String decoder_colorFormat_eventBus = "decoder_colorFormat_eventBus";
    public static final String encoder_colorFormat_eventBus = "encoder_colorFormat_eventBus";
    public static final String device_camera_info_eventBus = "device_camera_info_eventBus";
    public static final String codec_info_eventBus = "codec_info_eventBus";

    public static final String ENCODE_BIT_RATE_MODE_CQ = "CQ";
    public static final String ENCODE_BIT_RATE_MODE_CBR = "CBR";
    public static final String ENCODE_BIT_RATE_MODE_VBR = "VBR";

    // 编码类型的code值 方便页面显示顶部操作栏标题 和  方便选择之后知道选择的是哪个具体的条目
    public static final int REQUEST_CODE_ENCODER_TYPE = 1;
    public static final int REQUEST_CODE_ENCODER_NAME = 2;
    public static final int REQUEST_CODE_ENCODER_COLOR_FORMAT = 3;
    public static final int REQUEST_CODE_DECODER_TYPE = 4;
    public static final int REQUEST_CODE_DECODER_NAME = 5;
    public static final int REQUEST_CODE_DECODER_COLOR_FORMAT = 6;

    public static final int REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION = 7;
    public static final int REQUEST_CODE_FRAME_ORIENTATION = 8;
    public static final int REQUEST_CODE_CAPTURE_TYPE = 9;

    public static final int REQUEST_CODE_ENCODER_LEVEL = 10;
    public static final int REQUEST_CODE_ENCODER_VIDEO_BITRATE_MODE = 11;

    public static final int REQUEST_CODE_AUDIO_SAMPLE_USE_AUDIO_RECORDER = 12;
    public static final int REQUEST_CODE_AUDIO_SOURCE = 13;
    public static final int REQUEST_CODE_AUDIO_SAMPLE_RATE = 14;
    public static final int REQUEST_AUDIO_CHANNEL_STEREO_ENABLE = 15;
    public static final int REQUEST_AUDIO_TRANSPORT_BIT_RATE = 16;
    public static final int REQUEST_AUDIO_AGC_CONTROL_ENABLE = 17;
    public static final int REQUEST_AUDIO_AGC_LIMITER_ENABLE = 18;
    public static final int REQUEST_AUDIO_AGC_TARGET_DBOV = 19;
    public static final int REQUEST_AUDIO_AGC_COMPRESSION_LEVEL = 20;
    public static final int REQUEST_AUDIO_PRE_AMPLIFIER_ENABLE = 21;
    public static final int REQUEST_AUDIO_PRE_AMPLIFIER_LEVEL = 22;
    public static final int REQUEST_AUDIO_NOISE_SUPPRESSION_MODE = 23;
    public static final int REQUEST_AUDIO_NOISE_SUPPRESSION_LEVEL = 24;
    public static final int REQUEST_AUDIO_NOISE_HIGH_PASS_FILTER = 27;
    public static final int REQUEST_AUDIO_ECHO_CANCEL_MODE = 25;
    public static final int REQUEST_AUDIO_ECHO_CANCEL_FILTER_ENABLE = 26;

    // UI显示特殊标识
    public static final int REQUEST_CODE_VIDEO = -1;
    public static final int REQUEST_CODE_CAPTURE = -2;
    public static final int REQUEST_CODE_AUDIO = -3;

    // intent跳转列表条目requestCode KEY
    public static final String EXTRA_KEY_REQUEST_CODE_KEY = "EXTRA_KEY_REQUEST_CODE_KEY";
    public static final String EXTRA_KEY_REQUEST_ARRAYLIST_DATA =
            "EXTRA_KEY_REQUEST_ARRAYLIST_DATA";
}
