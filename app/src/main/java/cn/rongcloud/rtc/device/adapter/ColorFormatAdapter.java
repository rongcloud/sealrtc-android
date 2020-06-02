package cn.rongcloud.rtc.device.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.entity.ColorFormat;
import cn.rongcloud.rtc.device.utils.OnColorFormatItemClickListener;
import cn.rongcloud.rtc.util.ButtentSolp;
import java.util.List;

public class ColorFormatAdapter extends RecyclerView.Adapter<ColorFormatAdapter.ColorFormatHolder> {

    private List<ColorFormat> colorFormats;
    private String colorFormats_Select = "";

    private OnColorFormatItemClickListener listener;

    public ColorFormatAdapter(List<ColorFormat> list) {
        colorFormats = list;
    }

    public void setSelectItem(String list) {
        colorFormats_Select = list;
    }

    @Override
    public ColorFormatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.device_layout_colorformat_item, parent, false);
        return new ColorFormatHolder(view);
    }

    @Override
    public void onBindViewHolder(final ColorFormatHolder holder, final int position) {
        int colorValue = colorFormats.get(position).getColor();
        String colorAlias = colorFormats.get(position).getAlias();

        if (!TextUtils.isEmpty(colorAlias) && !colorAlias.startsWith("0x")) {
            holder.tv_colorFormat.setText(
                    "0x" + Integer.toHexString(colorValue) + "\n" + colorAlias);
        } else {
            holder.tv_colorFormat.setText(colorAlias);
        }
        holder.iv_select.setImageResource(R.drawable.device_icon_checkbox_checked);
        holder.iv_select.setSelected(false);
        if (colorFormats_Select.equals(colorAlias)) {
            holder.iv_select.setImageResource(R.drawable.device_icon_checkbox_hover);
            holder.iv_select.setSelected(true);
        }
        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ButtentSolp.check(v.getId(), 500)) {
                            return;
                        }
                        if (holder.iv_select.isSelected()) {
                            holder.iv_select.setImageResource(
                                    R.drawable.device_icon_checkbox_checked);
                            holder.iv_select.setSelected(false);
                        } else {
                            holder.iv_select.setImageResource(
                                    R.drawable.device_icon_checkbox_hover);
                            holder.iv_select.setSelected(true);
                        }
                        if (listener != null) {
                            listener.onClick(
                                    position,
                                    colorFormats.get(position).getAlias(),
                                    colorFormats.get(position).getColor());
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return colorFormats != null ? colorFormats.size() : 0;
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
