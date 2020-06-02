package cn.rongcloud.rtc.device.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.device.utils.OnItemClickListener;
import cn.rongcloud.rtc.util.ButtentSolp;
import java.util.List;

public class SelectionParametersAdapter
        extends RecyclerView.Adapter<SelectionParametersAdapter.CodecViewHolder> {

    private List<String> data;
    private OnItemClickListener listener;

    public SelectionParametersAdapter(List<String> list) {
        data = list;
    }

    @Override
    public CodecViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_selection_parameters, parent, false);
        return new CodecViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CodecViewHolder holder, final int position) {
        if (data == null || data.size() == 0) {
            return;
        }
        holder.tv_itemTitle.setText(data.get(position));
        holder.linear_select_parameter.setOnClickListener(
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
        return data != null ? data.size() : 0;
    }

    public class CodecViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_itemTitle;
        private LinearLayout linear_select_parameter;

        public CodecViewHolder(View itemView) {
            super(itemView);
            tv_itemTitle = (TextView) itemView.findViewById(R.id.tv_item);
            linear_select_parameter = itemView.findViewById(R.id.linear_select_parameter);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
