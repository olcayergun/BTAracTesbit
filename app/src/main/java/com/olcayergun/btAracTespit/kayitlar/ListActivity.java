package com.olcayergun.btAracTespit.kayitlar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.olcayergun.btAracTespit.MainActivity;
import com.olcayergun.btAracTespit.R;
import com.olcayergun.btAracTespit.jsonObjects.Kayit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private static String TAG = "Adaer";

    private ListView lv;
    private ArrayList<Model> modelArrayList;
    private ArrayList<Kayit> kayitArrayList;
    private CustomAdapter customAdapter;
    private String[] animallist = new String[]{"Lion", "Tiger", "Leopard", "Cat"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ListActivity is starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        lv = findViewById(R.id.lv);
        Button btnselect = findViewById(R.id.select);
        Button btndeselect = findViewById(R.id.deselect);
        Button btnnext = findViewById(R.id.next);

       // modelArrayList = getModel(false);
       // customAdapter = new CustomAdapter(this, modelArrayList);
        kayitArrayList = getKayitlar();
        customAdapter = new CustomAdapter(this, kayitArrayList);
        lv.setAdapter(customAdapter);

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //modelArrayList = getModel(true);
                //customAdapter = new CustomAdapter(ListActivity.this, modelArrayList);
                //lv.setAdapter(customAdapter);
            }
        });
        btndeselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //modelArrayList = getModel(false);
                //customAdapter = new CustomAdapter(ListActivity.this, modelArrayList);
                //lv.setAdapter(customAdapter);
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, NextActivity.class);
                startActivity(intent);
            }
        });
    }

    private ArrayList<Model> getModel(boolean isSelect) {
        ArrayList<Model> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Model model = new Model();
            model.setSelected(isSelect);
            model.setAnimal(animallist[i]);
            list.add(model);
        }
        return list;
    }

    private ArrayList<Kayit> getKayitlar() {
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
                        list.add(kayit);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return list;

    }
}