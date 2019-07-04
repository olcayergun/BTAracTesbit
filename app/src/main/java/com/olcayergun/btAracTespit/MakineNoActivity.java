package com.olcayergun.btAracTespit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

public class MakineNoActivity extends AppCompatActivity {
    private static String TAG = "Adaer";
    private static String[][] URLSFILES = {{"http://www.olcayergun.com/makinenolari.html"}, {""}};
    public static final String EXTRA_MESSAGE = "com.olcayergun.btAracTespit.extra.MAKINE_NO";

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makine_no);

        GetJSON asyncTask = new GetJSON();
        asyncTask.setListener(new GetJSON.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(String[] sa) {
                Log.d(TAG, "onAsyncTaskFinished " + sa.length);
                try {
                    JSONArray jsonArray = new JSONArray(sa[0]);
                    int l = jsonArray.length();
                    String[] items = new String[l];
                    for (int i = 0; i < l; i++) {
                        items[i] = jsonArray.getString(i);
                    }
                    listView.setAdapter(new ArrayAdapter(MakineNoActivity.this, android.R.layout.simple_list_item_1, items));
                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                }

            }
        });
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

    private void goNextActivity(String sMakineNo) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, sMakineNo);
        startActivity(intent);
    }

}
