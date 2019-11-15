package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class IsTipleri {
    private static String TAG = "Adaer";
    String ISTIPI_NO;
    String ISTIPI;
    String ISTIPI_TANIMI;

    public String getISTIPI_NO() {
        return ISTIPI_NO;
    }

    public String getISTIPI() {
        return ISTIPI;
    }

    public String getISTIPI_TANIMI() {
        return ISTIPI_TANIMI;
    }


    public IsTipleri(JSONObject obj) {
        try {
            ISTIPI_NO = obj.getString("istipino");
            ISTIPI = obj.getString("istipi");
            ISTIPI_TANIMI = obj.getString("istipitanimi");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}