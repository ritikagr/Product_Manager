package com.msme.iitism.productmanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScanner;
import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScannerBuilder;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private Button mScanButton;
    private DataBaseHandler mDbHandler;

    private Barcode barcodeResult;
    private String result;
    private static final int PERMISSION_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestPermission();

        mScanButton = (Button) findViewById(R.id.scanButton);
        mDbHandler = new DataBaseHandler(this);

        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                        .withActivity(MainActivity.this)
                        .withEnableAutoFocus(true)
                        .withBleepEnabled(false)
                        .withTrackerColor(Color.BLUE)
                        .withBackfacingCamera()
                        .withText("Scanning...")
                        .withCenterTracker()
                        .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                            @Override
                            public void onResult(Barcode barcode) {
                                barcodeResult = barcode;
                                result=barcode.rawValue;

                                addToDatabase(result);
                            }
                        })
                        .build();
                materialBarcodeScanner.startScan();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    }

    public void requestPermission()
    {
        Vector<String> permissions = new Vector<String>();
        int i=0;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)!= PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.BLUETOOTH);


        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH},PERMISSION_REQUEST);
    }

    private void startScan() {
        /**
         * Build a new MaterialBarcodeScanner
         */
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(MainActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(true)
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("Scanning...")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                        barcodeResult = barcode;
                        result=barcode.rawValue;

                        addToDatabase(result);
                    }
                })
                .build();
        materialBarcodeScanner.startScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MaterialBarcodeScanner.RC_HANDLE_CAMERA_PERM) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
                return;
            }
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error")
                    .setMessage(R.string.no_camera_permission)
                    .setPositiveButton(android.R.string.ok, listener)
                    .show();
        }
        else if(requestCode==PERMISSION_REQUEST)
        {
            int n = grantResults.length;
            if(grantResults.length>0)
            {
                int i=0;
                for(i=0;i<n;i++)
                {
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED)
                        break;
                }

                if(i<n)
                    showToast("Please grant all permission otherwise some features will not work...");
            }
            else
                showToast("Please grant all permission otherwise some features will not work...");
        }
    }

    public void addToDatabase(String intentResult)
    {
        if(intentResult!=null)
        {
            if(intentResult=="")
            {
                showToast("scan cancelled");
            }
            else
            {
                if(mDbHandler.isExists(intentResult) != true) {
                    //Product does not exists in database , So Popup will be visible to add product in database
                    final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    final LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.scan_dialog_layout,null);

                    final TextView mDialog_ProductId = (TextView) dialogView.findViewById(R.id.Dproduct_id);
                    final EditText mDialog_Quantity = (EditText) dialogView.findViewById(R.id.DQuantity);

                    mDialog_ProductId.setText(intentResult);
                    mDialog_Quantity.setText("1");
                    mDialog_Quantity.setSelection(mDialog_Quantity.getText().length());
                    Button mSaveButton = (Button) dialogView.findViewById(R.id.dialogSave);
                    Button mCancelButton = (Button) dialogView.findViewById(R.id.dialogCancel);

                    mSaveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!mDialog_Quantity.getText().toString().matches("")) {
                                Product product = new Product(mDialog_ProductId.getText().toString(), Integer.parseInt(mDialog_Quantity.getText().toString()));
                                mDbHandler.add_product(product);
                                alertDialog.dismiss();
                            }
                            else {
                                showToast("Please enter valid quantity!");
                            }
                        }
                    });

                    mCancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.setView(dialogView);
                    //To Prevent dismiss on backKey Press
                    alertDialog.setCancelable(false);
                    //To Prevent dismiss on outside Touch
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
                else {
                    //Existed Product Quantity will be increased by 1
                    final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    final LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.scan_dialog_layout,null);

                    final TextView mDialog_ProductId = (TextView) dialogView.findViewById(R.id.Dproduct_id);
                    final EditText mDialog_Quantity = (EditText) dialogView.findViewById(R.id.DQuantity);

                    mDialog_ProductId.setText(intentResult);
                    mDialog_Quantity.setText("1");
                    mDialog_Quantity.setSelection(mDialog_Quantity.getText().length());
                    Button mSaveButton = (Button) dialogView.findViewById(R.id.dialogSave);
                    Button mCancelButton = (Button) dialogView.findViewById(R.id.dialogCancel);
                    mSaveButton.setText("ADD");
                    mSaveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!mDialog_Quantity.getText().toString().matches("")) {
                                Product product = new Product(mDialog_ProductId.getText().toString(), Integer.parseInt(mDialog_Quantity.getText().toString()));
                                mDbHandler.updateProduct(product);
                                alertDialog.dismiss();
                            }
                            else {
                                showToast("Please enter valid quantity!");
                            }
                        }
                    });

                    mCancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.setView(dialogView);
                    //To Prevent dismiss on backKey Press
                    alertDialog.setCancelable(false);
                    //To Prevent dismiss on outside Touch
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }
        }
        else
        {

        }
    }

    public void addProduct(View view)
    {
        //Product does not exists in database , So Popup will be visible to add product in database
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        final LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.floating_button_dialog,null);

        final EditText mDialog_ProductId = (EditText) dialogView.findViewById(R.id.Dproduct_id);
        final EditText mDialog_Quantity = (EditText) dialogView.findViewById(R.id.DQuantity);

        mDialog_Quantity.setSelection(mDialog_Quantity.getText().length());
        mDialog_ProductId.setSelection(mDialog_ProductId.getText().length());

        Button mSaveButton = (Button) dialogView.findViewById(R.id.fDialogSave);
        Button mCancelButton = (Button) dialogView.findViewById(R.id.fDialogCancel);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mDialog_ProductId.getText().toString().matches("") && !mDialog_Quantity.getText().toString().matches("")) {
                    Product product = new Product(mDialog_ProductId.getText().toString(), Integer.parseInt(mDialog_Quantity.getText().toString()));
                    if (mDbHandler.isExists(mDialog_ProductId.getText().toString()) != true) {
                        mDbHandler.add_product(product);
                        alertDialog.dismiss();
                    }
                    else
                        showToast("Product already exists...");
                }
                else {
                    showToast("Please enter valid Product data!");
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(dialogView);
        //To Prevent dismiss on backKey Press
        alertDialog.setCancelable(false);
        //To Prevent dismiss on outside Touch
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }
    public void showToast(String text)
    {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }


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
        switch (id)
        {
            case R.id.productListIntent:
            {
                Intent intent = new Intent(this,Products.class);
                startActivity(intent);}
                break;
            case R.id.printIntent:
            {
                Intent intent = new Intent(this,Print.class);
                startActivity(intent);}
            break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new ConnectionManager(getApplicationContext()).setDevice("");
    }
}
