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

    //
    private String CIKIS_YERI;
    private String VARDIYA;
    private String VARDIYA_SORUMLUSU;
    private String URETIM_BOLUMU;

    public String getCIKIS_YERI() {
        return CIKIS_YERI;
    }

    public void setCIKIS_YERI(String CIKIS_YERI) {
        this.CIKIS_YERI = CIKIS_YERI;
    }

    public String getVARDIYA() {
        return VARDIYA;
    }

    public void setVARDIYA(String VARDIYA) {
        this.VARDIYA = VARDIYA;
    }

    public String getVARDIYA_SORUMLUSU() {
        return VARDIYA_SORUMLUSU;
    }

    public void setVARDIYA_SORUMLUSU(String VARDIYA_SORUMLUSU) {
        this.VARDIYA_SORUMLUSU = VARDIYA_SORUMLUSU;
    }

    public String getURETIM_BOLUMU() {
        return URETIM_BOLUMU;
    }

    public void setURETIM_BOLUMU(String URETIM_BOLUMU) {
        this.URETIM_BOLUMU = URETIM_BOLUMU;
    }

    public String getURETIM_SORUMLUSU() {
        return URETIM_SORUMLUSU;
    }

    public void setURETIM_SORUMLUSU(String URETIM_SORUMLUSU) {
        this.URETIM_SORUMLUSU = URETIM_SORUMLUSU;
    }

    public String getMAKINE_ADI() {
        return MAKINE_ADI;
    }

    public void setMAKINE_ADI(String MAKINE_ADI) {
        this.MAKINE_ADI = MAKINE_ADI;
    }

    private String URETIM_SORUMLUSU;
    private String MAKINE_ADI;

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
            setCIKIS_YERI(obj.getString("CIKIS_YERI"));
            setVARDIYA(obj.getString("VARDIYA"));
            setVARDIYA_SORUMLUSU(obj.getString("VARDIYA_SORUMLUSU"));
            setURETIM_BOLUMU(obj.getString("URETIM_BOLUMU"));
            setURETIM_SORUMLUSU(obj.getString("URETIM_SORUMLUSU"));
            setMAKINE_ADI(obj.getString("MAKINE_ADI"));
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
            jsonObject.put("CIKIS_YERI", getCIKIS_YERI());
            jsonObject.put("VARDIYA", getVARDIYA());
            jsonObject.put("VARDIYA_SORUMLUSU", getVARDIYA_SORUMLUSU());
            jsonObject.put("URETIM_BOLUMU", getURETIM_BOLUMU());
            jsonObject.put("URETIM_SORUMLUSU", getURETIM_SORUMLUSU());
            jsonObject.put("MAKINE_ADI", getMAKINE_ADI());
        } catch (Exception e) {
            Log.e(TAG, ", e");
        }
        return jsonObject;
    }
}