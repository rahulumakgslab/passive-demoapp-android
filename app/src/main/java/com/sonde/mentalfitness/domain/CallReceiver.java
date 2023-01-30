/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */

package com.sonde.mentalfitness.domain;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sonde.mentalfitness.presentation.ui.MainActivity;


public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallRecorder";
    public static final String ARG_NUM_PHONE = "arg_num_phone";
    public static final String ARG_INCOMING = "arg_incoming";
    private static boolean serviceStarted = false;
    private static ComponentName serviceName = null;

    public CallReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle;
        String state;
        String incomingNumber;
        String action = intent.getAction();
        Log.d(TAG, "====" + intent.toString());

        if (action != null && action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

            if ((bundle = intent.getExtras()) != null) {
                state = bundle.getString(TelephonyManager.EXTRA_STATE);
                Log.d(TAG, intent.getAction() + " " + state);
//                Toast.makeText(context, "Call state is " + state, Toast.LENGTH_LONG).show();

                if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    incomingNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    Log.d(TAG, "Incoming number: " + incomingNumber);
                    if (!serviceStarted) {
                        Intent intentService = new Intent(context, RecordingService.class);
                        serviceName = intentService.getComponent();
                        intentService.putExtra(ARG_NUM_PHONE, incomingNumber);
                        intentService.putExtra(ARG_INCOMING, true);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            context.startForegroundService(intentService);
                        else
                            context.startService(intentService);
                        serviceStarted = true;
                    }
                } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
//                    Intent intent1 = new Intent(context, MainActivity.class);
//                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    context.startActivity(intent1);
                    if (!serviceStarted) {
                        Intent intentService = new Intent(context, RecordingService.class);
                        serviceName = intentService.getComponent();
                        intentService.putExtra(ARG_INCOMING, false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            context.startForegroundService(intentService);
                        else
                            context.startService(intentService);
                        serviceStarted = true;
                    }
                } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    if (serviceStarted) {
                        Intent stopIntent = new Intent(context, RecordingService.class);
                        stopIntent.setComponent(serviceName);
                        context.stopService(stopIntent);
                        serviceStarted = false;
                    }
                    serviceName = null;
                }
            }
        }
    }

}
