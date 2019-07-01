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
    private String MAKINE_ADI;

    public String getCIKIS_YERI() {
        return CIKIS_YERI;
    }

    public String getVARDIYA() {
        return VARDIYA;
    }

    public String getVARDIYA_SORUMLUSU() {
        return VARDIYA_SORUMLUSU;
    }

    public String getURETIM_BOLUMU() {
        return URETIM_BOLUMU;
    }

    public String getURETIM_SORUMLUSU() {
        return URETIM_SORUMLUSU;
    }

    public String getMAKINE_ADI() {
        return MAKINE_ADI;
    }

    public Sabitler(JSONObject obj) {
        try {
            CIKIS_YERI = obj.getString("CIKIS_YERI");
            VARDIYA = obj.getString("VARDIYA");
            VARDIYA_SORUMLUSU = obj.getString("VARDIYA_SORUMLUSU");
            URETIM_BOLUMU = obj.getString("URETIM_BOLUMU");
            URETIM_SORUMLUSU = obj.getString("URETIM_SORUMLUSU");
            MAKINE_ADI = obj.getString("MAKINE_ADI");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}