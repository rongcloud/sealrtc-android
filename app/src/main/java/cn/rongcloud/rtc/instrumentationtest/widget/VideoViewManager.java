package cn.rongcloud.rtc.instrumentationtest.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.util.Utils;
import java.util.HashMap;
import java.util.Map;

public class VideoViewManager {

    private LinearLayout mLayout;
    private Map<String, RCRTCVideoView> mViewMap;

    public VideoViewManager(LinearLayout mLayout) {
        this.mLayout = mLayout;
        mViewMap = new HashMap<>();
    }


    public void addVideoView(String streamId, RCRTCVideoView videoView) {
//        if (mViewMap.containsKey(streamId)) {
//            return;
//        }
        removeVideoView(streamId);
        mViewMap.put(streamId, videoView);
        LayoutParams params = new LayoutParams((int) dpToPx(45), LayoutParams.MATCH_PARENT);
        params.leftMargin = params.rightMargin = (int) dpToPx(4);
        mLayout.addView(videoView, params);
    }

    public RCRTCVideoView removeVideoView(String streamId) {
        RCRTCVideoView view = mViewMap.remove(streamId);
        if (view != null) {
            mLayout.removeView(view);
        }

        return view;
    }

    public boolean hasVideoView(String streamId) {
        return mViewMap.containsKey(streamId);
    }

    public float dpToPx(float dp) {
        Resources resources = mLayout.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }


}
