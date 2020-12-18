package cn.rongcloud.rtc.util;

import cn.rongcloud.rtc.utils.FinLog;

/**
 * app 层自定义加解密 so 加载类。
 */
public class CustomizedEncryptionUtil {

    private static String TAG = "CustomizedEncryptionUtil";

    //so 名称 custom_frame_crypto 可以自定义
    //自定义 so 名称在文件 /src/main/cpp/CMakeLists.txt 下修改
    static {
        System.loadLibrary("custom_frame_crypto");
    }

    private static class SingletonHolder {

        static CustomizedEncryptionUtil instance = new CustomizedEncryptionUtil();
    }

    public static CustomizedEncryptionUtil getInstance() {
        return SingletonHolder.instance;
    }

    public void init() {
        FinLog.d(TAG, "load customized encryption so.");
    }
}
