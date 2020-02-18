package com.example.citcalljavaexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class InputOtp extends AppCompatActivity {

    EditText lastFourDigits;
    TextView firstToken;
    Boolean verifyCalled = false;
    String getTrxid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_otp);

        firstToken = (TextView) findViewById(R.id.FirstToken);
        lastFourDigits = (EditText) findViewById(R.id.inputOtp);

        Bundle bundle=getIntent().getExtras();
        getTrxid = bundle.getString("trxid");
        String first_token = bundle.getString("first_token");
        String length = bundle.getString("length");
        firstToken.setText(first_token);

        findViewById(R.id.btn_verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyOtp();
            }
        });
    }

    private void verifyOtp() {
        //broadcast receiver sometimes call multiple time
        // so we validate first
        if(!verifyCalled) {
            verifyCalled = true;
            System.out.println("verifyOtp called!");

            final String first = firstToken.getText().toString().trim();
            final String last = lastFourDigits.getText().toString().trim();
            final  String trxid = getTrxid;

            if (TextUtils.isEmpty(first)) {
                firstToken.setError("Please Fill Country Code");
                firstToken.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(last)) {
                lastFourDigits.setError("Please Fill Mobile Number");
                lastFourDigits.requestFocus();
                return;
            }

            class VarifyOtp extends AsyncTask<Void, Void, String> {

                private ProgressBar progressBar;
                private Button btnRequest;

                protected String doInBackground(Void... voids) {
                    //creating request handler object
                    RequestHandler requestHandler = new RequestHandler();
                    //creating request parameters
                    HashMap<String, String> params = new HashMap<>();
                    params.put("code", first + last);
                    params.put("trxid", trxid);
                    //returing the response
                    return requestHandler.sendPostRequest(URLs.URL_VERIFY, params);
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
                            finish();
                            startActivity(new Intent(InputOtp.this, Success.class));
                        } else {
                            Toast.makeText(getApplicationContext(), obj.getString("info"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            VarifyOtp vo = new VarifyOtp();
            vo.execute();
        }
    }

    private BroadcastReceiver fourDigitsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Extract your data - better to use constants...
            String get_last_four = intent.getStringExtra("last_four_digit");
            if(get_last_four != null && !get_last_four.isEmpty() && !get_last_four.equals("null")) {
                lastFourDigits.setText(get_last_four);
            }
        }
    };

    private BroadcastReceiver verifyCommandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("verify")) {
                String commandVerify = intent.getStringExtra("verify");
                if (commandVerify.equals("yes")) {
                    verifyOtp();
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if(fourDigitsReceiver != null){
                unregisterReceiver(fourDigitsReceiver);
            }
            if(verifyCommandReceiver != null){
                unregisterReceiver(verifyCommandReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.last_four_digit");
        filter.addAction("android.intent.action.verify");
        registerReceiver(fourDigitsReceiver , filter);
        registerReceiver(verifyCommandReceiver , filter);
    }
}
