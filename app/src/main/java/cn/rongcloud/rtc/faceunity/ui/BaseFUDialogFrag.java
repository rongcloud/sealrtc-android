package cn.rongcloud.rtc.faceunity.ui;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.faceunity.OnFUControlListener;

/** Created by wangw on 2020/4/1. */
public abstract class BaseFUDialogFrag extends DialogFragment {

    protected View mRootView;
    protected OnFUControlListener mFUControlListener;
    protected boolean mEnableFu;
    private FuDialogCallback mDialogCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = onInflateView(inflater, container, savedInstanceState);
        } else if (mRootView.getParent() != null) {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }
        return mRootView;
    }

    protected abstract View onInflateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null) return;
        Window window = dialog.getWindow();
        window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDialogCallback != null) mDialogCallback.onDismiss(mEnableFu);
    }

    public OnFUControlListener getFUControlListener() {
        return mFUControlListener;
    }

    public void setFUControlListener(OnFUControlListener FUControlListener) {
        mFUControlListener = FUControlListener;
    }

    public void showDescription(int res) {
        if (res == 0) return;
        TextView view = new TextView(getActivity());
        view.setText(res);
        view.setTextColor(Color.WHITE);
        view.setTextSize(33);
        view.setPadding(20, 20, 20, 20);
        view.setBackgroundResource(R.color.blink_transparent);
        Toast toast = new Toast(getActivity());
        toast.setGravity(Gravity.CENTER, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    public FuDialogCallback getDialogCallback() {
        return mDialogCallback;
    }

    public void setDialogCallback(FuDialogCallback dialogCallback) {
        mDialogCallback = dialogCallback;
    }

    interface FuDialogCallback {
        void onDismiss(boolean enableFu);
    }
}
