package com.olcayergun.btAracTespit;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendDeviceDetails extends AsyncTask<String, Void, String> {
    private static String TAG = "Adaer";
    private String URL = "http://www.olcayergun.com/4.php";
    private AppCompatActivity activity;

    public SendDeviceDetails(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        String data = "";
        try {
            URL url = new URL(URL);

            // Create the urlConnection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");

            // Send the post body
            if (params[0] != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(params[0]);
                writer.flush();
            }

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                data = sb.toString();
            } else {
                data = Integer.toString(statusCode);
            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
        return data;
    }

    @Override
    protected void onPostExecute(final String result) {
        super.onPostExecute(result);
        Log.d(TAG, result); // this is expecting a response code to be sent from your server upon receiving the POST data
        Handler handler = new Handler(activity.getApplicationContext().getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(activity.getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }
}

