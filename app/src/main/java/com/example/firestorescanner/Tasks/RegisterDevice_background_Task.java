package com.example.firestorescanner.Tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.firestorescanner.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import static android.support.v4.content.ContextCompat.startActivity;
import static com.example.firestorescanner.Constants.KEY_actual_position;
import static com.example.firestorescanner.Constants.KEY_device_name;
import static com.example.firestorescanner.Constants.KEY_institution_path;
import static com.example.firestorescanner.Constants.firestore_email;
import static com.example.firestorescanner.Helper.putValueInSharedPreferences;
import static com.example.firestorescanner.Tasks.RegisterDevice_Task_Helper.getInstitutionPath;
import static com.example.firestorescanner.Tasks.RegisterDevice_Task_Helper.isDeviceAlreadyExists;
import static com.example.firestorescanner.Tasks.RegisterDevice_Task_Helper.isEmailUseable;
import static com.example.firestorescanner.Tasks.RegisterDevice_Task_Helper.registerDevice;

public class RegisterDevice_background_Task extends AsyncTask<Void, Void, Boolean> {

    Activity activity;
    HashMap<String, Object> params;
    ProgressDialog progressDialog;

    public RegisterDevice_background_Task(Activity activity, HashMap<String, Object> params) {
        this.activity = activity;
        this.params = params;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(activity);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        Boolean isTaskSuccessfull = false;

        FirebaseFirestore db            = (FirebaseFirestore) params.get("firestoreINSTANCE");
        FirebaseAuth firebaseAuth       = (FirebaseAuth) params.get("firebaseAUTH");
        String device_name              = String.valueOf(params.get("device_NAME"));
        Long device_imei                = Long.parseLong(String.valueOf(params.get("device_IMEI")));
        String device_actual_possition  = String.valueOf(params.get("device_actual_POSITION"));
        String email                    = String.valueOf(params.get("email"));
        String password                 = String.valueOf(params.get("password"));
        String institution_Path         = getInstitutionPath(activity, db, firestore_email, email);

        if(institution_Path==null)
            return isTaskSuccessfull;

        if(!isDeviceAlreadyExists(db, institution_Path , device_imei))
        {
            if(isEmailUseable(activity, db, firebaseAuth, firestore_email, email, password)){
                isTaskSuccessfull = true;
                registerDevice(activity, db, institution_Path, device_name, device_actual_possition, device_imei, email);
                putValueInSharedPreferences(activity,KEY_institution_path, institution_Path);
                putValueInSharedPreferences(activity, KEY_device_name, device_name);
                putValueInSharedPreferences(activity, KEY_actual_position, device_actual_possition);
            }else
                isTaskSuccessfull = false;

        }else{
            // if email is usable  it can be used at login screen to add it to device info.
            if(isEmailUseable(activity, db, firebaseAuth, firestore_email, email, password)) {
                isTaskSuccessfull = true;
                //TODO: user can edit if device is already registered. get already stored details from getInstitutionPath.
                putValueInSharedPreferences(activity, KEY_institution_path, institution_Path);
                putValueInSharedPreferences(activity, KEY_device_name, device_name);
                    putValueInSharedPreferences(activity, KEY_actual_position, device_actual_possition);
            }else
                isTaskSuccessfull = false;
        }

        return isTaskSuccessfull;
    }

    @Override
    protected void onPostExecute(Boolean isTaskSuccessfull) {
        progressDialog.dismiss();
        if (isTaskSuccessfull) {
            //TODO: show Successfull Dialog.
            Toast.makeText(activity, "Task Successfull.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, LoginActivity.class);
            startActivity(activity, intent, null);
            activity.finish();
        }
        else
            Toast.makeText(activity,"Task not Successfull.", Toast.LENGTH_SHORT).show();
    }

}
