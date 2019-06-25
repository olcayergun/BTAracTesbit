package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Depo {
    private static String TAG = "Adaer";
    String id;
    String One;
    String Zero;
    String depokodu;
    String depotanimi;
    String Two;

    public Depo(JSONObject obj) {
        try {
            this.id = obj.getString("id");
            One = obj.getString("1");
            Zero = obj.getString("0");
            this.depokodu = obj.getString("depokodu");
            this.depotanimi = obj.getString("depotanimi");
            Two = obj.getString("2");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}