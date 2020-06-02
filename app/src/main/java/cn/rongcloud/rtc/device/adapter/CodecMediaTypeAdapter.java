package cn.rongcloud.rtc.device.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.utils.OnItemClickListener;
import cn.rongcloud.rtc.util.ButtentSolp;
import java.util.ArrayList;

public class CodecMediaTypeAdapter
        extends RecyclerView.Adapter<CodecMediaTypeAdapter.CodecViewHolder> {

    private ArrayList<String> codecInfoList;
    private String codecName = "";
    private OnItemClickListener listener;

    public CodecMediaTypeAdapter(ArrayList<String> list) {
        codecInfoList = list;
    }

    @Override
    public CodecViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.device_layout_codec_item, parent, false);
        return new CodecViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CodecViewHolder holder, final int position) {
        if (codecInfoList == null || codecInfoList.size() == 0) {
            return;
        }
        if (codecInfoList != null) {
            codecName = codecInfoList.get(position);
            if (!TextUtils.isEmpty(codecName)) {
                holder.tv_codecName.setText(codecName);
            }
        }
        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            if (ButtentSolp.check(v.getId(), 1500)) {
                                return;
                            }
                            listener.onClick(position);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return codecInfoList != null ? codecInfoList.size() : 0;
    }

    public class CodecViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_codecName;

        public CodecViewHolder(View itemView) {
            super(itemView);
            tv_codecName = (TextView) itemView.findViewById(R.id.tv_codecName);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
