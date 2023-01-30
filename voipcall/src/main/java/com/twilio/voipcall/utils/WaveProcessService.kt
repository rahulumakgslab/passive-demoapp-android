package com.twilio.voipcall.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.sondeservices.common.HealthCheckType
import com.sondeservices.edge.inference.InferenceCallback
import com.sondeservices.edge.inference.InferenceEngine
import com.sondeservices.edge.ml.model.Gender
import com.sondeservices.edge.ml.model.MetaData
import com.sondeservices.edge.ml.model.Score
import com.sondeservices.edge.ml.model.VFFinalScore
import com.twilio.voipcall.R
import com.twilio.voipcall.voiceprocessing.WavProcessingActivity
import org.greenrobot.eventbus.EventBus

const val ARG_FILE_PATH = "file_path"
val GENDER = "gender"
val YEAR = "year"

const val JOB_ID = 2

class WaveProcessService : JobIntentService() {
    //    val context : Context = BaseApplication.applicationContext()
    lateinit var notificationManager: NotificationManager

    companion object {
        fun newInstance(context: Context, filePath: String, gender: String, year: Int): Intent {
            val intent = Intent(context, WaveProcessService::class.java)
            intent.putExtra(ARG_FILE_PATH, filePath)
            intent.putExtra(GENDER, gender)
            intent.putExtra(YEAR, year)
            return intent
        }

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, WaveProcessService::class.java, JOB_ID, intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    val CHANNEL_ID = "score"
    override fun onHandleWork(intent: Intent) {
        val wavFilePath = intent.getStringExtra(ARG_FILE_PATH)!!
        val gender = intent.getStringExtra(GENDER)
        val year = intent.getIntExtra(YEAR, 2000)
        val inferenceEngine =
            InferenceEngine.createInference(
                applicationContext,
                MetaData(Gender.valueOf(gender!!), year)
            ) //TODO Get dynamic gender and birth year
        inferenceEngine.inferScore(
            wavFilePath,
            HealthCheckType.MENTAL_FITNESS,
            object : InferenceCallback {
                override fun onError(throwable: Throwable) {
                    Log.e("!!!", "Error : $throwable")
                }

                override fun onScore(score: Score) {
                    Log.d("!!", "### Score is ${score.getValue()}")
                    displayNotification(score)
                    EventBus.getDefault().post(score)
                }

            }

        )
    }

    private fun displayNotification(score: Score) {
        val notiId = System.currentTimeMillis().toInt()
        val intent = WavProcessingActivity.newInstance(this, score as VFFinalScore, notiId)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sonde_noti)
            .setColor(getColor(R.color.background_navy_blue))
            .setContentTitle("Your score is ready!")
            .setContentText("Please click here to see")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
        notificationManager.notify(notiId, builder.build())

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}