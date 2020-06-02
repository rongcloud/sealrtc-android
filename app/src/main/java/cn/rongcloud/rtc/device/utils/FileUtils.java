package cn.rongcloud.rtc.device.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;

public class FileUtils {
    private static final String TAG = "FileUtils";
    private static String SDCardPath = "";

    public static void saveText(
            Context appContext, String directory, String fileName, String fileContent) {
        File dir = new File(getAppStorage(appContext) + File.separator + directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File textFile = new File(dir.getAbsolutePath() + File.separator + fileName);
        FileOutputStream outputStream = null;
        OutputStreamWriter streamWriter = null;
        try {
            textFile.createNewFile();
            outputStream = new FileOutputStream(textFile);
            streamWriter = new OutputStreamWriter(outputStream);
            streamWriter.write(fileContent);
            streamWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(outputStream);
            closeStream(streamWriter);
        }
    }

    public static void closeStream(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {

        }
    }

    public static String getAppStorage(Context appContext) {
        return getSDCardStoragePath(appContext) + "/SealRTC";
    }

    /**
     * 获取外置 SD根目录
     *
     * @return
     */
    private static String getSDCardStoragePath(Context appContext) {
        if (TextUtils.isEmpty(SDCardPath)) {
            try {
                StorageManager sm =
                        (StorageManager) appContext.getSystemService(Context.STORAGE_SERVICE);
                Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
                String[] paths = (String[]) getVolumePathsMethod.invoke(sm);
                // second element in paths[] is secondary storage path
                SDCardPath = paths.length <= 1 ? paths[0] : null;
                return SDCardPath;
            } catch (Exception e) {
                Log.e(TAG, "getSecondaryStoragePath() failed", e);
            }
        } else {
            return SDCardPath;
        }
        return null;
    }
}
