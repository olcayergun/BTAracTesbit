package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Sabitler {
    private static String TAG = "Adaer";
    private String CIKIS_YERI;
    private String VARDIYA;
    private String VARDIYA_SORUMLUSU;
    private String URETIM_BOLUMU;
    private String URETIM_SORUMLUSU;
    private String MAKİNE_ADI;

    public Sabitler(JSONObject obj) {
        try {
            CIKIS_YERI = obj.getString("CIKIS_YERI");
            VARDIYA = obj.getString("VARDIYA");
            VARDIYA_SORUMLUSU = obj.getString("VARDIYA_SORUMLUSU");
            URETIM_BOLUMU = obj.getString("URETIM_BOLUMU");
            URETIM_SORUMLUSU = obj.getString("URETIM_SORUMLUSU");
            MAKİNE_ADI = obj.getString("MAKİNE_ADI");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}