package com.example.firestorescanner.Tasks;

import android.util.Log;

import com.example.firestorescanner.EmailModels.EmailModel;
import com.example.firestorescanner.EmailModels.Email_List;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Blocked;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Logged_In;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Logged_In_Email;
import static com.example.firestorescanner.Constants.firestore_email;

public class LoginDevice_Task_Helper {

    public static boolean signIn(FirebaseAuth firebaseAuth, String email, String password ){

        final boolean[] isTaskSuccessfull = {false};
        Task<AuthResult> task = firebaseAuth.signInWithEmailAndPassword(email, password);

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in isEmailUseable(): " + e);
        }

        if (task.isSuccessful()) {
            isTaskSuccessfull[0] = true;
        } else {
            isTaskSuccessfull[0] = false;
            Log.d("myCount","LogIn Not successfull./nError: " + task.getException());
        }
        return isTaskSuccessfull[0];
    }

    public static boolean updateEmailLoggedInAfterSignIn(FirebaseFirestore db, Email_List email_list){

        final boolean[] isTaskSuccessfull = {false};
        Task<Void> task = db.document(firestore_email)
                .set(email_list);

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in updateEmailLoggedInAfterSignIn(): " + e);
        }

        if(task.isSuccessful()){
            isTaskSuccessfull[0] = true;
            Log.d("myCount", "updateEmailLoggedInAfterSignIn: Email updated successfully.");
        }else{
            isTaskSuccessfull[0] = false;
            Log.d("myCount", "updateEmailLoggedInAfterSignIn: Email update Not successfull./nError: " + task.getException());
        }

        return isTaskSuccessfull[0];
    }

    public static boolean updateDeviceAfterSignIn(FirebaseFirestore db, String path, String email){

        final boolean[] isTaskSuccessfull = {false};
        Map<String,Object> updateValues = new HashMap<>();
        updateValues.put(firestore_DEVICE_FIELD_Logged_In_Email,email);
        updateValues.put(firestore_DEVICE_FIELD_Logged_In,true);

        Task<Void> task = db.document(path)
                .update(updateValues);

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in updateDeviceAfterSignIn(): " + e);
        }

        if(task.isSuccessful()){
            isTaskSuccessfull[0] = true;
            Log.d("myCount", "updateDeviceAfterSignIn: Device updated sucessfully.");
        }else{
            isTaskSuccessfull[0] = false;
            Log.d("myCount", "updateDeviceAfterSignIn: Device updated Not sucessful./nError: " + task.getException());
        }
        return isTaskSuccessfull[0];

    }

    //TODO: This methods firestore call can be removed.
    public static String login_isEmailUseable(FirebaseFirestore db, String path, final String email) {

        String emailResultTAG = "ERROR_isEmailUseable";
        Task<DocumentSnapshot> task = db.document(path)
                .get();

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in checkEmail_RegisteredDevice(): " + e);
        }

        if(task.isSuccessful()){
            final Email_List email_list = task.getResult().toObject(Email_List.class);

            for (int i = 0; i< Objects.requireNonNull(email_list).getEmails().size(); i++)
            {
                final EmailModel emailModel = email_list.getEmails().get(i);
                if(emailModel.getEmail().equals(email))
                {
                    if(emailModel.getRole().equals("Scanner")){
                        if (emailModel.getBlocked() == false) {

                            if(emailModel.getLogged_In() == false){
                                emailResultTAG = "NOT_LOGGED_IN_isEmailUseable";
                                break;
                            }else{
                                emailResultTAG = "LOGGED_IN_isEmailUseable";
                                break;
                            }
                        }else{
                            emailResultTAG = "BLOCKED_isEmailUseable";
                        }
                    }else{emailResultTAG = "DIFFERENT_ROLE_isEmailUseable";}
                }else if(i == (email_list.getEmails().size() -1))
                    //TODO: check this whole if-else scenario.
                    emailResultTAG = "NOT_FOUND_isEmailUseable";
            }
        }else{
            Log.d("myCount", "Error in checkEmail_RegisteredDevice(): " + task.getException());
            emailResultTAG = "ERROR_isEmailUseable";
        }

        return emailResultTAG;
    }

    public static String login_isDeviceLoggedInWithAnotherEmail(FirebaseFirestore db, String path){

        String deviceResultTAG = "ERROR_isDeviceLoggedInWithAnotherEmail";

        Task<DocumentSnapshot> task =db.document(path)
                                        .get();

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in isDeviceLoggedInWithAnotherEmail(): " + e);
        }

        if(task.isSuccessful()){
           DocumentSnapshot documentSnapshot = task.getResult();

            assert documentSnapshot != null;
            if(documentSnapshot.exists())
            {
                if(documentSnapshot.getBoolean(firestore_DEVICE_FIELD_Blocked)==false)
                {
                    if(documentSnapshot.getBoolean(firestore_DEVICE_FIELD_Logged_In)== false)
                        deviceResultTAG = "NOT_LOGGED_IN_isDeviceLoggedInWithAnotherEmail";
                    else
                        deviceResultTAG = "LOGGED_IN_isDeviceLoggedInWithAnotherEmail";
                }
                else
                    deviceResultTAG = "BLOCKED_isDeviceLoggedInWithAnotherEmail";
            }else
                deviceResultTAG = "NOT_FOUND_isDeviceLoggedInWithAnotherEmail";
        }
        else
            deviceResultTAG = "ERROR_isDeviceLoggedInWithAnotherEmail";

        return deviceResultTAG;
    }

    public static HashMap<String, Object> login_getInstitutionPath(FirebaseFirestore db, String path, String Email){

        HashMap<String, Object> returnData = new HashMap<>();
        Task<DocumentSnapshot> task =db.document(path)
                .get();

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in login_getInstitutionPath(): " + e);
            returnData.put("getInstitutionPathTAG", "ERROR_getInstitutionPath");
        }

        if (task.isSuccessful()){
            Email_List email_list = task.getResult().toObject(Email_List.class);

            if(email_list!= null){

                for (int i = 0; i< email_list.getEmails().size(); i++)
                {
                    EmailModel email = email_list.getEmails().get(i);

                    if(email.getEmail().equals(Email)){
                        returnData.put("getInstitutionPathTAG", "FOUND_getInstitutionPath");
                        returnData.put("institution_PATH", email.getInstitution_Path());
                        returnData.put("email_LIST", email_list);
                        returnData.put("email_found_at_INDEX", i);
                        break;
                    }
                    else if(i == (email_list.getEmails().size() -1))
                        returnData.put("getInstitutionPathTAG", "NOT_FOUND_getInstitutionPath");
                }
            }else
                returnData.put("getInstitutionPathTAG", "ERROR_getInstitutionPath");
        }
        else {
            returnData.put("getInstitutionPathTAG", "NOT_FOUND_getInstitutionPath");
            Log.d("myCount", "Error in login_getInstitutionPath(): " + task.getException());
        }

        return returnData;
    }

}
