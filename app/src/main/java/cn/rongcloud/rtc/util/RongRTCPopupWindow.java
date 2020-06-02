package cn.rongcloud.rtc.util;

import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.widget.PopupWindow;

/** Created by dengxudong on 2018/7/16. */
public class RongRTCPopupWindow extends PopupWindow {

    public RongRTCPopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
    }

    public RongRTCPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
    }

    @Override
    public void showAsDropDown(View anchor) {
        if (Build.VERSION.SDK_INT >= 24) {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
            setHeight(h);
        }
        super.showAsDropDown(anchor);
    }
}
