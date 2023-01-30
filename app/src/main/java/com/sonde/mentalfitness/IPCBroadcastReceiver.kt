package com.sonde.mentalfitness

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sonde.mentalfitness.domain.DailyRecordingWorker
import java.util.concurrent.TimeUnit

class IPCBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val data = intent?.getStringExtra("data")
        Log.d("IPCBroadcastReceiver", "Data received==>$data")
//        Toast.makeText(context?.applicationContext, "Data received==>$data", Toast.LENGTH_LONG)
//            .show()
        val timeDiff = 0L
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyRecordingWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build()
        WorkManager.getInstance(context!!.applicationContext).enqueue(dailyWorkRequest)
    }
}