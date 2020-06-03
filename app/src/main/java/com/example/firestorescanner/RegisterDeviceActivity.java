package com.example.firestorescanner;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.firestorescanner.Tasks.RegisterDevice_background_Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import static com.example.firestorescanner.Helper.getIMEInumber;

public class RegisterDeviceActivity extends AppCompatActivity implements Constants {

    FirebaseAuth firebaseAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextInputEditText et_email, et_password, et_device_name, et_actual_position, et_imei;
    public static final String TAG = logCatTag_Activity_REGISTER_DEVICE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device);

        Log.d(TAG, "onCreate()");

        firebaseAuth = FirebaseAuth.getInstance();

        et_email = findViewById(R.id.tiet_email);
        et_password = findViewById(R.id.tiet_password);
        et_device_name = findViewById(R.id.tiet_device_name);
        et_actual_position = findViewById(R.id.tiet_actual_location);
        et_imei = findViewById(R.id.tiet_imei);

        et_imei.setText(getIMEInumber(RegisterDeviceActivity.this));
        et_imei.setEnabled(false);
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

    public void registerDeviceClick(View view) {
        Log.d(TAG, "registerDeviceClick(): Register Button clicked.");

        final String email, password, device_name, actual_position;
        final Long imei;

        email = et_email.getText().toString();
        password = et_password.getText().toString();
        device_name = et_device_name.getText().toString();
        actual_position = et_actual_position.getText().toString();
        imei = Long.parseLong(et_imei.getText().toString());

        if(email.isEmpty()){
            et_email.setError("Provide your Email first!");
            et_email.requestFocus();
        }else if (password.isEmpty()) {
            et_password.setError("Set your password");
            et_password.requestFocus();
        }else if (device_name.isEmpty()) {
            et_device_name.setError("Oops! Enter Device Name.");
            et_device_name.requestFocus();
        }else if (actual_position.isEmpty()) {
            et_actual_position.setError("Oops! Field is Empty.");
            et_actual_position.requestFocus();
        }else if (!(email.isEmpty() && password.isEmpty())) {

            HashMap<String, Object> data = new HashMap<>();
            data.put("firestoreINSTANCE", db);
            data.put("firebaseAUTH", firebaseAuth);
            data.put("device_NAME", device_name);
            data.put("device_IMEI", imei);
            data.put("device_actual_POSITION",actual_position);
            data.put("email",email);
            data.put("password", password);

            RegisterDevice_background_Task register_device= new RegisterDevice_background_Task(RegisterDeviceActivity.this,data);
            register_device.execute();

        }else{
            Toast.makeText(RegisterDeviceActivity.this, "Fields Empty!", Toast.LENGTH_SHORT).show();
        }

    }
}
