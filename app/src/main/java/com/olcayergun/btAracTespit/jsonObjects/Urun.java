package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Urun {
    private static String TAG = "Adaer";
    String STOK_KODU;
    String STOK_ADI;

    public Urun(JSONObject obj) {
        try {
            STOK_KODU = obj.getString("STOK_KODU");
            STOK_ADI = obj.getString("STOK_ADI");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}
