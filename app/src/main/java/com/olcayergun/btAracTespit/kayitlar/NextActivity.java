package com.olcayergun.btAracTespit.kayitlar;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.olcayergun.btAracTespit.R;

public class NextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        TextView tv = findViewById(R.id.tv);
/*
        for (int i = 0; i < CustomAdapter.modelArrayList.size(); i++){
            if(CustomAdapter.modelArrayList.get(i).getSelected()) {
                tv.setText(tv.getText() + " " + CustomAdapter.modelArrayList.get(i).getAnimal());
            }
        }
*/    }
}