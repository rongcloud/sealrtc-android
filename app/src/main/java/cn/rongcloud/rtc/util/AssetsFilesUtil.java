package cn.rongcloud.rtc.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cn.rongcloud.rtc.device.utils.FileUtils;
import cn.rongcloud.rtc.utils.FinLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Created by RongCloud on 2016/11/21. */
public class AssetsFilesUtil {
    private static final String TAG = "AssetsFilesUtil";

    /**
     * 获取assets目录下的图片
     *
     * @param context
     * @param fileName
     * @return
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * 获取assets目录下的单个文件
     *
     * @param context
     * @param fileName
     * @return
     */
    public static File getFileFromAssetsFile(Context context, String fileName) {

        String path = "file:///android_asset/" + fileName;
        File file = new File(path);
        return file;
    }

    /**
     * 获取所有文件
     *
     * @param path
     * @return
     */
    public static String[] getfilesFromAssets(Context context, String path) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (String str : files) {
            System.out.print(str);
        }
        return files;
    }

    /**
     * 将assets下的文件放到sd指定目录下
     *
     * @param context 上下文
     * @param assetsPath assets下的路径
     * @param sdCardPath sd卡的路径
     */
    public static void putAssetsToSDCard(Context context, String assetsPath, String sdCardPath) {
        FileOutputStream fos = null;
        InputStream mIs = null;
        try {
            String mString[] = context.getAssets().list(assetsPath);
            if (mString.length == 0) { // 说明assetsPath为空,或者assetsPath是一个文件
                FinLog.d(TAG, "Find assets file: " + assetsPath);
                mIs = context.getAssets().open(assetsPath); // 读取流
                byte[] mByte = new byte[1024];
                int bt = 0;
                File filePath = new File(sdCardPath);
                if (!filePath.exists()) filePath.mkdirs();
                File file =
                        new File(
                                sdCardPath
                                        + File.separator
                                        + assetsPath.substring(assetsPath.lastIndexOf('/')));
                if (!file.exists()) {
                    file.createNewFile(); // 创建文件
                } else {
                    FinLog.d(TAG, "File already exist : " + file.getAbsolutePath());
                    return; // 已经存在直接退出
                }
                fos = new FileOutputStream(file); // 写入流
                while ((bt = mIs.read(mByte)) != -1) { // assets为文件,从文件中读取流
                    fos.write(mByte, 0, bt); // 写入流到文件中
                }
                fos.flush(); // 刷新缓冲区
                FinLog.d(TAG, "File copy successfully！");

            } else { // 当mString长度大于0,说明其为文件夹
                sdCardPath = sdCardPath + File.separator + assetsPath;
                File file = new File(sdCardPath);
                if (!file.exists()) file.mkdirs(); // 在sd下创建目录
                for (String stringFile : mString) { // 进行递归
                    putAssetsToSDCard(
                            context, assetsPath + File.separator + stringFile, sdCardPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeStream(fos);
            FileUtils.closeStream(mIs);
        }
    }
}
