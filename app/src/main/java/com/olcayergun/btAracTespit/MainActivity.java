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
import com.olcayergun.btAracTespit.kayitlar.LocationTrack;

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

    private static String[][] URLSFILES = {{"", "", "", ""}, {"urunler.txt", "depolar.txt", "plakalar.txt", "sabitler.txt"}};
    public static String[] SENDFILEURL = {"", "bilgi.txt"};
    private String sMakineNo;

    private ListView listView;
    private ArrayList<String> mDeviceList = new ArrayList<>();
    private TextView tvNTDurum;
    private TextView tvBTDurumu;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Intent intent = getIntent();
        sMakineNo = intent.getStringExtra(MakineNoActivity.EXTRA_MESSAGE);

        SharedPreferences sharedPref = getSharedPreferences(MakineNoActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        URLSFILES[0][0] = sharedPref.getString(MakineNoActivity.URUN, "");
        URLSFILES[0][1] = sharedPref.getString(MakineNoActivity.DEPO, "");
        URLSFILES[0][2] = sharedPref.getString(MakineNoActivity.PLAKALR, "");
        URLSFILES[0][3] = sharedPref.getString(MakineNoActivity.SABITLER, "");
        SENDFILEURL[0] = sharedPref.getString(MakineNoActivity.KAYIT, "");

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
                    sSendData[State] = item;
                    State = 1;
                    state1Process();
                } else if (State == 1) {
                    Urun urun = hmUrun.get(item);
                    sSendData[State] = urun.getSTOK_KODU();
                    State = 2;
                    ArrayList<String> keys = new ArrayList<>(hmDepo.keySet());
                    ArrayAdapter<String> arrayAdapter = fixItemColor(keys);
                    listView.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();
                } else if (State == 2) {
                    Depo depo = hmDepo.get(item);
                    sSendData[State] = depo.getDEPO_KODU();
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
                            double longitude = 0;
                            double latitude = 0;
                            if (locationTrack.canGetLocation()) {
                                longitude = locationTrack.getLongitude();
                                latitude = locationTrack.getLatitude();
                                Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                            } else {
                                locationTrack.showSettingsAlert();
                            }

                            joBilgiler.put("plaka", sSendData[0]);
                            joBilgiler.put("urun", sSendData[1]);
                            joBilgiler.put("depo", sSendData[2]);
                            joBilgiler.put("isSelected", false);
                            joBilgiler.put("isSend", false);
                            joBilgiler.put("zaman", getCurrentTimestamp());
                            joBilgiler.put("lokasyon_x", Double.toString(longitude));
                            joBilgiler.put("lokasyon_y", Double.toString(latitude));
                            joBilgiler.put("lokasyon_z", "zzz");
                            Sabitler sabitler = hmSabitler.get("1");
                            if (sabitler != null) {
                                joBilgiler.put("CIKIS_YERI", sabitler.getCalistigidepo());
                                joBilgiler.put("VARDIYA", sabitler.getVardiyagrubu());
                                joBilgiler.put("VARDIYA_SORUMLUSU", sabitler.getVardiyasorumlusu());
                                joBilgiler.put("URETIM_BOLUMU", sabitler.getUretimbolumu());
                                joBilgiler.put("URETIM_SORUMLUSU", sabitler.getVardiyasorumlusu());
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
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
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
