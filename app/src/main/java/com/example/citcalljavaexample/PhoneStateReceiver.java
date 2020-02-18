package com.example.citcalljavaexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            System.out.println("Reciver Start");
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            String lastFourDigits = "";

            //ringing
            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                if(incomingNumber != null && !incomingNumber.isEmpty() && !incomingNumber.equals("null")) {
                    lastFourDigits = incomingNumber.substring(incomingNumber.length() - 4);
                    Intent i = new Intent("android.intent.action.last_four_digit").putExtra(
                            "last_four_digit", lastFourDigits);
                    context.sendBroadcast(i);
                }
            }

            //answered
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
                Intent doVerify = new Intent("android.intent.action.verify").putExtra(
                        "verify", "yes");
                context.sendBroadcast(doVerify);
            }

            //rejectted
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                Intent doVerify = new Intent("android.intent.action.verify").putExtra(
                        "verify", "yes");
                context.sendBroadcast(doVerify);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
