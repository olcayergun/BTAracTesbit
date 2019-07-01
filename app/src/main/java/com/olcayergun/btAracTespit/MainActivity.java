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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.olcayergun.btAracTespit.jsonObjects.Depo;
import com.olcayergun.btAracTespit.jsonObjects.Plaka;
import com.olcayergun.btAracTespit.jsonObjects.Sabitler;
import com.olcayergun.btAracTespit.jsonObjects.Urun;
import com.olcayergun.btAracTespit.kayitlar.ListActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private static final int REQ_BT_ENABLE = 1;
    private boolean isMainActivity = true;
    private static String TAG = "Adaer";
    private static String BULUNAMADI = "Bulunamadı";
    BluetoothAdapter mBluetoothAdapter = null;

    private int State = 0; //0:Plaka Seçimi, 1:Ürün Seçimi, 2:Hedef Seçimi

    private HashMap<String, Urun> hmUrun = new HashMap<>();
    private HashMap<String, Depo> hmDepo = new HashMap<>();
    private HashMap<String, Plaka> hmPlaka = new HashMap<>();
    private HashMap<String, Sabitler> hmSabitler = new HashMap<>();
    private String[] sSendData = new String[3];

    private static String[][] URLSFILES = {
            {"http://www.olcayergun.com/urun.html",
                    "http://www.olcayergun.com/depo.html",
                    "http://www.olcayergun.com/plaka.html",
                    "http://www.olcayergun.com/sabitler.html"
            }, {
            "urunler.txt",
            "depolar.txt",
            "plakalar.txt",
            "sabitler.txt"
    }};
    public static String[] SENDFILEURL = {"http://www.olcayergun.com/4.php", "bilgi.txt"};

    private ListView listView;
    private ArrayList<String> mDeviceList = new ArrayList<>();
    private TextView tvNTDurum;
    private TextView tvBTDurumu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                Log.i(TAG, "A click on : ".concat(item).concat(" State:").concat(Integer.toString(State)));
                if (item.startsWith(BULUNAMADI)) {
                    return;
                }

                if (State == 0) {
                    //
                    sSendData[State] = item;
                    State = 1;
                    state1Process();
                } else if (State == 1) {
                    sSendData[State] = item;
                    State = 2;
                    ArrayList<String> keys = new ArrayList<>(hmDepo.keySet());
                    ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
                    listView.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();
                } else if (State == 2) {
                    sSendData[State] = item;
                    State = 0;
                    bilgileriKayitEt();

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

        dosyadanBilgileriAl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuGuncelle:
                Log.d(TAG, "Update info!!!");
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
                if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    Log.i(TAG, "Wifi Etkin");
                    tvNTDurum.setText(R.string.BILGILER_ALINIYOR);
                    webServistenBilgileriAl();
                } else {
                    Toast.makeText(getApplicationContext(), "Bağlantı yok!!!", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.menuKAYITLAR:
                Log.d(TAG, "Showing records!!!");
                Intent myIntent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.menuGeri:
                goBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(btReceiver);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public void onBackPressed() {
        goBack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_BT_ENABLE) {
            if (resultCode == RESULT_OK) {
                startBTDiscovery();
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bletooth aktif edilmedi!!!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }//onActivityResult

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume...");
        isMainActivity = true;
        startBTDiscovery();
    }

    //////////////////////////////////////////////////
    //yardımcı metotlar.
    //////////////////////////////////////////////////

    private void goBack() {
        Log.d(TAG, "Go back!!!");
        State--;
        if (State < 0) {
            State = 0;
        } else if (0 == State) {
            startBTDiscovery();
        } else if (1 == State) {
            state1Process();
        }
    }

    //
    private void state1Process() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        ArrayList<String> keys = new ArrayList<>(hmUrun.keySet());
        ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        while (mBluetoothAdapter.isDiscovering()) {
        }
    }

    //
    private void startBTDiscovery() {
        if (null != mBluetoothAdapter) {
            if (mBluetoothAdapter.isEnabled()) {
                if (!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();
                }
                tvBTDurumu.setText(R.string.CIHAZ_ARANIYOR);
                State = 0;
            } else {
                tvBTDurumu.setText(R.string.BT_ACINIZ);
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQ_BT_ENABLE);
            }
        } else {
            tvBTDurumu.setText(R.string.BT_BULUNAMADI);
        }
    }

    //
    private void bilgileriKayitEt() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        JSONObject joBilgiler = new JSONObject();
                        try {
                            joBilgiler.put("plaka", sSendData[0]);
                            joBilgiler.put("urun", sSendData[1]);
                            joBilgiler.put("depo", sSendData[2]);
                            joBilgiler.put("isSelected", false);
                            joBilgiler.put("isSend", false);
                            joBilgiler.put("zaman", getCurrentTimestamp());
                            Sabitler sabitler = hmSabitler.get("1");
                            if (sabitler != null) {
                                joBilgiler.put("CIKIS_YERI", sabitler.getCIKIS_YERI());
                                joBilgiler.put("VARDIYA", sabitler.getVARDIYA());
                                joBilgiler.put("VARDIYA_SORUMLUSU", sabitler.getVARDIYA_SORUMLUSU());
                                joBilgiler.put("URETIM_BOLUMU", sabitler.getURETIM_BOLUMU());
                                joBilgiler.put("URETIM_SORUMLUSU", sabitler.getVARDIYA_SORUMLUSU());
                                joBilgiler.put("MAKINE_ADI", sabitler.getMAKINE_ADI());
                            }
                            String fileData;
                            JSONArray jsonArray;
                            try {
                                FileInputStream fileInputStream = getApplication().openFileInput(SENDFILEURL[1]);
                                fileData = readFromFileInputStream(fileInputStream);
                                jsonArray = new JSONArray(fileData);
                            } catch (Exception e) {
                                Log.e(TAG, "", e);
                                jsonArray = new JSONArray();
                            }
                            jsonArray.put(joBilgiler);
                            localdosyasil(SENDFILEURL[1]);
                            localdosyaurunyaz(SENDFILEURL[1], jsonArray.toString());
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
                            hmUrun.put((String) jo.get("STOK_ADI"), u);
                        }
                        break;
                    case "depolar.txt":
                        hmDepo.clear();
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject jo = jsonObj.getJSONObject(i);
                            Depo d = new Depo(jo);
                            hmDepo.put((String) jo.get("DEPO_ISMI"), d);
                        }
                        break;

                    case "plakalar.txt":
                        hmPlaka.clear();
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject jo = jsonObj.getJSONObject(i);
                            Plaka p = new Plaka(jo);
                            hmPlaka.put((String) jo.get("BLUETOOTH"), p);
                        }
                        break;
                    case "sabitler.txt":
                        hmSabitler.clear();
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject jo = jsonObj.getJSONObject(i);
                            Sabitler s = new Sabitler(jo);
                            hmSabitler.put("1", s);
                        }
                        break;
                }
            }
            tvNTDurum.setText(R.string.YEREL_BİLGİLER_ALINDI);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            tvNTDurum.setText(R.string.YEREL_BİLGİLER_ALINAMADI);
        }
    }

    private void webServistenBilgileriAl() {
        Log.d(TAG, "onExampleAsyncTaskStarted ");
        tvNTDurum.setText(R.string.AGA_BAGLANILDI);
        GetJSON asyncTask = new GetJSON();
        asyncTask.setListener(new GetJSON.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(String[] sa) {
                Log.d(TAG, "onAsyncTaskFinished " + sa.length);

                if (sa == null) {
                    Log.e(TAG, "Bilgiler alınamadı.");
                    tvNTDurum.setText(R.string.BILGILER_ALINAMADI);
                } else {
                    for (int i = 0; i < sa.length; i++) {
                        localdosyaurunyaz(URLSFILES[1][i], sa[i]);
                    }
                    dosyadanBilgileriAl();
                    tvNTDurum.setText(R.string.BILGILER_ALINDI);
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
                fileInputStream.close();
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return retBuf.toString();
    }

    private String getCurrentTimestamp() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return dateFormat.format(date);
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

    //Boardcaat Reciev"er
    //BT
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "An intent action : ".concat(action != null ? action : ""));
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG, "Discovery is started.");
                tvBTDurumu.setText(R.string.CIHAZ_ARANIYOR);
                mDeviceList.clear();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "Discovery is stoped.");
                tvBTDurumu.setText(R.string.CİHAZ_ARAMASI_DURDU);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "", e);
                }
                if (null != mBluetoothAdapter && !mBluetoothAdapter.isDiscovering() && isMainActivity) {
                    if (State == 0) {
                        mBluetoothAdapter.startDiscovery();
                    }
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (null != device) {
                    String sDeviceName = device.getName() == null ? "null" : device.getName();
                    Log.d(TAG, "Device Name :".concat(sDeviceName));
                    String sDeviceAddress = device.getAddress() == null ? "null" : device.getAddress();
                    Log.d(TAG, "Device Address :".concat(sDeviceAddress));

                    Plaka plaka = hmPlaka.get(device.getAddress());
                    String sPlaka = plaka != null ? plaka.getPLAKA() : null;
                    if (null == sPlaka) {
                        sPlaka = BULUNAMADI.concat("(").concat(device.getAddress()).concat(")");
                    }

                    String s = sPlaka.concat("  ").concat("[").concat(sDeviceName).concat("-").concat(sDeviceAddress).concat("]");
                    Log.i(TAG, "A device is discovered : ".concat(s));
                    if (-1 == mDeviceList.indexOf(sPlaka)) {
                        mDeviceList.add(sPlaka);
                        listView.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, mDeviceList));
                        Log.i(TAG, sPlaka + " listeye ekleniyor");
                    }
                } else {
                    Log.d(TAG, "Device tanımadı.");
                }
            }
        }
    };

}
