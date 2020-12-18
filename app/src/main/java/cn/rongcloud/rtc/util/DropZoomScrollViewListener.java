package cn.rongcloud.rtc.util;

/**
 * @author Admin
 * @version $Rev$
 */
public interface DropZoomScrollViewListener {
    /**
     * 上滑监听
     *
     * @param scrollView
     * @param scrollX
     * @param scrollY
     * @param oldScrollX
     * @param oldScrollY
     */
    void onScrollChanged(DropZoomScrollView scrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY);

    /**
     * 下拉松开手指
     *
     * @param distance
     */
    void DropZoomScrollViewUpLitener(int distance);

    /**
     * 下拉中
     *
     * @param distance
     */
    void DropZoomScrollViewMoveLitener(int distance);
}
