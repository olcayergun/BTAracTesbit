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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

public class MakineNoActivity extends AppCompatActivity {
    private static String TAG = "Adaer";
    public static final String EXTRA_MESSAGE = "com.olcayergun.btAracTespit.extra.MAKINE_NO";
    private final String PREFERENCE_FILE_KEY = "URLSOFURL";
    private String sURL;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makine_no);

        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        sURL = sharedPref.getString(PREFERENCE_FILE_KEY, "");
        if (sURL.equals("")) {
            getURL();
        } else {
            getMakineKodular();
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
            editText.setText("http://www.olcayergun.com/makinenolari.html");
            Button BtnSubmit = dialogView.findViewById(R.id.buttonSubmit);
            Button btnCancel = dialogView.findViewById(R.id.buttonCancel);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogBuilder.dismiss();
                    if (sURL.equals("")) {
                        Toast.makeText(getApplicationContext(), "Bu uygulama kapanıyor...", Toast.LENGTH_LONG).show();
                        finish();
                        System.exit(0);
                    }
                }
            });
            BtnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogBuilder.dismiss();
                    sURL = editText.getText().toString();
                    if (sURL.equals("")) {
                        Toast.makeText(getApplicationContext(), "Bu uygulama kapanıyor...", Toast.LENGTH_LONG).show();
                        finish();
                        System.exit(0);
                    } else {
                        getMakineKodular();
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

    private void getMakineKodular() {
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
