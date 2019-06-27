package com.olcayergun.btAracTespit.jsonObjects;

import android.util.Log;

import org.json.JSONObject;

public class Kayit {
    private static String TAG = "Adaer";
    private boolean isSelected = false;
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

    private void setPlaka(String plaka) {
        this.plaka = plaka;
    }

    public String getUrun() {
        return urun;
    }

    private void setUrun(String urun) {
        this.urun = urun;
    }

    public String getDepo() {
        return depo;
    }

    private void setDepo(String depo) {
        this.depo = depo;
    }

    public String getZaman() {
        return zaman;
    }

    private void setZaman(String zaman) {
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
            setSelected(obj.getBoolean("isSelected"));
            setPlaka(obj.getString("plaka"));
            setUrun(obj.getString("urun"));
            setDepo(obj.getString("depo"));
            setZaman(obj.getString("zaman"));
            setSend(obj.getBoolean("isSend"));
        } catch (Exception e) {
            Log.e(TAG, "JsonToObject", e);
        }
    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("isSelected", isSelected());
            jsonObject.put("plaka", getPlaka());
            jsonObject.put("urun", getUrun());
            jsonObject.put("depo", getDepo());
            jsonObject.put("zaman", getZaman());
            jsonObject.put("isSend", isSend());
        } catch (Exception e) {
            Log.e(TAG, ", e");
        }
        return jsonObject;
    }
}