package com.example.firestorescanner.Tasks;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.firestorescanner.EmailModels.EmailModel;
import com.example.firestorescanner.EmailModels.Email_List;
import com.example.firestorescanner.Location;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Actual_Position;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Battery_Remaining;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Battery_Status;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Blocked;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Device_Name;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_GPS_Location;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_IMEI;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Logged_In;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Logged_In_Email;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Network_Status;
import static com.example.firestorescanner.Constants.firestore_DEVICE_FIELD_Scans_Today;
import static com.example.firestorescanner.Constants.firestore_device;
import static com.example.firestorescanner.Constants.firestore_email;

public class RegisterDevice_Task_Helper {

    public static boolean createEmail(Activity activity, FirebaseAuth firebaseAuth, final String email, String password){

        final boolean[] isTaskSuccessfull = {false};
        Task<AuthResult> task = firebaseAuth.createUserWithEmailAndPassword(email, password);

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in isEmailUseable(): " + e);
        }

        if (task.isSuccessful()) {
            isTaskSuccessfull[0] = true;
            Log.d("myCount", "Email Creation Successfull.");
        }else
            Log.d("myCount", "Email Creation not Successfull: " + task.getException());

        return isTaskSuccessfull[0];
    }

    public static void set_isEmailCreatedTrue(FirebaseFirestore db, Email_List emails, int email_index_found_at, String path){

        emails.getEmails().get(email_index_found_at).set_isEmailCreated(true);

        Task<Void> set_isEmailCreated_TrueTASK = db.document(path)
                .set(emails);
        try {
            Tasks.await(set_isEmailCreated_TrueTASK);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in set_isEmailCreatedTrue(): " + e);
        }
    }

    public static boolean isDeviceAlreadyExists(FirebaseFirestore db, String institution_Path, Long imei){

        final boolean[] isAlreadyExists = {false};
        String path = institution_Path + firestore_device;

        Task<QuerySnapshot> task =db.collection(path)
                .get();

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in isDeviceAlreadyExists(): " + e);
        }

        if (task.isSuccessful()){

            QuerySnapshot docs = task.getResult();
            if(docs != null){
                for (DocumentSnapshot doc : docs.getDocuments()){
                    Long deviceIMEI = doc.getLong(firestore_DEVICE_FIELD_IMEI);
                    if(imei == deviceIMEI){
                        isAlreadyExists[0] = true;
                        break;
                    }
                }
            }else
                Log.d("myCount", "Error in isDeviceAlreadyExists(): docs list null");
        }
        else
            Log.d("myCount", "Error in isDeviceAlreadyExists(): " + task.getException());

        return isAlreadyExists[0];

    }

    public static boolean isEmailUseable(Activity activity, FirebaseFirestore db, FirebaseAuth firebaseAuth, String path, String Email, String Password){

        final boolean[] isValid = {false};
        Task<DocumentSnapshot> task =db.document(path)
                .get();

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in isEmailUseable(): " + e);
        }

        if (task.isSuccessful()){
            Email_List email_list = task.getResult().toObject(Email_List.class);

            if(email_list!= null){

                for (int i = 0; i< email_list.getEmails().size(); i++)
                {
                    EmailModel email = email_list.getEmails().get(i);
                    String role = email.getRole();
                    Boolean blocked = email.getBlocked();
                    Boolean logged_In = email.getLogged_In();
                    Boolean isEmailCreated = email.get_isEmailCreated();

                    if(email.getEmail().equals(Email))
                    {
                        if(role.equals("Scanner")){
                            if(!blocked){
                                if(isEmailCreated){
                                    if(!logged_In){
                                        isValid[0] = true;
                                    }else
                                        Toast.makeText(activity, "Another Device is currently this Email Id. Kindly use another Email Id.", Toast.LENGTH_LONG).show();
                                }else{
                                    isValid[0] = true;
                                    //TODO: create Email.
                                    Boolean isCreationSuccessfull = createEmail(activity, firebaseAuth ,Email, Password);
                                    if(isCreationSuccessfull)
                                        set_isEmailCreatedTrue(db,email_list,i, firestore_email);
                                }
                            }else
                                Toast.makeText(activity, "This email Id is blocked by Manager. Kindly use another Email Id.", Toast.LENGTH_LONG).show();
                        }else
                            Toast.makeText(activity, "Email was not created for Scanner.", Toast.LENGTH_LONG).show();

                    }else if(i == (email_list.getEmails().size() -1))
                        Toast.makeText(activity , "Email not found.", Toast.LENGTH_LONG).show();
                }

            }else
                Log.d("myCount", "Error in isEmailUseable(): email list null");
        }
        else {
            Toast.makeText(activity , "Email not found.", Toast.LENGTH_LONG).show();
            Log.d("myCount", "Error in isEmailUseable(): " + task.getException());
        }
        return isValid[0];
    }

    public static void registerDevice(final Activity activity, FirebaseFirestore db, String institution_Path , String device_name, String actual_possition, Long imei, String email){

        Location location = new Location(activity);

        Map<String, Object> device = new HashMap<>();
        device.put(firestore_DEVICE_FIELD_Device_Name,device_name);
        device.put(firestore_DEVICE_FIELD_IMEI,imei);
        device.put(firestore_DEVICE_FIELD_Blocked, false);
        device.put(firestore_DEVICE_FIELD_Scans_Today, Long.parseLong("0"));
        device.put(firestore_DEVICE_FIELD_Actual_Position, actual_possition);
        device.put(firestore_DEVICE_FIELD_GPS_Location, new GeoPoint(location.getLatitude(), location.getLongitude()));
        device.put(firestore_DEVICE_FIELD_Network_Status, "Offline");
        device.put(firestore_DEVICE_FIELD_Battery_Remaining, Long.parseLong("100"));
        device.put(firestore_DEVICE_FIELD_Battery_Status,"Discharging");
        device.put(firestore_DEVICE_FIELD_Logged_In_Email, email);
        device.put(firestore_DEVICE_FIELD_Logged_In, false);

        String path = institution_Path + firestore_device + "/" + device_name;

        db.document(path)      //TODO: change path to Institution_path.
                .set(device)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(activity , "Device Registered.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity , "Device Registeration unsuccessful.Error: "+ e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getInstitutionPath(final Activity activity, FirebaseFirestore db, String path, String Email){

        String institution_Path = null;
        Task<DocumentSnapshot> task =db.document(path)
                .get();

        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d("myCount", "Error in getInstitutionPath(): " + e);
        }

        if (task.isSuccessful()){
            Email_List email_list = task.getResult().toObject(Email_List.class);

            if(email_list!= null){

                for (int i = 0; i< email_list.getEmails().size(); i++)
                {
                    EmailModel email = email_list.getEmails().get(i);

                    if(email.getEmail().equals(Email))
                        institution_Path = email.getInstitution_Path();
                    else if(i == (email_list.getEmails().size() -1))
                        Toast.makeText(activity , "Email not found.", Toast.LENGTH_LONG).show();
                }
            }else
                Log.d("myCount", "Error in getInstitutionPath(): email list null");
        }
        else {
            Toast.makeText(activity , "Email not found.", Toast.LENGTH_LONG).show();
            Log.d("myCount", "Error in getInstitutionPath(): " + task.getException());
        }

        return institution_Path;
    }
}
