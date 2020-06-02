package cn.rongcloud.rtc.faceunity.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.rongcloud.rtc.faceunity.ui.control.LightMakeupControlView;

/** 轻美妆 Created by wangw on 2020/4/1. */
public class FULightMakeupFrag extends BaseFUDialogFrag {

    private LightMakeupControlView mLightMakeupView;

    @Override
    protected View onInflateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLightMakeupView = new LightMakeupControlView(getActivity());
        mLightMakeupView.setOnFUControlListener(mFUControlListener);
        return mLightMakeupView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mEnableFu = mLightMakeupView.isEnableLightMakeup();
        super.onDismiss(dialog);
    }
}
