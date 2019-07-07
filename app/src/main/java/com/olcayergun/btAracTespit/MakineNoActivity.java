package com.olcayergun.btAracTespit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class MakineNoActivity extends AppCompatActivity {
    private static String TAG = "Adaer";
    public static final String EXTRA_MESSAGE = "com.olcayergun.btAracTespit.extra.MAKINE_NO";
    public static final String PREFERENCE_FILE_KEY = "PREFERENCESFILE";
    public static final String MAKINE_NOLARI = "MAKINE_NOLARI";
    public static final String SABITLER = "SABITLER";
    public static final String PLAKALR = "PLAKALR";
    public static final String DEPO = "DEPO";
    public static final String URUN = "URUN";
    public static final String KAYIT = "KAYIT";
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makine_no);

        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String sURL = sharedPref.getString(PREFERENCE_FILE_KEY, "");
        if (sURL.equals("")) {
            getURL();
        } else {
            getURLler(sURL);
        }
    }

    private void goNextActivity(String sMakineNo) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, sMakineNo);
        startActivity(intent);
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
                Log.d(TAG, "Settings Menu!!!");
                getURL();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getURL() {
        try {
            final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog, null);

            final EditText editText = dialogView.findViewById(R.id.edt_comment);
            editText.setText("http://www.olcayergun.com/urller.html");
            Button BtnSubmit = dialogView.findViewById(R.id.buttonSubmit);
            Button btnCancel = dialogView.findViewById(R.id.buttonCancel);


            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogBuilder.dismiss();
                }
            });
            BtnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogBuilder.dismiss();
                    String sURL = editText.getText().toString();
                    if (!sURL.equals("")) {
                        getURLler(sURL);
                        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(PREFERENCE_FILE_KEY, sURL);
                        editor.apply();
                    }
                }
            });

            dialogBuilder.setView(dialogView);
            dialogBuilder.setCanceledOnTouchOutside(false);
            dialogBuilder.show();
        } catch (Exception e) {
            Log.e(TAG, ",e");
        }
    }

    private void getURLler(String sMainURL) {
        GetJSON asyncTask = new GetJSON();
        asyncTask.setListener(new GetJSON.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(String[] sa) {
                Log.d(TAG, "getURLler : onAsyncTaskFinished " + sa.length);
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
                    getMakinecKodular();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });
        String[][] URLSFILES = {{sMainURL}, {""}};
        asyncTask.execute(URLSFILES);
    }

    private void getMakinecKodular() {
        GetJSON asyncTask = new GetJSON();
        asyncTask.setListener(new GetJSON.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(String[] sa) {
                Log.d(TAG, "onAsyncTaskFinished " + sa.length);

                SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(PREFERENCE_FILE_KEY, sa[0]);
                editor.apply();

                try {
                    JSONArray jsonArray = new JSONArray(sa[0]);
                    int l = jsonArray.length();
                    String[] items = new String[l];
                    for (int i = 0; i < l; i++) {
                        items[i] = jsonArray.getString(i);
                    }
                    listView.setAdapter(new ArrayAdapter(MakineNoActivity.this, android.R.layout.simple_list_item_1, items));
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    String[] mobileArray = {"Bağlantı yok..."};
                    listView.setAdapter(new ArrayAdapter(MakineNoActivity.this, android.R.layout.simple_list_item_1, mobileArray));
                }
            }
        });
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String sURL = sharedPref.getString("MAKINE_NOLARI", "");
        String[][] URLSFILES = {{sURL}, {""}};
        asyncTask.execute(URLSFILES);
        listView = findViewById(R.id.lvMakineNo);
        String[] mobileArray = {"Makine Noları alınıyor..."};
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mobileArray);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                goNextActivity(item);
            }

        });
    }
}
