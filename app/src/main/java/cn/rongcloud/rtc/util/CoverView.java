package cn.rongcloud.rtc.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.rtc.VideoViewManager;
import cn.rongcloud.rtc.utils.FinLog;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;

import cn.rongcloud.rtc.R;
import io.rong.imlib.RongIMClient;

/**
 * @Author DengXuDong.
 * @Time 2018/2/7.
 * @Description:
 */
public class CoverView extends RelativeLayout {

    private static final String TAG = "CoverView";

    public RelativeLayout mRl_Container, rl_CoverBase;
    public TextView tv_userName;
    private GradientDrawable mGroupDrawable;
    public ImageView iv_Header, iv_Audiolevel;
    private Context mContext;
    //    private TextPaint textPaint;
    private String UserId = "", UserName = "RongRTC";
    public ProgressBar progressBar;
    public RongRTCVideoView rongRTCVideoView = null;
    public VideoViewManager.RenderHolder mRenderHolder;
    private View trackTest;
    private View firstFrameTest;
    private View testLayout;
    private TextView showUserMessage;
    public CoverView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public CoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        try {
            LayoutInflater.from(mContext).inflate(R.layout.layout_cover, this, true);
            trackTest = findViewById(R.id.auto_test);
            testLayout = findViewById(R.id.testLayout);
            firstFrameTest = findViewById(R.id.auto_test2);
            mRl_Container = (RelativeLayout) findViewById(R.id.relative_cover);
            rl_CoverBase = (RelativeLayout) findViewById(R.id.rl_CoverBase);
            showUserMessage = (TextView) findViewById(R.id.show_user_message);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            tv_userName = (TextView) findViewById(R.id.tv_UserName);
            tv_userName.setTextColor(Color.WHITE);
            tv_userName.clearFocus();

//            textPaint=tv_userName.getPaint();
//            textPaint.setFakeBoldText(true);
            iv_Header = (ImageView) findViewById(R.id.iv_bg);
            iv_Audiolevel = (ImageView) findViewById(R.id.iv_audiolevel);
            iv_Audiolevel.setImageResource(R.drawable.sound);

            mGroupDrawable = (GradientDrawable) iv_Header.getBackground();

            int Height = SessionManager.getInstance(Utils.getContext()).getInt(Utils.KEY_screeHeight);
            int width = SessionManager.getInstance(Utils.getContext()).getInt(Utils.KEY_screeWidth);
            ViewGroup.LayoutParams para;
            para = iv_Header.getLayoutParams();
            para.height = Height;
            para.width = width;

            iv_Header.setLayoutParams(para);
            iv_Header.setOnClickListener(clickListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAudioLevel() {
        if (null == iv_Audiolevel) {
            return;
        }
        if (iv_Audiolevel.getVisibility() != VISIBLE) {
            iv_Audiolevel.setVisibility(VISIBLE);
        }
    }

    public void closeAudioLevel() {
        if (null == iv_Audiolevel) {
            return;
        }
        if (iv_Audiolevel.getVisibility() != INVISIBLE) {
            iv_Audiolevel.setVisibility(INVISIBLE);
        }
    }

    public void setUserInfo(String name, String id) {

        if (null != tv_userName && !TextUtils.isEmpty(name)) {
            UserName = UserUtils.truncatameUserName(name);
            tv_userName.setText(UserName);
        }

        if (!TextUtils.isEmpty(id)) {
            this.UserId = id;
        }
        setUserType();
    }

    /**
     * 隱藏用戶名等
     */
    private void setCoverTransoarent() {
        try {
            iv_Header.setVisibility(INVISIBLE);
            tv_userName.setVisibility(INVISIBLE);
            closeLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 設置視頻
     *
     * @param videoView
     */
    public void setRongRTCVideoView(RongRTCVideoView videoView) {
        this.rongRTCVideoView = videoView;
        if (null == mRl_Container) {
            return;
        }
        try {
            for (int i = 0; i < mRl_Container.getChildCount(); i++) {
                if (mRl_Container.getChildAt(i) instanceof RongRTCVideoView) {
                    mRl_Container.removeView(mRl_Container.getChildAt(i));
                }
            }
            LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.CENTER_IN_PARENT);
            mRl_Container.addView(rongRTCVideoView, p);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showBlinkVideoView() {
        for (int i = 0; i < mRl_Container.getChildCount(); i++) {
            if (mRl_Container.getChildAt(i) instanceof RongRTCVideoView) {
                mRl_Container.getChildAt(i).setVisibility(VISIBLE);
                if(UserId.equals(RongIMClient.getInstance().getCurrentUserId())){
                    showUserMessage.setText("自己");
                }
                showUserMessage.setVisibility(VISIBLE);
                showUserMessage.bringToFront();
            }
        }
        setCoverTransoarent();
    }

    public void showUserHeader() {
        try {
            iv_Header.setVisibility(VISIBLE);
            tv_userName.setVisibility(VISIBLE);
            for (int i = 0; i < mRl_Container.getChildCount(); i++) {
                if (mRl_Container.getChildAt(i) instanceof RongRTCVideoView) {
                    RongRTCVideoView videoView = (RongRTCVideoView) mRl_Container.getChildAt(i);
                    if (videoView.getVisibility() == VISIBLE) {
                        closeLoading();
                    }
                    videoView.setVisibility(INVISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FinLog.i(TAG, "coverView Error:" + e.getMessage());
        }
    }

    public void showLoading() {
        if (null == progressBar) {
            return;
        }
        progressBar.setVisibility(VISIBLE);
    }

    public void closeLoading() {
        if (null == progressBar) {
            return;
        }
        progressBar.setVisibility(GONE);
    }

    /**
     * 设置用户类型 根据类型设置背景
     */
    private void setUserType() {
//        String colorStr = " ";
//        colorStr = SessionManager.getInstance(Utils.getContext()).getString("color" + UserId);
//        if (!TextUtils.isEmpty(colorStr)) {
//            mGroupDrawable.setColor(Color.parseColor(colorStr));
//            return;
//        }
        if (mGroupDrawable != null) {
//            switch (new Random().nextInt(6)) {
//                case 0:
//                    colorStr = "#0066CC";
//                    break;
//                case 1:
//                    colorStr = "#009900";
//                    break;
//                case 2:
//                    colorStr = "#CC3333";
//                    break;
//                case 3:
//                    colorStr = "#CC9966";
//                    break;
//                case 4:
//                    colorStr = "#FF9900";
//                    break;
//                case 5:
//                    colorStr = "#CC33CC";
//                    break;
//            }
            mGroupDrawable.setColor(Color.BLACK);
        }
    }

    public RongRTCVideoView getRongRTCVideoView() {
        return rongRTCVideoView;
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (rongRTCVideoView != null) {
                    mGroupDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                    rongRTCVideoView.performClick();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //todo delete
    public void setTrackisAdded() {
        Log.i(TAG,"setTrackisAdded");
        trackTest.setVisibility(VISIBLE);
        testLayout.bringToFront();
    }

    //todo delete
    public void setFirstDraw() {
        Log.i(TAG,"setTrackisAdded");
        firstFrameTest.setVisibility(VISIBLE);
        testLayout.bringToFront();
    }
}
