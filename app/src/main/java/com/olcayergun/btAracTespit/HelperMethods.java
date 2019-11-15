package com.olcayergun.btAracTespit;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.olcayergun.btAracTespit.jsonObjects.Kayit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.olcayergun.btAracTespit.MakineNoActivity.PREFERENCE_FILE_KEY;

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

    public static String readFromFileInputStream(FileInputStream fileInputStream) {
        StringBuilder retBuf = new StringBuilder();
        try {
            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String lineData = bufferedReader.readLine();
                while (lineData != null) {
                    retBuf.append(lineData);
                    lineData = bufferedReader.readLine();
                }
                fileInputStream.close();
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return retBuf.toString();
    }

    public static <T, C extends Collection<T>> void addWithLimit(C c, T itemToAdd, int limit) {
        List<T> list = new ArrayList<>(c);
        list.add(itemToAdd);
        while (list.size() > limit) {
            list.remove(0);
        }
        c.clear();
        c.addAll(list);
    }

    public static ArrayList<Kayit> getKayitlar(int iSelected, FileInputStream fileInputStream) {
        ArrayList<Kayit> list = new ArrayList<>();
        StringBuilder retBuf = new StringBuilder();
        try {
            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String lineData = bufferedReader.readLine();
                while (lineData != null) {
                    retBuf.append(lineData);
                    lineData = bufferedReader.readLine();
                }
                if (retBuf.length() > 0) {
                    JSONArray jsonArray = new JSONArray(retBuf.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Kayit kayit = new Kayit(jsonObject);
                        switch (iSelected) {
                            case 0:
                                kayit.setSelected(false);
                                break;
                            case 1:
                                kayit.setSelected(true);
                                break;
                            case 2:
                                kayit.setSelected(kayit.isSend());
                                break;
                        }
                        list.add(kayit);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }
}
