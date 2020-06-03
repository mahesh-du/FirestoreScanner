package com.example.firestorescanner.Tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import static com.example.firestorescanner.Constants.KEY_isBatteryAlertExists;
import static com.example.firestorescanner.Helper.putValueInSharedPreferences;
import static com.example.firestorescanner.Tasks.AlertBattery_Task_Helper.addBatteryAlert;
import static com.example.firestorescanner.Tasks.AlertBattery_Task_Helper.deleteBatteryAlert;

public class AlertBattery_background_Task extends AsyncTask<Void, Void, Boolean> {

    Context context;
    HashMap<String, Object> params;

    public AlertBattery_background_Task(Context context, HashMap<String, Object> params) {
        this.context = context;
        this.params = params;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Boolean resultTAG = null;


        FirebaseFirestore db                            = (FirebaseFirestore) params.get("firestoreINSTANCE");
        String path                                     = String.valueOf(params.get("path"));
        String device_name                              = String.valueOf(params.get("device_NAME"));
        String operation                                = String.valueOf(params.get("operation"));

        switch (operation){
            case "ADD_ALERT": {
                resultTAG = addBatteryAlert(db, path, params);
                if (resultTAG){
                    putValueInSharedPreferences(context, KEY_isBatteryAlertExists, "true");
                    Log.d("myCount", "AlertBattery_background_Task: KEY_isBatteryAlertExists is TRUE.");
                }
                break;}
            case "DELETE_ALERT": {
                resultTAG = deleteBatteryAlert(db, path, device_name);  //add alert_type to get a particular type of alert.
                if (resultTAG){
                    putValueInSharedPreferences(context, KEY_isBatteryAlertExists, "false");
                    Log.d("myCount", "AlertBattery_background_Task: KEY_isBatteryAlertExists is FALSE.");
                }
                break;}
        }

        return resultTAG;
    }

    @Override
    protected void onPostExecute(Boolean isTaskSuccessfull) {
        if (isTaskSuccessfull)
            Log.d("myCount", "AlertBattery_background_Task: isTaskSuccessfull from doInBackground() returned TRUE.");
        else
            Log.d("myCount","AlertBattery_background_Task: isTaskSuccessfull from doInBackground() returned FALSE.");
    }
}
