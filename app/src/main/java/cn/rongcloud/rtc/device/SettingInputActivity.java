package cn.rongcloud.rtc.device;

import static cn.rongcloud.rtc.device.utils.Consts.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.adapter.AVSettingsDataSource;
import cn.rongcloud.rtc.device.entity.EventBusInfo;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.util.SessionManager;
import org.greenrobot.eventbus.EventBus;

public class SettingInputActivity extends DeviceBaseActivity {
    private static final String TAG = "SettingInputActivity";
    private LinearLayout linear_cameraDisplayOrientation,
            linear_frameOrientation,
            linear_audioSource,
            linear_audioSampleRate,
            linear_audioBitRate,
            linear_targetDBOV,
            linear_compressionLevel,
            linear_ampliferLevel;
    private EditText edit_cameraDisplayOrientation,
            edit_frameOrientation,
            edit_audioSource,
            edit_audioSampleRate,
            edit_audioBitRate,
            edit_targetDBOV,
            edit_compressionLevel,
            edit_ampliferLevel;
    private int requestCode = -1;

    public static void startActivity(Context context, int requestCode) {
        Intent intent = new Intent(context, SettingInputActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_REQUEST_CODE_KEY, requestCode);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_input);
        linear_cameraDisplayOrientation = findViewById(R.id.linear_cameraDisplayOrientation);
        linear_frameOrientation = findViewById(R.id.linear_frameOrientation);
        linear_audioSource = findViewById(R.id.linear_audio_source);
        linear_audioSampleRate = findViewById(R.id.linear_audio_sample_rate);
        linear_audioBitRate = findViewById(R.id.linear_audio_bit_rate);
        linear_targetDBOV = findViewById(R.id.linear_audio_targetDBOV);
        linear_compressionLevel = findViewById(R.id.linear_audio_compress_level);
        linear_ampliferLevel = findViewById(R.id.linear_audio_amplifier_level);

        edit_cameraDisplayOrientation = findViewById(R.id.edit_cameraDisplayOrientation);
        edit_frameOrientation = findViewById(R.id.edit_frameOrientation);
        edit_audioSource = findViewById(R.id.edit_audio_source);
        edit_audioSampleRate = findViewById(R.id.edit_audio_sample_rate);
        edit_audioBitRate = findViewById(R.id.edit_audio_bit_rate);
        edit_targetDBOV = findViewById(R.id.edit_audio_targetDBOV);
        edit_compressionLevel = findViewById(R.id.edit_audio_compress_level);
        edit_ampliferLevel = findViewById(R.id.edit_audio_amplifier_level);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        requestCode = bundle.getInt(EXTRA_KEY_REQUEST_CODE_KEY);
        if (requestCode == REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION) {
            linear_cameraDisplayOrientation.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_CODE_FRAME_ORIENTATION) {
            linear_frameOrientation.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_CODE_AUDIO_SOURCE) {
            linear_audioSource.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_CODE_AUDIO_SAMPLE_RATE) {
            linear_audioSampleRate.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_AUDIO_TRANSPORT_BIT_RATE) {
            linear_audioBitRate.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_AUDIO_AGC_TARGET_DBOV) {
            linear_targetDBOV.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_AUDIO_AGC_COMPRESSION_LEVEL) {
            linear_compressionLevel.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_AUDIO_PRE_AMPLIFIER_LEVEL) {
            linear_ampliferLevel.setVisibility(View.VISIBLE);
        }

        String cameraDisplayOrientation =
                AVSettingsDataSource.getInstance()
                        .getItemConfig(
                                AVSettingsDataSource.SettingCategory.VideoCamera,
                                REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION);
        edit_cameraDisplayOrientation.setText(cameraDisplayOrientation);
        edit_cameraDisplayOrientation.setSelection(getStringLength(cameraDisplayOrientation));

        String frameOrientation =
                AVSettingsDataSource.getInstance()
                        .getItemConfig(
                                AVSettingsDataSource.SettingCategory.VideoCamera,
                                REQUEST_CODE_FRAME_ORIENTATION);
        edit_frameOrientation.setText(frameOrientation);
        edit_frameOrientation.setSelection(getStringLength(frameOrientation));

        String audioSource =
                AVSettingsDataSource.getInstance()
                        .getItemConfig(
                                AVSettingsDataSource.SettingCategory.AudioCapture,
                                REQUEST_CODE_AUDIO_SOURCE);
        edit_audioSource.setText(audioSource);
        edit_audioSource.setSelection(getStringLength(audioSource));

        String audioSampleRate =
                AVSettingsDataSource.getInstance()
                        .getItemConfig(
                                AVSettingsDataSource.SettingCategory.AudioCapture,
                                REQUEST_CODE_AUDIO_SAMPLE_RATE);
        edit_audioSampleRate.setText(audioSampleRate);
        edit_audioSampleRate.setSelection(getStringLength(audioSampleRate));

        String audioBitRate =
                AVSettingsDataSource.getInstance()
                        .getItemConfig(
                                AVSettingsDataSource.SettingCategory.AudioCapture,
                                REQUEST_AUDIO_TRANSPORT_BIT_RATE);
        edit_audioBitRate.setText(audioBitRate);
        edit_audioBitRate.setSelection(getStringLength(audioBitRate));

