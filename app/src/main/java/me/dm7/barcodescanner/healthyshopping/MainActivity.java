package me.dm7.barcodescanner.healthyshopping;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int ZXING_CAMERA_PERMISSION = 1;
    public static final int REQUEST_CODE_GETSCANCODE = 1014;
    //private TextView textView; // text code view
    public String cody;

    //************************************************************

    private Element[] nets;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;

    Dialog dialog;

    //************************************************************

    //private Class<?> mClss; //#1
    // #1 dla wielu activities
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        //setupToolbar();
        //textView = (TextView) findViewById(R.id.textView2); //text code view

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectWifi();
                Snackbar.make(view, "Skanuje WiFi ...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

/*    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }*/

    // odbierz dane do textView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_GETSCANCODE:
                if (resultCode == Activity.RESULT_OK) {
                    cody = data.getStringExtra("message");
                    try {
                        String[] item = cody.split(";");
                        String ssid = item[0];
                        String pass = item[1];
                        String networkSSID = ssid.split(":")[1];
                        String networkPass = pass.split(":")[1];
                        WifiConfiguration wifiConfig = new WifiConfiguration();
                        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
                        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                        //remember id
                        int netId = wifiManager.addNetwork(wifiConfig);
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(netId, true);
                        wifiManager.reconnect();
                        //textView.setText(cody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    public void launchSimpleActivity(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, SimpleScannerActivity.class);
            startActivityForResult(intent, REQUEST_CODE_GETSCANCODE);
            //startActivity(intent);
        }
    }
    // #1 start
    /*public void launchSimpleActivity(View v) {
        launchActivity(SimpleScannerActivity.class);
    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }*/
    // #1 end

   /* @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(this, mClss);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }*/

    public void detectWifi() {
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            this.wifiManager.startScan();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        this.wifiList = this.wifiManager.getScanResults();

        Toast.makeText(this, "Liczba znalezionych WiFi: " + wifiList.size(), Toast.LENGTH_SHORT).show();

        this.nets = new Element[wifiList.size()];
        for (int i = 0; i < wifiList.size(); i++) {

            String item = wifiList.get(i).toString();
            //Toast.makeText(this, item, Toast.LENGTH_SHORT).show();

            String[] mang_item = item.split(",");
            String item_ssid = mang_item[0];
            String ssid = item_ssid.split(": ")[1];

            nets[i] = new Element(ssid);
        }

        AdapterElements adapterElements = new AdapterElements(this);
        ListView netList = (ListView) findViewById(R.id.lv_ssid);
        netList.setAdapter(adapterElements);
        netList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), nets[i].getTitle() + "", Toast.LENGTH_SHORT).show();

                showdialow(nets[i].getTitle());
            }
        });

        //showdialow();
    }

    public void showdialow(final String wifi) {
        dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Wprowadz hasło do WiFi");
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_layout);

        Button btn_ok = (Button) dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        final EditText edt_Password = (EditText) dialog.findViewById(R.id.edt_password);

        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.cb_show);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    edt_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else {
                    edt_Password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String haslo = edt_Password.getText().toString();

                if (TextUtils.isEmpty(haslo)) {
                    edt_Password.setError("Nie prawidłowe hasło");
                }
                else{
                    Toast.makeText(MainActivity.this, "Nazwa WiFi: " + wifi + " Hasło: " + haslo, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    try {
                        WifiConfiguration wifiConfig = new WifiConfiguration();
                        wifiConfig.SSID = String.format("\"%s\"", wifi);
                        wifiConfig.preSharedKey = String.format("\"%s\"", haslo);

                        //WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                        //remember id
                        int netId = wifiManager.addNetwork(wifiConfig);
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(netId, true);
                        wifiManager.reconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    class AdapterElements extends ArrayAdapter<Object> {
        Activity context;

        public AdapterElements(Activity context) {
            super(context, R.layout.items, nets);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();

            View item = inflater.inflate(R.layout.items, null);
            TextView tvSsid = (TextView) item.findViewById(R.id.tvSSID);

            tvSsid.setText(nets[position].getTitle());

            return item;
        }
    }

    //************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}