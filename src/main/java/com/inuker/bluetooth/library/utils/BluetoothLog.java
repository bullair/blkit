package com.inuker.bluetooth.library.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by dingjikerbo on 2015/12/16.
 */
public class BluetoothLog {

    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "miio-bluetooth";

    public static void i(String msg) {
        if (DEBUG) Log.i(LOG_TAG, msg);
    }

    public static void e(String msg) {
        if (DEBUG) Log.e(LOG_TAG, msg);
    }

    public static void v(String msg) {
        if (DEBUG) Log.v(LOG_TAG, msg);
    }

    public static void d(String msg) {
        if (DEBUG) Log.d(LOG_TAG, msg);
    }

    public static void w(String msg) {
        if (DEBUG) Log.w(LOG_TAG, msg);
    }

    public static void e(Throwable e) {
        if (DEBUG) e(getThrowableString(e));
    }

    public static void w(Throwable e) {
        w(getThrowableString(e));
    }

    private static String getThrowableString(Throwable e) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        while (e != null) {
            e.printStackTrace(printWriter);
            e = e.getCause();
        }

        String text = writer.toString();

        printWriter.close();

        return text;
    }
}
