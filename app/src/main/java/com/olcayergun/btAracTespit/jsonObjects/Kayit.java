package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Kayit {
    private static String TAG = "Adaer";
    private boolean isSelected;
    private String plaka;
    private String urun;
    private String depo;
    private String zaman;
    private boolean isSend;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getPlaka() {
        return plaka;
    }

    public void setPlaka(String plaka) {
        this.plaka = plaka;
    }

    public String getUrun() {
        return urun;
    }

    public void setUrun(String urun) {
        this.urun = urun;
    }

    public String getDepo() {
        return depo;
    }

    public void setDepo(String depo) {
        this.depo = depo;
    }

    public String getZaman() {
        return zaman;
    }

    public void setZaman(String zaman) {
        this.zaman = zaman;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public Kayit(JSONObject obj) {
        try {
            isSelected = obj.getBoolean("isSelected");
            plaka = obj.getString("plaka");
            urun= obj.getString("urun");
            depo= obj.getString("depo");
            zaman= obj.getString("zaman");
            isSend = obj.getBoolean("isSend");
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }
}