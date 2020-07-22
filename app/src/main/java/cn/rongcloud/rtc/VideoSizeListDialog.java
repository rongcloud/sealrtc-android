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

/**
 * Created by wangw on 2020/5/7.
 */
public class VideoSizeListDialog extends BaseDialogFragment {

  int[] resolutions = {R.string.rtc_resolution_standard, R.string.rtc_resolution_high, R.string.rtc_resolution_ultrahigh};
  private static Context myContext;

  public static VideoSizeListDialog newInstance(Context context) {
    Bundle args = new Bundle();
    myContext = context;
    VideoSizeListDialog fragment = new VideoSizeListDialog();
    fragment.setArguments(args);
    return fragment;
  }

  private OnItemClickListener mOnItemClickListener;

  @Override
  protected View createDialogView(LayoutInflater inflater, @Nullable ViewGroup container) {
    Context context = getActivity();
    RecyclerView recyclerView = new RecyclerView(context);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));

    VideoResolutionAdapter adapter = new VideoResolutionAdapter(resolutions);
    recyclerView.setAdapter(adapter);

    return recyclerView;
  }

  class VideoResolutionAdapter extends RecyclerView.Adapter<ItemVH> {

    int[] resolutions;

    public VideoResolutionAdapter(int[] resolutions) {
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
    RCRTCVideoResolution mData = RCRTCVideoResolution.RESOLUTION_480_640;

    public ItemVH(TextView itemView) {
      super(itemView);
      mLbl = itemView;
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (mOnItemClickListener != null) {
            RCRTCVideoStreamConfig.Builder mConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
            mConfigBuilder.setVideoResolution(mData);
            mOnItemClickListener.onItemClick(mConfigBuilder.build());
          }
          dismiss();
        }
      });
    }

    public void onBindData(int resId) {
      if (resId == R.string.rtc_resolution_standard) {
        mData = RCRTCVideoResolution.RESOLUTION_360_480;
      } else if (resId == R.string.rtc_resolution_high) {
        mData = RCRTCVideoResolution.RESOLUTION_480_640;
      } else if (resId == R.string.rtc_resolution_ultrahigh) {
        mData = RCRTCVideoResolution.RESOLUTION_720_1280;
      }
      mLbl.setText(myContext.getResources().getText(resId));
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
