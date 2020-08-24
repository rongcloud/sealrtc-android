package cn.rongcloud.rtc.device.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.entity.AudioSourceInfo;
import cn.rongcloud.rtc.device.utils.OnColorFormatItemClickListener;
import cn.rongcloud.rtc.util.ButtentSolp;
import java.util.List;

public class AudioSourceAdapter extends RecyclerView.Adapter<AudioSourceAdapter.ColorFormatHolder> {

    private int audioSourceSelected = 0;
    private List<AudioSourceInfo> audioSourceInfos;
    private OnColorFormatItemClickListener listener;

    public AudioSourceAdapter(List<AudioSourceInfo> list) {
        audioSourceInfos = list;
    }

    public void setSelectItem(int selected) {
        audioSourceSelected = selected;
    }

    @Override
    public ColorFormatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.device_layout_colorformat_item, parent, false);
        return new ColorFormatHolder(view);
    }

    @Override
    public void onBindViewHolder(
            final ColorFormatHolder holder, final int position) {
        String currentName = audioSourceInfos.get(position).getName();
        int currentCode = audioSourceInfos.get(position).getCode();

        holder.tv_colorFormat.setText(String.format("%s %d", currentName, currentCode));
        if (audioSourceSelected == currentCode) {
            holder.iv_select.setImageResource(R.drawable.device_icon_checkbox_hover);
            holder.iv_select.setSelected(true);
        } else {
            holder.iv_select.setImageResource(R.drawable.device_icon_checkbox_checked);
            holder.iv_select.setSelected(false);
        }

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ButtentSolp.check(v.getId(), 500)) {
                            return;
                        }

                        audioSourceSelected = audioSourceInfos.get(position).getCode();
                        notifyDataSetChanged();

                        if (listener != null) {
                            listener.onClick(
                                    position,
                                    audioSourceInfos.get(position).getName(),
                                    audioSourceInfos.get(position).getCode());
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return audioSourceInfos != null ? audioSourceInfos.size() : 0;
    }

    public class ColorFormatHolder extends RecyclerView.ViewHolder {

        private TextView tv_colorFormat;
        private ImageView iv_select;

        public ColorFormatHolder(View itemView) {
            super(itemView);
            tv_colorFormat = (TextView) itemView.findViewById(R.id.tv_colorFormat);
            iv_select = (ImageView) itemView.findViewById(R.id.iv_select);
        }
    }

    public void setOnItemClickListener(OnColorFormatItemClickListener listener) {
        this.listener = listener;
    }
}
