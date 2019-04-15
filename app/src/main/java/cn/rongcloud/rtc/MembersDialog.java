package cn.rongcloud.rtc;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MembersDialog extends DialogFragment {

    private RecyclerView mMembersRecyclerView;
    private TextView titleTextView;
    private MembersAdapter mAdapter;
    private List<ItemModel> modelList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_show_member, container, false);
        mMembersRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_members);
        mMembersRecyclerView.setAdapter(mAdapter = new MembersAdapter(getActivity()));
        mMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        v.findViewById(R.id.tv_join_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        titleTextView = (TextView) v.findViewById(R.id.tv_name);
        titleTextView.setText(getString(R.string.room_online_members, modelList.size()));
        mAdapter.setData(modelList);
        final Window window = getDialog().getWindow();
        ColorDrawable colorDrawable = new ColorDrawable();
        colorDrawable.setColor(Color.TRANSPARENT);
        window.setBackgroundDrawable(colorDrawable);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.dimAmount = 0.0f;
        window.setAttributes(wlp);
        setCancelable(true);
        return v;
    }

    public void update(List<ItemModel> list) {
        modelList = list;
        if (titleTextView != null) {
            titleTextView.setText(getString(R.string.room_online_members, modelList.size()));
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public class MembersAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        private Context mContext;
        private List<ItemModel> mDataList = new ArrayList<>();

        public MembersAdapter(Context context) {
            this.mContext = context;
        }

        public void setData(List<ItemModel> mDataList) {
            this.mDataList = mDataList;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_member, parent, false);
            if (viewType == TYPE_ITEM) {
                return new ItemViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.bind(mDataList.get(position));
        }

        @Override
        public int getItemCount() {
            if (mDataList != null)
                return mDataList.size();
            else
                return 0;
        }

        @Override
        public int getItemViewType(int position) {
                return TYPE_ITEM;
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView modeTextView;

        protected ItemViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_name);
            modeTextView = (TextView) itemView.findViewById(R.id.tv_join_mode);
        }

        public void bind(ItemModel model) {
            ItemModel itemModel = model;
            nameTextView.setText(itemModel.name);
            modeTextView.setText(itemModel.mode);

        }
    }

    public static class ItemModel {
        public String userId;
        public String name;
        public String mode;
        public long joinTime;

        public ItemModel(String name, String mode) {
            this.name = name;
            this.mode = mode;
        }

        public ItemModel() {

        }
    }
}
