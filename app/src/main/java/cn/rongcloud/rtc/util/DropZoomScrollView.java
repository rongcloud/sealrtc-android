package cn.rongcloud.rtc.util;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/** Created by admin on 2017/8/25. 具有顶部IMageview 拉伸阻尼效果 */
public class DropZoomScrollView extends NestedScrollView implements View.OnTouchListener {
    // 记录首次按下位置
    private float mFirstPosition = 0;
    // 是否正在放大
    private Boolean mScaling = false;
    private View dropZoomView;
    private int dropZoomViewWidth;
    private int dropZoomViewHeight;
    private int distance = 0; // 滚动距离系数  -400 ----650
    private DropZoomScrollViewListener observableScrollViewListener = null;

    public DropZoomScrollView(Context context) {
        super(context);
    }

    public DropZoomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DropZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        if (getChildAt(0) != null) {
            ViewGroup vg = (ViewGroup) getChildAt(0);
            if (vg.getChildAt(0) != null) {
                dropZoomView = vg.getChildAt(0);
                setOnTouchListener(this);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (dropZoomViewWidth <= 0 || dropZoomViewHeight <= 0) {
            dropZoomViewWidth = dropZoomView.getMeasuredWidth();
            dropZoomViewHeight = dropZoomView.getMeasuredHeight();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                // 手指离开后恢复图片
                mScaling = false;
                replyImage();
                if (observableScrollViewListener != null) {
                    observableScrollViewListener.DropZoomScrollViewUpLitener(distance);
                }
                distance = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mScaling) {
                    if (getScrollY() == 0) {
                        mFirstPosition = event.getY(); // 滚动到顶部时记录位置，否则正常返回
                    } else {
                        break;
                    }
                }
                distance = (int) ((event.getY() - mFirstPosition) * 0.6); // 滚动距离乘以一个系数
                if (distance < 0) { // 当前位置比记录位置要小，正常返回
                    break;
                }
                // 处理放大
                mScaling = true;
                setZoom(2f + distance); // 设置放大速度
                if (observableScrollViewListener != null && distance > 0) {
                    observableScrollViewListener.DropZoomScrollViewMoveLitener(distance);
                }
                return true; // 返回true表示已经完成触摸事件，不再处理
        }
        return false;
    }
    // 回弹动画 (使用了属性动画)
    public void replyImage() {
        final float distance = dropZoomView.getMeasuredWidth() - dropZoomViewWidth;
        // 设置动画
        ValueAnimator anim =
                ObjectAnimator.ofFloat(0.0F, 1.0F).setDuration((long) (distance * 0.5)); // 设置回缩复原耗时
        anim.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float cVal = (Float) animation.getAnimatedValue();
                        setZoom(distance - ((distance) * cVal));
                    }
                });
        anim.start();
    }
    // 缩放
    public void setZoom(float s) {
        if (dropZoomViewHeight <= 0 || dropZoomViewWidth <= 0) {
            return;
        }
        ViewGroup.LayoutParams lp = dropZoomView.getLayoutParams();
        //        lp.width = (int) (dropZoomViewWidth + s);
        lp.width = (int) (dropZoomViewWidth);
        lp.height = (int) (dropZoomViewHeight * ((dropZoomViewWidth + s) / dropZoomViewWidth));
        dropZoomView.setLayoutParams(lp);
    }

    public void setDropZoomScrollViewListener(
            DropZoomScrollViewListener observableScrollViewListener) {
        this.observableScrollViewListener = observableScrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (observableScrollViewListener != null) {
            observableScrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
}
