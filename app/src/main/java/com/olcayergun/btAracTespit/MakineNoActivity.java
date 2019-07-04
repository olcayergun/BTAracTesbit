package com.olcayergun.btAracTespit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;


public class MakineNoActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.olcayergun.btAracTespit.extra.MAKINE_NO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makine_no);

        ListView listView = findViewById(R.id.lvMakineNo);
        String[] mobileArray = {"Android", "IPhone", "WindowsMobile", "Blackberry",
                "WebOS", "Ubuntu", "Windows7", "Max OS X"};
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
