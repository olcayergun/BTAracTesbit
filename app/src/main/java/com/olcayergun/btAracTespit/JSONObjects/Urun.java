package com.olcayergun.btAracTespit.JSONObjects;

import android.util.Log;

import org.json.JSONObject;

public class Urun {
    private static String TAG = "Adaer";
    String id;
    String One;
    String Zero;
    String urunkodu;
    String uruntaninimi;
    String Two;

    public Urun(JSONObject obj) {
        try {
            this.id = obj.getString("id");
            One = obj.getString("1");
            Zero = obj.getString("0");
            this.urunkodu = obj.getString("urunkodu");
            this.uruntaninimi = obj.getString("uruntanimi");
            Two = obj.getString("2");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}
