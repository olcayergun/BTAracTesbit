package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Sabitler {
    private static String TAG = "Adaer";
    private String calistigidepo;
    private String vardiyagrubu;
    private String vardiyasorumlusu;
    private String uretimbolumu;
    private String uretimsorumlusu;
    private String makineno;
    private String durumu;

    public String getCalistigidepo() {
        return calistigidepo;
    }

    public String getVardiyagrubu() {
        return vardiyagrubu;
    }

    public String getVardiyasorumlusu() {
        return vardiyasorumlusu;
    }

    public String getUretimbolumu() {
        return uretimbolumu;
    }

    public String getUretimsorumlusu() {
        return uretimsorumlusu;
    }

    public String getMakineno() {
        return makineno;
    }

    public String getDurumu() {
        return durumu;
    }

    public Sabitler(JSONObject obj) {
        try {
            calistigidepo = obj.getString("calistigidepo");
            vardiyagrubu = obj.getString("vardiyagrubu");
            vardiyasorumlusu = obj.getString("vardiyasorumlusu");
            uretimbolumu = obj.getString("uretimbolumu");
            uretimsorumlusu = obj.getString("uretimsorumlusu");
            makineno = obj.getString("makineno");
            durumu = obj.getString("durumu");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}