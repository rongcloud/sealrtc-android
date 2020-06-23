package cn.rongcloud.rtc;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;
import cn.rongcloud.rtc.faceunity.ui.dialog.BaseDialogFragment;
import java.util.Arrays;

/**
 * Created by wangw on 2020/5/7.
 */
public class VideoSizeListDialog extends BaseDialogFragment {

  public static VideoSizeListDialog newInstance() {

    Bundle args = new Bundle();

    VideoSizeListDialog fragment = new VideoSizeListDialog();
    fragment.setArguments(args);
    return fragment;
  }

  private OnItemClickListener mOnItemClickListener;
  private RCRTCVideoStreamConfig.Builder mConfigBuilder;

  @Override
  protected View createDialogView(LayoutInflater inflater, @Nullable ViewGroup container) {
    Context context = getActivity();
    RecyclerView recyclerView = new RecyclerView(context);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    RCRTCVideoResolution[] values = RCRTCVideoResolution.values();

    VideoResolutionAdapter adapter = new VideoResolutionAdapter(
        Arrays.copyOfRange(values, 1, values.length));
    recyclerView.setAdapter(adapter);
    mConfigBuilder = RCRTCVideoStreamConfig.Builder.create();

    return recyclerView;
  }

  class VideoResolutionAdapter extends RecyclerView.Adapter<ItemVH> {

    RCRTCVideoResolution[] resolutions;

    public VideoResolutionAdapter(RCRTCVideoResolution[] resolutions) {
      this.resolutions = resolutions;
    }

    @Override
    public ItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
      TextView itemView = new TextView(parent.getContext());
      itemView.setPadding(0, 20, 0, 20);
      return new ItemVH(itemView);
    }

    @Override
    public void onBindViewHolder(ItemVH holder, int position) {
      holder.onBindData(resolutions[position]);

    }

    @Override
    public int getItemCount() {
      return resolutions.length;
    }
  }

  class ItemVH extends ViewHolder {

    TextView mLbl;
    RCRTCVideoResolution mData;

    public ItemVH(TextView itemView) {
      super(itemView);
      mLbl = itemView;
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (mOnItemClickListener != null) {
            mConfigBuilder.setVideoResolution(mData);
            mOnItemClickListener.onItemClick(mConfigBuilder.build());
          }
          dismiss();
        }
      });
    }

    public void onBindData(RCRTCVideoResolution resolution) {
      mData = resolution;
      mLbl.setText(resolution.getLabel());
    }
  }

  public OnItemClickListener getOnItemClickListener() {
    return mOnItemClickListener;
  }

  public void setOnItemClickListener(
      OnItemClickListener onItemClickListener) {
    this.mOnItemClickListener = onItemClickListener;
  }

  public interface OnItemClickListener {

    void onItemClick(RCRTCVideoStreamConfig config);
  }
}
