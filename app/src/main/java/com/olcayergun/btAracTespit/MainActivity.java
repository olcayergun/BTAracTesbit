package com.olcayergun.btAracTespit;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.olcayergun.btAracTespit.jsonObjects.Depo;
import com.olcayergun.btAracTespit.jsonObjects.Plaka;
import com.olcayergun.btAracTespit.jsonObjects.Urun;
import com.olcayergun.btAracTespit.kayitlar.ListActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "Adaer";
    BluetoothAdapter mBluetoothAdapter = null;

    private int State = 0; //0:Plaka Seçimi, 1:Ürün Seçimi, 2:Hedef Seçimi

    private HashMap hmUrun = new HashMap();
    private HashMap hmDepo = new HashMap();
    private HashMap hmPlaka = new HashMap();
    private String[] sSendData = new String[3];

    private static String[][] URLSFILES = {{"http://www.olcayergun.com/urun.html", "http://www.olcayergun.com/depo.html", "http://www.olcayergun.com/plaka.html"},
            {"urunler.txt", "depolar.txt", "plakalar.txt"}};
    private static String[] SENDFILEURL = {"", "bilgi.txt"};

    private ListView listView;
    private ArrayList mDeviceList = new ArrayList();
    private Button bGeri;
    private TextView tvNTDurum;
    private TextView tvBTDurumu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /////
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                if (item.equals("Bulunamadı.")) {
                    return;
                }
                Log.i(TAG, "A click on : ".concat(item).concat(" State:").concat(Integer.toString(State)));
                if (State == 0) {
                    sSendData[State] = item;
                    State = 1;
                    state1Process();
                } else if (State == 1) {
                    sSendData[State] = item;
                    State = 2;

                    ArrayList<String> keys = new ArrayList<String>(hmDepo.keySet());
                    ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
                    listView.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();
                } else if (State == 2) {
                    sSendData[State] = item;
                    State = 0;
                    Toast.makeText(MainActivity.this, sSendData[0].concat(sSendData[1]).concat(sSendData[2]), Toast.LENGTH_LONG).show();

                    bilgileriKayıtEt();

                    bGeri.setEnabled(false);
                    if (null != mBluetoothAdapter && !mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.startDiscovery();
                    }
                }
            }
        });

        /////
        tvNTDurum = findViewById(R.id.tv1);
        tvNTDurum.setText("");
        tvBTDurumu = findViewById(R.id.tv2);
        tvBTDurumu.setText("");

        //////
        bGeri = findViewById(R.id.bGeri);
        bGeri.setEnabled(false);
        bGeri.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                State--;
                if (State < 0) {
                    State = 0;
                } else if (0 == State) {
                    startBTDiscovery();
                } else if (1 == State) {
                    state1Process();
                }
            }
        });

        /////
        Button bKayitlar = findViewById(R.id.bKayitlar);
        bKayitlar.setEnabled(true);
        bKayitlar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Showing recods!!!1");
                Intent myIntent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(myIntent);
            }
        });

        ////
        Button bGuncelle = findViewById(R.id.bGüncelle);
        bGuncelle.setEnabled(true);
        bGuncelle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Update info!!!1");
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
                if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    Log.i(TAG, "Wifi Etkin");
                    tvNTDurum.setText("Bilgiler alınıyor.");
                    webServistenBilgileriAl();
                } else {
                    Toast.makeText(getApplicationContext(), "Bağlantı yok!!!", Toast.LENGTH_LONG);
                }
            }
        });

        /////
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startBTDiscovery();

        IntentFilter BTfilter = new IntentFilter();
        BTfilter.addAction(BluetoothDevice.ACTION_FOUND);
        BTfilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        BTfilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btReceiver, BTfilter);
        Log.d(TAG, "Registered BT");

        IntentFilter NetworkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(NetworkChangeReceiver, NetworkFilter);
        Log.d(TAG, "Registereted Network");

        dosyadanBilgileriAl();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(btReceiver);
            unregisterReceiver(NetworkChangeReceiver);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    //////////////////////////////////////////////////
    //yardımcı metotlar.
    //////////////////////////////////////////////////
    //
    private void state1Process() {
        Log.d(TAG, "1");
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        Log.d(TAG, "2");
        while (mBluetoothAdapter.isDiscovering()) {
        }
        Log.d(TAG, "3");

        ArrayList<String> keys = new ArrayList<String>(hmUrun.keySet());
        ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        bGeri.setEnabled(true);
        Log.d(TAG, "4");
    }

    //
    private void startBTDiscovery() {
        if (null != mBluetoothAdapter) {
            if (mBluetoothAdapter.isEnabled()) {
                if (!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();
                }
                tvBTDurumu.setText("Cihaz aranıyor...");
                State = 0;
            } else {
                tvBTDurumu.setText("Bluetooth açınız.");
            }
        } else {
            tvBTDurumu.setText("Bluetooth bulunamadı.");
        }
        bGeri.setEnabled(false);
    }

    //
    private void bilgileriKayıtEt() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        JSONObject postData = new JSONObject();
                        try {
                            postData.put("plaka", sSendData[0]);
                            postData.put("urun", sSendData[1]);
                            postData.put("depo", sSendData[2]);
                            postData.put("isSelected", false);
                            postData.put("isSend", false);
                            postData.put("zaman", getCurrentTimestamp());
                            String fileData = "";
                            try {
                                FileInputStream fileInputStream = getApplication().openFileInput(SENDFILEURL[1]);
                                fileData = readFromFileInputStream(fileInputStream);
                            } catch (FileNotFoundException e) {
                                Log.e(TAG, "", e);
                            }
                            fileData = fileData.concat(postData.toString());
                            GetJSON.localdosyasil(SENDFILEURL[1]);
                            GetJSON.localdosyaurunyaz(SENDFILEURL[1], fileData);

                            if (isNetworkAvailable()) {
                                new SendDeviceDetails(MainActivity.this).execute("http://www.olcayergun.com/4.php", fileData);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String newString = Arrays.toString(sSendData);
        builder.setMessage(newString.concat(" - ").concat("Doğru mu?")).setPositiveButton("Evet", dialogClickListener)
                .setNegativeButton("Hayır", dialogClickListener).show();
    }

    //
    private void localdosyaoku(String filename) {
        try {
            Context ctx = getApplicationContext();
            FileInputStream fileInputStream = ctx.openFileInput(filename);
            String fileData = readFromFileInputStream(fileInputStream);

            if (fileData.length() > 0) {
                Log.d(TAG, "Dosya okuma (" + filename + ")" + fileData);
                JSONArray jsonObj = new JSONArray(fileData);
                switch (filename) {
                    case "urunler.txt":
                        hmUrun.clear();
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject jo = jsonObj.getJSONObject(i);
                            Urun u = new Urun(jo);
                            hmUrun.put(jo.get("STOK_ADI"), u);
                        }
                        break;
                    case "depolar.txt":
                        hmDepo.clear();
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject jo = jsonObj.getJSONObject(i);
                            Depo d = new Depo(jo);
                            hmDepo.put(jo.get("DEPO_ISMI"), d);
                        }
                        break;
                    case "plakalar.txt":
                        hmPlaka.clear();
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject jo = jsonObj.getJSONObject(i);
                            Plaka p = new Plaka(jo);
                            hmPlaka.put(jo.get("BLUETOOTH"), p);
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    private void webServistenBilgileriAl() {
        Log.d(TAG, "onExampleAsyncTaskStarted ");
        tvNTDurum.setText("Ağ bağlanıldı.");
        GetJSON asyncTask = new GetJSON(this);
        asyncTask.setListener(new GetJSON.ExampleAsyncTaskListener() {
            @Override
            public void onExampleAsyncTaskFinished(String s) {
                Log.d(TAG, "onExampleAsyncTaskFinished " + s);

                if (!s.equals("OK")) {
                    Log.e(TAG, "Bilgiler alınamadı.");
                    tvNTDurum.setText("Bilgiler alınamadı.");
                } else {
                    dosyadanBilgileriAl();
                    tvNTDurum.setText("Bilgiler alındı.");
                }
            }
        });
        asyncTask.execute(URLSFILES);
    }

    private void dosyadanBilgileriAl() {
        String[] saDosyalar = URLSFILES[1];
        for (String sDosya : saDosyalar) {
            localdosyaoku(sDosya);
        }
    }

    private ArrayAdapter<String> fixItemColor(ArrayList arrayList) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.BLACK);

                // Generate ListView Item using TextView
                return view;
            }
        };
        return arrayAdapter;
    }

    private String readFromFileInputStream(FileInputStream fileInputStream) {
        StringBuilder retBuf = new StringBuilder();

        try {
            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String lineData = bufferedReader.readLine();
                while (lineData != null) {
                    retBuf.append(lineData);
                    lineData = bufferedReader.readLine();
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return retBuf.toString();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getCurrentTimestamp() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }

    //Boardcaat Reciever
    //NT
    private final BroadcastReceiver NetworkChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getExtras() == null) {
                return;
            }

            String action = intent.getAction();
            Log.i(TAG, "An network intent action : ".concat(action != null ? action : ""));
/*
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                Log.i(TAG, "Wifi Etkin");
                webServistenBilgileriAl();
            }
*/
        }
    };

    //BT
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "An intent action : ".concat(action != null ? action : ""));
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG, "Discovery is started.");
                tvBTDurumu.setText("Cihaz aranıyor...");
                mDeviceList.clear();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "Discovery is stoped.");
                tvBTDurumu.setText("Cihaz araması durdu...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                if (null != mBluetoothAdapter && !mBluetoothAdapter.isDiscovering()) {
                    if (State == 0) {
                        mBluetoothAdapter.startDiscovery();
                    }
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (null != device) {
                    Plaka plaka = (Plaka) hmPlaka.get(device.getAddress());
                    String sPlaka = plaka != null ? plaka.getPLAKA() : null;
                    if (null == sPlaka) {
                        sPlaka = "Bulunamadı.";
                    } else {
                        if (-1 != mDeviceList.indexOf("Bulunamadı.")) {
                            mDeviceList.clear();
                        }
                    }
                    String s = sPlaka.concat("  ").concat("[").concat(device.getName().concat("-").concat(device.getAddress())).concat("]");
                    Log.i(TAG, "A device is discovered : ".concat(s));
                    if (-1 == mDeviceList.indexOf(sPlaka)) {
                        mDeviceList.add(sPlaka);
                        listView.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, mDeviceList));
                    }
                }
            }
        }
    };

}
