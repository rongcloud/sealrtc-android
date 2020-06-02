package cn.rongcloud.rtc.faceunity.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.rongcloud.rtc.faceunity.ui.control.BeautyControlView;

/** 美颜 Created by wangw on 2020/4/1. */
public class FUBeautyFrag extends BaseFUDialogFrag {

    private BeautyControlView mBeautyControlView;

    @Override
    protected View onInflateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBeautyControlView = new BeautyControlView(getActivity());
        mBeautyControlView.setOnFUControlListener(mFUControlListener);
        mBeautyControlView.onResume();
        mEnableFu = true;
        return mBeautyControlView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBeautyControlView != null) {
            mBeautyControlView.onResume();
        }
    }
}
