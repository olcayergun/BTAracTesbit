package com.olcayergun.btAracTespit;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.olcayergun.btAracTespit.jsonObjects.Depo;
import com.olcayergun.btAracTespit.jsonObjects.Kayit;
import com.olcayergun.btAracTespit.jsonObjects.IsTipleri;
import com.olcayergun.btAracTespit.jsonObjects.Plaka;
import com.olcayergun.btAracTespit.jsonObjects.Sabitler;
import com.olcayergun.btAracTespit.jsonObjects.Urun;
import com.olcayergun.btAracTespit.kayitlar.CustomAdapter;
import com.olcayergun.btAracTespit.kayitlar.ListActivity;
import com.olcayergun.btAracTespit.kayitlar.LocationTrack;
import com.olcayergun.btAracTespit.kayitlar.SendDeviceDetails;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.olcayergun.btAracTespit.MakineNoActivity.PREFERENCE_FILE_KEY;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_BT_ENABLE = 1;
    private boolean isMainActivity = true;
    private static String TAG = "Adaer";
    private static String BULUNAMADI = "Bulunamadı";
    BluetoothAdapter mBluetoothAdapter = null;

    private int State = 0; //0:Plaka Seçimi, 1:Ürün Seçimi, 2:Hedef Seçimi

    private HashMap<String, Urun> hmUrun = new HashMap<>();
    private HashMap<String, Depo> hmDepo = new HashMap<>();
    private HashMap<String, Plaka> hmPlakaMac = new HashMap<>();
    private HashMap<String, Plaka> hmPlakaName = new HashMap<>();
    private HashMap<String, Sabitler> hmSabitler = new HashMap<>();
    private HashMap<String, IsTipleri> hmIsTipleri = new HashMap<>();
    private String[] sSendData = new String[3];

    private static String[][] URLSFILES = {{"", "", "", "", ""}, {"urunler.txt", "depolar.txt", "plakalar.txt", "sabitler.txt", "istipleri.txt"}};
    public static String[] SENDFILEURL = {"", "bilgi.txt", ""};
    private String sMakineNo;

    private ListView listView, listViewOld;
    private ArrayList<String> mDeviceList = new ArrayList<>();
    private TextView tvNTDurum;
    private TextView tvBTDurumu;
    LinearLayout linearLayoutOld;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<String>();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    private boolean bBTName = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList permissions = new ArrayList<>();
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        locationTrack = new LocationTrack(MainActivity.this);
        startService(new Intent(this, LocationTrack.class));

        Intent intent = getIntent();
        sMakineNo = intent.getStringExtra(MakineNoActivity.EXTRA_MESSAGE);

        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        URLSFILES[0][0] = sharedPref.getString(MakineNoActivity.URUN, "");
        URLSFILES[0][1] = sharedPref.getString(MakineNoActivity.DEPO, "");
        URLSFILES[0][2] = sharedPref.getString(MakineNoActivity.PLAKALR, "");
        URLSFILES[0][3] = sharedPref.getString(MakineNoActivity.SABITLER, "");
        URLSFILES[0][4] = sharedPref.getString(MakineNoActivity.ISTIPLERI, "");
        SENDFILEURL[0] = sharedPref.getString(MakineNoActivity.KAYIT, "");
        SENDFILEURL[2] = sharedPref.getString(MakineNoActivity.IKINCIKAYIT, "");

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        bBTName = SP.getBoolean("bBTNameUse", false);

        ///
        linearLayoutOld = findViewById(R.id.llKayitli);

        listViewOld = findViewById(R.id.lvOldlistView);
        listViewOld.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                Log.i(TAG, "A click on : ".concat(item).concat(" State:").concat(Integer.toString(State)));

                if (State == 0) {
                    int i = item.indexOf('_');
                    if (-1 != i) {
                        sSendData[State] = item.substring(0, i);
                    } else {
                        sSendData[State] = item;
                    }
                    State = 1;
                    state1Process();
                }
            }
        });


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
                    int i = item.indexOf('_');
                    sSendData[State] = item.substring(0, i);
                    State = 1;
                    state1Process();
                } else if (State == 1) {
                    Urun urun = hmUrun.get(item);
                    sSendData[State] = urun != null ? urun.getSTOK_KODU() : null;
                    State = 2;
                    ArrayList<String> keys = new ArrayList<>(hmDepo.keySet());
                    ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
                    listView.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();
                } else if (State == 2) {
                    Depo depo = hmDepo.get(item);
                    sSendData[State] = depo != null ? depo.getDEPO_KODU() : null;
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
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

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
        locationTrack.stopListener();
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_BT_ENABLE) {
            if (resultCode == RESULT_OK) {
                startBTDiscovery();
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth aktif edilmedi!!!", Toast.LENGTH_LONG).show();
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
            super.onBackPressed();
        } else if (0 == State) {
            ArrayList<String> keys = new ArrayList<>();
            ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
            listView.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();

            listViewOld.setVisibility(View.VISIBLE);
            linearLayoutOld.setVisibility(View.VISIBLE);
            startBTDiscovery();
        } else if (1 == State) {
            state1Process();
        }
    }

    //
    private void state1Process() {
        //if (mBluetoothAdapter.isDiscovering()) {
        //    mBluetoothAdapter.cancelDiscovery();
        //}
        ArrayList<String> keys = new ArrayList<>(hmUrun.keySet());
        ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        listViewOld.setVisibility(View.GONE);
        linearLayoutOld.setVisibility(View.GONE);
        //while (mBluetoothAdapter.isDiscovering()) {
        //}
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
                        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        boolean bSendDate = SP.getBoolean("sendDataOnConn", false);
                        //
                        JSONObject joBilgiler = new JSONObject();
                        try {
                            double longitude = 0;
                            double latitude = 0;
                            double altitude = 0;
                            if (locationTrack.canGetLocation()) {
                                longitude = locationTrack.getLongitude();
                                latitude = locationTrack.getLatitude();
                                altitude = locationTrack.getAltitude();
                                Toast.makeText(getApplicationContext(), "Longitude:" + longitude + "\nLatitude:" + latitude + "\nAltitude:" + altitude, Toast.LENGTH_SHORT).show();
                            } else {
                                locationTrack.showSettingsAlert();
                            }

                            joBilgiler.put("plaka", sSendData[0]);
                            joBilgiler.put("urun", sSendData[1]);
                            joBilgiler.put("depo", sSendData[2]);
                            joBilgiler.put("isSelected", bSendDate);
                            joBilgiler.put("isSend", false);
                            joBilgiler.put("zaman", getCurrentTimestamp());
                            joBilgiler.put("lokasyon_x", Double.toString(longitude));
                            joBilgiler.put("lokasyon_y", Double.toString(latitude));
                            joBilgiler.put("lokasyon_z", Double.toString(altitude));
                            Sabitler sabitler = hmSabitler.get("1");
                            if (sabitler != null) {
                                joBilgiler.put("CIKIS_YERI", sabitler.getCalistigidepo());
                                joBilgiler.put("VARDIYA", sabitler.getVardiyagrubu());
                                joBilgiler.put("VARDIYA_SORUMLUSU", sabitler.getVardiyasorumlusu());
                                joBilgiler.put("URETIM_BOLUMU", sabitler.getUretimbolumu());
                                joBilgiler.put("URETIM_SORUMLUSU", sabitler.getUretimsorumlusu());
                                joBilgiler.put("MAKINE_ADI", sMakineNo);
                            }
                            String fileData;
                            JSONArray jsonArray;
                            try {
                                FileInputStream fileInputStream = getApplication().openFileInput(SENDFILEURL[1]);
                                fileData = HelperMethods.readFromFileInputStream(fileInputStream);
                                jsonArray = new JSONArray(fileData);
                            } catch (Exception e) {
                                Log.e(TAG, "", e);
                                jsonArray = new JSONArray();
                            }
                            jsonArray.put(joBilgiler);
                            HelperMethods.localdosyasil(getApplicationContext(), SENDFILEURL[1]);
                            HelperMethods.localdosyaurunyaz(getApplicationContext(), SENDFILEURL[1], jsonArray.toString());

                            //Send automatic Send
                            if (bSendDate) {
                                SendDeviceDetails sendDeviceDetails = new SendDeviceDetails();
                                sendDeviceDetails.setListener(new SendDeviceDetails.AsyncTaskListener() {
                                    @Override
                                    public void onAsyncTaskFinished(String s) {
                                        Log.d(TAG, "onAsyncTaskFinished " + s);
                                        try {
                                            ArrayList<Kayit> kayitArrayList = HelperMethods.getKayitlar(2, getApplicationContext().openFileInput(SENDFILEURL[1]));
                                            CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), kayitArrayList);

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
                                        } catch (Exception e) {
                                            Log.e(TAG, "", e);
                                        }
                                    }

                                });
                                sendDeviceDetails.execute(new String[] {jsonArray.toString(), MainActivity.SENDFILEURL[2]});
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                        }

                        ArrayList<String> keys = new ArrayList<>();
                        ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
                        listView.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();

                        listViewOld.setVisibility(View.VISIBLE);
                        linearLayoutOld.setVisibility(View.VISIBLE);

                        State = 0;

                        startBTDiscovery();
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
            String fileData = HelperMethods.readFromFileInputStream(fileInputStream);

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
                        hmPlakaMac.clear();
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject jo = jsonObj.getJSONObject(i);
                            Plaka p = new Plaka(jo);
                            hmPlakaMac.put(jo.getString("BLUETOOTH"), p);
                            hmPlakaName.put(jo.getString("ISIM"), p);
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
                    case "istipleri.txt":
                        hmIsTipleri.clear();
                        for (int i = 0; i < jsonObj.length(); i++) {
                            JSONObject jo = jsonObj.getJSONObject(i);
                            IsTipleri it = new IsTipleri(jo);
                            hmIsTipleri.put(jo.getString("istipino"), it);
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

                if (null == sa) {
                    Log.e(TAG, "Bilgiler alınamadı.");
                    tvNTDurum.setText(R.string.BILGILER_ALINAMADI);
                } else {
                    for (int i = 0; i < sa.length; i++) {
                        HelperMethods.localdosyaurunyaz(getApplicationContext(), URLSFILES[1][i], sa[i]);
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

        //Update Old Machines to List
        String[] sOldMakineNos = null;

        /////Get Old Machhines from URL
        sOldMakineNos = new String[hmPlakaName.size()];
        int i = 0;
        for (Plaka plaka : hmPlakaName.values()) {
            sOldMakineNos[i++] = plaka.getPLAKA();
        }
        Arrays.sort(sOldMakineNos);

        listViewOld.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, sOldMakineNos));
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

    private String getCurrentTimestamp() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    //Boardcaat Recivier
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
                    try {
                        String sDeviceName = device.getName() == null ? "null" : device.getName();
                        Log.d(TAG, "Device Name :".concat(sDeviceName));
                        String sDeviceAddress = device.getAddress() == null ? "null" : device.getAddress();
                        Log.d(TAG, "Device Address :".concat(sDeviceAddress));

                        Plaka plaka = bBTName ? hmPlakaName.get(device.getName()) : hmPlakaMac.get(device.getAddress());
                        String sPlaka;
                        if (null == plaka) {
                            return;
                        } else {
                            sPlaka = plaka.getPLAKA().concat("_").concat(device.getName());
                        }

                        if (-1 == mDeviceList.indexOf(sPlaka)) {
                            mDeviceList.add(sPlaka);
                            listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, mDeviceList));
                            Log.i(TAG, sPlaka + " listeye ekleniyor");
                        }
                        String s = sPlaka.concat("  ").concat("[").concat(sDeviceName).concat("-").concat(sDeviceAddress).concat("]");
                        Log.i(TAG, "A device is discovered : ".concat(s));
                    } catch (Exception e) {
                        Log.e(TAG, "While handling new device...", e);
                    }
                } else {
                    Log.d(TAG, "Device tanımadı.");
                }
            }
        }
    };

    ///////////////////////////////
    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
