package com.example.firestorescanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static com.example.firestorescanner.Helper.check_If_SP_exists;

public class SplashScreenActivity extends AppCompatActivity implements Constants{

    private static final int PERMISSION_REQUEST_CODE = 200;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    public static final String TAG = logCatTag_Activity_SPLASH_SCREEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Log.d(TAG, "onCreate()");

        firebaseAuth = FirebaseAuth.getInstance();
        Permissions( new String[]{ACCESS_FINE_LOCATION,ACCESS_NETWORK_STATE,READ_PHONE_STATE,CAMERA});

        if(!check_If_SP_exists(SplashScreenActivity.this)) {
            startActivity(new Intent(SplashScreenActivity.this, RegisterDeviceActivity.class));
            finish();
        }
        else{
            check_If_USER_Already_Logged_In();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        firebaseAuth.addAuthStateListener(authStateListener);
        Log.d(TAG, "Adding AuthStateListener.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Removing AuthStateListener.");
        firebaseAuth.removeAuthStateListener(authStateListener);
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    public void Permissions(String[] permissions)
    {
        Log.d(TAG, "Permissions() called.");
        List<String> permissions_disabled = new ArrayList<>();

        for(String permission : permissions)
        {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED)
            {
                permissions_disabled.add(permission);
            }
        }
        if(permissions_disabled.size() > 0) {
            String[] temp_permissions_disabled = permissions_disabled.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, temp_permissions_disabled, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() called.");
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    List<String> permission_still_not_enabled = new ArrayList<>();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    {
                        for(int i= 0; i< grantResults.length;i++)
                        {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                            {
                                permission_still_not_enabled.add(permissions[i]);
                            }
                        }

                        if(permission_still_not_enabled.size() > 0) {
                            String[] temp_permission_still_not_enabled = permission_still_not_enabled.toArray(new String[0]);
                            ActivityCompat.requestPermissions(this, temp_permission_still_not_enabled, PERMISSION_REQUEST_CODE);
                        }else
                            break;
                    }
                    else
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        for(int i= 0; i< grantResults.length;i++)
                        {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                            {
                                permission_still_not_enabled.add(permissions[i]);
                            }
                        }

                        if(permission_still_not_enabled.size() > 0) {
                            String[] temp_permission_still_not_enabled = permission_still_not_enabled.toArray(new String[0]);
                            ActivityCompat.requestPermissions(this, temp_permission_still_not_enabled, PERMISSION_REQUEST_CODE);
                        }else {
                            if(!check_If_SP_exists(SplashScreenActivity.this)) {
                                startActivity(new Intent(SplashScreenActivity.this, RegisterDeviceActivity.class));
                                finish();
                            }
                            else{
                                //TODO: check if user is logged in.
                                check_If_USER_Already_Logged_In();
                            }
                            break;
                        }
                    }

                }
                break;
        }
    }

/**check if user is logged in.**/
    private void check_If_USER_Already_Logged_In() {
        Log.d(TAG, "check_If_USER_Already_Logged_In() called.");

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //TODO: check if device exists and is not blocked.
                    Toast.makeText(SplashScreenActivity.this, "User Logged In.", Toast.LENGTH_SHORT).show();
                    Log.d(logCatTag_Activity_SPLASH_SCREEN, "User Logged In.");
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SplashScreenActivity.this, "Login to continue.", Toast.LENGTH_SHORT).show();
                    Log.d(logCatTag_Activity_SPLASH_SCREEN, "Login to continue.");
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }


}
