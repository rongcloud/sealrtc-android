package cn.rongcloud.rtc.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.rtc.LoadDialog;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.RongRTCConfig;
import cn.rongcloud.rtc.device.camrea.DeviceCameraActivity;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.stream.local.RongRTCCapture;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.debug.RTCDevice;

import static cn.rongcloud.rtc.device.utils.Consts.codec_info_eventBus;
import static cn.rongcloud.rtc.device.utils.Consts.device_camera_info_eventBus;

public class DeviceMainActivity extends DeviceBaseActivity {

    private static final String TAG = "DeviceMainActivity";
    private TextView tv_encodeInfo;
    private TextView tv_decoderInfo;
    private TextView tv_cameraInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity_oem_main);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        tv_encodeInfo = (TextView) findViewById(R.id.tv_encodeInfo);
        tv_decoderInfo = (TextView) findViewById(R.id.tv_decoderInfo);
        tv_cameraInfo = (TextView) findViewById(R.id.tv_cameraInfo);

        initData();
    }

    public void deviceClick(View view) {
        if (view.getId() == R.id.btn_codec) {
            Intent intent = new Intent(DeviceMainActivity.this, CodecActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_camera) {
            Intent intent = new Intent(DeviceMainActivity.this, DeviceCameraActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_apply) {
            apply();
        }else if(view.getId()==R.id.btn_clear){
            clear();
        }
    }

    private void clear() {
        LoadDialog.show(DeviceMainActivity.this);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.encoder_colorFormat_alias_key);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.decoder_colorFormat_alias_key);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.colorFormat_val_key);

        SessionManager.getInstance(Utils.getContext()).remove(Consts.encoder_key);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.decoder_key);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.encoderType_key);


        SessionManager.getInstance(Utils.getContext()).remove(Consts.decoderType_key);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.encoderLevel_key);


        SessionManager.getInstance(Utils.getContext()).remove(Consts.acquisitionMode_key);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.capture_cameraDisplayOrientation_key);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.capture_frameOrientation_key);

        SessionManager.getInstance(Utils.getContext()).remove(Consts.decoder_colorFormat_eventBus);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.encoder_colorFormat_eventBus);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.device_camera_info_eventBus);
        SessionManager.getInstance(Utils.getContext()).remove(Consts.codec_info_eventBus);

        Map<String, Object> map = new HashMap<>();
        map.put(RTCDevice.CodecCofig.isTexture, true);
        map.put(RTCDevice.CodecCofig.hwEncode, true);
        map.put(RTCDevice.CodecCofig.hwDecode, true);
        map.put(RTCDevice.CodecCofig.highProfile, true);
        map.put(RTCDevice.CodecCofig.enCodeColor, 0);
        map.put(RTCDevice.CodecCofig.deCodeColor, 0);
        RTCDevice.getInstance().setCodecConfig(map);

        RongRTCConfig.Builder builder = new RongRTCConfig.Builder();
        builder.setCameraDisplayOrientation(0);
        builder.setFrameOrientation(-1);
        RongRTCCapture.getInstance().setRTCConfig(builder.build());
        Toast.makeText(this, "完成", Toast.LENGTH_SHORT).show();
        initData();
        LoadDialog.dismiss(DeviceMainActivity.this);
    }

    private void initData() {
        if (!SessionManager.getInstance(Utils.getContext()).contains(Consts.encoderType_key)) {
            SessionManager.getInstance(Utils.getContext()).put(Consts.encoderType_key, true);
        }
        if (!SessionManager.getInstance(Utils.getContext()).contains(Consts.decoderType_key)) {
            SessionManager.getInstance(Utils.getContext()).put(Consts.decoderType_key, true);
        }
        if (!SessionManager.getInstance(Utils.getContext()).contains(Consts.encoderLevel_key)) {
            SessionManager.getInstance(Utils.getContext()).put(Consts.encoderLevel_key, true);
        }
        if (!SessionManager.getInstance(Utils.getContext()).contains(Consts.acquisitionMode_key)) {
            SessionManager.getInstance(Utils.getContext()).put(Consts.acquisitionMode_key, true);
        }
        if (!SessionManager.getInstance(Utils.getContext()).contains(Consts.capture_cameraDisplayOrientation_key)) {
            SessionManager.getInstance(Utils.getContext()).put(Consts.capture_cameraDisplayOrientation_key, 0);
        }
        if (!SessionManager.getInstance(Utils.getContext()).contains(Consts.capture_frameOrientation_key)) {
            SessionManager.getInstance(Utils.getContext()).put(Consts.capture_frameOrientation_key, -1);
        }
        initEncodeInfo();
        initDecodeInfo();
        initAcquisitionMode();
    }

    private void initEncodeInfo() {
        boolean encoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderType_key);
        boolean encoderLevel = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderLevel_key);
        StringBuffer stringBuffer_encoder = new StringBuffer();
        stringBuffer_encoder.append(encoderType ? "硬编" : "软编");
        if (encoderType) {
            String enCoderName = SessionManager.getInstance(Utils.getContext()).getString(Consts.encoder_key);
            String enCoderColorName = SessionManager.getInstance(Utils.getContext()).getString(Consts.encoder_colorFormat_alias_key);
            int colorFormat = SessionManager.getInstance(Utils.getContext()).getInt(Consts.colorFormat_val_key);
            stringBuffer_encoder.append("\n");
            stringBuffer_encoder.append("编码器名称：");
            stringBuffer_encoder.append(enCoderName);
            stringBuffer_encoder.append("\n");
            stringBuffer_encoder.append("颜色空间：");
            stringBuffer_encoder.append(enCoderColorName);
            stringBuffer_encoder.append("-");
            stringBuffer_encoder.append(colorFormat);
            stringBuffer_encoder.append("\n");
            stringBuffer_encoder.append("编码级别：");
            stringBuffer_encoder.append(encoderLevel ? "highProfile" : "baseLine");
        }
        tv_encodeInfo.setText(stringBuffer_encoder.toString());
    }

    private void initDecodeInfo() {
        boolean decoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.decoderType_key);
        StringBuffer stringBuffer_decoder = new StringBuffer();
        stringBuffer_decoder.append(decoderType ? "硬解" : "软解");
        if (decoderType) {
            String deCoderName = SessionManager.getInstance(Utils.getContext()).getString(Consts.decoder_key);
            String deCoderColorName = SessionManager.getInstance(Utils.getContext()).getString(Consts.decoder_colorFormat_alias_key);
            int colorFormat = SessionManager.getInstance(Utils.getContext()).getInt(Consts.colorFormat_val_key);
            stringBuffer_decoder.append("\n");
            stringBuffer_decoder.append("解码器名称：");
            stringBuffer_decoder.append(deCoderName);
            stringBuffer_decoder.append("\n");
            stringBuffer_decoder.append("颜色空间：");
            stringBuffer_decoder.append(deCoderColorName);
            stringBuffer_decoder.append("-");
            stringBuffer_decoder.append(colorFormat);
        }
        tv_decoderInfo.setText(stringBuffer_decoder.toString());
    }

    private void initAcquisitionMode() {
        StringBuffer stringBuffer_capture = new StringBuffer();
        boolean acquisitionMode = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.acquisitionMode_key);
        int cameraDisplayOrientation = SessionManager.getInstance(Utils.getContext()).getInt(Consts.capture_cameraDisplayOrientation_key);
        int frameOrientation = SessionManager.getInstance(Utils.getContext()).getInt(Consts.capture_frameOrientation_key);
        stringBuffer_capture.append("摄像头采集方式：");
        stringBuffer_capture.append(acquisitionMode ? "texture" : "yuv");
        stringBuffer_capture.append("\n");
        stringBuffer_capture.append("cameraDisplayOrientation：");
        stringBuffer_capture.append(cameraDisplayOrientation);
        stringBuffer_capture.append("\n");
        stringBuffer_capture.append("frameOrientation：");
        stringBuffer_capture.append(frameOrientation);
        tv_cameraInfo.setText(stringBuffer_capture.toString());
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBusComplaint(String val) {
        if (val.equals(device_camera_info_eventBus)) {
            initAcquisitionMode();
        } else if (val.equals(codec_info_eventBus)) {
            initDecodeInfo();
            initEncodeInfo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadDialog.dismiss(DeviceMainActivity.this);
        EventBus.getDefault().removeStickyEvent(device_camera_info_eventBus);
        EventBus.getDefault().removeStickyEvent(codec_info_eventBus);
        EventBus.getDefault().unregister(this);
    }

    private void apply() {
        boolean acquisitionMode = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.acquisitionMode_key);
        boolean encoderLevel_key = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderLevel_key);
        boolean encoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderType_key);
        boolean decoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.decoderType_key);
        RongRTCConfig.Builder builder = new RongRTCConfig.Builder();
        Map<String, Object> map = new HashMap<>();
        map.put(RTCDevice.CodecCofig.isTexture, acquisitionMode);
        map.put(RTCDevice.CodecCofig.hwEncode, encoderType);
        map.put(RTCDevice.CodecCofig.hwDecode, decoderType);
        map.put(RTCDevice.CodecCofig.highProfile, encoderLevel_key);
        if (encoderType) {
            String enCoderName = SessionManager.getInstance(Utils.getContext()).getString(Consts.encoder_key);
            String enCoderColorName = SessionManager.getInstance(Utils.getContext()).getString(Consts.encoder_colorFormat_alias_key);
            int colorFormat = SessionManager.getInstance(Utils.getContext()).getInt(Consts.colorFormat_val_key);
            map.put(RTCDevice.CodecCofig.enCodeColor, colorFormat);
            if (enCoderName.indexOf("h264") != -1) {
                builder.videoCodecs(RongRTCConfig.RongRTCVideoCodecs.H264);
            } else if (enCoderName.indexOf("vp9") != -1) {

            } else if (enCoderName.indexOf("vp8") != -1) {
                builder.videoCodecs(RongRTCConfig.RongRTCVideoCodecs.VP8);
            }
        }
        if (decoderType) {
            String deCoderName = SessionManager.getInstance(Utils.getContext()).getString(Consts.decoder_key);
            String deCoderColorName = SessionManager.getInstance(Utils.getContext()).getString(Consts.decoder_colorFormat_alias_key);
            int colorFormat = SessionManager.getInstance(Utils.getContext()).getInt(Consts.colorFormat_val_key);
            map.put(RTCDevice.CodecCofig.deCodeColor, colorFormat);
        }
        int cameraDisplayOrientation = SessionManager.getInstance(Utils.getContext()).getInt(Consts.capture_cameraDisplayOrientation_key);
        int frameOrientation = SessionManager.getInstance(Utils.getContext()).getInt(Consts.capture_frameOrientation_key);

        builder.setCameraDisplayOrientation(cameraDisplayOrientation);
        builder.setFrameOrientation(frameOrientation);



        RTCDevice.getInstance().setCodecConfig(map);
        RongRTCCapture.getInstance().setRTCConfig(builder.build());
        finish();
    }
}
