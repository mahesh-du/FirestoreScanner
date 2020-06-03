package com.example.firestorescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorescanner.Tasks.LoginDevice_background_Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import static com.example.firestorescanner.Helper.check_If_SP_exists;
import static com.example.firestorescanner.Helper.getValueFromSharedPreferences;

public class LoginActivity extends AppCompatActivity implements Constants{

    TextInputEditText et_email, et_password;
    TextView txt_forgot_password;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String Institution_Path, Device_Name, actual_Position;
    public static final String TAG = logCatTag_Activity_LOGIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        Log.d(TAG, "onCreate()");

        et_email = findViewById(R.id.tiet_email);
        et_password = findViewById(R.id.tiet_password);
        txt_forgot_password = findViewById(R.id.txt_forgot_password);

        firebaseAuth = FirebaseAuth.getInstance();

        if(check_If_SP_exists(LoginActivity.this)) {
            Institution_Path = getValueFromSharedPreferences(LoginActivity.this, KEY_institution_path).toString();
            Device_Name = getValueFromSharedPreferences(LoginActivity.this, KEY_device_name).toString();
            actual_Position = getValueFromSharedPreferences(LoginActivity.this, KEY_actual_position).toString();
        }
        else{
            Log.d(TAG, "check_If_SP_exists(): Returned FALSE.");
            startActivity(new Intent(LoginActivity.this, RegisterDeviceActivity.class));
            finish();
        }

        txt_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!et_email.getText().toString().isEmpty()){
                firebaseAuth.sendPasswordResetEmail(et_email.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "txt_forgot_password Button Clicked: Email sent.");
                                    Toast.makeText(LoginActivity.this, "Email Sent. Please Check your inbox.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Log.d(TAG, "txt_forgot_password Button Clicked: Email sent failed.");
                                    Toast.makeText(LoginActivity.this, "Email Sent failed. Please Retry.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                }
                else
                    Toast.makeText(LoginActivity.this, "Please enter email.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
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
        onDestroy();
        super.onBackPressed();
        Log.d(TAG, "onBackPressed()");
    }


    public void loginClick(View view) {
        Log.d(TAG, "loginClick(): Login Button clicked.");

        final String email = et_email.getText().toString();
        final String password= et_password.getText().toString();

        if (email.isEmpty()) {
            et_email.setError("Provide your Email first!");
            et_email.requestFocus();
        } else if (password.isEmpty()) {
            et_password.setError("Enter Password!");
            et_password.requestFocus();
        } else if (email.isEmpty() && password.isEmpty())
            Toast.makeText(LoginActivity.this, "Fields Empty!", Toast.LENGTH_SHORT).show();
         else if (!(email.isEmpty() && password.isEmpty())) {

            HashMap<String, Object> params = new HashMap<>();
            params.put("firestoreINSTANCE",db);
            params.put("firebaseAUTH", firebaseAuth);
            params.put("device_NAME", Device_Name);
            params.put("email", email);
            params.put("password", password);

            LoginDevice_background_Task login = new LoginDevice_background_Task(LoginActivity.this, params);
            login.execute();

        } else {
            Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "loginClick(): Error.");
        }
    }

}
