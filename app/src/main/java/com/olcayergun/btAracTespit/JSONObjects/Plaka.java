package com.olcayergun.btAracTespit.JSONObjects;

import android.util.Log;

import org.json.JSONObject;

public class Plaka {
    private static String TAG = "Adaer";
    String id;
    String One;
    String Zero;
    String bluetooth;
    String plaka;
    String Two;

    public Plaka(JSONObject obj) {
        try {
            this.id = obj.getString("id");
            One = obj.getString("1");
            Zero = obj.getString("0");
            this.bluetooth = obj.getString("bluetooth");
            this.plaka = obj.getString("plaka");
            Two = obj.getString("2");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
    public String getPlaka() {
        return plaka;
    }
}