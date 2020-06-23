package cn.rongcloud.rtc.call;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import cn.rongcloud.rtc.engine.view.RongRTCGestureDetector;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import io.rong.common.RLog;

public class VideoContainerLayout extends RelativeLayout
        implements RongRTCGestureDetector.RongRTCGestureLayoutEvents {

    private RongRTCGestureDetector gestureDetector;
    private Rect layoutRect = new Rect();
    private static final String TAG = VideoContainerLayout.class.getSimpleName();

    public VideoContainerLayout(@NonNull Context context) {
        super(context);
        gestureDetector = new RongRTCGestureDetector(context, null);
        gestureDetector.setLayoutEvents(this);
    }

    public VideoContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new RongRTCGestureDetector(context, null);
        gestureDetector.setLayoutEvents(this);
    }

    public VideoContainerLayout(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gestureDetector = new RongRTCGestureDetector(context, null);
        gestureDetector.setLayoutEvents(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoContainerLayout(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        gestureDetector = new RongRTCGestureDetector(context, null);
        gestureDetector.setLayoutEvents(this);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        RLog.d(TAG, "setOnClickListener: ");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        RLog.d(TAG, "onInterceptTouchEvent: ");
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        //        RLog.d(TAG, "onTouchEvent: ");
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        RLog.d(TAG, "onMeasure: ");
        final int width = getDefaultSize(Integer.MAX_VALUE, widthMeasureSpec);
        final int height = getDefaultSize(Integer.MAX_VALUE, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView instanceof RCRTCVideoView) {
                gestureDetector.setLayoutSize(width, height);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        RLog.d(TAG, "onLayout: ");
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView instanceof RCRTCVideoView) {
                gestureDetector.requestSize();
            }
        }
    }

    private void gestureVideoView(int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView instanceof RCRTCVideoView) {

                int childWidth = right - left;
                int childHeight = bottom - top;
                int childWidthMeasureSpec =
                        MeasureSpec.makeMeasureSpec((int) childWidth, MeasureSpec.EXACTLY);
                int childHeightMeasureSpec =
                        MeasureSpec.makeMeasureSpec((int) childHeight, MeasureSpec.EXACTLY);

                childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                childView.layout(left, top, right, bottom);
                childView.invalidate();
            }
        }
        //        invalidate();

    }

    @Override
    public void onGestureLayout(int left, int top, int right, int bottom) {
        RLog.d(TAG, "onGestureLayout: ");
        layoutRect.left = left;
        layoutRect.right = right;
        layoutRect.top = top;
        layoutRect.bottom = bottom;
        gestureVideoView(left, top, right, bottom);
    }
}
