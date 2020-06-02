package cn.rongcloud.rtc.faceunity.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.faceunity.entity.Effect;
import cn.rongcloud.rtc.faceunity.entity.EffectEnum;
import cn.rongcloud.rtc.faceunity.ui.adapter.EffectRecyclerAdapter;

/** 贴纸 Created by wangw on 2020/4/1. */
public class FUEffectFrag extends BaseFUDialogFrag {

    private EffectRecyclerAdapter mEffectRecyclerAdapter;
    private Effect mCurrentEffect;

    @Override
    protected View onInflateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fu_effect, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.fu_effect_recycler);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(
                mEffectRecyclerAdapter =
                        new EffectRecyclerAdapter(
                                getActivity(), Effect.EFFECT_TYPE_NORMAL, mFUControlListener));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mEffectRecyclerAdapter.setOnDescriptionChangeListener(
                new EffectRecyclerAdapter.OnDescriptionChangeListener() {
                    @Override
                    public void onDescriptionChangeListener(Effect click) {
                        mCurrentEffect = click;
                        //                showDescription(click.description());
                    }
                });
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mEnableFu =
                mCurrentEffect != null
                        && !TextUtils.equals(
                                mCurrentEffect.bundleName(), EffectEnum.EffectNone.bundleName());
        super.onDismiss(dialog);
    }
}
