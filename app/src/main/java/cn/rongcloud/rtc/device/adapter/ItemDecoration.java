package cn.rongcloud.rtc.device.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import cn.rongcloud.rtc.R;

public class ItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private int mDividerHeight;
    private int inset;

    /**
     * 使用recycleitem中定义好的颜色
     *
     * @param context
     * @param dividerHeight 分割线高度
     */
    public ItemDecoration(Context context, int dividerHeight) {
        mDivider = ContextCompat.getDrawable(context, R.drawable.recycleitem);
        mDividerHeight = dividerHeight;
    }

    public ItemDecoration(Context context, int inset, int dividerHeight) {
        this.inset = inset;
        mDivider = ContextCompat.getDrawable(context, R.drawable.recycleitem);
        mDividerHeight = dividerHeight;
    }

    /**
     * @param context
     * @param divider 分割线Drawable
     * @param dividerHeight 分割线高度
     */
    public ItemDecoration(Context context, Drawable divider, int dividerHeight) {
        if (divider == null) {
            mDivider = ContextCompat.getDrawable(context, R.drawable.recycleitem);
        } else {
            mDivider = divider;
        }
        mDividerHeight = dividerHeight;
    }

    // 获取分割线尺寸
    @Override
    public void getItemOffsets(
            Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, mDividerHeight);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        // 最后一个item不画分割线
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            //            int bottom = top + mDividerHeight;
            int bottom = top + mDivider.getIntrinsicHeight();
            if (inset > 0) {
                mDivider.setBounds(left + inset, top, right - inset, bottom);
            } else {
                mDivider.setBounds(left, top, right, bottom);
            }
            mDivider.draw(c);
        }
    }
}
