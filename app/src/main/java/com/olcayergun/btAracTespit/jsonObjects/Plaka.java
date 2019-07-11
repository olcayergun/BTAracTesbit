package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Plaka {
    private static String TAG = "Adaer";

    public String getPLAKA() {
        return PLAKA;
    }

    String PLAKA;
    String BLUETOOTHMAC;
    String BLUETOOTHNAME;

    public Plaka(JSONObject obj) {
        try {
            PLAKA = obj.getString("PLAKA");
            BLUETOOTHMAC = obj.getString("BLUETOOTH");
            BLUETOOTHNAME = obj.getString("ISIM");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}