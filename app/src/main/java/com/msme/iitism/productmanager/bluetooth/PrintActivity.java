package com.msme.iitism.productmanager.bluetooth;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.msme.iitism.productmanager.DataBaseHandler;
import com.msme.iitism.productmanager.Product;
import com.prowesspride.api.Printer_GEN;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by RAJAT-PC on 29-11-2016.
 */
public class PrintActivity {
    private GlobalPool mGp;
    public static final int DEVICE_NOTCONNECTED = -100;
    private Printer_GEN ptrGen;
    private int iRetVal;
    private Context context;
    private Byte bFontStyle;
    private static final String TAG = "Print Activity";
    private String mData = null;
    private List<Product> mProductList = null;
    private DataBaseHandler mDbHandler;
    private String space = "";

    Handler ptrHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(context, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    public PrintActivity(Byte fontStyle)
    {
        this.bFontStyle = fontStyle;

        switch (this.bFontStyle) {
            case Printer_GEN.FONT_LARGE_NORMAL:
                space = "    ";
                break;
            case Printer_GEN.FONT_LARGE_BOLD:
                space = "    ";
                break;
            case Printer_GEN.FONT_SMALL_NORMAL:
                space = "                    ";
                break;
            case Printer_GEN.FONT_SMALL_BOLD:
                space = "                    ";
                break;
        }
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
                    ptrGen.iAddData(bFontStyle, params[0]);
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
}

