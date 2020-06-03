package com.example.firestorescanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorescanner.EmailModels.EmailModel;
import com.example.firestorescanner.EmailModels.Email_List;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import static com.example.firestorescanner.Helper.check_If_SP_exists;
import static com.example.firestorescanner.Helper.getValueFromSharedPreferences;

public class MainActivity extends AppCompatActivity implements Constants {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView txt_scan_count;
    String Institution_Path, Device_Name;
    String path ;
    public static final String TAG = logCatTag_Activity_MAIN;
                                                                                                   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");

        txt_scan_count = findViewById(R.id.txt_scan_count);

        //setup Toolbar.
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Scanner");
        //myToolbar.setElevation(1f);
        setSupportActionBar(myToolbar);

        //get Institution_Path and Device_Name from SP.
        if(check_If_SP_exists(MainActivity.this)) {
            Institution_Path = getValueFromSharedPreferences(MainActivity.this, KEY_institution_path).toString();
            Device_Name = getValueFromSharedPreferences(MainActivity.this, KEY_device_name).toString();
        }
        else{
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(MainActivity.this, RegisterDeviceActivity.class));
        }

        path = Institution_Path + firestore_device +"/";

        //start service to update battery.
        Intent backgroundService = new Intent(getApplicationContext(), BatteryService.class);
        startService(backgroundService);

        //get Realtime Scan Count.
        setScanCount();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
        Log.d(TAG, "onBackPressed()");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {

                Intent backgroundService = new Intent(getApplicationContext(), BatteryService.class);
                stopService(backgroundService);
//TODO: run it background thread.
                updateDeviceAfterSignOut();
                searchEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                FirebaseAuth.getInstance().signOut();

                Intent I = new Intent(MainActivity.this, LoginActivity.class);
                finish();
                startActivity(I);
                break;
            }

        }
        return true;
    }

    public void searchEmail(final String email){
        Log.d(TAG, "searchEmail()");
        Task<DocumentSnapshot> task = db.document(firestore_email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        final Email_List email_list = documentSnapshot.toObject(Email_List.class);
                        if(email_list!= null){
                            for (int i = 0; i< email_list.getEmails().size(); i++)
                            {
                                final EmailModel emailModel = email_list.getEmails().get(i);
                                if(emailModel.getEmail().equals(email))
                                {
                                    emailModel.setLogged_In(false);
                                    updateEmailLoggedInAfterSignOut(email_list);
                                }else if(i == (email_list.getEmails().size() -1)){
                                    Toast.makeText(MainActivity.this, "Email not found.", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "MainActivity searchEmail: Email not found.");
                                }
                            }
                        }
                        else
                            Toast.makeText(MainActivity.this, "Email not found.", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "MainActivity searchEmail: Email List empty.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "MainActivity searchEmail./nError: " + e);
                    }
                });
        /*try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "MainActivity searchEmail TRY CATCH./nError: " + e);
        }*/
        while (!task.isComplete()){
            Log.d(TAG, "MainActivity searchEmail.");
        }
    }

    public void updateEmailLoggedInAfterSignOut(Email_List email_list){
        Log.d(TAG, "updateEmailLoggedInAfterSignOut()");
        Task<Void> task = db.document(firestore_email)
                .set(email_list)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(MainActivity.this, "Email updated sucessfully.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "MainActivity updateEmailLoggedInAfterSignOut: Email updated sucessfully.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(MainActivity.this, "Email update Not sucessful./nError: " + e, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "MainActivity updateEmailLoggedInAfterSignOut: Email update Not sucessful./nError: " + e);
            }
        });
        while(!task.isComplete()){
            Log.d(TAG, "MainActivity updateEmailLoggedInAfterSignOut.");
        }
    }

    public void updateDeviceAfterSignOut(){
        Log.d(TAG, "updateDeviceAfterSignOut()");
        Map<String,Object> updateValues = new HashMap<>();
        updateValues.put(firestore_DEVICE_FIELD_Logged_In, false);
        db.document(path + Device_Name)
                .update(updateValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(MainActivity.this, "MainActivity: Device updated sucessfully.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "MainActivity updateDeviceAfterSignOut: Device updated sucessfully.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(MainActivity.this, "Device updated Not sucessful./nError: " + e, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "MainActivity updateDeviceAfterSignOut: Device updated Not sucessful./nError: " + e);
            }
        });
    }

    public void start_scan(View view) {
        Log.d(TAG, "start_scan(): Start Scan Button clicked.");
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent); //Barcode Scanner to scan for us
    }

    public void setScanCount()
    {
        Log.d(TAG, "setScanCount()");
        db.document(path + Device_Name)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        txt_scan_count.setText(documentSnapshot.get(firestore_DEVICE_FIELD_Scans_Today).toString());
                    }
                });
    }

}
