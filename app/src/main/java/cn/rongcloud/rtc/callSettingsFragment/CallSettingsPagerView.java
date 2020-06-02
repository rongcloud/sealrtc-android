package cn.rongcloud.rtc.callSettingsFragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import cn.rongcloud.rtc.R;

public class CallSettingsPagerView extends FrameLayout
        implements CallSettingsPager, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public interface CallSettingsPagerListener {
        void onAudioSwitch(boolean isOn);

        void onUploadClick();
    }

    private SwitchCompat sCallSettingsAudioParams;
    private AppCompatButton sCallSettingsOtherParams;
    private CallSettingsPagerListener listener;
    private boolean isAudioChecked;
    private static final String TAG = CallSettingsPagerView.class.getSimpleName();

    public CallSettingsPagerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallSettingsPagerListener(CallSettingsPagerListener listener) {
        this.listener = listener;
    }

    /** ********************** CallSettingsPager ************************ */
    @Override
    public void showVideoPager() {

        switchPager(R.layout.layout_call_settings_pager_video);
    }

    @Override
    public void showAudioPager() {
        switchPager(R.layout.layout_call_settings_pager_audio);
        initAudio();
    }

    @Override
    public void showOtherPager() {
        switchPager(R.layout.layout_call_settings_pager_other);
        initOther();
    }

    @Override
    public void setAudioOn(boolean isOn) {
        sCallSettingsAudioParams.setChecked(isOn);
    }

    private void switchPager(int viewLayout) {
        removeAllViews();
        View view = LayoutInflater.from(getContext()).inflate(viewLayout, null);
        LayoutParams params =
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(view, params);
    }

    private void initAudio() {
        sCallSettingsAudioParams = findViewById(R.id.s_call_settings_audio_params);
        sCallSettingsAudioParams.setOnCheckedChangeListener(this);
        sCallSettingsAudioParams.setChecked(isAudioChecked);
    }

    private void initVideo() {}

    private void initOther() {
        sCallSettingsOtherParams = findViewById(R.id.s_call_settings_other_params);
        sCallSettingsOtherParams.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        switch (compoundButton.getId()) {
            case R.id.s_call_settings_audio_params:
                {
                    isAudioChecked = b;
                    if (listener != null) {
                        listener.onAudioSwitch(b);
                    }
                    break;
                }

            default:
                {
                    break;
                }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.s_call_settings_other_params:
                {
                    if (listener != null) {
                        listener.onUploadClick();
                    }
                    break;
                }

            default:
                {
                    break;
                }
        }
    }
}
