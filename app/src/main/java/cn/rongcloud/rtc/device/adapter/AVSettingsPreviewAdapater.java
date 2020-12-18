package cn.rongcloud.rtc.device.adapter;

import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_AGC_COMPRESSION_LEVEL;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_AGC_CONTROL_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_AGC_LIMITER_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_AGC_TARGET_DBOV;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_CHANNEL_STEREO_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_ECHO_CANCEL_FILTER_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_ECHO_CANCEL_MODE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_NOISE_SUPPRESSION_LEVEL;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_NOISE_SUPPRESSION_MODE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_AUDIO_PRE_AMPLIFIER_ENABLE;
import static cn.rongcloud.rtc.device.utils.Consts.REQUEST_CODE_AUDIO_SAMPLE_USE_AUDIO_RECORDER;
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

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.entity.AVConfigInfo;
import cn.rongcloud.rtc.util.Utils;
import java.util.List;

public class AVSettingsPreviewAdapater
        extends RecyclerView.Adapter<AVSettingsPreviewAdapater.CodecViewHolder> {
    private static final String TAG = "AVSettingsPreviewAdapater";
    private List<AVConfigInfo> avConfigInfoList;

    public AVSettingsPreviewAdapater(List<AVConfigInfo> infos) {
        avConfigInfoList = infos;
    }

    @Override
    public CodecViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_avsettings_preview_adapter, parent, false);
        return new CodecViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            final CodecViewHolder holder, final int position) {
        if (avConfigInfoList == null
                || avConfigInfoList.size() == 0
                || position < 0
                || position >= avConfigInfoList.size()) {
            return;
        }
        final AVConfigInfo info = avConfigInfoList.get(position);
        holder.setTitle(info.getTitle());

        switch (info.getRequestCode()) {
            case REQUEST_CODE_ENCODER_TYPE:
                String hw_encoder =
                        Utils.getContext().getResources().getString(R.string.hw_encoder_str);
                String soft_encoder_str =
                        Utils.getContext().getResources().getString(R.string.soft_encoder_str);
                holder.showContent(info.getItemValue());
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
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_CODE_CAMERA_DISPLAY_ORIENTATION:
                holder.showContent(info.getItemValue());
                String cameraOrientationRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.camer_display_orientation_remark);
                break;
            case REQUEST_CODE_FRAME_ORIENTATION:
                holder.showContent(info.getItemValue());
                String frameOrientationRemark =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.frame_orientation_remark);
                break;
            case REQUEST_CODE_CAPTURE_TYPE:
                String capture_type_texture =
                        Utils.getContext().getResources().getString(R.string.capture_type_texture);
                String capture_type_yuv =
                        Utils.getContext().getResources().getString(R.string.capture_type_yuv);
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_CODE_ENCODER_LEVEL:
                String encoder_level_baseline =
                        Utils.getContext()
                                .getResources()
                                .getString(R.string.encoder_leval_baseline);
                String encoder_level_height =
                        Utils.getContext().getResources().getString(R.string.encoder_leval_hight);
                holder.showContent(info.getItemValue());
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
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_CODE_AUDIO_SAMPLE_USE_AUDIO_RECORDER:
            case REQUEST_AUDIO_CHANNEL_STEREO_ENABLE:
            case REQUEST_AUDIO_AGC_CONTROL_ENABLE:
            case REQUEST_AUDIO_PRE_AMPLIFIER_ENABLE:
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_AUDIO_AGC_TARGET_DBOV:
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_AUDIO_AGC_COMPRESSION_LEVEL:
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_AUDIO_AGC_LIMITER_ENABLE:
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_AUDIO_ECHO_CANCEL_FILTER_ENABLE:
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_AUDIO_NOISE_SUPPRESSION_MODE:
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_AUDIO_NOISE_SUPPRESSION_LEVEL:
                holder.showContent(info.getItemValue());
                break;
            case REQUEST_AUDIO_ECHO_CANCEL_MODE:
                holder.showContent(info.getItemValue());
                break;
            default:
                holder.showContent(info.getItemValue());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return avConfigInfoList != null ? avConfigInfoList.size() : 0;
    }

    public class CodecViewHolder extends RecyclerView.ViewHolder {
        private TextView mTv_itemTitle;
        private TextView mTv_content;

        public CodecViewHolder(View itemView) {
            super(itemView);
            mTv_itemTitle = itemView.findViewById(R.id.tv_itemTitle);
            mTv_content = itemView.findViewById(R.id.tc_content);
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
            if (!TextUtils.isEmpty(content)) {
                mTv_content.setText(content);
            } else {
                mTv_content.setText("");
            }
        }
    }
}
