package com.olcayergun.btAracTespit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetJSON extends AsyncTask<String[][], Void, String> {

    protected static MainActivity mainActivity;

    GetJSON(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private ExampleAsyncTaskListener listener;
    private static String TAG = "Adaer";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (listener != null) {
            listener.onExampleAsyncTaskFinished(s);
        }
    }

    @SuppressLint("WrongThread")
    @Override
    protected String doInBackground(String[][]... strings) {
        try {
            String[] saURLler = strings[0][0];
            String[] saDosyalar = strings[0][1];
            URL url;
            HttpURLConnection con;
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader;
            String json;
            for (int i = 0; i < saURLler.length; i++) {
                Log.d(TAG, "url : ".concat(saURLler[i]));
                url = new URL(saURLler[i]);
                con = (HttpURLConnection) url.openConnection();
                sb.setLength(0);
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json).append("\n");
                }
                bufferedReader.close();
                con.disconnect();
                Log.d(TAG, "Sonuç : ".concat(sb.toString().trim()));
                localdosyaurunyaz(saDosyalar[i], sb.toString().trim());
            }
        } catch (Exception e) {
            Log.e(TAG, "Notification - doInBackground...", e);
            return "No_Data";
        }
        return "OK";
    }

    void setListener(ExampleAsyncTaskListener listener) {
        this.listener = listener;
    }

    public interface ExampleAsyncTaskListener {
        void onExampleAsyncTaskFinished(String string);
    }

    public static void localdosyaurunyaz(String filename, String textToWrite) {
        try {
            localdosyasil(filename);
            FileOutputStream outputStream = mainActivity.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(textToWrite.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.i(TAG, filename.concat("dosya yazma hatası"), e);
        }
    }

    public static void localdosyasil(String filename) {
        try {
            File dir = mainActivity.getFilesDir();
            File file = new File(dir, filename);
            boolean deleted = file.delete();
            Log.i(TAG, filename.concat(" dosya silme SONUCU: ".concat(Boolean.toString(deleted))));

        } catch (Exception e) {
            Log.i(TAG, filename.concat("dosya silme hatası"), e);
        }
    }
}