package cn.rongcloud.rtc.device.adapter;

import static cn.rongcloud.rtc.device.utils.Consts.*;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.entity.AVConfigInfo;
import cn.rongcloud.rtc.device.entity.EventBusInfo;
import cn.rongcloud.rtc.device.utils.OnItemClickListener;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.Utils;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

public class AVSettingsParameterAdapter
        extends RecyclerView.Adapter<AVSettingsParameterAdapter.CodecViewHolder> {
    private static final String TAG = "AVSettingsAdapter";
    private OnItemClickListener listener;
    private List<AVConfigInfo> avConfigInfoList;

    public AVSettingsParameterAdapter(List<AVConfigInfo> infos) {
        avConfigInfoList = infos;
    }

    @Override
    public CodecViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_avsettings_adapter, parent, false);
        return new CodecViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CodecViewHolder holder, final int position) {
        if (avConfigInfoList == null
                || avConfigInfoList.size() == 0
                || position < 0
                || position >= avConfigInfoList.size()) {
            return;
        }
        holder.mRadioGroup.setOnCheckedChangeListener(null);
        holder.itemView.setOnClickListener(null);
        final AVConfigInfo info = avConfigInfoList.get(position);
        holder.setTitle(info.getTitle());
        final String defaultStr = info.getItemValue();
        switch (info.getRequestCode()) {
            case REQUEST_CODE_ENCODER_TYPE:
                String hw_encoder =
                        Utils.getContext().getResources().getString(R.string.hw_encoder_str);
                String soft_encoder_str =
                        Utils.getContext().getResources().getString(R.string.soft_encoder_str);
                holder.showRadio(info.getItemValue(), hw_encoder, soft_encoder_str);
                break;
            case REQUEST_CODE_ENCODER_NAME:
            case REQUEST_CODE_ENCODER_COLOR_FORMAT:
            case REQUEST_CODE_DECODER_NAME:
            case REQUEST_CODE_DECODER_COLOR_FORMAT:
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_CODE_DECODER_TYPE:
                String hw_decoder_str =
                        Utils.getContext().getResources().getString(R.string.hw_decoder_str);
                String soft_decoder_str =
                        Utils.getContext().getResources().getString(R.string.soft_decoder_str);
                holder.showRadio(info.getItemValue(), hw_decoder_str, soft_decoder_str);
                break;
            case REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION:
                holder.showContent(defaultStr);
                String cameraOrientationRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.camer_display_orientation_remark);
                holder.showRemark(cameraOrientationRemark);
                break;
            case REQUEST_CODE_FRAME_ORIENTATION:
                holder.showContent(defaultStr);
                String frameOrientationRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.frame_orientation_remark);
                holder.showRemark(frameOrientationRemark);
                break;
            case REQUEST_CODE_CAPTURE_TYPE:
                String capture_type_texture =
                        Utils.getContext().getResources().getString(R.string.capture_type_texture);
                String capture_type_yuv =
                        Utils.getContext().getResources().getString(R.string.capture_type_yuv);
                holder.showRadio(info.getItemValue(), capture_type_texture, capture_type_yuv);
                break;
            case REQUEST_CODE_ENCODER_LEVEL:
                String encoder_level_baseline =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.encoder_leval_baseline);
                String encoder_level_height =
                        Utils.getContext().getResources().getString(R.string.encoder_leval_hight);
                holder.showRadio(info.getItemValue(), encoder_level_height, encoder_level_baseline);
                break;
            case REQUEST_CODE_ENCODER_VIDEO_BITRATE_MODE:
                String encoder_bit_rate_mode_cq =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.encoder_bit_rate_mode_cq);
                String encoder_bit_rate_mode_vbr =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.encoder_bit_rate_mode_vbr);
                String encoder_bit_rate_mode_cbr =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.encoder_bit_rate_mode_cbr);
                holder.showRadio(
                        info.getItemValue(),
                        encoder_bit_rate_mode_vbr,
                        encoder_bit_rate_mode_cq,
                        encoder_bit_rate_mode_cbr);
                String bitRateModeRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.encoder_bit_rate_mode_remark);
                holder.showRemark(bitRateModeRemark);
                break;
            case REQUEST_CODE_AUDIO_SAMPLE_USE_AUDIO_RECORDER:
            case REQUEST_AUDIO_CHANNEL_STEREO_ENABLE:
            case REQUEST_AUDIO_AGC_CONTROL_ENABLE:
            case REQUEST_AUDIO_PRE_AMPLIFIER_ENABLE:
                holder.showRadio(
                        info.getItemValue(), Boolean.TRUE.toString(), Boolean.FALSE.toString());
                break;
            case REQUEST_AUDIO_AGC_TARGET_DBOV:
                holder.showContent(info.getItemValue());
                String targetDbovRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_agc_targetDBOV_remark);
                holder.showRemark(targetDbovRemark);
                break;
            case REQUEST_AUDIO_AGC_COMPRESSION_LEVEL:
                holder.showContent(info.getItemValue());
                String compressionLevelRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_agc_compression_remark);
                holder.showRemark(compressionLevelRemark);
                break;
            case REQUEST_AUDIO_AGC_LIMITER_ENABLE:
                holder.showRadio(
                        info.getItemValue(), Boolean.TRUE.toString(), Boolean.FALSE.toString());
                String agcLimiterRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_agc_limiter_enable_remark);
                holder.showRemark(agcLimiterRemark);
                break;
            case REQUEST_AUDIO_ECHO_CANCEL_FILTER_ENABLE:
                holder.showRadio(
                        info.getItemValue(), Boolean.TRUE.toString(), Boolean.FALSE.toString());
                String echoCancelFilterRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_echo_cancel_filter_remark);
                holder.showRemark(echoCancelFilterRemark);
                break;
            case REQUEST_AUDIO_NOISE_SUPPRESSION_MODE:
                holder.showRadio(info.getItemValue(), "0", "1", "2", "3");
                String suppressionModeRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_noise_suppression_remark);
                holder.showRemark(suppressionModeRemark);
                break;
            case REQUEST_AUDIO_NOISE_SUPPRESSION_LEVEL:
                holder.showRadio(info.getItemValue(), "0", "1", "2", "3");
                String suppressionLevelRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_noise_suppression_level_remark);
                holder.showRemark(suppressionLevelRemark);
                break;
            case REQUEST_AUDIO_NOISE_HIGH_PASS_FILTER:
                holder.showRadio(
                        info.getItemValue(), Boolean.TRUE.toString(), Boolean.FALSE.toString());
                String highPassFilterRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_noise_high_pass_filter_remark);
                holder.showRemark(highPassFilterRemark);
                break;
            case REQUEST_AUDIO_ECHO_CANCEL_MODE:
                holder.showRadio(info.getItemValue(), "0", "1", "2");
                String echoCancelModeRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.audio_echo_cancel_mode_remark);
                holder.showRemark(echoCancelModeRemark);
                break;
            default:
                holder.showContent(info.getItemValue());
                break;
        }
        holder.mRadioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        String selectedVal = null;
                        if (checkedId == R.id.radioButton1) {
                            selectedVal = holder.mRadioButton1.getText().toString();
                        } else if (checkedId == R.id.radioButton2) {
                            selectedVal = holder.mRadioButton2.getText().toString();
                        } else if (checkedId == R.id.radioButton3) {
                            selectedVal = holder.mRadioButton3.getText().toString();
                        } else if (checkedId == R.id.radioButton4) {
                            selectedVal = holder.mRadioButton4.getText().toString();
                        }

                        Log.e(TAG, "onCheckedChanged: " + selectedVal);
                        EventBusInfo eventBusInfo1 =
                                new EventBusInfo(info.getRequestCode(), selectedVal, 0);
                        EventBus.getDefault().post(eventBusInfo1);
                    }
                });
        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ButtentSolp.check(v.getId(), 1500)) {
                            return;
                        }
                        if (listener != null) {
                            listener.onClick(position);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return avConfigInfoList != null ? avConfigInfoList.size() : 0;
    }

    public class CodecViewHolder extends RecyclerView.ViewHolder {
        private TextView mTv_itemTitle;
        private TextView mTv_content;
        private TextView mTv_remark;
        private RadioGroup mRadioGroup;
        private RadioButton mRadioButton1, mRadioButton2, mRadioButton3, mRadioButton4;

        public CodecViewHolder(View itemView) {
            super(itemView);
            mTv_itemTitle = itemView.findViewById(R.id.tv_itemTitle);
            mTv_content = itemView.findViewById(R.id.tc_content);
            mTv_remark = itemView.findViewById(R.id.tc_remark);
            mRadioGroup = itemView.findViewById(R.id.radioGroup);
            mRadioButton1 = itemView.findViewById(R.id.radioButton1);
            mRadioButton2 = itemView.findViewById(R.id.radioButton2);
            mRadioButton3 = itemView.findViewById(R.id.radioButton3);
            mRadioButton4 = itemView.findViewById(R.id.radioButton4);
        }

        public void setTitle(String title) {
            if (!TextUtils.isEmpty(title)) {
                mTv_itemTitle.setText(title);
            } else {
                mTv_itemTitle.setText("");
            }
        }

        /** 仅显示内容信息，该条目需要点击跳转选择值 */
        public void showContent(String content) {
            mTv_content.setVisibility(View.VISIBLE);
            mRadioGroup.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(content)) {
                mTv_content.setText(content);
            } else {
                mTv_content.setText("");
            }
            mTv_remark.setVisibility(View.GONE);
        }

        public void showRadio(String selectedVal, String val1, String val2) {
            showRadio(selectedVal, val1, val2, null, null);
        }

        public void showRadio(String selectedVal, String val1, String val2, String val3) {
            showRadio(selectedVal, val1, val2, val3, null);
        }
        /**
         * 显示单选内容
         *
         * @param selectedVal 选中值
         * @param val1 选项1内容
         * @param val2 选项2内容
         * @param val3 选项3内容
         */
        public void showRadio(
                String selectedVal, String val1, String val2, String val3, String val4) {
            Log.i(
                    TAG,
                    "showRadio: selected "
                            + selectedVal
                            + " "
                            + val1
                            + " "
                            + val2
                            + " "
                            + val3
                            + " "
                            + val4);
            mTv_content.setVisibility(View.GONE);
            mRadioGroup.setVisibility(View.VISIBLE);
            mTv_remark.setVisibility(View.GONE);

            mRadioButton1.setText(val1);
            mRadioButton2.setText(val2);
            mRadioButton3.setText(val3);
            mRadioButton4.setText(val4);

            mRadioButton1.setVisibility(TextUtils.isEmpty(val1) ? View.GONE : View.VISIBLE);
            mRadioButton2.setVisibility(TextUtils.isEmpty(val2) ? View.GONE : View.VISIBLE);
            mRadioButton3.setVisibility(TextUtils.isEmpty(val3) ? View.GONE : View.VISIBLE);
            mRadioButton4.setVisibility(TextUtils.isEmpty(val4) ? View.GONE : View.VISIBLE);

            if (!TextUtils.isEmpty(val1)
                    && TextUtils.equals(selectedVal.toUpperCase(), val1.toUpperCase())
                    && !mRadioButton1.isChecked()) {
                mRadioButton1.setChecked(false);
                mRadioButton2.setChecked(false);
                mRadioButton3.setChecked(false);
                mRadioButton4.setChecked(false);

                mRadioButton1.setChecked(true);
            } else if (!TextUtils.isEmpty(val2)
                    && TextUtils.equals(selectedVal.toUpperCase(), val2.toUpperCase())
                    && !mRadioButton2.isChecked()) {
                mRadioButton1.setChecked(false);
                mRadioButton2.setChecked(false);
                mRadioButton3.setChecked(false);
                mRadioButton4.setChecked(false);

                mRadioButton2.setChecked(true);
            } else if (!TextUtils.isEmpty(val3)
                    && TextUtils.equals(selectedVal.toUpperCase(), val3.toUpperCase())
                    && !mRadioButton3.isChecked()) {
                mRadioButton1.setChecked(false);
                mRadioButton2.setChecked(false);
                mRadioButton3.setChecked(false);
                mRadioButton4.setChecked(false);

                mRadioButton3.setChecked(true);
            } else if (!TextUtils.isEmpty(val4)
                    && TextUtils.equals(selectedVal.toUpperCase(), val4.toUpperCase())
                    && !mRadioButton4.isChecked()) {
                mRadioButton1.setChecked(false);
                mRadioButton2.setChecked(false);
                mRadioButton3.setChecked(false);
                mRadioButton4.setChecked(false);

                mRadioButton4.setChecked(true);
            }
        }

        public void showRemark(String itemRemark) {
            mTv_remark.setText(itemRemark);
            mTv_remark.setVisibility(TextUtils.isEmpty(itemRemark) ? View.GONE : View.VISIBLE);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
