# Use Android [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver) to Read Incoming Call
In this demo, we will walk you through using the Android [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver) to let your users receive Miscall OTP without typing the code.

If you get stuck at any point, you can check out the finished source code on our [GitHub](https://github.com/citcall/android-java-autoinput-demo).

## Setup
If you haven't already, prepare your Android apps backend's API. You can see [sample backend with PHP](https://github.com/citcall/backend).

## Permissions
Make sure your users allowed these permissions!

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.CALL_PHONE" />

## [Receiver](https://developer.android.com/guide/topics/manifest/receiver-element)

    <receiver android:name="com.citcalldemo.citcalljavaexample.PhoneStateReceiver">
        <intent-filter>
            <action android:name="android.intent.action.PHONE_STATE" />
        </intent-filter>
    </receiver>
## User interface
You also can see the interface [here](https://github.com/citcall/android-java-autoinput-demo/blob/master/app/src/main/res/layout/activity_mobile_verification.xml)

    <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MobileVerification">
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@android:color/white"
            android:padding="@dimen/space_x2">
            <ImageView
                android:id="@+id/img_logo"
                android:layout_width="@dimen/space_x14"
                android:layout_height="@dimen/space_x5"
                android:layout_centerHorizontal="true"
                android:layout_marginTop="@dimen/space_x2"
                android:src="@drawable/citcall" />
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_above="@+id/txt_copyright"
                android:layout_below="@+id/img_logo"
                android:layout_centerInParent="true"
                android:gravity="center"
                android:orientation="vertical"
                android:paddingLeft="@dimen/space_x2"
                android:paddingRight="@dimen/space_x2">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="@dimen/space_x2"
                    android:orientation="horizontal">
                    <EditText
                        android:id="@+id/field_phone_prefix"
                        style="@style/EditTextAppearance"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginRight="@dimen/space_x1"
                        android:gravity="center"
                        android:minWidth="@dimen/space_x6"
                        android:text="+62" />
                    <EditText
                        android:id="@+id/field_phone_suffix"
                        style="@style/EditTextAppearance"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:digits="0123456789"
                        android:inputType="number" />
                </LinearLayout>
                <Button
                    android:id="@+id/btn_verify"
                    style="@style/ButtonPrimary"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="@dimen/space_half"
                    android:text="Verifiy" />
                <ProgressBar
                    android:visibility="gone"
                    android:id="@+id/progressBar"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_centerHorizontal="true"
                    android:layout_centerVertical="true" />
            </LinearLayout>
            <TextView
                android:id="@+id/txt_copyright"
                style="@style/TextAppearanceRegularDisplay"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentBottom="true"
                android:layout_centerHorizontal="true"
                android:padding="@dimen/space_x2"
                android:text="citcall.com" />
        </RelativeLayout>
    </androidx.constraintlayout.widget.ConstraintLayout>

Users will fill prefix and number and click btn_verify button to make and your android App doing request to your API.

## Request OTP

Move back over to your [MobileVerification.java](https://github.com/citcall/android-java-autoinput-demo/blob/master/app/src/main/java/com/citcalldemo/citcalljavaexample/MobileVerification.java) file. In onCreate, set an onClickListener for the button. When the button is clicked, you want to call requestOtp() method that will doing request to your API.

    //onCreate
    prefix = (EditText)findViewById(R.id.field_phone_prefix);
    mobileNumber = (EditText)findViewById(R.id.field_phone_suffix);

    findViewById(R.id.btn_verify).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            requestOtp();
        }
    });

you will use an [AsyncTask](https://developer.android.com/reference/android/os/AsyncTask)

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

After request sent, you need to parse the result returned from your API.

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

it will create an intent that open [InputOtp.java](https://github.com/citcall/android-java-autoinput-demo/blob/master/app/src/main/java/com/citcalldemo/citcalljavaexample/InputOtp.java).

## Verify phone number
You need to handle [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver) based on [PhoneStateReceiver.java](https://github.com/citcall/android-java-autoinput-demo/blob/master/app/src/main/java/com/citcalldemo/citcalljavaexample/PhoneStateReceiver.java) see [InputOtp.java](https://github.com/citcall/android-java-autoinput-demo/blob/master/app/src/main/java/com/citcalldemo/citcalljavaexample/InputOtp.java) on line 129 to 174

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

When miscall received and state changed it will call verifyOtp() method that will doing request to your API.

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

you will use an [AsyncTask](https://developer.android.com/reference/android/os/AsyncTask)

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

After request sent, you need to parse the result returned from your API.

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
    
if error respon = false it will create an intent that open [Success.java](https://github.com/citcall/android-java-autoinput-demo/blob/master/app/src/main/java/com/citcalldemo/citcalljavaexample/Success.java).

I hope this gives a good example of how to use the Android [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver) to Read Incoming Call.


Contribute
----------

1. Check for open issues or open a new issue for a feature request or a bug
2. Fork [the repository][] on Github to start making your changes to the
    `master` branch (or branch off of it)
3. Write a test which shows that the bug was fixed or that the feature works as expected
4. Send a pull request and bug us until We merge it
