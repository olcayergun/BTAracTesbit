package com.olcayergun.btAracTespit;

import android.util.Log;

/**
 * Utility methods to print log messages
 *
 * @author shraddhas
 */
public class LogUtils {
    private static String TAG = "Adaer";

    /**
     * Send a message to the debug log if debugging is on
     */
    public static void trace(final String msg) {
        final String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        final String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        final String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        final int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

        Log.d(TAG, "#" + lineNumber + " " + className + "." + methodName + "() : " + msg);
    }
}