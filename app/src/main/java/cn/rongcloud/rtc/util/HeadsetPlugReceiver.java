package cn.rongcloud.rtc.util;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class HeadsetPlugReceiver extends BroadcastReceiver {

    public boolean FIRST_HEADSET_PLUG_RECEIVER = false;
    private static OnHeadsetPlugListener headsetPlugListener = null;

    private boolean mBluetoothConnected;

    public HeadsetPlugReceiver(boolean bluetoothConnected) {
        mBluetoothConnected = bluetoothConnected;
    }

    public boolean isBluetoothConnected() {
        return mBluetoothConnected;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        HeadsetInfo headsetInfo = null;
        // Log.d("HeadsetPlugReceiver", "onReceive: "+intent.getAction()+" , state=
        // "+intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)+" , SCO_AUDIO_STATE=
        // "+intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1));
        if ("android.intent.action.HEADSET_PLUG".equals(action)) {
            int state = -1;
            if (FIRST_HEADSET_PLUG_RECEIVER) {
                if (intent.hasExtra("state")) {
                    state = intent.getIntExtra("state", -1);
                }
                if (state == 1) {
                    headsetInfo = new HeadsetInfo(true, 1);
                } else if (state == 0) {
                    headsetInfo = new HeadsetInfo(false, 1);
                }
            } else {
                FIRST_HEADSET_PLUG_RECEIVER = true;
            }
        } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothProfile.STATE_DISCONNECTED:
                    mBluetoothConnected = false;
                    headsetInfo = new HeadsetInfo(false, 0);
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    mBluetoothConnected = true;
                    headsetInfo = new HeadsetInfo(true, 0);
                    break;
            }
        } else if (AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED.equals(action)) {
            if (headsetPlugListener != null) {
                headsetPlugListener.onNotifySCOAudioStateChange(
                        (intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)));
            }
        }
        //        else if (BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED.equals(action)){
        //            int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
        //            switch (state) {
        //                case BluetoothHeadset.STATE_AUDIO_DISCONNECTED:
        //                    headsetInfo = new HeadsetInfo(false, 0);
        //                    break;
        //                case BluetoothHeadset.STATE_AUDIO_CONNECTED:
        //                    headsetInfo = new HeadsetInfo(true, 0);
        //                    break;
        //            }
        //        }
        if (null != headsetInfo && headsetPlugListener != null) {
            Log.i(
                    "HeadsetPlugReceiver",
                    "isConnected:" + headsetInfo.isConnected() + ",type:" + headsetInfo.getType());
            headsetPlugListener.onNotifyHeadsetState(
                    headsetInfo.isConnected(), headsetInfo.getType());
        }
    }

    public static void setOnHeadsetPlugListener(OnHeadsetPlugListener onHeadsetPlugListener) {
        headsetPlugListener = onHeadsetPlugListener;
    }
}
