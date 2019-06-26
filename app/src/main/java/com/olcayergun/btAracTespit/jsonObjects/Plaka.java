package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Plaka {
    private static String TAG = "Adaer";

    public String getPLAKA() {
        return PLAKA;
    }

    String PLAKA;
    String BLUETOOTH;

    public Plaka(JSONObject obj) {
        try {
            PLAKA = obj.getString("PLAKA");
            BLUETOOTH = obj.getString("BLUETOOTH");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}