/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package cn.rongcloud.rtc.util;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.entity.CMPAddress;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** AppRTCUtils provides helper functions for managing thread safety. */
public final class AppRTCUtils {
    // 自定义的key
    public static final String CUSTOM_CMPKEY = "CUSTOM_CMPAddress_KEY";
    public static final String APPID_KEY = "appid_key";
    // 生产环境key
    public static final String SELECT_KEY = "SELECT_KEY";

    public static final String CER_URL = "cerUrl";

    private AppRTCUtils() {}

    /**
     * NonThreadSafe is a helper class used to help verify that methods of a class are called from
     * the same thread.
     */
    public static class NonThreadSafe {
        private final Long threadId;

        public NonThreadSafe() {
            // Store thread ID of the creating thread.
            threadId = Thread.currentThread().getId();
        }

        /** Checks if the method is called on the valid/creating thread. */
        public boolean calledOnValidThread() {
            return threadId.equals(Thread.currentThread().getId());
        }
    }

    /** Helper method which throws an exception when an assertion has failed. */
    public static void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    /** Helper method for building a string of thread information. */
    public static String getThreadInfo() {
        return "@[name="
                + Thread.currentThread().getName()
                + ", id="
                + Thread.currentThread().getId()
                + "]";
    }

    /** Information about the current build, taken from system properties. */
    public static void logDeviceInfo(String tag) {
        Log.d(
                tag,
                "Android SDK: "
                        + Build.VERSION.SDK_INT
                        + ", "
                        + "Release: "
                        + Build.VERSION.RELEASE
                        + ", "
                        + "Brand: "
                        + Build.BRAND
                        + ", "
                        + "Device: "
                        + Build.DEVICE
                        + ", "
                        + "Id: "
                        + Build.ID
                        + ", "
                        + "Hardware: "
                        + Build.HARDWARE
                        + ", "
                        + "Manufacturer: "
                        + Build.MANUFACTURER
                        + ", "
                        + "Model: "
                        + Build.MODEL
                        + ", "
                        + "Product: "
                        + Build.PRODUCT);
    }

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @throws IOException
     */
    public static InputStream downLoadFromUrl(String urlStr) throws IOException {
        InputStream inputStream = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3 * 1000);
            conn.setRequestProperty(
                    "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            inputStream = conn.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        byte[] getData = readInputStream(inputStream);
        //        //文件保存位置
        //        File saveDir = new File(savePath);
        //        if (!saveDir.exists()) {
        //            saveDir.mkdir();
        //        }
        //        File file = new File(saveDir + File.separator + fileName);
        //        FileOutputStream fos = new FileOutputStream(file);
        //        fos.write(getData);
        //        if (fos != null) {
        //            fos.close();
        //        }
        //        if (inputStream != null) {
        //            inputStream.close();
        //        }
        //        System.out.println("info:" + url + " download success");
        return inputStream;
    }
    // 生产
    // cmpServer=cmp.blinkcloud.cn:443,,snifferServer=cmp.blinkcloud.cn:443
    // tokenServerURL=https://api.blinkcloud.cn:8800/token
    // cerUrl=https://api.blinktalk.online:8081/key/prod/blinktalk.crt

    /**
     * 保存cmp地址
     *
     * @param cmpServer
     * @param serverURl
     * @param type 0:选择的地址 key不一样，1：自定义的地址
     * @return
     */
    public static boolean setCMPAddress(String cmpServer, String serverURl, int type) {
        boolean tag = true;
        try {
            JSONArray mJsonArray = new JSONArray();
            JSONObject mJsonObj = new JSONObject();

            if (TextUtils.isEmpty(cmpServer)) {
                Toast.makeText(
                                Utils.getContext(),
                                R.string.serverCMPcannotbeempty,
                                Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            if (TextUtils.isEmpty(serverURl)) {
                Toast.makeText(
                                Utils.getContext(),
                                R.string.serverTOKENcannotbeempty,
                                Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            if (!isURL(serverURl)) {
                Toast.makeText(
                                Utils.getContext(),
                                R.string.tokenAddressisINcorrect,
                                Toast.LENGTH_SHORT)
                        .show();
                return false;
            }

            try {
                mJsonObj.put("cmpServer", cmpServer);
                mJsonObj.put("serverURl", serverURl);
                mJsonArray.put(mJsonObj);
                String key = CUSTOM_CMPKEY;
                if (type == 0) {
                    key = SELECT_KEY;
                } else if (type == 1) {
                    key = CUSTOM_CMPKEY;
                }
                SessionManager.getInstance().put(key, mJsonArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            tag = false;
        }
        return tag;
    }

    public static CMPAddress getCMPAddress(String key) {
        CMPAddress cmpAddress = null;
        if (SessionManager.getInstance().contains(key)) {
            try {
                String CMPJson = SessionManager.getInstance().getString(key);
                JSONArray jsonArray = new JSONArray(CMPJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    cmpAddress =
                            new CMPAddress(
                                    obj.get("cmpServer").toString(),
                                    obj.get("serverURl").toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cmpAddress;
    }

    public static String getAppID() {
        String appid = "";
        appid = SessionManager.getInstance().getString(APPID_KEY);
        return appid;
    }

    public static boolean setAppID(String appID) {
        if (TextUtils.isEmpty(appID)) {
            SessionManager.getInstance().remove(APPID_KEY);
            return true;
        } else {
            SessionManager.getInstance().put(APPID_KEY, appID);
            return true;
        }
    }

    /**
     * 验证是否是网址
     *
     * @param url
     * @return true：是
     */
    private static boolean isURL(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        Pattern pattern =
                Pattern.compile(
                        "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");
        return pattern.matcher(url).matches();
    }

    /**
     * 验证端口号或ip
     *
     * @param str
     * @return
     */
    private static boolean isCmpServer(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        Pattern pattern =
                Pattern.compile(
                        "^(\\d|[1-9]\\d|1\\d{2}|2[0-5][0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-5][0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-5][0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-5][0-5]):([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-5]{2}[0-3][0-5])$");
        return pattern.matcher(str).matches();
    }
}
