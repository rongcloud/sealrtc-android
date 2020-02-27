package cn.rongcloud.rtc.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.rongcloud.rtc.LoadDialog;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.RongRTCConfig;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.stream.local.RongRTCCapture;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.utils.FinLog;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    public void deviceClick(View view) {
        if (ButtentSolp.check(view.getId(), 1500)) {
            FinLog.v(TAG, getString(R.string.btnsolpstr));
            return;
        }
        if (view.getId() == R.id.btn_apply) {
            apply();
        }else if(view.getId()==R.id.btn_clear){
            clear();
        }else if(view.getId()==R.id.btn_codecSettings){
            Intent intent = new Intent(DeviceMainActivity.this, DeviceSettingsActivity.class);
            startActivity(intent);
        }
    }

    private void clear() {
        LoadDialog.show(DeviceMainActivity.this);
        SessionManager.getInstance().remove(Consts.SP_ENCODER_COLOR_FORMAT_ALIAS_KEY);
        SessionManager.getInstance().remove(Consts.SP_DECODER_COLOR_FORMAT_ALIAS_KEY);
        SessionManager.getInstance().remove(Consts.SP_DECODER_COLOR_FORMAT_VAL_KEY);
        SessionManager.getInstance().remove(Consts.SP_ENCODER_COLOR_FORMAT_VAL_KEY);

        SessionManager.getInstance().remove(Consts.SP_ENCODER_NAME_KEY);
        SessionManager.getInstance().remove(Consts.SP_DECODER_NAME_KEY);
        SessionManager.getInstance().remove(Consts.SP_ENCODER_TYPE_KEY);


        SessionManager.getInstance().remove(Consts.SP_DECODER_TYPE_KEY);
        SessionManager.getInstance().remove(Consts.SP_ENCODER_LEVEL_KEY);


        SessionManager.getInstance().remove(Consts.ACQUISITION_MODE_KEY);
        SessionManager.getInstance().remove(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY);
        SessionManager.getInstance().remove(Consts.CAPTURE_FRAME_ORIENTATION_KEY);

        SessionManager.getInstance().remove(Consts.decoder_colorFormat_eventBus);
        SessionManager.getInstance().remove(Consts.encoder_colorFormat_eventBus);
        SessionManager.getInstance().remove(Consts.device_camera_info_eventBus);
        SessionManager.getInstance().remove(Consts.codec_info_eventBus);

        RongRTCConfig.Builder builder = new RongRTCConfig.Builder()
                .setCameraDisplayOrientation(0)
                .setFrameOrientation(-1)
                .enableHardWareEncode(true)
                .enableHardWareDecode(true)
                .enableHardWareEncodeHighProfile(true)
                .setHardWareEncodeColor(0)
                .enableVideoTexture(true)
                .setHardWareDecodeColor(0);
        RongRTCCapture.getInstance().setRTCConfig(builder.build());
        Toast.makeText(this, "完成", Toast.LENGTH_SHORT).show();
        initData();
        LoadDialog.dismiss(DeviceMainActivity.this);
    }

    private void initData() {
        if (!SessionManager.getInstance().contains(Consts.SP_ENCODER_TYPE_KEY)) {
            SessionManager.getInstance().put(Consts.SP_ENCODER_TYPE_KEY, true);
        }
        if (!SessionManager.getInstance().contains(Consts.SP_DECODER_TYPE_KEY)) {
            SessionManager.getInstance().put(Consts.SP_DECODER_TYPE_KEY, true);
        }
        if (!SessionManager.getInstance().contains(Consts.SP_ENCODER_LEVEL_KEY)) {
            SessionManager.getInstance().put(Consts.SP_ENCODER_LEVEL_KEY, true);
        }
        if (!SessionManager.getInstance().contains(Consts.ACQUISITION_MODE_KEY)) {
            SessionManager.getInstance().put(Consts.ACQUISITION_MODE_KEY, true);
        }
        if (!SessionManager.getInstance().contains(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY)) {
            SessionManager.getInstance().put(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY, 0);
        }
        if (!SessionManager.getInstance().contains(Consts.CAPTURE_FRAME_ORIENTATION_KEY)) {
            SessionManager.getInstance().put(Consts.CAPTURE_FRAME_ORIENTATION_KEY, -1);
        }
        initEncodeInfo();
        initDecodeInfo();
        initAcquisitionMode();
    }

    private void initEncodeInfo() {
        boolean encoderType = SessionManager.getInstance().getBoolean(Consts.SP_ENCODER_TYPE_KEY);
        boolean encoderLevel = SessionManager.getInstance().getBoolean(Consts.SP_ENCODER_LEVEL_KEY);
        StringBuffer stringBuffer_encoder = new StringBuffer();
        stringBuffer_encoder.append(encoderType ? "硬编" : "软编");
        if (encoderType) {
            String enCoderName = SessionManager.getInstance().getString(Consts.SP_ENCODER_NAME_KEY);
            String enCoderColorName = SessionManager.getInstance().getString(Consts.SP_ENCODER_COLOR_FORMAT_ALIAS_KEY);
            int colorFormat = SessionManager.getInstance().getInt(Consts.SP_ENCODER_COLOR_FORMAT_VAL_KEY);
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
        boolean decoderType = SessionManager.getInstance().getBoolean(Consts.SP_DECODER_TYPE_KEY);
        StringBuffer stringBuffer_decoder = new StringBuffer();
        stringBuffer_decoder.append(decoderType ? "硬解" : "软解");
        if (decoderType) {
            String deCoderName = SessionManager.getInstance().getString(Consts.SP_DECODER_NAME_KEY);
            String deCoderColorName = SessionManager.getInstance().getString(Consts.SP_DECODER_COLOR_FORMAT_ALIAS_KEY);
            int colorFormat = SessionManager.getInstance().getInt(Consts.SP_DECODER_COLOR_FORMAT_VAL_KEY);
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
        boolean acquisitionMode = SessionManager.getInstance().getBoolean(Consts.ACQUISITION_MODE_KEY);
        int cameraDisplayOrientation = SessionManager.getInstance().getInt(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY);
        int frameOrientation = SessionManager.getInstance().getInt(Consts.CAPTURE_FRAME_ORIENTATION_KEY);
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
        LoadDialog.show(DeviceMainActivity.this);
        boolean acquisitionMode = SessionManager.getInstance().getBoolean(Consts.ACQUISITION_MODE_KEY);
        boolean encoderLevel_key = SessionManager.getInstance().getBoolean(Consts.SP_ENCODER_LEVEL_KEY);
        boolean encoderType = SessionManager.getInstance().getBoolean(Consts.SP_ENCODER_TYPE_KEY);
        boolean decoderType = SessionManager.getInstance().getBoolean(Consts.SP_DECODER_TYPE_KEY);
        RongRTCConfig.Builder builder = new RongRTCConfig.Builder();
        builder.enableVideoTexture(acquisitionMode);
        if (encoderType) {
            String enCoderName = SessionManager.getInstance().getString(Consts.SP_ENCODER_NAME_KEY);
            String enCoderColorName = SessionManager.getInstance().getString(Consts.SP_ENCODER_COLOR_FORMAT_ALIAS_KEY);
            int colorFormat = SessionManager.getInstance().getInt(Consts.SP_ENCODER_COLOR_FORMAT_VAL_KEY);
            builder.setHardWareEncodeColor(colorFormat);
            if (enCoderName.indexOf("h264") != -1) {
                builder.videoCodecs(RongRTCConfig.RongRTCVideoCodecs.H264);
            } else if (enCoderName.indexOf("vp9") != -1) {

            } else if (enCoderName.indexOf("vp8") != -1) {
                builder.videoCodecs(RongRTCConfig.RongRTCVideoCodecs.VP8);
            }
        }
        if (decoderType) {
            String deCoderName = SessionManager.getInstance().getString(Consts.SP_DECODER_NAME_KEY);
            String deCoderColorName = SessionManager.getInstance().getString(Consts.SP_DECODER_COLOR_FORMAT_ALIAS_KEY);
            int colorFormat = SessionManager.getInstance().getInt(Consts.SP_DECODER_COLOR_FORMAT_VAL_KEY);
            builder.setHardWareDecodeColor(colorFormat);
        }
        int cameraDisplayOrientation = SessionManager.getInstance().getInt(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY);
        int frameOrientation = SessionManager.getInstance().getInt(Consts.CAPTURE_FRAME_ORIENTATION_KEY);

        builder.setCameraDisplayOrientation(cameraDisplayOrientation);
        builder.setFrameOrientation(frameOrientation);

        builder.enableHardWareEncode(encoderType);
        builder.enableHardWareDecode(decoderType);
        builder.enableHardWareEncodeHighProfile(encoderLevel_key);

        RongRTCCapture.getInstance().setRTCConfig(builder.build());

        LoadDialog.dismiss(DeviceMainActivity.this);
        Toast.makeText(this, "应用成功", Toast.LENGTH_SHORT).show();
        finish();
    }
    public void backClick(View view){
        finish();
    }
}
