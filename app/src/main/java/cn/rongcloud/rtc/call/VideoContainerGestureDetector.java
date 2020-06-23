package cn.rongcloud.rtc.call;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/** 计算出双击， 单击，缩放，平移事件, 边界检测。 */
public class VideoContainerGestureDetector
        implements ScaleGestureDetector.OnScaleGestureListener,
                GestureDetector.OnGestureListener,
                GestureDetector.OnDoubleTapListener {

    private static final String TAG = "VideoContainerGesture";
    /** 用于存放矩阵的9个值 */
    private final float[] matrixValues = new float[9];

    private ScaleGestureDetector mScaleGesture;
    private GestureDetector mGesture;
    private Context context;
    private Matrix matrix;
    private float preScale = 1.0f;
    private Rect boxMargin;
    private int boxWidth;
    private int boxHeight;
    private boolean checkBox;
    private float minScale = 0f;
    private float maxScale = 8f;
    private float maxControlScale = 4f;
    private float minControlScale = 1.0f;
    private boolean gestureAble = false;
    //    private PointF scaleTrans = new PointF();
    /** 是否为滑动手势 */
    private boolean scrolling;

    private boolean isScaling;

    private boolean scaleGestureAble;
    private boolean translateGestureAble;
    private ViewContainerGestureListener gestureListener;

    public VideoContainerGestureDetector(Context context) {

        init(context);
    }

    private void init(Context context) {
        matrix = new Matrix();
        boxMargin = new Rect();
        //        scaleTrans.x = 0f;
        //        scaleTrans.y = 0f;
        scaleGestureAble = true;
        checkBox = true;
        scrolling = false;
        translateGestureAble = true;
        mScaleGesture = new ScaleGestureDetector(context, this);
        mGesture = new GestureDetector(context, this);
        mGesture.setOnDoubleTapListener(this);
        gestureAble = true;
    }

    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public void setMaxControlScale(float maxControlScale) {
        this.maxControlScale = maxControlScale;
    }

    public void setMinControlScale(float minControlScale) {
        this.minControlScale = minControlScale;
    }

    public void setGestureAble(boolean gestureAble) {
        this.gestureAble = gestureAble;
    }

    public void reset() {
        matrix.reset();
        updateTranslateAction();
        updateScaleAction();
    }

    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, parsEvent("onTouchEvent: ", event));
        if (event.getAction() == MotionEvent.ACTION_POINTER_2_DOWN) {
            //            Log.d(TAG, "onTouchEvent: scale is true");
            isScaling = true;
        }

        if (event.getAction() == MotionEvent.ACTION_POINTER_UP
            || event.getAction() == MotionEvent.ACTION_POINTER_2_UP) {
            isScaling = false;
        }
        if (translateGestureAble && mGesture != null) {
            mGesture.onTouchEvent(event);
        }

        if (scaleGestureAble && mScaleGesture != null) {
            mScaleGesture.onTouchEvent(event);
        }

        checkScrollBounds(event);

        return true;
    }

    public void setGestureListener(ViewContainerGestureListener gestureListener) {
        this.gestureListener = gestureListener;
    }

    public void updateTranslate(float tranlateX, float tranlateY) {
        //        Log.d(TAG, "updateTranslate: x " + tranlateX + ", y" + tranlateY);
        this.matrix.postTranslate(tranlateX, tranlateY);
        updateTranslateAction();
    }

    private void updateTranslateAction() {

        //        RectF rect = getMatrixRectF();
        //        if (gestureEvents != null) {
        //            float width = rect.width();
        //            float height = rect.height();

        float translateX = getMatrixTranslateX();
        float translateY = getMatrixTranslateY();
        //        Log.d(
        //            TAG,
        //            "updateTranslateAction: translateX: " + translateX + ", translateY " +
        // translateY);

        //        float transPercentX = translateX / width;
        //        float transPercentY = translateY / height;

        if (gestureListener != null) {
            gestureListener.onTranslate(translateX, translateY);
        }
    }

    private void updateScaleAction() {
        float scale = getMatrixScale();

        //        Log.d(TAG, "updateScaleAction: " + scale);

        if (gestureListener != null) {
            gestureListener.onScale(scale, scale);
        }
    }

    private void checkScrollBounds(MotionEvent motionEvent) {
        if (isScrollingEnd(motionEvent) && !isScaling) {
            //            Log.d(TAG, "checkScrollBounds: end");
            scrolling = false;
            if (gestureListener != null) {
                gestureListener.onTranlateEnd();
            }
            //            checkMatrixBounds();
        }
    }

    /**
     * 移动时，进行边界判断，主要判断宽或高大于屏幕的。
     *
     * <p>通过此方法可确保画面的
     */
    private void checkMatrixBounds() {

        if (!checkBox) {
            return;
        }

        RectF rect = getMatrixRectF();
        // todo 计算边界
        float deltaX = 0, deltaY = 0;

        // 判断移动或缩放后，图片显示是否超出屏幕边界
        Rect screenBox = getScreenBox();
        int broadwiseScroll = checkBroadwiseScroll(screenBox, rect);
        int endwiseScroll = checkEndwiseScroll(screenBox, rect);
        matrix.postTranslate(broadwiseScroll, endwiseScroll);

        //        Log.d(
        //            TAG,
        //            "checkMatrixBounds: broadwiseScroll "
        //                + broadwiseScroll
        //                + ", endwiseScroll "
        //                + endwiseScroll);
        updateTranslateAction();
    }

    private Rect getScreenBox() {
        Rect screenBox = new Rect();
        screenBox.top = 0 - boxMargin.top;
        screenBox.left = 0 - boxMargin.left;
        screenBox.right = boxWidth + boxMargin.right;
        screenBox.bottom = boxHeight + boxMargin.bottom;
        return screenBox;
    }

    private int checkBroadwiseScroll(Rect screenBox, RectF box) {
        if (screenBox.width() <= 0) {
            return 0;
        }
        //        Log.d(
        //            TAG,
        //            "checkBroadwiseScroll: screenBox left "
        //                + screenBox.left
        //                + " screenBox right "
        //                + screenBox.right
        //                + " box left "
        //                + box.left
        //                + " box right "
        //                + box.right);
        if (screenBox.left > box.left && screenBox.right > box.right) {

            return screenBox.left - (int) box.left;
        }

        if (screenBox.left < box.left && screenBox.right < box.right) {
            return screenBox.right - (int) box.right;
        }

        return 0;
    }

    private int checkEndwiseScroll(Rect screenBox, RectF box) {

        if (screenBox.height() <= 0) {
            return 0;
        }
        //        Log.d(
        //            TAG,
        //            "checkBroadwiseScroll: screenBox top "
        //                + screenBox.top
        //                + " screenBox bottom "
        //                + screenBox.bottom
        //                + " box top "
        //                + box.top
        //                + " box bottom "
        //                + box.bottom);

        if (screenBox.top > box.top && screenBox.bottom > box.top) {
            return screenBox.top - (int) box.top;
        }

        if (screenBox.top < box.top && screenBox.bottom < box.bottom) {
            return screenBox.bottom - (int) box.bottom;
        }

        return 0;
    }

    /**
     * 获取view在当前matrix中映射的位置
     *
     * @return
     */
    private RectF getMatrixRectF() {

        RectF rect = new RectF();
        rect.set(0, 0, boxWidth, boxHeight);
        matrix.mapRect(rect);

        //        Log.d(
        //            TAG,
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

    /**
     * 获取当前屏幕横向移动
     *
     * @return
     */
    private final float getMatrixTranslateX() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MTRANS_X];
    }

    /**
     * 获取当前屏幕纵向移动
     *
     * @return
     */
    private final float getMatrixTranslateY() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MTRANS_Y];
    }

    private boolean isScrollingEnd(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (scrolling) {
                return true;
            }
        }
        return false;
    }

    private final float getMatrixScale() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X]; // todo 现在只按x方向缩放，需要按x*x + y*y 开放做参数。
    }

    private void checkControlScale() {
        if (getMatrixScale() > maxControlScale) {
            float reserveScale = maxControlScale / getMatrixScale();
            matrix.postScale(reserveScale, reserveScale, 0, 0);
            updateScaleAction();
        } else if (getMatrixScale() < minControlScale) {
            float reserveScale = minControlScale / getMatrixScale();
            matrix.postScale(reserveScale, reserveScale, 0, 0);
            updateScaleAction();
        }
    }

    /** OnGestureListener ********************* */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(
            MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
//        Log.d(TAG, "onScroll:  scaling " + isScaling);
        if (isScaling || getMatrixScale() == 1.0f) {        // 不允许拖动
            return false;
        }
        float scrollDx = distanceX * -1.0f;
        float scrollDy = distanceY * -1.0f; // distance 为小值减大值

        scrolling = true;

        //        Log.d(TAG, "onScroll: distanceX : " + distanceX + " , distanceY : " + distanceY);
        //        Log.d(TAG, "onScroll: scrollDx : " + scrollDx + " , scrollDy : " + scrollDy);

        matrix.postTranslate(scrollDx, scrollDy);
        updateTranslateAction();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {}

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

        return false;
    }

    /** * OnScaleGestureListener *************************** */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        float scale = scaleFactor / this.preScale; // 由于matrix的post方法累计scale,需要去除上一次的scale.
        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();
