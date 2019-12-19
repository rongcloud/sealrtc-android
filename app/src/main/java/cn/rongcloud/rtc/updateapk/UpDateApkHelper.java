package cn.rongcloud.rtc.updateapk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;


public class UpDateApkHelper {
    private static final String TAG = "UpDateApkHelper";
    private static final String GET_CLIENT_NEW_VERSION = "https://downloads.rongcloud.cn/configuration.json";
    private static final String DOWNLOAD_WEBSITE = "https://www.rongcloud.cn/demo/proxy/sealrtc";
    private Activity activity;

    public UpDateApkHelper(Activity activity) {
        this.activity = activity;
    }

    public void diffVersionFromServer() {
        final Request request = new Request.Builder().url(GET_CLIENT_NEW_VERSION).method(RequestMethod.GET).build();
        HttpClient.getDefault().request(request, new HttpClient.ResultCallback() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject root = new JSONObject(result);
                    if (root.getInt("code") == 200) {
                        JSONObject res = root.getJSONObject("result");
                        JSONObject client = res.getJSONObject("client");
                        JSONObject android = client.getJSONObject("android");
                        final String remoteVersion = android.getString("version");
                        final String downLoadUrl = android.getString("url");
                        String localVersion = getVersionLocal();
                        Log.i(TAG, "onResponse() remote version: " + remoteVersion + " local version: " + localVersion + " downLoadUrl " + downLoadUrl);
                        if (needUpDate(remoteVersion, localVersion)) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showUpdateDialog(remoteVersion, downLoadUrl);
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode) {
                Log.i(TAG, "onFailure() errorCode = " + errorCode);
            }

            @Override
            public void onError(IOException exception) {
                Log.i(TAG, "onFailure() onError = " + exception);
            }
        });
    }

    private String getVersionLocal() {
        PackageManager packageManager = activity.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(activity.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showUpdateDialog(String targetVersion, final String downLoadUrl) {
        final AlertDialog dlg = new AlertDialog.Builder(activity).create();
        dlg.setTitle(String.format(activity.getString(R.string.apk_update_title), targetVersion));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, String.format(activity.getString(R.string.rtc_dialog_ok), targetVersion), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(activity, R.string.downloading_apk, Toast.LENGTH_SHORT).show();
                UpdateService.Builder.create(downLoadUrl)
                        .setStoreDir("update/flag")
                        .setDownloadSuccessNotificationFlag(Notification.DEFAULT_ALL)
                        .setDownloadErrorNotificationFlag(Notification.DEFAULT_ALL)
                        .build(activity);
                dlg.cancel();
            }
        });

        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, String.format(activity.getString(R.string.rtc_dialog_cancel), targetVersion), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dlg.cancel();
            }
        });
        dlg.show();
//        Window window = dlg.getWindow();
//        window.setContentView(R.layout.rtc_dialog_download);
//        window.setBackgroundDrawableResource(android.R.color.transparent);
//        TextView title = (TextView) window.findViewById(R.id.tv_title);
//        title.setText(String.format(activity.getString(R.string.apk_update_title), targetVersion));
//        TextView text = (TextView) window.findViewById(R.id.friendship_content1);
//        TextView photo = (TextView) window.findViewById(R.id.friendship_content2);
//        text.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction("android.intent.action.VIEW");
//                Uri content_url = Uri.parse(DOWNLOAD_WEBSITE);
//                intent.setData(content_url);
//                activity.startActivity(intent);
//                dlg.cancel();
//            }
//        });
//        photo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(activity, R.string.downloading_apk, Toast.LENGTH_SHORT).show();
//                UpdateService.Builder.create(downLoadUrl)
//                        .setStoreDir("update/flag")
//                        .setDownloadSuccessNotificationFlag(Notification.DEFAULT_ALL)
//                        .setDownloadErrorNotificationFlag(Notification.DEFAULT_ALL)
//                        .build(activity);
//
//                dlg.cancel();
//            }
//        });
    }

    /**
     * @param localVersion
     * @param remoteVersion
     * @return
     */
    private boolean needUpDate(String remoteVersion, String localVersion) {
        int hrv = Integer.parseInt(remoteVersion.substring(0, 1));
        int hlv = Integer.parseInt(localVersion.substring(0, 1));
        Log.i(TAG, "hrv = " + hrv + " hlv = " + hlv);

        float lrv = Float.parseFloat(remoteVersion.substring(2));
        float llv = Float.parseFloat(localVersion.substring(2));
        Log.i(TAG, "lrv = " + lrv + " llv = " + llv);
        return hrv > hlv || lrv > llv;
    }
}
