package com.msme.iitism.productmanager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ritik on 12/12/2016.
 */
public class ConnectionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    private static final String PREF_NAME = "com.msme.iitism.productmanager";
    private static final String KEY_IS_CONNECTED = "is_connected";
    private static final String KEY_IS_BONDED = "is_bonded";
    private static final String Device_MAC = "device_mac";

    int PRIVATE_MODE = 0;

    public ConnectionManager(Context context)
    {
        this.context = context;
        this.pref = this.context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        this.editor = pref.edit();
    }

    public void setBonded(boolean isBonded)
    {
        editor.putBoolean(KEY_IS_BONDED, isBonded);
        editor.commit();
    }

    public boolean getBondedState()
    {
        return pref.getBoolean(KEY_IS_BONDED, false);
    }

    public void setConnected(boolean isConnected)
    {
        editor.putBoolean(KEY_IS_CONNECTED, isConnected);
        editor.commit();
    }

    public boolean getConnectedState()
    {
        return pref.getBoolean(KEY_IS_CONNECTED, false);
    }

    public void setDevice(String mac)
    {
        editor.putString(Device_MAC, mac);
        editor.commit();
    }

    public String getDevice()
    {
        return pref.getString(Device_MAC, "");
    }
}
