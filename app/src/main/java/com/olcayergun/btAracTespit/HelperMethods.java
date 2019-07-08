package com.olcayergun.btAracTespit;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class HelperMethods {
    private static String TAG = "Adaer";

    public static void localdosyaurunyaz(Context contex, String filename, String textToWrite) {
        try {
            localdosyasil(contex, filename);
            FileOutputStream outputStream = contex.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(textToWrite.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.i(TAG, filename.concat("dosya yazma hatası"), e);
        }
    }

    public static void localdosyasil(Context context, String filename) {
        try {
            File dir = context.getFilesDir();
            File file = new File(dir, filename);
            boolean deleted = file.delete();
            Log.i(TAG, filename.concat(" dosya silme SONUCU: ".concat(Boolean.toString(deleted))));

        } catch (Exception e) {
            Log.i(TAG, filename.concat("dosya silme hatası"), e);
        }
    }

}