        String audioAgcTargetDbov =
                AVSettingsDataSource.getInstance()
                        .getItemConfig(
                                AVSettingsDataSource.SettingCategory.AudioAGC,
                                REQUEST_AUDIO_AGC_TARGET_DBOV);
        edit_targetDBOV.setText(audioAgcTargetDbov);
        edit_targetDBOV.setSelection(getStringLength(audioAgcTargetDbov));

        String audioAgcCompression =
                AVSettingsDataSource.getInstance()
                        .getItemConfig(
                                AVSettingsDataSource.SettingCategory.AudioAGC,
                                REQUEST_AUDIO_AGC_COMPRESSION_LEVEL);
        edit_compressionLevel.setText(audioAgcCompression);
        edit_compressionLevel.setSelection(getStringLength(audioAgcCompression));

        String audioPreAmplifierLevel =
                AVSettingsDataSource.getInstance()
                        .getItemConfig(
                                AVSettingsDataSource.SettingCategory.AudioAGC,
                                REQUEST_AUDIO_PRE_AMPLIFIER_LEVEL);
        edit_ampliferLevel.setText(audioPreAmplifierLevel);
        edit_ampliferLevel.setSelection(getStringLength(audioPreAmplifierLevel));
    }

    public void settingInputClick(View view) {
        if (view.getId() == R.id.linear_save) {
            save();
        } else if (view.getId() == R.id.settings_back) {
            finish();
        }
    }

    private void save() {
        String content = "";
        if (requestCode == REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION) {
            int cameraDisplayOrientation = 0;
            try {
                content = edit_cameraDisplayOrientation.getText().toString().trim();
                cameraDisplayOrientation = Integer.valueOf(content);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Consts.capture_cameraDisplayOrientation_key , e: " + e.getMessage());
            }
            SessionManager.getInstance()
                    .put(Consts.CAPTURE_CAMERA_DISPLAY_ORIENTATION_KEY, cameraDisplayOrientation);
        } else if (requestCode == REQUEST_CODE_FRAME_ORIENTATION) {
            int frameOrientation = -1;
            try {
                content = edit_frameOrientation.getText().toString().trim();
                frameOrientation = Integer.valueOf(content);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Consts.capture_frameOrientation_key , e: " + e.getMessage());
            }
            SessionManager.getInstance()
                    .put(Consts.CAPTURE_FRAME_ORIENTATION_KEY, frameOrientation);
        } else if (requestCode == REQUEST_CODE_AUDIO_SOURCE) {
            try {
                int audioSource = Integer.valueOf(edit_audioSource.getText().toString().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "Consts.edit_audioSource , e: " + ex.getMessage());
                return;
            }
            content = edit_audioSource.getText().toString().trim();
        } else if (requestCode == REQUEST_CODE_AUDIO_SAMPLE_RATE) {
            try {
                int audioSampleRate =
                        Integer.valueOf(edit_audioSampleRate.getText().toString().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "Consts.edit_audioSampleRate , e: " + ex.getMessage());
                return;
            }
            content = edit_audioSampleRate.getText().toString().trim();
        } else if (requestCode == REQUEST_AUDIO_TRANSPORT_BIT_RATE) {
            try {
                int audioBitRate = Integer.valueOf(edit_audioBitRate.getText().toString().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "Consts.edit_audioBitRate , e: " + ex.getMessage());
                return;
            }
            content = edit_audioBitRate.getText().toString().trim();
        } else if (requestCode == REQUEST_AUDIO_AGC_TARGET_DBOV) {
            try {
                int targetDBOV = Integer.valueOf(edit_targetDBOV.getText().toString().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "Consts.edit_targetDBOV , e: " + ex.getMessage());
                return;
            }
            content = edit_targetDBOV.getText().toString().trim();
        } else if (requestCode == REQUEST_AUDIO_AGC_COMPRESSION_LEVEL) {
            try {
                int compressionLevel =
                        Integer.valueOf(edit_compressionLevel.getText().toString().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "Consts.compressionLevel , e: " + ex.getMessage());
                return;
            }
            content = edit_compressionLevel.getText().toString().trim();
        } else if (requestCode == REQUEST_AUDIO_PRE_AMPLIFIER_LEVEL) {
            try {
                float ampliferLevel = Float.valueOf(edit_ampliferLevel.getText().toString().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "Consts.ampliferLevel , e: " + ex.getMessage());
                return;
            }
            content = edit_ampliferLevel.getText().toString().trim();
        }
        EventBusInfo info = new EventBusInfo(requestCode, getDefaultContent(content), 0);
        EventBus.getDefault().post(info);
        finish();
    }

    private String getDefaultContent(String content) {
        String val = content;
        switch (requestCode) {
            case REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION:
                if (TextUtils.isEmpty(content)) {
                    val = "0";
                }
                break;
            case REQUEST_CODE_FRAME_ORIENTATION:
                if (TextUtils.isEmpty(content)) {
                    val = "-1";
                }
                break;
            default:
                break;
        }
        return val;
    }

    private int getStringLength(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        return str.length();
    }
}
