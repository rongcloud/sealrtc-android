package cn.rongcloud.rtc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;

public class UserVideoView extends FrameLayout {
    private TextView userName;
    private RCRTCVideoView rongRTCVideoView;
    private String userId;
    private RCRTCVideoInputStream inputStream;

    public UserVideoView(@NonNull Context context) {
        this(context, null);
    }

    public UserVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = inflate(context, R.layout.user_container_layout, null);
        LayoutParams params =
                new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(view, params);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        userName = (TextView) findViewById(R.id.user_name);
        rongRTCVideoView = (RCRTCVideoView) findViewById(R.id.video_view);
        userName.setText(userId);
        if (inputStream != null) {
            inputStream.setVideoView(rongRTCVideoView);
        }
    }

    public void setUserId(String userId) {
        this.userId = userId;
        if (userName != null) {
            userName.setText(userId);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setInputStream(RCRTCVideoInputStream inputStream) {
        this.inputStream = inputStream;
    }
}
