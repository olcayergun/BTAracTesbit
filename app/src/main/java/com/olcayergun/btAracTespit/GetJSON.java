package com.olcayergun.btAracTespit;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetJSON extends AsyncTask<String[][], Void, String[]> {
    private AsyncTaskListener listener;
    private static String TAG = "Adaer";

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "");
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String[] s) {
        super.onPostExecute(s);
        if (listener != null) {
            listener.onAsyncTaskFinished(s);
        }
    }

    @SuppressLint("WrongThread")
    @Override
    protected String[] doInBackground(String[][]... strings) {
        String[] saResult = null;
        try {
            String[] saURLler = strings[0][0];
            URL url;
            HttpURLConnection con;
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader;
            String json;
            saResult = new String[saURLler.length];
            for (int i = 0; i < saURLler.length; i++) {
                Log.d(TAG, "Async Task Url : ".concat(saURLler[i]));
                url = new URL(saURLler[i]);
                con = (HttpURLConnection) url.openConnection();
                int iResponseCode = con.getResponseCode();
                Log.d(TAG, "Async Task Response Code: : ".concat(Integer.toString(iResponseCode)));
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                sb.setLength(0);
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json).append("\n");
                }
                bufferedReader.close();
                con.disconnect();
                Log.d(TAG, "Sonuç : ".concat(sb.toString().trim()));
                saResult[i] = sb.toString().trim();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception - doInBackground...", e);
        }
        return saResult;
    }

    void setListener(AsyncTaskListener listener) {
        this.listener = listener;
    }

    public interface AsyncTaskListener {
        void onAsyncTaskFinished(String[] string);
    }
}