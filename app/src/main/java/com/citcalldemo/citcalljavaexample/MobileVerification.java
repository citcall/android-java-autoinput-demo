package com.citcalldemo.citcalljavaexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MobileVerification extends AppCompatActivity {

    public static final int CALL_LOG_PERMISSION = 1;
    EditText prefix,mobileNumber;

    public int retrySquence = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_verification);

        requestPermissionCallLog();

        prefix = (EditText)findViewById(R.id.field_phone_prefix);
        mobileNumber = (EditText)findViewById(R.id.field_phone_suffix);

        findViewById(R.id.btn_verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestOtp();
            }
        });
    }

    public void requestPermissionCallLog() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if((ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MobileVerification.this,
                        new String[] {Manifest.permission.READ_CALL_LOG}, CALL_LOG_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CALL_LOG_PERMISSION: {
                // If READ_PHONE_STATE denied
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                    startActivity(new Intent(MobileVerification.this, NoPermissionActivity.class));
                }
                return;
            }
        }
    }

    private void requestOtp() {
        final String reqPrefix = prefix.getText().toString().trim();
        final String reqMsisdn = mobileNumber.getText().toString().trim();

        if (TextUtils.isEmpty(reqPrefix)) {
            prefix.setError("Please Fill Country Code");
            prefix.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(reqMsisdn)) {
            mobileNumber.setError("Please Fill Mobile Number");
            mobileNumber.requestFocus();
            return;
        }

        class RequestOtp extends AsyncTask<Void, Void, String> {

            private ProgressBar progressBar;
            private Button btnRequest;

            protected String doInBackground(Void... voids) {
                //creating request handler object
                RequestHandler requestHandler = new RequestHandler();
                //creating request parameters
                HashMap<String, String> params = new HashMap<>();
                params.put("msisdn", reqPrefix + reqMsisdn);
                params.put("retry", String.valueOf(retrySquence));
                //returing the response
                return requestHandler.sendPostRequest(URLs.URL_REQUEST, params);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                btnRequest = (Button) findViewById(R.id.btn_verify);
                btnRequest.setEnabled(false);
                //displaying the progress bar while user registers on the server
                progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //hiding the progressbar after completion
                progressBar.setVisibility(View.GONE);
                btnRequest.setEnabled(true);
                try {
                    //converting response to json object
                    JSONObject obj = new JSONObject(s);
                    //if no error in response
                    if (!obj.getBoolean("error")) {
                        Intent i = new Intent(MobileVerification.this,InputOtp.class);
                        i.putExtra("trxid", obj.getString("trxid"));
                        i.putExtra("first_token", obj.getString("first_token"));
                        i.putExtra("length", obj.getString("length"));
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), obj.getString("info"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        RequestOtp ro = new RequestOtp();
        ro.execute();
    }
}
