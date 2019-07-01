package com.olcayergun.btAracTespit.kayitlar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.olcayergun.btAracTespit.MainActivity;
import com.olcayergun.btAracTespit.R;
import com.olcayergun.btAracTespit.jsonObjects.Kayit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

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
        kayitArrayList = getKayitlar(2);
        customAdapter = new CustomAdapter(this, kayitArrayList);
        lv.setAdapter(customAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.listmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSelect:
                Log.d(TAG, "Select All!!!");
                kayitArrayList = getKayitlar(1);
                customAdapter = new CustomAdapter(ListActivity.this, kayitArrayList);
                lv.setAdapter(customAdapter);
                return true;
            case R.id.menuDeselect:
                Log.d(TAG, "Delesct All!!!");
                kayitArrayList = getKayitlar(0);
                customAdapter = new CustomAdapter(ListActivity.this, kayitArrayList);
                lv.setAdapter(customAdapter);
                return true;
            case R.id.menuGonder:
                Log.d(TAG, "Send Button!!!");
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < CustomAdapter.kayitArrayList.size(); i++) {
                    Kayit kayit = CustomAdapter.kayitArrayList.get(i);
                    if (kayit.isSelected() && !kayit.isSend()) {
                        kayit.setSend(true);
                        jsonArray.put(kayit.getJSONObject());
                    }
                }
                if (jsonArray.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Gönderilmek için kayıt seçilmedi yada \nsadece önceden gönderilmiş kayıtlar seçildi..", Toast.LENGTH_LONG).show();
                } else {
                    SendDeviceDetails sendDeviceDetails = new SendDeviceDetails();
                    sendDeviceDetails.setListener(new SendDeviceDetails.AsyncTaskListener() {
                        @Override
                        public void onAsyncTaskFinished(String s) {
                            Log.d(TAG, "onAsyncTaskFinished " + s);
                            JSONArray jsonArray = new JSONArray();
                            for (int i = 0; i < CustomAdapter.kayitArrayList.size(); i++) {
                                Kayit kayit = CustomAdapter.kayitArrayList.get(i);
                                if (kayit.isSelected()) {
                                    kayit.setSend(true);
                                }
                                jsonArray.put(kayit.getJSONObject());
                            }
                            localdosyasil(MainActivity.SENDFILEURL[1]);
                            localdosyaurunyaz(MainActivity.SENDFILEURL[1], jsonArray.toString());
                            customAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Kayıtlar gönderildi ve kayıt güncellendi.", Toast.LENGTH_LONG).show();
                        }
                    });
                    sendDeviceDetails.execute(jsonArray.toString());
                }
                return true;
            case R.id.menuSil:
                Log.d(TAG, "Delete!!!");
                boolean bGonderilmemisUyarisi = false;
                boolean bSecilmemisUyarisi = true;
                for (int i = 0; i < CustomAdapter.kayitArrayList.size(); i++) {
                    if (CustomAdapter.kayitArrayList.get(i).isSelected()) {
                        bSecilmemisUyarisi = false;
                        Kayit kayit = CustomAdapter.kayitArrayList.get(i);
                        if (!kayit.isSend()) {
                            bGonderilmemisUyarisi = true;
                        }
                    }
                }
                if (bSecilmemisUyarisi) {
                    Log.d(TAG, "No selection...");
                    Toast.makeText(getApplicationContext(), "Sil için kayıt seçilmedi.", Toast.LENGTH_LONG).show();
                } else if (bGonderilmemisUyarisi) {
                    Log.d(TAG, "Some records are not sent yet");
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    delete();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                    builder.setMessage("Gönderilmemiş kayıtlar var. Silinsin mi?").setPositiveButton("Evet", dialogClickListener)
                            .setNegativeButton("Hayır", dialogClickListener).show();

                } else {
                    Log.d(TAG, "Asking for Delete!!!");
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    delete();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                    builder.setMessage("Kayıtlar silinsin mi?").setPositiveButton("Evet", dialogClickListener)
                            .setNegativeButton("Hayır", dialogClickListener).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void delete() {
        Log.d(TAG, "Real Delete!!!");

        JSONArray jsonArray = new JSONArray();
        Iterator<Kayit> it = CustomAdapter.kayitArrayList.iterator();
        while (it.hasNext()) {
            Kayit kayit = it.next(); // must be called before you can call i.remove()
            if (kayit.isSelected()) {
                it.remove();
            } else {
                jsonArray.put(kayit.getJSONObject());
            }
        }

        localdosyasil(MainActivity.SENDFILEURL[1]);
        localdosyaurunyaz(MainActivity.SENDFILEURL[1], jsonArray.toString());
        customAdapter.notifyDataSetChanged();
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