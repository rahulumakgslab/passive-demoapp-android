package com.sonde.mentalfitness.domain

import android.content.Context
import android.content.Intent
import java.util.*
import java.util.concurrent.TimeUnit
import android.os.Build
import android.util.Log
import androidx.work.*


class DailyRecordingWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters
) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Log.d("!!!", "Started work manager==========")
        val intent = Intent(appContext, RecordingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 10:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 10)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
//        val timeDiff = 5*60*1000L//dueDate.timeInMillis - currentDate.timeInMillis
//        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyRecordingWorker>()
//            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build()
//        WorkManager.getInstance(appContext).enqueue(dailyWorkRequest)
        return Result.success()
    }
}