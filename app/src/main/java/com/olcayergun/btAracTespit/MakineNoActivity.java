package com.olcayergun.btAracTespit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;

public class MakineNoActivity extends AppCompatActivity {
    private static String TAG = "Adaer";
    private String FILE_MAKINENO = "file_makineno";
    public static final String EXTRA_MESSAGE = "com.olcayergun.btAracTespit.extra.MAKINE_NO";
    public static final String PREFERENCE_FILE_KEY = "PREFERENCESFILE";
    public static final String MAINURL = "MAIN_URL";
    public static final String MAKINE_NOLARI = "MAKINE_NOLARI";
    public static final String SABITLER = "SABITLER";
    public static final String PLAKALR = "PLAKALR";
    public static final String DEPO = "DEPO";
    public static final String URUN = "URUN";
    public static final String KAYIT = "KAYIT";
    ListView listView, listViewOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makine_no);

        listView = findViewById(R.id.lvMakineNo);
        listViewOld = findViewById(R.id.lvOldMakineNo);
        String[] mobileArray = {"Makine Noları alınıyor..."};
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mobileArray);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                String[] sa = item.split(" ");
                Intent intent = new Intent(MakineNoActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_MESSAGE, sa[0]);
                startActivity(intent);
            }
        });

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String sMainURL = SP.getString("mainUrl", "");

        //SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        //sMainURL= sharedPref.getString(MAINURL, "");
        if (sMainURL.equals("")) {
            Toast.makeText(getApplicationContext(), "Ana URL den veriler alınamıyor!!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, MyPreferencesActivity.class);
            startActivity(i);
            //getURL();
        } else {
            getURLler(sMainURL);


            try {
                FileInputStream fileInputStream = getApplication().openFileInput(FILE_MAKINENO);
                String fileData = HelperMethods.readFromFileInputStream(fileInputStream);
                putMakineNoIntoList(fileData);
            } catch (Exception e) {
                Log.e(TAG, "Getting Makine No from files.", e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.makinenomenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSettings:
                Log.d(TAG, "Settings Menu2!!!");
                Intent i = new Intent(this, MyPreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ");
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String sMainURL = SP.getString("mainUrl", "");
        boolean bBTNameUse = SP.getBoolean("bBTNameUse", false);
        Log.d(TAG, "The Main URL : ".concat(sMainURL));
        Log.d(TAG, "BT Name usage : ".concat(Boolean.toString(bBTNameUse)));

        getURLler(sMainURL);
    }

    private void getURLler(String sMainURL) {
        GetJSON asyncTask = new GetJSON();
        asyncTask.setListener(new GetJSON.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(String[] sa) {
                Log.d(TAG, "getURLler : onAsyncTaskFinished " + sa.length);
                if (null == sa[0]) {
                    Log.d(TAG, "sa[0] is null!!!");
                    Toast.makeText(getApplicationContext(), "Ana URL'den bilgiler alınamadı!!!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(sa[0]);
                        String sMAKINE_NOLARI = ((JSONObject) jsonArray.get(0)).getString("url");
                        String sSABITLER = ((JSONObject) jsonArray.get(1)).getString("url");
                        String sPLAKALR = ((JSONObject) jsonArray.get(2)).getString("url");
                        String sDEPO = ((JSONObject) jsonArray.get(3)).getString("url");
                        String sURUN = ((JSONObject) jsonArray.get(4)).getString("url");
                        String sKAYIT = ((JSONObject) jsonArray.get(5)).getString("url");

                        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(MAKINE_NOLARI, sMAKINE_NOLARI);
                        editor.putString(SABITLER, sSABITLER);
                        editor.putString(PLAKALR, sPLAKALR);
                        editor.putString(DEPO, sDEPO);
                        editor.putString(URUN, sURUN);
                        editor.putString(KAYIT, sKAYIT);
                        editor.apply();
                        getMakineNolar();
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            }
        });
        String[][] URLSFILES = {{sMainURL}, {""}};
        asyncTask.execute(URLSFILES);
    }

    private void getMakineNolar() {
        GetJSON asyncTask = new GetJSON();
        asyncTask.setListener(new GetJSON.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(String[] sa) {
                Log.d(TAG, "onAsyncTaskFinished " + sa.length);
                HelperMethods.localdosyaurunyaz(getApplicationContext(), FILE_MAKINENO, sa[0]);
                putMakineNoIntoList(sa[0]);
            }
        });
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String sURL = sharedPref.getString("MAKINE_NOLARI", "");
        String[][] URLSFILES = {{sURL}, {""}};
        asyncTask.execute(URLSFILES);
    }

    private void putMakineNoIntoList(String s) {
        Log.d(TAG, "Putting Makine Nos into List :" + s);
        try {
            JSONArray jsonArray = new JSONArray(s);
            int l = jsonArray.length();
            String[] items = new String[l];
            for (int i = 0; i < l; i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                items[i] = jsonObject.getString("makineno").concat(" ").concat(jsonObject.getString("makineisci"));
            }
            listView.setAdapter(new ArrayAdapter(MakineNoActivity.this, android.R.layout.simple_list_item_1, items));
        } catch (Exception e) {
            Log.e(TAG, "", e);
            String[] mobileArray = {"Makine Noları alınamadı..."};
            listView.setAdapter(new ArrayAdapter(MakineNoActivity.this, android.R.layout.simple_list_item_1, mobileArray));
        }

        String[] sOldMakineNos = new String[5];
        sOldMakineNos[0] = "Eski Makine 0";
        sOldMakineNos[1] = "Eski Makine 1";
        sOldMakineNos[2] = "Eski Makine 2";
        sOldMakineNos[3] = "Eski Makine 3";
        sOldMakineNos[4] = "Eski Makine 4";
        listViewOld.setAdapter(new ArrayAdapter(MakineNoActivity.this, android.R.layout.simple_list_item_1, sOldMakineNos));
    }
}