package cn.rongcloud.rtc.call;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.call.VideoContainerGestureDetector.ViewContainerGestureListener;
import cn.rongcloud.rtc.core.RendererCommon;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.screen_cast.RongRTCScreenCastHelper;
import cn.rongcloud.rtc.util.CoverView;
import cn.rongcloud.rtc.util.CoverView.ContainerNameListener;
import cn.rongcloud.rtc.utils.FinLog;

/**
 * Created by RongCloud on 2017/3/30.
 */
public class ContainerLayout extends RelativeLayout implements ViewContainerGestureListener,
    ContainerNameListener {

    private static final String TAG = "ContainerLayout";
    private Context context;
    private VideoContainerGestureDetector gestureDetector;
    private ContainerLayoutGestureEvents mGeustureEvents;
    public static boolean enableGestureDetect = true;
    private RelativeLayout videoChild;
    private LinearLayout indexLayout;
    private TextView videoNameTv;
    private ImageView audioLevelImage;
    private Rect gestrueMargin = new Rect();

    public interface ContainerLayoutGestureEvents {

        void onSingleClick();

        void onDoubleClick();
    }

    public ContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new VideoContainerGestureDetector(context);
        gestureDetector.setGestureListener(this);
        this.context = context;
        init(context);
    }

    private void init(Context context) {

        indexLayout = new LinearLayout(context);
        indexLayout.setOrientation(LinearLayout.HORIZONTAL);

        videoNameTv = new TextView(context);
        videoNameTv.setTextColor(getResources().getColor(R.color.colorWhite));
        videoNameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        indexLayout.addView(videoNameTv, nameParams);

        audioLevelImage = new ImageView(context);
        Glide.with(context).asGif().load(R.drawable.sound)
            .into(audioLevelImage);
        LinearLayout.LayoutParams levelParams = new LinearLayout.LayoutParams(
            (int) getResources().getDimension(R.dimen.audio_level_size),
            (int) getResources().getDimension(R.dimen.audio_level_size));
        levelParams.gravity = Gravity.CENTER_VERTICAL;
        indexLayout.addView(audioLevelImage, levelParams);
    }

    private void refresNameLayout() {
        if (indexLayout != null) {
            removeView(indexLayout);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//            params.addRule(RelativeLayout.ALIGN_LEFT);
            addView(indexLayout, params);
        }
    }

    public void setGestureEvents(ContainerLayoutGestureEvents events) {
        mGeustureEvents = events;
    }

    public void addView(
        final VideoViewManager.RenderHolder renderHolder, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        updateHoldListener(renderHolder);
        videoChild = renderHolder.containerLayout;
        if (renderHolder.containerLayout.getParent() != null
            && renderHolder.containerLayout.getParent() != ContainerLayout.this) {
            return;
        }
        Log.i(TAG, "addView()");

        super.addView(videoChild, getBigContainerParams(renderHolder));
        refresNameLayout();
//        init(context);
        renderHolder
            .coverView
            .getRongRTCVideoView()
            .setOnSizeChangedListener(
                new RCRTCVideoView.OnSizeChangedListener() {
                    @Override
                    public void onChanged(RCRTCVideoView.Size size) {
                        FinLog.d(
                            TAG,
                            "get video view exact size,refresh view size: W = "
                                + size.with
                                + " ,H = "
                                + size.height);
                        if (renderHolder.containerLayout.getParent() != null
                            && renderHolder.containerLayout.getParent()
                            != ContainerLayout.this) {
                            return;
                        }
                        if (videoChild != null) {
                            ContainerLayout.this.removeView(videoChild);
                        }
                        videoChild = renderHolder.containerLayout;
                        videoChild.setGravity(Gravity.CENTER);
                        ContainerLayout.this
                            .addView(videoChild, getBigContainerParams(renderHolder));
                        refresNameLayout();
                    }
                });
    }

    private void updateHoldListener(VideoViewManager.RenderHolder renderHolder) {
        if (renderHolder != null) {
            CoverView  coverView = this.renderHolder != null ? this.renderHolder.coverView : null;
            if (this.renderHolder != null && coverView != null) {
                coverView.setNameListener(null);
                Log.d(TAG, "updateHoldListener: " + renderHolder.getUserName());
            }
            renderHolder.coverView.setNameListener(this);
            renderHolder.coverView.hidNameIndexView();
            renderHolder.coverView.updateNameTv();
            this.renderHolder = renderHolder;
        }
    }

    @Override
    public void showAudioLevel() {
        if (null == audioLevelImage) {
            return;
        }
        if (audioLevelImage.getVisibility() != VISIBLE) {
            audioLevelImage.setVisibility(VISIBLE);
        }
    }

    @Override
    public void hideAudioLevel() {
        if (null == audioLevelImage) {
            return;
        }
        if (audioLevelImage.getVisibility() != INVISIBLE) {
            audioLevelImage.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void updateNameInfo(String name, String id, String tag) {
        if (null != videoNameTv && !TextUtils.isEmpty(name)) {
            videoNameTv.setText(name.length() > 4 ? name.substring(0, 4) : name);
        }
        if (videoNameTv != null) {
            if (!TextUtils.isEmpty(tag) && TextUtils
                .equals(tag, "FileVideo")) {
                videoNameTv.setText(
                    name + "-" + getResources().getString(R.string.user_video_custom));
            } else if (!TextUtils.isEmpty(tag)
                && TextUtils.equals(tag, RongRTCScreenCastHelper.VIDEO_TAG)) {
                videoNameTv.setText(
                    name + "-" + getResources().getString(R.string.user_shared_screen));
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return enableGestureDetect;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enableGestureDetect && gestureDetector != null) {
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public void refreshView(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        if (renderHolder == null
            || null == renderHolder.coverView
            || null == renderHolder.coverView.rongRTCVideoView) {
            return;
        }
        if (videoChild != null) {
            removeView(videoChild);
        }
        videoChild = renderHolder.containerLayout;
        // 解决横屏显示共享内容不全问题
        renderHolder
            .coverView
            .getRongRTCVideoView()
            .setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        videoChild.setGravity(Gravity.CENTER);

        ContainerLayout.this.addView(videoChild
            , getBigContainerParams(renderHolder));
        refresNameLayout();
        gestureDetector.reset();
    }

    public void resetGestureView() {
        if (gestureDetector != null) {
            gestureDetector.reset();
        }
    }

    @NonNull
    private LayoutParams getBigContainerParams(VideoViewManager.RenderHolder renderHolder) {
        LayoutParams layoutParams =
            new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (renderHolder.coverView != null
            && renderHolder.coverView.getRongRTCVideoView() != null) {
            RCRTCVideoView videoView = renderHolder.coverView.getRongRTCVideoView();
            if (screenHeight > screenWidth) { // V
                int layoutParamsHeight =
                    (videoView.rotatedFrameHeight == 0 || videoView.rotatedFrameWidth == 0)
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : screenWidth
                            * videoView.rotatedFrameHeight
                            / videoView.rotatedFrameWidth;
                layoutParams = new LayoutParams(screenWidth, layoutParamsHeight);
            } else {
                int layoutParamsWidth =
                    (videoView.rotatedFrameWidth == 0 || videoView.rotatedFrameHeight == 0)
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (screenHeight
                            * videoView.rotatedFrameWidth
                            / videoView.rotatedFrameHeight
                            > screenWidth
                            ? screenWidth
                            : screenHeight
                                * videoView.rotatedFrameWidth
                                / videoView.rotatedFrameHeight);
                layoutParams = new LayoutParams(layoutParamsWidth, screenHeight);
            }
        }
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        return layoutParams;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    private void updateGeustMargin(
        int containerWidth, int containerHeight, int boxWidth, int boxHeight) {
        gestrueMargin.left = (containerWidth - boxWidth) / 2;
        gestrueMargin.top = (containerHeight - boxHeight) / 2;
        gestrueMargin.right = gestrueMargin.left;
        gestrueMargin.bottom = gestrueMargin.top;
        //        Log.d("VideoContainerGesture",
        //            "updateGeustMargin: left" + gestrueMargin.left + ", top " + gestrueMargin.top
        //                + ", right " + gestrueMargin.right + ", bottom " + gestrueMargin.bottom);
    }

    private float checkBroadwiseBounds(RectF boxBounds) {
        //        Log.d("VideoContainerGesture",
        //            "checkBroadwiseBounds: child left " + boxBounds.left + ", right " +
        // boxBounds.right
        //                + ", container width " + getWidth());

        float overResult = 0;

        if (boxBounds.left + gestrueMargin.left > 0
            && boxBounds.right + gestrueMargin.left > getWidth()) {     // 右边超界

            if (getChildScaleValue() == 1.0f) {     // 居中
                overResult = getWidth() - (gestrueMargin.left / 2) - boxBounds.right;
            } else if (getChildScaleValue() < 1.0f) {
                overResult = getWidth() - gestrueMargin.left - boxBounds.right;
            } else {
                overResult = -boxBounds.left - gestrueMargin.left;
            }
        }

        if (boxBounds.left + gestrueMargin.left < 0
            && boxBounds.right + gestrueMargin.left < getWidth()) {
            if (getChildScaleValue() == 1.0f) {
                overResult = -(gestrueMargin.left / 2) - boxBounds.left;
            } else if (getChildScaleValue() < 1.0f) {
                overResult = -gestrueMargin.left - boxBounds.left;
            } else {
                overResult = getWidth() - gestrueMargin.left - boxBounds.right;
            }
        }

        return overResult;
    }

    private float checkEndwiseBounds(RectF boxBounds) {
//        Log.d("VideoContainerGesture",
//            "checkEndwiseBounds: child top " + boxBounds.top + ", bottom " +
//                boxBounds.bottom
//                + ", container height " + getHeight());
        float overResult = 0;
        if (0 > boxBounds.top + gestrueMargin.top
            && getHeight() > boxBounds.bottom + gestrueMargin.top) {           //上出界
            if (getChildScaleValue() == 1.0f) {     // 居中
                overResult = -(int) boxBounds.top - (gestrueMargin.top / 2);
            } else if (getChildScaleValue() <= 1.0f) {
                overResult = -(int) boxBounds.top - gestrueMargin.top;
            } else {
                overResult = getHeight() - (int) boxBounds.bottom - gestrueMargin.top;
            }
        }

        if (0 < boxBounds.top + gestrueMargin.top
            && getHeight() < boxBounds.bottom + gestrueMargin.top) {
            if (getChildScaleValue() == 1.0f) {
                overResult = getHeight() - (int) boxBounds.bottom - (gestrueMargin.top / 2);
            } else if (getChildScaleValue() < 1.0f) {
                overResult = getHeight() - (int) boxBounds.bottom - gestrueMargin.top;
            } else {
                overResult = -(int) boxBounds.top - gestrueMargin.top;
            }
        }

        return overResult;
    }

    /**
     * 获取view在当前matrix中映射的位置
     *
     * @return
     */
    private RectF getMatrixRectF() {

        RectF rect = new RectF();
        rect.set(0, 0, videoChild.getWidth(), videoChild.getHeight());
        videoChild.getMatrix().mapRect(rect);

//        Log.d(
//            "VideoContainerGesture",
//            "getMatrixRectF left "
//                + rect.left
//                + " top: "
//                + rect.top
//                + " right : "
//                + rect.right
//                + " bottom : "
//                + rect.bottom);
        return rect;
    }

    private PointF getChildTranslateValue() {
        float[] matrixValues = new float[9];
        videoChild.getMatrix().getValues(matrixValues);
        //        Log.d("VideoContainerGesture",
        //            "onTranslate: x " + +", y "
        //                +);
        return new PointF(matrixValues[Matrix.MTRANS_X], matrixValues[Matrix.MTRANS_Y]);
    }

    private float getChildScaleValue() {
        float[] matrixValues = new float[9];
        videoChild.getMatrix().getValues(matrixValues);
        //        Log.d("VideoContainerGesture",
        //            "onTranslate: x " + +", y "
        //                +);
        float scale = matrixValues[Matrix.MSCALE_X];
        Log.d(TAG, "getChildScaleValue: " + scale);
        return scale;
    }

    /**
     * ********** ViewContainerGeusterListener ***************
     */
    @Override
    public void onScale(float scaleX, float scaleY) {
        if (videoChild != null) {
            videoChild.setScaleX(scaleX);
            videoChild.setScaleY(scaleY);
        }
    }

    @Override
    public void onTranslate(float translateX, float translateY) {
        if (videoChild != null) {
            videoChild.setTranslationX(translateX);
            videoChild.setTranslationY(translateY);

//            getMatrixRectF();
        }
    }

    @Override
    public void onSingleTap() {
        if (mGeustureEvents != null) {
            mGeustureEvents.onSingleClick();
        }
    }

    @Override
    public void onDoubleTap() {
        if (mGeustureEvents != null) {
            mGeustureEvents.onDoubleClick();
        }
    }

    @Override
    public void onTranlateEnd() {

        if (gestureDetector != null) {
            if (videoChild != null) {
                updateGeustMargin(
                    getWidth(), getHeight(), videoChild.getWidth(), videoChild.getHeight());
            }
            RectF childBox = getMatrixRectF();
            gestureDetector.updateTranslate(
                checkBroadwiseBounds(childBox), checkEndwiseBounds(childBox));
        }
    }

    @Override
    public void onScaleEnd() {

        if (gestureDetector != null) {
            RectF childBox = getMatrixRectF();
            gestureDetector.updateTranslate(
                checkBroadwiseBounds(childBox), checkEndwiseBounds(childBox));
            if (getChildScaleValue() == 1.0f) {
                gestureDetector.reset();
            }

        }
    }

    private int screenWidth;
    private int screenHeight;
    private VideoViewManager.RenderHolder renderHolder;
}
