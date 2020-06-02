package cn.rongcloud.rtc.faceunity.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.faceunity.OnFUControlListener;
import cn.rongcloud.rtc.faceunity.ui.widget.CustomButton;
import java.util.HashMap;
import java.util.Map;

/** Created by wangw on 2020/4/1. */
public class FUMenuDialogFrag extends BaseFUDialogFrag implements View.OnClickListener {

    private CustomButton mBtnBeauty;
    private CustomButton mBtnEffect;
    private CustomButton mBtnMakeup;

    private BaseFUDialogFrag mCurrentDialog;
    private FuMenuDialogCallback mMenuDialogCallback;

    public static FUMenuDialogFrag newInstance(OnFUControlListener listener) {
        Bundle args = new Bundle();
        FUMenuDialogFrag fragment = new FUMenuDialogFrag();
        fragment.setArguments(args);
        fragment.setFUControlListener(listener);
        return fragment;
    }

    private Map<String, BaseFUDialogFrag> mFragMap = new HashMap<>();

    @Override
    protected View onInflateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fu_menu, container, false);
        mBtnBeauty = view.findViewById(R.id.fu_beauty);
        mBtnBeauty.setOnClickListener(this);
        mBtnEffect = view.findViewById(R.id.fu_effect);
        mBtnEffect.setOnClickListener(this);
        mBtnMakeup = view.findViewById(R.id.fu_makeup);
        mBtnMakeup.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(final View v) {
        Class<? extends BaseFUDialogFrag> clz = null;
        switch (v.getId()) {
            case R.id.fu_beauty:
                clz = FUBeautyFrag.class;
                break;
            case R.id.fu_makeup:
                clz = FULightMakeupFrag.class;
                break;
            case R.id.fu_effect:
                clz = FUEffectFrag.class;
                break;
        }
        if (clz != null) {
            String name = clz.getName();
            BaseFUDialogFrag frag = mFragMap.get(name);
            Activity activity = getActivity();
            if (frag == null) {
                frag = (BaseFUDialogFrag) DialogFragment.instantiate(activity, name);
                frag.setFUControlListener(mFUControlListener);
                frag.setDialogCallback(
                        new FuDialogCallback() {
                            @Override
                            public void onDismiss(boolean enableFu) {
                                mCurrentDialog = null;
                                ((CustomButton) v).showDot(enableFu);
                                if (mMenuDialogCallback != null) {
                                    mMenuDialogCallback.onDismiss();
                                }
                            }
                        });
                mFragMap.put(name, frag);
            }
            mCurrentDialog = frag;
            mCurrentDialog.show(activity.getFragmentManager(), name);
        }
        dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mMenuDialogCallback != null && mCurrentDialog == null) {
            mMenuDialogCallback.onDismiss();
        }
    }

    public FuMenuDialogCallback getMenuDialogCallback() {
        return mMenuDialogCallback;
    }

    public void setMenuDialogCallback(FuMenuDialogCallback menuDialogCallback) {
        mMenuDialogCallback = menuDialogCallback;
    }

    public interface FuMenuDialogCallback {
        void onDismiss();
    }
}
