package cn.rongcloud.rtc.instrumentationtest;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import java.util.List;

public class OperationAdapter extends Adapter {

    private List<OperationModel> mDataList;
    private OnClickItemChildListener mOnClickItemChildListener;

    public OperationAdapter(List data) {
        mDataList = data;
    }

    public OnClickItemChildListener getOnClickItemChildListener() {
        return mOnClickItemChildListener;
    }

    public void setOnClickItemChildListener(OnClickItemChildListener onClickItemChildListener) {
        mOnClickItemChildListener = onClickItemChildListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new OperationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_operation, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((OperationViewHolder) holder).bindData(mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class OperationViewHolder extends ViewHolder {

        public final Button mBtnSubmit;
        public final TextView mTvLbl;
        public final HorizontalScrollView mScrollLayout;
        public final TextView mTvEx;
        private OperationModel mData;

        public OperationViewHolder(View itemView) {
            super(itemView);
            mBtnSubmit = itemView.findViewById(R.id.btn_submit);
            mTvLbl = itemView.findViewById(R.id.tv_lbl);
            mScrollLayout = itemView.findViewById(R.id.scroll_layout);
            mTvEx = itemView.findViewById(R.id.tv_ex);
            mBtnSubmit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mData.requesting(OperationAdapter.this);
                    if (mOnClickItemChildListener != null) {
                        mOnClickItemChildListener.onClickItem(mData, OperationViewHolder.this);
                    }
                    bindData(mData);
                }
            });
        }

        public void bindData(OperationModel data) {
            mData = data;
            mBtnSubmit.setContentDescription(data.getDesc());
            mBtnSubmit.setText(data.getBtnText());
            mBtnSubmit.setTextColor(data.getTextColor());
            mTvLbl.setText(data.getDesc());
            mTvEx.setText(data.getExtra());
            mTvEx.setVisibility(TextUtils.isEmpty(data.getExtra()) ? View.GONE : View.VISIBLE);
        }
    }

    public interface OnClickItemChildListener<T, V> {

        void onClickItem(T data, V viewHolder);
    }


}
