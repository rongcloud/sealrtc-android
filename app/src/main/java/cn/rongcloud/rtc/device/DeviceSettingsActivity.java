package cn.rongcloud.rtc.device;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.entity.AVConfigInfo;
import cn.rongcloud.rtc.device.entity.CodecInfo;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;

public class DeviceSettingsActivity extends DeviceBaseActivity {


    private static final int SIZE = 2;

    private List<AVConfigInfo> codecConfigInfos;
    private ArrayList<CodecInfo> encoderInfo = new ArrayList<>();
    private ArrayList<CodecInfo> decoderInfo = new ArrayList<>();
    private ArrayList<String> encoderName = new ArrayList<>();
    private ArrayList<String> decoderName = new ArrayList<>();

    private String CodecType;//编码、解码标识符 ，CodecType.equals("1")?"解码器列表":"编码器列表"
    private ArrayList<String> list_select = new ArrayList<String>(SIZE);//是/否选择通用集合

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codec_settings);
    }

//
//    private void init() {
//        list_EncoderType.clear();
//        list_DecoderType.clear();
//        list_captureType.clear();
//        list_encodeLevel.clear();
//        encoderInfo.clear();
//        decoderInfo.clear();
//        encoderName.clear();
//        decoderName.clear();
//        list_select.clear();
//
//        //设置默认值和初始化列表数据
//        String hw_encoder = getResources().getString(R.string.hw_encoder_str);
//        list_EncoderType.add(hw_encoder);
//        String soft_encoder_str = getResources().getString(R.string.soft_encoder_str);
//        list_EncoderType.add(soft_encoder_str);
//
//        String hw_decoder_str = getResources().getString(R.string.hw_decoder_str);
//        list_DecoderType.add(hw_decoder_str);
//
//        String soft_decoder_str = getResources().getString(R.string.soft_decoder_str);
//        list_DecoderType.add(soft_decoder_str);
//
//        String capture_type_texture = getResources().getString(R.string.capture_type_texture);
//        list_captureType.add(capture_type_texture);
//
//        String encoder_leval_baseline=getResources().getString(R.string.encoder_leval_baseline);
//        list_encodeLevel.add(encoder_leval_baseline);
//
//        String encoder_leval_hight=getResources().getString(R.string.encoder_leval_hight);
//        list_encodeLevel.add(encoder_leval_hight);
//
//        String capture_type_yuv = getResources().getString(R.string.capture_type_yuv);
//        list_captureType.add(capture_type_yuv);
//
//        String select_YES=getResources().getString(R.string.settings_text_gpufliter_yes);
//        String select_NO=getResources().getString(R.string.settings_text_gpufliter_no);
//        list_select.add(select_YES);
//        list_select.add(select_NO);
//
//        if (codecConfigInfos != null && codecConfigInfos.size() > 0) {
//            codecConfigInfos.clear();
//        }
//        String codecInfoJson = RTCDevice.getInstance().getMediaCodecInfo();
//        if (!TextUtils.isEmpty(codecInfoJson)) {
//            Gson gson = new Gson();
//            List<CodecInfo> codecInfoList = gson.fromJson(codecInfoJson, new TypeToken<List<CodecInfo>>() {
//            }.getType());
//            for (int i = 0; i < codecInfoList.size(); i++) {
//                if (codecInfoList.get(i).getCodecName().indexOf("decoder") != -1) {
//                    decoderInfo.add(codecInfoList.get(i));
//                    decoderName.add(codecInfoList.get(i).getCodecName());
//                } else {
//                    encoderInfo.add(codecInfoList.get(i));
//                    encoderName.add(codecInfoList.get(i).getCodecName());
//                }
//            }
//        }
//        boolean acquisitionMode = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.ACQUISITION_MODE_KEY);
//        boolean encoderLevel_key = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderLevel_key);
//        boolean encoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderType_key);
//        boolean decoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.decoderType_key);
//        String enCoderName = SessionManager.getInstance(Utils.getContext()).getString(Consts.encoder_key);
//        String enCoderColorName = SessionManager.getInstance(Utils.getContext()).getString(Consts.encoder_colorFormat_alias_key);
//
//        int decoder_colorFormat_val = SessionManager.getInstance(Utils.getContext()).getInt(Consts.decoder_colorFormat_val_key);
//        int encoder_colorFormat_val = SessionManager.getInstance(Utils.getContext()).getInt(Consts.encoder_colorFormat_val_key);
//
//        String deCoderName = SessionManager.getInstance(Utils.getContext()).getString(decoder_key);
//        String deCoderColorName = SessionManager.getInstance(Utils.getContext()).getString(Consts.decoder_colorFormat_alias_key);
//        int cameraDisplayOrientation = SessionManager.getInstance(Utils.getContext()).getInt(Consts.capture_cameraDisplayOrientation_key);
//        int frameOrientation = SessionManager.getInstance(Utils.getContext()).getInt(Consts.capture_frameOrientation_key);
//
//        codecConfigInfos = new ArrayList<CodecConfigInfo>();
//        {
//            codecConfigInfos.add(new CodecConfigInfo(getResources().getString(R.string.encode_type_str), list_EncoderType, REQUEST_CODE_ENCODER_TYPE, encoderType ? hw_encoder : soft_encoder_str));
//            codecConfigInfos.add(new CodecConfigInfo(getResources().getString(R.string.encoder_name_str), encoderName, REQUEST_CODE_ENCODER_NAME, enCoderName));
//            codecConfigInfos.add(new CodecConfigInfo(getResources().getString(R.string.encoder_color_format_str), null, REQUEST_CODE_ENCODER_COLOR_FORMAT, enCoderColorName));
//            codecConfigInfos.add(new CodecConfigInfo(getResources().getString(R.string.encoder_level), list_encodeLevel, REQUEST_CODE_ENCODER_LEVEL, encoderLevel_key?encoder_leval_hight:encoder_leval_baseline));
//
//            codecConfigInfos.add(new CodecConfigInfo(getResources().getString(R.string.decoder_type_str), list_DecoderType, REQUEST_CODE_DECODER_TYPE, decoderType ? hw_decoder_str : soft_decoder_str));
//            codecConfigInfos.add(new CodecConfigInfo(getResources().getString(R.string.decoder_name_str), decoderName, REQUEST_CODE_DECODER_NAME, deCoderName));
//            codecConfigInfos.add(new CodecConfigInfo(getResources().getString(R.string.decoder_color_format_str), null, REQUEST_CODE_DECODER_COLOR_FORMAT, deCoderColorName));
//            codecConfigInfos.add(new CodecConfigInfo("CameraDisplayOrientation", null, REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION, String.valueOf(cameraDisplayOrientation)));
//            codecConfigInfos.add(new CodecConfigInfo("FrameOrientation", null, REQUEST_CODE_FRAME_ORIENTATION, frameOrientation == 0 ? "-1" : String.valueOf(frameOrientation)));
//            codecConfigInfos.add(new CodecConfigInfo(getResources().getString(R.string.capture_type_str), list_captureType, REQUEST_CODE_CAPTURE_TYPE, acquisitionMode ? capture_type_texture : capture_type_yuv));
//        }
//    }



}