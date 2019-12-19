package cn.rongcloud.rtc.device;

import android.app.Activity;
import android.os.Bundle;
import cn.rongcloud.rtc.R;

public class DeviceBaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_base);
    }
}
