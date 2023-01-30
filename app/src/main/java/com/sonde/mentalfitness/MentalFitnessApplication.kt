package com.sonde.mentalfitness

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.sonde.base.presentation.BaseApplication
import com.sonde.mentalfitness.Constants.Companion.channelID
import com.sonde.mentalfitness.Constants.Companion.notificationChannelName

class MentalFitnessApplication : BaseApplication() {


    override fun onCreate() {
        super.onCreate()
//        FirebaseApp.initializeApp(this);
        createNotificationChannel()
    }

    companion object {
        fun applicationContext(): Context {
            return BaseApplication.applicationContext()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel: NotificationChannel = NotificationChannel(
                channelID,
                notificationChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onTerminate() {
        Log.d("CallAudioFileBuilder", "onTerminate")
        super.onTerminate()
    }
}