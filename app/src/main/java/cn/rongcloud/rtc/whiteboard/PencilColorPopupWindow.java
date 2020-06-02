package cn.rongcloud.rtc.whiteboard;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.PopupWindow;
import cn.rongcloud.rtc.R;
import com.herewhite.sdk.Room;

public class PencilColorPopupWindow extends PopupWindow {
    private Context context;

    public PencilColorPopupWindow(Context context, Room room) {
        super();
        this.context = context;
        View view =
                LayoutInflater.from(context).inflate(R.layout.item_white_board_pencil_popup, null);
        GridView gridView = (GridView) view.findViewById(R.id.white_board_pencil_view);
        WhiteBoardGridViewAdapter gridViewAdapter =
                new WhiteBoardGridViewAdapter(context, room, this);
        gridView.setAdapter(gridViewAdapter);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
    }

    public void show(final View view) {
        final View contentView = getContentView();
        int xoff, yoff;
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        if (contentView.getHeight() == 0) {
            int widthMeasureSpec =
                    View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.AT_MOST);
            int heightMeasureSpec =
                    View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.AT_MOST);
            contentView.measure(widthMeasureSpec, heightMeasureSpec);
            xoff = getXoff(view, contentView.getMeasuredWidth());
            yoff = -(view.getHeight() + contentView.getMeasuredHeight());
        } else {
            xoff = getXoff(view, contentView.getWidth());
            yoff = -(view.getHeight() + contentView.getHeight());
        }
        showAsDropDown(view, xoff, yoff);
    }

    private int getXoff(View view, int bgWidth) {
        return (view.getWidth() - bgWidth) / 2
                + view.getContext().getResources().getDimensionPixelSize(R.dimen.popup_window_xoff);
    }
}
