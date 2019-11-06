package com.olcayergun.btAracTespit.kayitlar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.olcayergun.btAracTespit.HelperMethods;
import com.olcayergun.btAracTespit.MainActivity;
import com.olcayergun.btAracTespit.R;
import com.olcayergun.btAracTespit.jsonObjects.Kayit;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;

import static com.olcayergun.btAracTespit.HelperMethods.localdosyaurunyaz;

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
        try {
            kayitArrayList = HelperMethods.getKayitlar(2, getApplication().openFileInput(MainActivity.SENDFILEURL[1]));
            customAdapter = new CustomAdapter(this, kayitArrayList);
            lv.setAdapter(customAdapter);
        } catch (Exception e) {
            Log.i(TAG, "", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.listmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.menuSettings:
                    Log.d(TAG, "Select All!!!");
                    kayitArrayList = HelperMethods.getKayitlar(1, getApplication().openFileInput(MainActivity.SENDFILEURL[1]));
                    customAdapter = new CustomAdapter(ListActivity.this, kayitArrayList);
                    lv.setAdapter(customAdapter);
                    return true;
                case R.id.menuDeselect:
                    Log.d(TAG, "Delesct All!!!");
                    kayitArrayList = HelperMethods.getKayitlar(0, getApplication().openFileInput(MainActivity.SENDFILEURL[1]));
                    customAdapter = new CustomAdapter(ListActivity.this, kayitArrayList);
                    lv.setAdapter(customAdapter);
                    return true;
                case R.id.menuGonder:
                    Log.d(TAG, "Send Button!!!");
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < CustomAdapter.kayitArrayList.size(); i++) {
                        Kayit kayit = CustomAdapter.kayitArrayList.get(i);
                        //if (kayit.isSelected() && !kayit.isSend()) {
                        if (kayit.isSelected()) {
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
                                HelperMethods.localdosyasil(getApplicationContext(), MainActivity.SENDFILEURL[1]);
                                HelperMethods.localdosyaurunyaz(getApplicationContext(), MainActivity.SENDFILEURL[1], jsonArray.toString());
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
            }
        } catch (Exception e) {
            Log.i(TAG, "", e);
        }
        return super.onOptionsItemSelected(item);
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

        HelperMethods.localdosyasil(getApplicationContext(), MainActivity.SENDFILEURL[1]);
        localdosyaurunyaz(getApplicationContext(), MainActivity.SENDFILEURL[1], jsonArray.toString());
        customAdapter.notifyDataSetChanged();
    }


}