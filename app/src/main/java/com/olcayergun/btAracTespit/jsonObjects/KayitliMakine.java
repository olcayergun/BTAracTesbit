package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class KayitliMakine {
    private static String TAG = "Adaer";
    String MAKINE_NO;
    String MAKINE_ISCI;
    String VARDIYE_GRUBU;
    String URETIM_BOLUMU;
    String VARDIYE_SORUMLUSU;
    String URETIM_SORUMLUSU;
    String CALISTIGI_BOLGE;
    String DURUMU;

    public String getMAKINE_NO() {
        return MAKINE_NO;
    }

    public String getMAKINE_ISCI() {
        return MAKINE_ISCI;
    }

    public String getVARDIYE_GRUBU() {
        return VARDIYE_GRUBU;
    }

    public String getURETIM_BOLUMU() {
        return URETIM_BOLUMU;
    }

    public String getVARDIYE_SORUMLUSU() {
        return VARDIYE_SORUMLUSU;
    }

    public String getURETIM_SORUMLUSU() {
        return URETIM_SORUMLUSU;
    }

    public String getCALISTIGI_BOLGE() {
        return CALISTIGI_BOLGE;
    }

    public String getDURUMU() {
        return DURUMU;
    }

    public KayitliMakine(JSONObject obj) {
        try {
            MAKINE_NO = obj.getString("makineno");
            MAKINE_ISCI = obj.getString("makineisci");
            VARDIYE_GRUBU = obj.getString("vardiyagrubu");
            URETIM_BOLUMU = obj.getString("uretimbolumu");
            VARDIYE_SORUMLUSU = obj.getString("vardiyasorumlusu");
            URETIM_SORUMLUSU = obj.getString("uretimsorumlusu");
            CALISTIGI_BOLGE = obj.getString("calistigidepo");
            DURUMU = obj.getString("durumu");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}