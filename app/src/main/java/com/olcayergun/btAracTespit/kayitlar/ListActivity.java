package com.olcayergun.btAracTespit.kayitlar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.olcayergun.btAracTespit.MainActivity;
import com.olcayergun.btAracTespit.R;
import com.olcayergun.btAracTespit.SendDeviceDetails;
import com.olcayergun.btAracTespit.jsonObjects.Kayit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private static String TAG = "Adaer";

    private ListView lv;
    private ArrayList<Kayit> kayitArrayList;
    private CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ListActivity is starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        lv = findViewById(R.id.lv);
        Button btnselect = findViewById(R.id.select);
        Button btndeselect = findViewById(R.id.deselect);
        Button btnnext = findViewById(R.id.next);
        Button btnsil = findViewById(R.id.sil);

        kayitArrayList = getKayitlar(2);
        customAdapter = new CustomAdapter(this, kayitArrayList);
        lv.setAdapter(customAdapter);

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kayitArrayList = getKayitlar(1);
                customAdapter = new CustomAdapter(ListActivity.this, kayitArrayList);
                lv.setAdapter(customAdapter);
            }
        });
        btndeselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kayitArrayList = getKayitlar(0);
                customAdapter = new CustomAdapter(ListActivity.this, kayitArrayList);
                lv.setAdapter(customAdapter);
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < CustomAdapter.kayitArrayList.size(); i++) {
                    Kayit kayit = CustomAdapter.kayitArrayList.get(i);
                    if (kayit.isSelected()) {
                        kayit.setSend(true);
                        jsonArray.put(kayit.getJSONObject());
                    }
                }
                if (jsonArray.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Gönderilmek için kayıt seçilmedi.", Toast.LENGTH_LONG).show();
                } else {
                    SendDeviceDetails sendDeviceDetails = new SendDeviceDetails();
                    sendDeviceDetails.execute(jsonArray.toString());
                    sendDeviceDetails.setListener(new SendDeviceDetails.AsyncTaskListener() {
                        @Override
                        public void onAsyncTaskFinished(String s) {
                            Log.d(TAG, "onAsyncTaskFinished " + s);
                            JSONArray jsonArray = new JSONArray();
                            localdosyasil(MainActivity.SENDFILEURL[1]);
                            for (int i = 0; i < CustomAdapter.kayitArrayList.size(); i++) {
                                Kayit kayit = CustomAdapter.kayitArrayList.get(i);
                                if (kayit.isSelected()) {
                                    kayit.setSend(true);
                                }
                                jsonArray.put(kayit.getJSONObject());
                            }
                            localdosyaurunyaz(MainActivity.SENDFILEURL[1], jsonArray.toString());
                            customAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Kayıtlar gönderildi ve kayıt güncellendi.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        btnsil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < CustomAdapter.kayitArrayList.size(); i++) {
                    if (CustomAdapter.kayitArrayList.get(i).isSelected()) {
                        Kayit kayit = CustomAdapter.kayitArrayList.get(i);
                        kayit.setSend(true);
                        jsonArray.put(kayit.getJSONObject());
                    }
                }
                if (jsonArray.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Sil için kayıt seçilmedi.", Toast.LENGTH_LONG).show();
                } else {

                }
            }
        });
    }

    private ArrayList<Kayit> getKayitlar(int iSelected) {
        ArrayList<Kayit> list = new ArrayList<>();
        StringBuilder retBuf = new StringBuilder();
        try {
            FileInputStream fileInputStream = getApplication().openFileInput(MainActivity.SENDFILEURL[1]);
            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String lineData = bufferedReader.readLine();
                while (lineData != null) {
                    retBuf.append(lineData);
                    lineData = bufferedReader.readLine();
                }
                if (retBuf.length() > 0) {
                    JSONArray jsonArray = new JSONArray(retBuf.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Kayit kayit = new Kayit(jsonObject);
                        switch (iSelected) {
                            case 0:
                                kayit.setSelected(false);
                                break;
                            case 1:
                                kayit.setSelected(true);
                                break;
                            case 2:
                                kayit.setSelected(kayit.isSend());
                                break;
                        }
                        list.add(kayit);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return list;

    }
    public void localdosyasil(String filename) {
        try {
            File dir = getApplicationContext().getFilesDir();
            File file = new File(dir, filename);
            boolean deleted = file.delete();
            Log.i(TAG, filename.concat(" dosya silme SONUCU: ".concat(Boolean.toString(deleted))));

        } catch (Exception e) {
            Log.i(TAG, filename.concat("dosya silme hatası"), e);
        }
    }

    public void localdosyaurunyaz(String filename, String textToWrite) {
        try {
            localdosyasil(filename);
            FileOutputStream outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(textToWrite.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.i(TAG, filename.concat("dosya yazma hatası"), e);
        }
    }

}