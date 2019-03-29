package cn.rongcloud.rtc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import cn.rongcloud.rtc.core.RendererCommon;
import cn.rongcloud.rtc.utils.FinLog;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;

/**
 * Created by RongCloud on 2017/3/30.
 */

public class ContainerLayout extends RelativeLayout {
    private static final String TAG = "ContainerLayout";
    private Context context;
    public ContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
    }

    public void addView(final VideoViewManager.RenderHolder renderHolder, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.renderHolder = renderHolder;
        if (renderHolder.containerLayout.getParent()!=null && renderHolder.containerLayout.getParent()!=ContainerLayout.this){
            return;
        }
        Log.i(TAG,"addView()");
        super.addView(renderHolder.containerLayout, getBigContainerParams(renderHolder.coverView.getRongRTCVideoView()));
        renderHolder.coverView.getRongRTCVideoView().setOnSizeChangedListener(new RongRTCVideoView.OnSizeChangedListener() {
            @Override
            public void onChanged(RongRTCVideoView.Size size) {
                FinLog.d(TAG,"得到准确尺寸，重新刷新视图：size= W:" + size.with + " H:" + size.height);
                if (renderHolder.containerLayout.getParent()!=null && renderHolder.containerLayout.getParent()!=ContainerLayout.this){
                    return;
                }
                ContainerLayout.this.removeAllViews();
                renderHolder.containerLayout.setGravity(Gravity.CENTER);
                ContainerLayout.this.addView(renderHolder.containerLayout, getBigContainerParams(renderHolder.coverView.getRongRTCVideoView()));
            }
        });
    }

    public void refreshView(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        if (renderHolder == null || null==renderHolder.coverView || null==renderHolder.coverView.rongRTCVideoView)
            return;
        ContainerLayout.this.removeAllViews();
        //解决横屏显示共享内容不全问题
        renderHolder.coverView.getRongRTCVideoView().setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        renderHolder.containerLayout.setGravity(Gravity.CENTER);
        ContainerLayout.this.addView(renderHolder.containerLayout, getBigContainerParams(renderHolder.coverView.getRongRTCVideoView()));
    }

    @NonNull
    private LayoutParams getBigContainerParams(RongRTCVideoView videoView) {
        LayoutParams layoutParams=null;
        if (screenHeight > screenWidth) { //V
            int layoutParamsHeight=(videoView.rotatedFrameHeight == 0 || videoView.rotatedFrameWidth == 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : screenWidth * videoView.rotatedFrameHeight / videoView.rotatedFrameWidth;
            layoutParams = new LayoutParams(screenWidth, layoutParamsHeight);
        }else {
            int layoutParamsWidth=(videoView.rotatedFrameHeight == 0 || videoView.rotatedFrameHeight == 0) ? ViewGroup.LayoutParams.WRAP_CONTENT :(screenWidth * videoView.rotatedFrameWidth / videoView.rotatedFrameHeight > screenWidth ? screenWidth : screenHeight * videoView.rotatedFrameWidth / videoView.rotatedFrameHeight);
            layoutParams = new LayoutParams(layoutParamsWidth, screenHeight);
        }
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        return layoutParams;
    }

    private int screenWidth;
    private int screenHeight;
    private VideoViewManager.RenderHolder renderHolder;

}
