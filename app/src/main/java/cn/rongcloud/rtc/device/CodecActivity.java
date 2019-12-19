package cn.rongcloud.rtc.device;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.LoadDialog;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.entity.CodecInfo;
import cn.rongcloud.rtc.device.utils.Consts;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.debug.RTCDevice;

import static cn.rongcloud.rtc.device.utils.Consts.codec_info_eventBus;
import static cn.rongcloud.rtc.device.utils.Consts.decoder_colorFormat_eventBus;
import static cn.rongcloud.rtc.device.utils.Consts.encoder_colorFormat_eventBus;

public class CodecActivity extends DeviceBaseActivity {

    private TextView tv_decoder_info, tv_encoder_info;
    private ArrayList<CodecInfo> encoderInfo = new ArrayList<>();
    private ArrayList<CodecInfo> decoderInfo = new ArrayList<>();
    private ArrayList<String> encoderName = new ArrayList<>();
    private ArrayList<String> decoderName = new ArrayList<>();
    private LinearLayout linear_encoderConfig, linear_decoderConfig;
    private RadioGroup radioGroup_encoder, radioGroup_decoder, radioGroup_encoderlevel;
    private RelativeLayout rela_apply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity_codec);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initView();

        initData();
    }

    private void initView() {
        rela_apply = (RelativeLayout) findViewById(R.id.rela_apply);
        tv_encoder_info = (TextView) findViewById(R.id.tv_encoder_info);
        tv_decoder_info = (TextView) findViewById(R.id.tv_decoder_info);
        linear_encoderConfig = (LinearLayout) findViewById(R.id.linear_encoderConfig);
        linear_decoderConfig = (LinearLayout) findViewById(R.id.linear_decoderConfig);
        radioGroup_encoder = (RadioGroup) findViewById(R.id.radioGroup_encoder);
        radioGroup_decoder = (RadioGroup) findViewById(R.id.radioGroup_decoder);
        radioGroup_encoderlevel = (RadioGroup) findViewById(R.id.radioGroup_encoderlevel);
        RadioButton radio_hwEncoder = (RadioButton) findViewById(R.id.radio_hwEncoder);
        RadioButton radio_softEncoder = (RadioButton) findViewById(R.id.radio_softEncoder);
        RadioButton radio_hwDecoder = (RadioButton) findViewById(R.id.radio_hwDecoder);
        RadioButton radio_softDecoder = (RadioButton) findViewById(R.id.radio_softDecoder);
        RadioButton radio_highProfile = (RadioButton) findViewById(R.id.radio_highProfile);
        RadioButton radio_baseLine = (RadioButton) findViewById(R.id.radio_baseLine);
        boolean encoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderType_key);
        boolean decoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.decoderType_key);
        boolean encoderLevel_key = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderLevel_key);
        if (encoderType) {
            radio_hwEncoder.setChecked(true);
            radio_softEncoder.setChecked(false);
            linear_encoderConfig.setVisibility(View.VISIBLE);
            initEncoder();
        } else {
            radio_hwEncoder.setChecked(false);
            radio_softEncoder.setChecked(true);
            linear_encoderConfig.setVisibility(View.GONE);
        }
        if (decoderType) {
            radio_hwDecoder.setChecked(true);
            radio_softDecoder.setChecked(false);
            initDecoder();
            linear_decoderConfig.setVisibility(View.VISIBLE);
        } else {
            radio_hwDecoder.setChecked(false);
            radio_softDecoder.setChecked(true);
            linear_decoderConfig.setVisibility(View.GONE);
        }
        if (encoderLevel_key) {
            radio_highProfile.setChecked(true);
            radio_baseLine.setChecked(false);
        } else {
            radio_highProfile.setChecked(false);
            radio_baseLine.setChecked(true);
        }

        rela_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply();
            }
        });
        radioGroup_encoder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_hwEncoder) {
                    SessionManager.getInstance(Utils.getContext()).put(Consts.encoderType_key, true);
                    linear_encoderConfig.setVisibility(View.VISIBLE);
                    initEncoder();
                } else if (checkedId == R.id.radio_softEncoder) {
                    SessionManager.getInstance(Utils.getContext()).put(Consts.encoderType_key, false);
                    linear_encoderConfig.setVisibility(View.GONE);
                }
            }
        });
        radioGroup_decoder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_hwDecoder) {
                    SessionManager.getInstance(Utils.getContext()).put(Consts.decoderType_key, true);
                    linear_decoderConfig.setVisibility(View.VISIBLE);
                    initDecoder();
                } else if (checkedId == R.id.radio_softDecoder) {
                    SessionManager.getInstance(Utils.getContext()).put(Consts.decoderType_key, false);
                    linear_decoderConfig.setVisibility(View.GONE);
                }
            }
        });
        radioGroup_encoderlevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_highProfile) {
                    SessionManager.getInstance(Utils.getContext()).put(Consts.encoderLevel_key, true);
                } else if (checkedId == R.id.radio_baseLine) {
                    SessionManager.getInstance(Utils.getContext()).put(Consts.encoderLevel_key, false);
                }
            }
        });
    }

    private void apply() {
        Toast.makeText(this, "完成", Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(codec_info_eventBus);
        finish();
    }

    private void initData() {
        LoadDialog.show(CodecActivity.this);
        encoderInfo.clear();
        decoderInfo.clear();
        encoderName.clear();
        decoderName.clear();
        String codecInfoJson = RTCDevice.getInstance().getMediaCodecInfo();
        Gson gson = new Gson();
        List<CodecInfo> codecInfoList = gson.fromJson(codecInfoJson, new TypeToken<List<CodecInfo>>() {
        }.getType());
        for (int i = 0; i < codecInfoList.size(); i++) {
            if (codecInfoList.get(i).getCodecName().indexOf("decoder") != -1) {
                decoderInfo.add(codecInfoList.get(i));
                decoderName.add(codecInfoList.get(i).getCodecName());
            } else {
                encoderInfo.add(codecInfoList.get(i));
                encoderName.add(codecInfoList.get(i).getCodecName());
            }
        }
        LoadDialog.dismiss(CodecActivity.this);
    }

    public void click(View view) {
        Intent intent = new Intent(CodecActivity.this, CodecListActivity.class);
        Bundle bundle = new Bundle();
        if (view.getId() == R.id.btn_encoder) {
            bundle.putParcelableArrayList("CodecInfo", encoderInfo);
            bundle.putStringArrayList("CodecName", encoderName);
            bundle.putString("CodecType", "0");
        } else if (view.getId() == R.id.btn_decoder) {
            bundle.putParcelableArrayList("CodecInfo", decoderInfo);
            bundle.putStringArrayList("CodecName", decoderName);
            bundle.putString("CodecType", "1");
        }
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void initEncoder() {
        boolean encoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.encoderType_key);
        StringBuffer stringBuffer_encoder = new StringBuffer();
        stringBuffer_encoder.append(encoderType ? "硬编" : "软编");
        if (encoderType) {
            int colorFormat=SessionManager.getInstance(Utils.getContext()).getInt(Consts.colorFormat_val_key);
            String enCoderName = SessionManager.getInstance(Utils.getContext()).getString(Consts.encoder_key);
            String enCoderColorName = SessionManager.getInstance(Utils.getContext()).getString(Consts.encoder_colorFormat_alias_key);
            stringBuffer_encoder.append("\n");
            stringBuffer_encoder.append("编码器名称：");
            stringBuffer_encoder.append(enCoderName);
            stringBuffer_encoder.append("\n");
            stringBuffer_encoder.append("颜色空间：");
            stringBuffer_encoder.append(enCoderColorName);
            stringBuffer_encoder.append("-");
            stringBuffer_encoder.append(colorFormat);
        }
        tv_encoder_info.setText(stringBuffer_encoder.toString());
    }

    private void initDecoder() {
        boolean decoderType = SessionManager.getInstance(Utils.getContext()).getBoolean(Consts.decoderType_key);
        StringBuffer stringBuffer_decoder = new StringBuffer();
        stringBuffer_decoder.append(decoderType ? "硬解" : "软解");
        if (decoderType) {
            int colorFormat=SessionManager.getInstance(Utils.getContext()).getInt(Consts.colorFormat_val_key);
            String deCoderName = SessionManager.getInstance(Utils.getContext()).getString(Consts.decoder_key);
            String deCoderColorName = SessionManager.getInstance(Utils.getContext()).getString(Consts.decoder_colorFormat_alias_key);
            stringBuffer_decoder.append("\n");
            stringBuffer_decoder.append("解码器名称：");
            stringBuffer_decoder.append(deCoderName);
            stringBuffer_decoder.append("\n");
            stringBuffer_decoder.append("颜色空间：");
            stringBuffer_decoder.append(deCoderColorName);
            stringBuffer_decoder.append("-");
            stringBuffer_decoder.append(colorFormat);
        }
        tv_decoder_info.setText(stringBuffer_decoder.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onBusComplaint(String val) {
        if (val.equals(decoder_colorFormat_eventBus)) {//decoder
            initDecoder();
        } else if (val.equals(encoder_colorFormat_eventBus)) {
            initEncoder();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadDialog.dismiss(CodecActivity.this);
        EventBus.getDefault().removeStickyEvent(decoder_colorFormat_eventBus);
        EventBus.getDefault().removeStickyEvent(encoder_colorFormat_eventBus);
        EventBus.getDefault().unregister(this);
    }
}
