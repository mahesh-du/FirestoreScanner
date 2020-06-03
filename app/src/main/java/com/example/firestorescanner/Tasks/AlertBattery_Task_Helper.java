package com.example.firestorescanner.Tasks;

import android.util.Log;

import com.example.firestorescanner.AlertModels.Alert_BatteryModel;
import com.example.firestorescanner.AlertModels.Alert_Battery_ListModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.firestorescanner.Constants.firestore_ALERT_FIELD_Alert_Type_BATTERY_LOW;

public class AlertBattery_Task_Helper {

    public static HashMap<String, Object> getBatteryAlerts(FirebaseFirestore db, String path){

        Boolean isTaskSuccessfull = false;
        HashMap<String, Object> returnData= new HashMap<>();

        Task<DocumentSnapshot> task = db.document(path).get();

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in getBatteryAlerts(): " + e);
        }

        if(task.isSuccessful()){
            isTaskSuccessfull = true;
            Alert_Battery_ListModel alert_battery_listModel = task.getResult().toObject(Alert_Battery_ListModel.class);
            returnData.put("Data", alert_battery_listModel);

            Log.d("myCount", "getBatteryAlerts: Device updated sucessfully.");
        }else{
            isTaskSuccessfull = false;
            Log.d("myCount", "getBatteryAlerts: Device updated Not sucessful./nError: " + task.getException());
        }

        returnData.put("isTaskSuccessfull", isTaskSuccessfull);
        return returnData;
    }

    public static Boolean addBatteryAlert(FirebaseFirestore db, String path, HashMap<String, Object> newAlert){

        HashMap<String, Object> batteryAlerts= getBatteryAlerts(db, path);
        Boolean isTaskSuccessfull = Boolean.parseBoolean(String.valueOf(batteryAlerts.get("isTaskSuccessfull")));

        if(isTaskSuccessfull){
            Alert_Battery_ListModel alert_battery_list = (Alert_Battery_ListModel) batteryAlerts.get("Data");

            Alert_BatteryModel alert_batteryModel = new Alert_BatteryModel();
            alert_batteryModel.setAlertType              (firestore_ALERT_FIELD_Alert_Type_BATTERY_LOW);
            alert_batteryModel.setDevice_Name            (String.valueOf(newAlert.get("device_NAME")));
            alert_batteryModel.setDevice_Actual_Position (String.valueOf(newAlert.get("device_ACTUAL_POSITION")));
            alert_batteryModel.setCreated_At             (new Date());
            alert_batteryModel.setBattery_Remaining      (Long.parseLong(String.valueOf(newAlert.get("BATTERY_REMAINING"))));
            alert_batteryModel.setBattery_Status         (String.valueOf(newAlert.get("BATTERY_STATUS")));
            alert_batteryModel.setMessage                ("Battery Low. Please connect your charger.");
            alert_batteryModel.setAlert_Generated        (false);

            if(alert_battery_list.getAlert()== null) {
                List<Alert_BatteryModel> alert_batteryModelList = new ArrayList<>();
                alert_batteryModelList.add(alert_batteryModel);
                alert_battery_list.setAlert(alert_batteryModelList);
            }
            else {
                for (int i = 0; i <alert_battery_list.getAlert().size() ; i++) {
                    Alert_BatteryModel alert = alert_battery_list.getAlert().get(i);
                    if(alert!=null){
                        if (alert.getDevice_Name().equals(alert_batteryModel.getDevice_Name())){
                            if(alert.getAlertType().equals(alert_batteryModel.getAlertType())){
                                // alert of this type for this device exists.
                                alert_battery_list.getAlert().remove(i);
                                alert_battery_list.getAlert().add(i,alert_batteryModel);
                                break;
                            }
                        }
                    }
                    if(i == (alert_battery_list.getAlert().size()-1))
                        alert_battery_list.getAlert().add(alert_batteryModel);
                }
            }
            isTaskSuccessfull = updateBatteryAlerts(db, path, alert_battery_list);

            if(!isTaskSuccessfull){
                Log.d("myCount","addBatteryAlert(): isTaskSuccessfull from updateBatteryAlerts() returned false.");
                return isTaskSuccessfull;
            }
        }else{
            Log.d("myCount","addBatteryAlert(): isTaskSuccessfull from getBatteryAlerts() returned false.");
            return isTaskSuccessfull;
        }
        return isTaskSuccessfull;
    }

    public static Boolean updateBatteryAlerts(FirebaseFirestore db, String path, Alert_Battery_ListModel newAlert_battery_list ){

        Boolean isTaskSuccessfull = false;

        Task<Void> task = db.document(path).set(newAlert_battery_list);

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in updateBatteryAlerts(): " + e);
        }

        if(task.isSuccessful()){
            isTaskSuccessfull = true;

            Log.d("myCount", "updateBatteryAlerts: Alert added sucessfully.");
        }else{
            isTaskSuccessfull = false;
            Log.d("myCount", "updateBatteryAlerts: Alert addition Not sucessful./nError: " + task.getException());
        }

        return isTaskSuccessfull;
    }

    public static Boolean deleteBatteryAlert(FirebaseFirestore db, String path, String device_Name){

        Boolean isTaskSuccessfull = false;

        HashMap<String, Object> returnedData = getBatteryAlerts(db, path);
        Alert_Battery_ListModel alert_battery_list = (Alert_Battery_ListModel) returnedData.get("Data");
        if(alert_battery_list.getAlert()!= null) {
            for (Alert_BatteryModel alert : alert_battery_list.getAlert()) {
                if(alert.getDevice_Name().equals(device_Name)){
                    alert_battery_list.getAlert().remove(alert);
                    break;
                }
            }
        }
        //doesn't check if device not found.
        Task<Void> task = db.document(path).set(alert_battery_list);

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in deleteBatteryAlert(): " + e);
        }

        if(task.isSuccessful()){
            isTaskSuccessfull = true;
            Log.d("myCount", "deleteBatteryAlert: Alert deleted sucessfully.");
        }else{
            isTaskSuccessfull = false;
            Log.d("myCount", "deleteBatteryAlert: Alert deletion Not sucessful./nError: " + task.getException());
        }

        return isTaskSuccessfull;
    }

}
