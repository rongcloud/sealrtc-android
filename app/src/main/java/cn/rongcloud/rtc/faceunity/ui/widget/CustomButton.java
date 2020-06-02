package cn.rongcloud.rtc.faceunity.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.rongcloud.rtc.R;

/** Created by wangw on 2020/4/7. */
public class CustomButton extends ConstraintLayout {

    private ImageView mIvIcon;
    private TextView mTvTitle;
    private View mIvDot;

    public CustomButton(Context context) {
        this(context, null);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.customButton);
        int icon = typedArray.getResourceId(R.styleable.customButton_icon, 0);
        if (icon != 0) {
            setIcon(icon);
        }
        int text = typedArray.getResourceId(R.styleable.customButton_text, 0);
        if (text != 0) {
            setTitle(text);
        } else {
            String str = typedArray.getString(R.styleable.customButton_text);
            if (!TextUtils.isEmpty(str)) mTvTitle.setText(str);
        }
        showDot(typedArray.getBoolean(R.styleable.customButton_showdot, false));
    }

    private void initView() {
        inflate(getContext(), R.layout.view_custom_button, this);
        mIvIcon = findViewById(R.id.iv_icon);
        mTvTitle = findViewById(R.id.tv_title);
        mIvDot = findViewById(R.id.iv_dot);
    }

    public void showDot(boolean isShow) {
        if (mIvDot == null) return;
        mIvDot.setVisibility(isShow ? VISIBLE : GONE);
    }

    public void setIcon(int resId) {
        if (mIvIcon != null) {
            mIvIcon.setImageResource(resId);
        }
    }

    public void setTitle(int resId) {
        if (mTvTitle != null) {
            mTvTitle.setText(resId);
        }
    }
}
