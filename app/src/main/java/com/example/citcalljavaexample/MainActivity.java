package com.example.citcalljavaexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int PHONE_STATE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionPhoneState();
    }

    public void requestPermissionPhoneState() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if((ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_PERMISSION);
            } else {
                startActivity(new Intent(MainActivity.this, MobileVerification.class));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PHONE_STATE_PERMISSION: {
                // If READ_PHONE_STATE denied
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(MainActivity.this, MobileVerification.class));
                } else {
                    startActivity(new Intent(MainActivity.this, NoPermissionActivity.class));
                }
                return;
            }
        }
    }
}
