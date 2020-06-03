package com.example.firestorescanner.Tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.firestorescanner.EmailModels.Email_List;
import com.example.firestorescanner.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static android.support.v4.content.ContextCompat.startActivity;
import static com.example.firestorescanner.Constants.firestore_device;
import static com.example.firestorescanner.Constants.firestore_email;
import static com.example.firestorescanner.Tasks.LoginDevice_Task_Helper.login_getInstitutionPath;
import static com.example.firestorescanner.Tasks.LoginDevice_Task_Helper.login_isDeviceLoggedInWithAnotherEmail;
import static com.example.firestorescanner.Tasks.LoginDevice_Task_Helper.login_isEmailUseable;
import static com.example.firestorescanner.Tasks.LoginDevice_Task_Helper.signIn;
import static com.example.firestorescanner.Tasks.LoginDevice_Task_Helper.updateDeviceAfterSignIn;
import static com.example.firestorescanner.Tasks.LoginDevice_Task_Helper.updateEmailLoggedInAfterSignIn;

public class LoginDevice_background_Task extends AsyncTask<Void, Void, String> {

    Activity activity;
    HashMap<String, Object> params;
    ProgressDialog progressDialog;

    public LoginDevice_background_Task(Activity activity, HashMap<String, Object> params) {
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
    protected String doInBackground(Void... voids) {

        String resultTAG = null;

        FirebaseFirestore db                            = (FirebaseFirestore) params.get("firestoreINSTANCE");
        FirebaseAuth firebaseAuth                       = (FirebaseAuth) params.get("firebaseAUTH");
        String device_name                              = String.valueOf(params.get("device_NAME"));
        String email                                    = String.valueOf(params.get("email"));
        String password                                 = String.valueOf(params.get("password"));

        HashMap<String, Object> institution_PathDATA    = login_getInstitutionPath(db, firestore_email, email);
        String institution_Path;
        Email_List emails;
        Integer email_found_at_INDEX;
        resultTAG = String.valueOf(institution_PathDATA.get("getInstitutionPathTAG"));

        if(resultTAG.equals("FOUND_getInstitutionPath")) {
            institution_Path = String.valueOf(institution_PathDATA.get("institution_PATH"));
            emails = (Email_List) institution_PathDATA.get("email_LIST");
            email_found_at_INDEX = (Integer) institution_PathDATA.get("email_found_at_INDEX");
            emails.getEmails().get(email_found_at_INDEX).setLogged_In(true);
        }else
            return resultTAG;

        //TODO: This methods firestore call can be removed. filter emailList retrieved above.
//        resultTAG = login_isEmailUseable(db, firestore_email, email);
//
//        if(resultTAG.equals("NOT_LOGGED_IN_isEmailUseable")) {
//            resultTAG = login_isDeviceLoggedInWithAnotherEmail(db, institution_Path + firestore_device + "/" + device_name);
//            if(resultTAG.equals("NOT_LOGGED_IN_isDeviceLoggedInWithAnotherEmail")){
//                if(signIn(firebaseAuth, email, password)){
//                    if(updateEmailLoggedInAfterSignIn(db,emails)){
//                        if(updateDeviceAfterSignIn(db, institution_Path + firestore_device +"/" + device_name, email))
//                            resultTAG = "TASK_SUCCESSFUL";
//                        else
//                            resultTAG = "DEVICE_SIGN_IN_NOT_SUCCESSFUL";
//                    }
//                    else
//                        resultTAG = "EMAIL_SIGN_IN_NOT_SUCCESSFUL";
//                }else
//                    resultTAG = "SIGN_IN_NOT_SUCCESSFUL";
//            }else{
//                //left empty.
//            }
//        }else{
//            //left empty.
//        }

        List<Task<DocumentSnapshot>> taskList = new ArrayList<>(2);
        taskList.add(db.document(firestore_email).get());                                                       //login_isEmailUseable
        taskList.add(db.document(institution_Path + firestore_device + "/" + device_name).get());   //login_isDeviceLoggedInWithAnotherEmail

        Task<List<Object>> task = Tasks.whenAllSuccess(taskList);

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if(task.isSuccessful()){

            for (Object obj : Objects.requireNonNull(task.getResult())){
                DocumentSnapshot doc = (DocumentSnapshot) obj;
                if(doc.exists()) {
                    //TODO: check conditions.





                }else
                    return resultTAG;
            }
        }
        else
            return resultTAG;

        Task<AuthResult> authResultTask = firebaseAuth.signInWithEmailAndPassword(email, password);

        List<Task<Void>> updateTaskList = new ArrayList<>(3);
        updateTaskList.add(db.document(firestore_email).set(emails));
        updateTaskList.add(db.document(institution_Path + firestore_device +"/" + device_name).update(updateValues));

        Task<List<Object>> updateTaskListResult = Tasks.whenAllSuccess(taskList);

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if(task.isSuccessful()){

            for (Object obj : Objects.requireNonNull(task.getResult())){
                DocumentSnapshot doc = (DocumentSnapshot) obj;
                if(doc.exists()) {
                    //TODO: check conditions.
                }else
                    return resultTAG;
            }
        }
        else
            return resultTAG;

        return resultTAG;
    }

    @Override
    protected void onPostExecute(String resultTAG) {
        progressDialog.dismiss();
        String toastMESSAGE = null, logMESSAGE = null;
        switch (resultTAG){
            case "NOT_FOUND_getInstitutionPath": {toastMESSAGE ="Error. Please Retry."; logMESSAGE = "Error. Please Retry."; break;}
            case "ERROR_getInstitutionPath": {toastMESSAGE ="Error. Please Retry."; logMESSAGE = "Error. Please Retry."; break;}
            case "DEVICE_SIGN_IN_NOT_SUCCESSFUL": { toastMESSAGE = "Sign not successful. Please Retry."; logMESSAGE = "Device Sign not successful. Please Retry."; break;}
            case "EMAIL_SIGN_IN_NOT_SUCCESSFUL": { toastMESSAGE = "Sign not successful. Please Retry."; logMESSAGE = "Email Sign not successful. Please Retry."; break;}
            case "SIGN_IN_NOT_SUCCESSFUL": { toastMESSAGE = "Sign not successful. Please Retry."; logMESSAGE = "Authentication Sign not successful. Please Retry."; break;}

            case "ERROR_isEmailUseable": {toastMESSAGE ="Sign not successful. Please Retry."; logMESSAGE = "Email Task not successful."; break;}
            case "LOGGED_IN_isEmailUseable": {toastMESSAGE ="This Email is in use by another Device. Kindly use another Email."; logMESSAGE = "This Email is in use by another Device. Kindly use another Email."; break;}
            case "BLOCKED_isEmailUseable": {toastMESSAGE ="This Email is blocked by your Manager. Kindly use another Email."; logMESSAGE = "This Email is blocked by your Manager. Kindly use another Email."; break;}
            case "NOT_FOUND_isEmailUseable": {toastMESSAGE ="Email not found. Kindly use another Email."; logMESSAGE = "Email not found. Kindly use another Email."; break;}
            case "DIFFERENT_ROLE_isEmailUseable": {toastMESSAGE ="Email not registered for this Application. Kindly use another Email."; logMESSAGE = "Email not registered for this Application."; break;}

            case "ERROR_isDeviceLoggedInWithAnotherEmail": {toastMESSAGE ="Sign not successful. Please Retry."; logMESSAGE = "Device Task not successful."; break;}
            case "LOGGED_IN_isDeviceLoggedInWithAnotherEmail": {toastMESSAGE ="This device is logged In with another email."; logMESSAGE = "This device is logged In with another email."; break;}
            case "BLOCKED_isDeviceLoggedInWithAnotherEmail": {toastMESSAGE ="This Device is blocked by your Manager."; logMESSAGE = "This Device is blocked by your Manager."; break;}
            case "NOT_FOUND_isDeviceLoggedInWithAnotherEmail": {toastMESSAGE ="Device not registered. Kindly Contact your manager."; logMESSAGE = "Device not registered. Kindly Contact your manager."; break;}
            default: {toastMESSAGE ="Error. Please Retry."; logMESSAGE = "Error. Incorrect resultTAG."; break;}
        }
        Toast.makeText(activity, toastMESSAGE, Toast.LENGTH_SHORT).show();
        Log.d("myCount", logMESSAGE);

        if (resultTAG.equals("TASK_SUCCESSFUL")) {
            //TODO: show Successfull Dialog.
            Toast.makeText(activity, "Task Successfull.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, MainActivity.class);
            startActivity(activity, intent, null);
            activity.finish();
        }

    }

}

