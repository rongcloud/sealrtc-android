package cn.rongcloud.rtc.device.camrea;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.DeviceBaseActivity;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;

import static cn.rongcloud.rtc.device.utils.Consts.device_camera_info_eventBus;

public class DeviceCameraActivity extends DeviceBaseActivity {

    private static final String TAG = "DeviceCameraActivity";
    private RadioGroup radioGroup_acquisitionMode;
    private RadioButton radio_texture, radio_yuv;
    private EditText edit_frameOrientation, edit_cameraDisplayOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_camera);
        edit_frameOrientation = (EditText) findViewById(R.id.edit_frameOrientation);
        edit_cameraDisplayOrientation = (EditText) findViewById(R.id.edit_cameraDisplayOrientation);
        int cameraDisplayOrientation = SessionManager.getInstance(Utils.getContext()).getInt(Consts.capture_cameraDisplayOrientation_key);
        int frameOrientation = SessionManager.getInstance(Utils.getContext()).getInt(Consts.capture_frameOrientation_key);
        if (cameraDisplayOrientation != 0) {
            edit_cameraDisplayOrientation.setText(cameraDisplayOrientation + "");
        }
        if (frameOrientation != -1) {
            edit_frameOrientation.setText(frameOrientation + "");
        }

        radioGroup_acquisitionMode = (RadioGroup) findViewById(R.id.radioGroup_acquisitionMode);
        radio_texture = (RadioButton) findViewById(R.id.radio_texture);
        radio_yuv = (RadioButton) findViewById(R.id.radio_yuv);
        boolean acquisitionMode = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.acquisitionMode_key);
        if (acquisitionMode) {
            radio_texture.setChecked(true);
            radio_yuv.setChecked(false);
        } else {
            radio_texture.setChecked(false);
            radio_yuv.setChecked(true);
        }
        radioGroup_acquisitionMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_texture) {
                    SessionManager.getInstance(Utils.getContext()).put(Consts.acquisitionMode_key, true);
                } else if (checkedId == R.id.radio_yuv) {
                    SessionManager.getInstance(Utils.getContext()).put(Consts.acquisitionMode_key, false);
                }
            }
        });
    }

    public void applyClick(View view) {
        int frameOrientation = -1;
        int cameraDisplayOrientation = 0;
        try {
            frameOrientation = Integer.valueOf(edit_frameOrientation.getText().toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Consts.capture_frameOrientation_key , e: " + e.getMessage());
        }
        try {
            cameraDisplayOrientation = Integer.valueOf(edit_cameraDisplayOrientation.getText().toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Consts.capture_cameraDisplayOrientation_key , e: " + e.getMessage());
        }
        SessionManager.getInstance(Utils.getContext()).put(Consts.capture_cameraDisplayOrientation_key, cameraDisplayOrientation);
        SessionManager.getInstance(Utils.getContext()).put(Consts.capture_frameOrientation_key, frameOrientation);
        Toast.makeText(this, "完成！", Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(device_camera_info_eventBus);
        finish();
    }
}
