/*
 *  Copyright 2013 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package cn.rongcloud.rtc;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Singleton helper: install a default unhandled exception handler which shows an informative dialog
 * and kills the app. Useful for apps whose error-handling consists of throwing RuntimeExceptions.
 * NOTE: almost always more useful to Thread.setDefaultUncaughtExceptionHandler() rather than
 * Thread.setUncaughtExceptionHandler(), to apply to background threads as well.
 */
public class UnhandledExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "AppRTCDemoActivity";
    private final Activity activity;

    private static final String ROOT_PATH =
            Environment.getExternalStorageDirectory().toString()
                    + File.separator
                    + "BlinkDemo"
                    + File.separator;
    private static final String CRASH_FOLDER = ROOT_PATH + "crash" + File.separator;

    public UnhandledExceptionHandler(final Activity activity) {
        this.activity = activity;
    }

    public void uncaughtException(Thread unusedThread, final Throwable e) {
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        String title = "Fatal error: " + getTopLevelCauseMessage(e);
                        String msg = getRecursiveStackTrace(e);
                        TextView errorView = new TextView(activity);
                        errorView.setText(msg);
                        errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                        ScrollView scrollingContainer = new ScrollView(activity);
                        scrollingContainer.addView(errorView);
                        Log.e(TAG, title + "\n\n" + msg);
                        DialogInterface.OnClickListener listener =
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        System.exit(1);
                                    }
                                };
                        //                AlertDialog.Builder builder =
                        //                        new AlertDialog.Builder(activity);
                        //                builder
                        //                        .setTitle(title)
                        //                        .setView(scrollingContainer)
                        //                        .setPositiveButton("Exit", listener).show();

                        writeToFile(msg);
                    }
                });
    }

    private void writeToFile(String message) {
        if (message == null) return;
        prepareFolder();

        String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".crash";
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(CRASH_FOLDER + fileName));
            out.write(message.getBytes());
            out.flush();
        } catch (Exception e) {

        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void prepareFolder() {
        mkdir(ROOT_PATH);
        mkdir(CRASH_FOLDER);
    }

    private static void mkdir(String dir) {
        File dirFile = new File(dir);
        if (dirFile.exists() && dirFile.isDirectory()) return;

        dirFile.mkdirs();
    }

    // Returns the Message attached to the original Cause of |t|.
    private static String getTopLevelCauseMessage(Throwable t) {
        Throwable topLevelCause = t;
        while (topLevelCause.getCause() != null) {
            topLevelCause = topLevelCause.getCause();
        }
        return topLevelCause.getMessage();
    }

    // Returns a human-readable String of the stacktrace in |t|, recursively
    // through all Causes that led to |t|.
    private static String getRecursiveStackTrace(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
