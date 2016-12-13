package com.msme.iitism.productmanager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.msme.iitism.productmanager.bluetooth.BTDiscovery;
import com.msme.iitism.productmanager.bluetooth.BluetoothComm;
import com.msme.iitism.productmanager.bluetooth.BluetoothPair;
import com.msme.iitism.productmanager.bluetooth.GlobalPool;
import com.msme.iitism.productmanager.bluetooth.PrintActivity;
import com.prowesspride.api.Printer_GEN;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;

public class Print extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Switch enable_bt;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVERY_BT = 2;

    private Hashtable<String, String> mhtDeviceInfo = null;
    private LinearLayout mllSelectedDeviceInfo;
    private Button mbtPair;
    private Button mbtConnect;
    private TextView mtvDeviceInfo;
    private boolean mblBonded = false;
    private BluetoothDevice mbDevice = null;
    private GlobalPool mGp = null;
    private List<Product> mProductList;
    private DataBaseHandler mDbHandler;

    private static byte bFontstyle;
    private String[] entertext_font = {
            "FONT LARGE NORMAL", "FONT LARGE BOLD",
            "FONT SMALL NORMAL", "FONT SMALL BOLD"
    };

    private Spinner mSpinner;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public static final int DEVICE_NOTCONNECTED = -100;
    private Printer_GEN ptrGen;
    private int iRetVal;
    private static final String TAG = "Print Activity";
    private String mData = null;
    private String space = "";

    private ConnectionManager mcm;

    Handler ptrHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(Print.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        enable_bt = (Switch) findViewById(R.id.enable_bt_switch);

        mllSelectedDeviceInfo = (LinearLayout) findViewById(R.id.selected_device);
        mtvDeviceInfo = (TextView) findViewById(R.id.device_info);
        mbtPair = (Button) findViewById(R.id.btPair);
        mbtConnect = (Button) findViewById(R.id.btConnect);
        this.mGp = new GlobalPool();

        mhtDeviceInfo = new Hashtable<String, String>();

        if(mBluetoothAdapter.isEnabled())
            enable_bt.setChecked(true);

        mcm = new ConnectionManager(getApplicationContext());
        String address = mcm.getDevice();
        if(address != "")
        {
            Log.i("DEVICEMAC",address);
            mbDevice = mBluetoothAdapter.getRemoteDevice(address);

            mllSelectedDeviceInfo.setVisibility(View.VISIBLE);
            mtvDeviceInfo.setText(mbDevice.getName());
            if(mbDevice.getBondState() == BluetoothDevice.BOND_BONDED) {

            }
            else{
                mbtPair.setVisibility(View.VISIBLE);
                mbtPair.setEnabled(true);
            }
        }
        else
        {
            Log.i("DEVICEMAC","empty");
        }

        enable_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                startBluetoothDeviceTask(compoundButton,b);
            }
        });

        mSpinner = (Spinner) findViewById(R.id.fontSizeSpinner);
        mSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> arradGFontsty = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, entertext_font);
        arradGFontsty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(arradGFontsty);
    }

    public void startBluetoothDeviceTask(CompoundButton compoundButton, boolean b)
    {
        if(b)
        {
            if(mBluetoothAdapter!=null)
            {
                if(!mBluetoothAdapter.isEnabled())
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                }
            }
        }
        else
        {
            mBluetoothAdapter.disable();
            compoundButton.setChecked(false);
        }
    }

    public void scanBluetoothDeviceTask(View v)
    {
        if(mBluetoothAdapter==null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter.isEnabled())
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                     || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},1);
            else {
                Intent btDiscoveryIntent = new Intent(Print.this, BTDiscovery.class);
                startActivityForResult(btDiscoveryIntent, REQUEST_DISCOVERY_BT);
            }
        }
        else
            showToast("Please turn on Bluetooth...");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    Intent btDiscoveryIntent = new Intent(Print.this, BTDiscovery.class);
                    startActivityForResult(btDiscoveryIntent, REQUEST_DISCOVERY_BT);
                }
                else
                    showToast("Please enable the Location Permission for discovering the Bluetooth Devices");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==REQUEST_ENABLE_BT)
        {
            if (resultCode==RESULT_OK)
            {
                enable_bt.setChecked(true);
            }
            else if(resultCode==RESULT_CANCELED)
            {
                enable_bt.setChecked(false);
            }
        }
        else if(requestCode == REQUEST_DISCOVERY_BT)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if(mhtDeviceInfo==null)
                    mhtDeviceInfo = new Hashtable<String, String>();

                this.mhtDeviceInfo.put("MAC", data.getStringExtra("MAC"));
                this.mhtDeviceInfo.put("NAME", data.getStringExtra("NAME"));
                this.mhtDeviceInfo.put("RSSI", data.getStringExtra("RSSI"));
                this.mhtDeviceInfo.put("COD", data.getStringExtra("COD"));
                this.mhtDeviceInfo.put("BOND", data.getStringExtra("BOND"));

                this.mllSelectedDeviceInfo.setVisibility(View.VISIBLE);

                this.mtvDeviceInfo.setText(this.mhtDeviceInfo.get("NAME"));

                if(!this.mhtDeviceInfo.get("BOND").equals(getString(R.string.actDiscovery_bond_nothing)))
                {
                    this.mbtConnect.setEnabled(true);
                    this.mbtConnect.setVisibility(View.VISIBLE);
                    this.mbtPair.setVisibility(View.GONE);

                    mcm.setDevice(mhtDeviceInfo.get("MAC"));
                }
                else
                {
                    mbDevice = mBluetoothAdapter.getRemoteDevice(this.mhtDeviceInfo.get("MAC"));

                    this.mbtPair.setEnabled(true);
                    this.mbtPair.setVisibility(View.VISIBLE);
                    this.mbtConnect.setVisibility(View.GONE);

                    mcm.setDevice(mbDevice.getAddress());
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickBtnPair(View v)
    {
        new PairTask().execute(this.mhtDeviceInfo.get("MAC"));
        mbtPair.setEnabled(false);
    }

    public void onClickBtnConnect(View v)
    {
        new ConnSocketTask().execute(this.mhtDeviceInfo.get("MAC"));
        mbtConnect.setEnabled(false);
    }

    BroadcastReceiver _mPairingRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = null;
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mblBonded = (device.getBondState() == BluetoothDevice.BOND_BONDED);
            }
        }
    };

    private class PairTask extends AsyncTask<String,String,Integer>
    {
        private static final int RET_BOND_OK = 0x00;
        private static final int RET_BOND_FAIL = 0x01;

        private static final int iTimeOut = 1000*15;

        @Override
        protected void onPreExecute() {
            showToast("Pairing...");
            IntentFilter pairingRequestFilter = new IntentFilter(BluetoothPair.PAIRING_REQUEST);
            IntentFilter bondStateChangedFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

            registerReceiver(_mPairingRequest, pairingRequestFilter);
            registerReceiver(_mPairingRequest, bondStateChangedFilter);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            final int iStepTime = 150;
            int iWait = iTimeOut;

            try{
                mbDevice = mBluetoothAdapter.getRemoteDevice(strings[0]);
                BluetoothPair.createBond(mbDevice);
                mblBonded = false;
                //storing device state
                //mcm.setBonded(true);
                //mcm.setDevice(strings[0]);
            } catch (Exception e) {
                Log.d(getString(R.string.app_name), "create bond failed");
                e.printStackTrace();
                return RET_BOND_FAIL;
            }

            while(!mblBonded && iWait>0)
            {
                SystemClock.sleep(iStepTime);
                iWait-=iStepTime;
            }

            if(iWait>0)
            {
                Log.e("Application", "create Bond successful! RET_BOND_OK ");
            }
            else {
                Log.e("Application", "create Bond failed! RET-BOND_FAIL");
            }

            return (iWait>0) ? RET_BOND_OK : RET_BOND_FAIL;
        }

        @Override
        protected void onPostExecute(Integer result) {
            unregisterReceiver(_mPairingRequest);

            if(RET_BOND_OK == result)
            {
                showToast("Bluetooth Bonding Successful!");

                mbtPair.setVisibility(View.GONE);
                mbtConnect.setVisibility(View.VISIBLE);
                mhtDeviceInfo.put("BOND", getString(R.string.actDiscovery_bond_bonded));
                //mcm.setDevice(mbDevice.getAddress());
            }
            else {
                showToast("Bluetooth Bonding Failed!");

                try{
                    BluetoothPair.removeBond(mbDevice);
                } catch (Exception e) {
                    Log.d(getString(R.string.app_name), "RemoveBond Failed!");
                    e.printStackTrace();
                }

                mbtPair.setEnabled(true);
                //mcm.setBonded(false);
                //mcm.setConnected(false);
                //new ConnSocketTask().execute(mbDevice.getAddress());
            }
        }
    }

    private class ConnSocketTask extends AsyncTask<String,String,Integer> {
        private static final int CONN_FAIL = 0x01;
        private static final int CONN_SUCCESS = 0x02;
        private ProgressDialog mpd = null;

        @Override
        protected void onPreExecute() {
            this.mpd = new ProgressDialog(Print.this);
            this.mpd.setMessage("Connecting to Device...");
            this.mpd.setCancelable(false);
            this.mpd.setCanceledOnTouchOutside(false);
            this.mpd.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if(mGp.createConn(strings[0]))
            {
                return CONN_SUCCESS;
            }
            else
                return CONN_FAIL;
        }

        @Override
        protected void onPostExecute(Integer result) {
            this.mpd.dismiss();

            if(CONN_SUCCESS == result)
            {
                mbtConnect.setVisibility(View.GONE);
                //mcm.setConnected(true);
                showToast("Bluetooth Connection Established Successfuly!");
            }
            else {
                mbtConnect.setEnabled(true);
                //mcm.setConnected(false);
                showToast("Bluetooth Connection Failed!");
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long arg3){
        switch (pos) {
            case 0:
                bFontstyle = Printer_GEN.FONT_LARGE_NORMAL;//(byte) 0x01;
                space = "    ";
                break;
            case 1:
                bFontstyle = Printer_GEN.FONT_LARGE_BOLD;//(byte) 0x02;
                space = "    ";
                break;
            case 2:
                bFontstyle = Printer_GEN.FONT_SMALL_NORMAL;//(byte) 0x03;
                space = "                    ";
                break;
            case 3:
                bFontstyle = Printer_GEN.FONT_SMALL_BOLD;//(byte) 0x04;
                space = "                    ";
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void OnClickPrint(View v)
    {
        mDbHandler = new DataBaseHandler(Print.this);
        mProductList = mDbHandler.getAllProducts();

        print(mProductList);
    }

    public void print(List<Product> productList){

        try{
            InputStream input = BluetoothComm.misIn;
            OutputStream outstream = BluetoothComm.mosOut;
            ptrGen = new Printer_GEN(BTDiscovery.impressSetUp, outstream, input);

            mData = "Product ID"+space+"Quantity\n";
            for (Product product:productList) {
                String s = "";
                String id = product.getProduct_id();
                int n = id.length();

                for(int i=0;i<(11+space.length()-n);i++)
                    s+=" ";

                mData += "\n" + product.getProduct_id()+ s + String.valueOf(product.getCount())+ "\n";
            }

            new PrintDataTask().execute(mData);

        }catch(Exception e){
            Log.d(TAG,"......<<<>>>..... ptrGen initialization exception");
            showToast("Printer_GEN Initialization Exception");
        }

    }

    public class PrintDataTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                try {
                    ptrGen.iFlushBuf();
                }catch(Exception e){
                    Log.i(TAG,"Data not flushed");
                }

                try {
                    ptrGen.iAddData(bFontstyle, params[0]);
                }catch(Exception e){
                    Log.i(TAG,"Data not changed");
                }
                iRetVal = ptrGen.iStartPrinting(1);
                ptrGen.iPaperFeed();

            } catch (NullPointerException e) {
                iRetVal = DEVICE_NOTCONNECTED;
                return iRetVal;
            }
            return iRetVal;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (iRetVal == DEVICE_NOTCONNECTED) {
                ptrHandler.obtainMessage(1,"Device not connected").sendToTarget();
                Log.e(TAG,"Notconnected");
            } else if (iRetVal == Printer_GEN.SUCCESS) {
                ptrHandler.obtainMessage(1, "Printing Successful").sendToTarget();
                Log.e(TAG,"Printing Successful");
            } else if (iRetVal == Printer_GEN.PLATEN_OPEN) {
                ptrHandler.obtainMessage(1,"Platen open").sendToTarget();
                Log.e(TAG,"Platen open");
            } else if (iRetVal == Printer_GEN.PAPER_OUT) {
                ptrHandler.obtainMessage(1,"Paper out").sendToTarget();
                Log.e(TAG,"Paper out");
            } else if (iRetVal == Printer_GEN.IMPROPER_VOLTAGE) {
                ptrHandler.obtainMessage(1,"Printer at improper voltage").sendToTarget();
                Log.e(TAG,"Printer at improper voltage");
            } else if (iRetVal == Printer_GEN.FAILURE) {
                ptrHandler.obtainMessage(1, "Printing failed").sendToTarget();
                Log.e(TAG,"Printing failed");
            } else if (iRetVal == Printer_GEN.PARAM_ERROR) {
                ptrHandler.obtainMessage(1,"Parameter error").sendToTarget();
                Log.e(TAG,"Parameter error");
            }else if (iRetVal == Printer_GEN.NO_RESPONSE) {
                ptrHandler.obtainMessage(1,"No response from Pride device").sendToTarget();
                Log.e(TAG,"No response from Pride device");
            }else if (iRetVal== Printer_GEN.DEMO_VERSION) {
                ptrHandler.obtainMessage(1,"Library in demo version").sendToTarget();
                Log.e(TAG,"Library in demo version");
            }else if (iRetVal==Printer_GEN.INVALID_DEVICE_ID) {
                ptrHandler.obtainMessage(1,"Connected  device is not authenticated.").sendToTarget();
                Log.e(TAG,"Connected  device is not authenticated.");
            }else if (iRetVal==Printer_GEN.NOT_ACTIVATED) {
                ptrHandler.obtainMessage(1,"Library not activated").sendToTarget();
                Log.e(TAG,"Library not activated");
            }else if (iRetVal==Printer_GEN.NOT_SUPPORTED) {
                ptrHandler.obtainMessage(1,"Not Supported").sendToTarget();
                Log.e(TAG,"Not Supported");
            }else{
                ptrHandler.obtainMessage(1,"Unknown Response from Device").sendToTarget();
                Log.e(TAG,"Unknown Response from Device");
            }
            super.onPostExecute(result);
        }
    }

    public void showToast(String s)
    {
        Toast.makeText(Print.this, s, Toast.LENGTH_SHORT).show();
    }
}