//        Log.d(
//            TAG,
//            "OnScaleGestureListener onScale: "
//                + scale
//                + " focusX : "
//                + focusX
//                + " focusY:"
//                + focusY
//                + " factorScale : "
//                + scaleFactor);

        if ((scale > 1 && getMatrixScale() >= maxScale)
                || (scale < 1 && getMatrixScale() <= minScale)) { // 判断放缩极限值
            return false;
        }

        float matrixCenterX = getMatrixRectF().width() / 2;

        matrix.postScale(scale, scale, 0, 0); // 以中心点缩放

        // 如果超过极限值就回复到极限值
        if (getMatrixScale() > maxScale) {
            float scaleDx = maxScale / getMatrixScale();
            matrix.postScale(scaleDx, scaleDx, 0, 0);
        }

        if (getMatrixScale() <= minScale) {
            float scaleDx = minScale / getMatrixScale();
            matrix.postScale(scaleDx, scaleDx, 0, 0);
        }

        this.preScale = scaleFactor;
        updateScaleAction();
        updateTranslateAction(); // 由于matrix有缩放中心点，导致缩放的同时会有位置移动
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        this.preScale = 1.0f;
        isScaling = true;
        //        scaleTrans.x = getMatrixTranslateX();
        //        scaleTrans.y = getMatrixTranslateY();
