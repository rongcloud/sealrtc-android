package cn.rongcloud.rtc.screen;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

/** @Author dengxudong. @Time 2018/1/15. @Description: */
public class ChangeOrientationHandler extends Handler {
    private static final String TAG = "ChangeOrientationHandler";
    private Activity activity;

    public ChangeOrientationHandler(Activity ac) {
        super();
        activity = ac;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == 888) {
            int orientation = msg.arg1;
            if (orientation > 45 && orientation < 135) {
                activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                //                Log.d(TAG, "横屏翻转: ");
            } else if (orientation > 135 && orientation < 225) {
                activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                //                Log.d(TAG, "竖屏翻转: ");
            } else if (orientation > 225 && orientation < 315) {
                activity.setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
                //                Log.d(TAG, "横屏: ");
            } else if ((orientation > 315 && orientation < 360)
                    || (orientation > 0 && orientation < 45)) {
                activity.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
                //                Log.d(TAG, "竖屏: ");
            }
        }
        super.handleMessage(msg);
    }
}
