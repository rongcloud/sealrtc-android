package cn.rongcloud.rtc.callSettingsFragment;

public interface CallSettingsPager {

    void showVideoPager();

    void showAudioPager();

    void showOtherPager();

    void setAudioOn(boolean isOn);
}
