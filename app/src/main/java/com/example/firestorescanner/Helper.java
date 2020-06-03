package com.example.firestorescanner;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class Helper implements Constants{

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        String str = String.valueOf((activeNetworkInfo != null && activeNetworkInfo.isConnected()));
        Toast.makeText(context, str, Toast.LENGTH_SHORT);
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean check_If_SP_exists(Context context){

        String Institution_Path = getValueFromSharedPreferences(context,KEY_institution_path).toString();
        String Device_Name = getValueFromSharedPreferences(context,KEY_device_name).toString();
        if(Institution_Path.equals("false") && Device_Name.equals("false")){
            //TODO: SP doesn't exists.
            return false;
        }else
            return true;

    }

    public static Object getValueFromSharedPreferences(Context context, String Key)
    {
        SharedPreferences sharedPreferences;
        final String MY_PREFERENCES = "MyPrefs";
        sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Key, "false");
    }

    public static void putValueInSharedPreferences(Context context, String Key, String Value)
    {
        SharedPreferences sharedPreferences;
        final String MY_PREFERENCES = "MyPrefs";
        sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Key, Value);

        editor.apply();
    }

    public static String getIMEInumber(Context context)
    {
        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission not Enabled.", Toast.LENGTH_SHORT).show();
            return "null";
        }
        else
            return telephonyManager.getDeviceId(); //TODO: Permission check is required.
    }

}
