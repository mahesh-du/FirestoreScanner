package com.example.firestorescanner;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.firestorescanner.Tasks.AlertBattery_background_Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import static com.example.firestorescanner.Helper.getValueFromSharedPreferences;

public class BatteryService extends Service implements Constants{

    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_WIFI = 1;
    public static final int NETWORK_STATUS_MOBILE = 2;
    public static String Institution_Path, Device_Name, Device_Actual_Position;
    public static Boolean isBatteryAlertExists;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Institution_Path = getValueFromSharedPreferences(getApplicationContext(), KEY_institution_path).toString();
        Device_Name = getValueFromSharedPreferences(getApplicationContext(), KEY_device_name).toString();
        Device_Actual_Position = getValueFromSharedPreferences(getApplicationContext(), KEY_actual_position).toString();
        isBatteryAlertExists = Boolean.parseBoolean(String.valueOf(getValueFromSharedPreferences(getApplicationContext(), KEY_isBatteryAlertExists)));

        registerBatteryLevelReceiver();
        Log.d("myCount", "Service onCreate: screenOnOffReceiver is registered.");
    }


    @Override
    public void onDestroy() {

        unregisterReceiver(battery_receiver);
        super.onDestroy();
    }

    private BroadcastReceiver battery_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPresent = intent.getBooleanExtra("present", false);
            //int plugged = intent.getIntExtra("plugged", -1);
            int scale = intent.getIntExtra("scale", -1);
            //int health = intent.getIntExtra("health", 0);
            int status = intent.getIntExtra("status", 0);
            int rawlevel = intent.getIntExtra("level", -1);
            //int temperature = intent.getIntExtra("temperature", 0);
            long level = 0;

            Bundle bundle = intent.getExtras();

            Log.i("BatteryLevel", bundle.toString());

            if (isPresent) {
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }

                long Battery_Left = level;
                String Status = getStatusString(status);

                //if((Battery_Left%5) == 0)

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference doc = db.document(Institution_Path + firestore_device+"/" + Device_Name);
                    doc.update(firestore_DEVICE_FIELD_Battery_Remaining, Battery_Left);
                    doc.update(firestore_DEVICE_FIELD_Battery_Status, Status);
                    doc.update(firestore_DEVICE_FIELD_Network_Status, getConnectivityStatusString(getApplicationContext()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("myCount", "Battery and Network details updated successfully.");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),"Error: " + e,Toast.LENGTH_SHORT).show();
                                    Log.d("myCount", "Error: " + e);
                                }
                            });

                HashMap<String, Object> params = new HashMap<>();
                params.put("firestoreINSTANCE", db);
                params.put("path", Institution_Path + firestore_alerts_battery);
                params.put("device_NAME", Device_Name);
                params.put("device_ACTUAL_POSITION", Device_Actual_Position);
                params.put("BATTERY_REMAINING", Battery_Left);
                params.put("BATTERY_STATUS", Status);
                if(Battery_Left<=50){
                    //if((Battery_Left%5)==0)
                        params.put("operation", "ADD_ALERT");
                }
                else if(isBatteryAlertExists)
                    params.put("operation", "DELETE_ALERT");
                else
                    return;

                AlertBattery_background_Task alertBattery_background_task = new AlertBattery_background_Task(getApplicationContext(), params);
                alertBattery_background_task.execute();

                /*info += ("Plugged: " + getPlugTypeString(plugged) + "\n");
                info += ("Health: " + getHealthString(health) + "\n");
                info += ("Temperature: " + temperature + "\n");*/

            } else {
                    //empty.
            }
        }
    };

    private String getPlugTypeString(int plugged) {
        String plugType = "Unknown";

        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                plugType = "AC";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                plugType = "USB";
                break;
        }

        return plugType;
    }

    private String getHealthString(int health) {
        String healthString = "Unknown";

        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthString = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthString = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthString = "Over Voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthString = "Over Heat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthString = "Failure";
                break;
        }

        return healthString;
    }

    private String getStatusString(int status) {
        String statusString = "Unknown";

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusString = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusString = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusString = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusString = "Not Charging";
                break;
        }

        return statusString;
    }

    public int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public String getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        String status = "";
        if (conn == TYPE_WIFI) {
            status = "WIFI";
        } else if (conn == TYPE_MOBILE) {
            status = "Mobile Data";
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = "Offline";
        }
        return status;
    }

    private void registerBatteryLevelReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(battery_receiver, filter);
    }

}