//        Log.d(TAG, "onScaleBegin: scale is " + isScaling);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        checkControlScale();
        isScaling = false;
        if (gestureListener != null) {
            gestureListener.onScaleEnd();
        }
//        Log.d(TAG, "onScaleEnd: scale is " + isScaling);
    }

    /**
     * ******************************
     * OnDoubleTapListener********************************************
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        if (gestureListener != null) {
            gestureListener.onSingleTap();
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {

        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        if (gestureListener != null) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                gestureListener.onDoubleTap();
            }
        }
        return false;
    }

    private String parsEvent(String method, MotionEvent event) {
        String parseStr = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                {
                    parseStr = "ACTION_DOWN";
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                {
                    parseStr = "ACTION_MOVE";

                    break;
                }
            case MotionEvent.ACTION_UP:
                {
                    parseStr = "ACTION_UP";
                    break;
                }
            case MotionEvent.ACTION_CANCEL:
                {
                    parseStr = "ACTION_CANCEL";
                    break;
                }
            case MotionEvent.ACTION_SCROLL:
                {
                    parseStr = "ACTION_SCROLL";
                    break;
                }

            case MotionEvent.ACTION_POINTER_UP:
                {
                    parseStr = "ACTION_POINTER_UP";
                    break;
                }
            case MotionEvent.ACTION_POINTER_2_DOWN:
                {
                    parseStr = "ACTION_POINTER_2_DOWN";
                    break;
                }
            default:
                {
                }
        }

        return method + event.getAction() + " -- " + parseStr;
    }

    public interface ViewContainerGestureListener {

        void onScale(float scaleX, float scaleY);

        void onScaleEnd();

        void onTranslate(float translateX, float translateY);

        void onTranlateEnd();

        void onSingleTap();

        void onDoubleTap();
    }
}
