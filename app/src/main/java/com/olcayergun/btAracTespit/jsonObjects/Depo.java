package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Depo {
    private static String TAG = "Adaer";
    String DEPO_KODU;
    String DEPO_ISMI;

    public String getDEPO_KODU() {
        return DEPO_KODU;
    }

    public String getDEPO_ISMI() {
        return DEPO_ISMI;
    }

    public Depo(JSONObject obj) {
        try {
            DEPO_KODU = obj.getString("DEPO_KODU");
            DEPO_ISMI = obj.getString("DEPO_ISMI");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}