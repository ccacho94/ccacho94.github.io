package com.example.weighttrackingapp_chelseacacho;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

public class SmsHelper {

    private final Context context;

    public SmsHelper(Context context) {
        this.context = context;
    }

    public boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean sendSms(String phoneNumber, String message) {
        if (!hasSmsPermission()) {
            return false;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendGoalReachedSms(String phoneNumber, double currentWeight, GoalManager goalManager) {
        String message = "Congrats! You reached your weight goal. " +
                "Current weight: " + currentWeight +
                ". Target weight: " + goalManager.getTargetWeight() +
                ". Goal type: " + goalManager.getGoalDirectionText();

        return sendSms(phoneNumber, message);
    }
}