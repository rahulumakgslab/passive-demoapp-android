package com.sonde.mentalfitness.domain

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sonde.mentalfitness.Constants.Companion.channelID
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.data.Result
import com.sonde.mentalfitness.data.local.db.AppDatabase
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.data.local.user.UserLocalDataSourceImpl
import com.sonde.mentalfitness.data.remote.network.RetrofitBuilder
import com.sonde.mentalfitness.data.remote.user.UserRemoteDataSourceImpl
import com.sonde.mentalfitness.data.repository.UserRepositoryImpl
import com.sonde.mentalfitness.presentation.ui.MainActivity
import com.sonde.voip_vad.OnWaveFileReadyListener
import com.sondeservices.edge.ml.model.Gender
import com.sondeservices.edge.ml.model.MetaData
import com.twilio.voipcall.utils.WaveProcessService.Companion.enqueueWork
import com.twilio.voipcall.utils.WaveProcessService.Companion.newInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val ARG_IS_CALL_RECORDING = "is_call_recording"

class RecordingService : Service(), OnWaveFileReadyListener {
    lateinit var recordingManager: RecordingManager
    val sharedPreferenceHelper =
        SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
    private val userRepository = UserRepositoryImpl(
        UserRemoteDataSourceImpl(RetrofitBuilder.apiService),
        UserLocalDataSourceImpl(
            AppDatabase.db.userDao(),
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        )
    )

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
//        Log.d("CallAudioFileBuilder", "onDestroy")
//        Log.d("CallAudioFileBuilder", "onDestroy 2")
        recordingManager.onStopCapturing()
        recordingManager.forceBuildAudioFile()
        sharedPreferenceHelper.setRecordingServiceRunning(false)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedPreferenceHelper.setRecordingServiceRunning(true)
        val isCallRecording = intent?.getBooleanExtra(ARG_IS_CALL_RECORDING, false)
        var delay: Long = 0
        if (isCallRecording == true) {
            delay = 5000 // 5 seconds
        }

        Handler().postDelayed({
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

            val notification = NotificationCompat.Builder(this, channelID)
                .setContentTitle("Sonde")
                .setContentText("Your voice is being analyzing for checking your mental fitness")
                .setSmallIcon(com.twilio.voipcall.R.drawable.ic_sonde_noti)
                .setColor(getColor(com.twilio.voipcall.R.color.background_navy_blue))
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1, notification)
            if (!this::recordingManager.isInitialized) {
                recordingManager = RecordingManager(
                    MentalFitnessApplication.applicationContext(),
                    MediaRecorder.AudioSource.MIC
                )
                recordingManager.setOnWaveFileReadyListener(this)
            }
            recordingManager.onStartCapturing()
            sharedPreferenceHelper.setTotalTimeElapsed()
        }, delay)
        return START_NOT_STICKY
    }

    override fun onWavFileReady(waveFilePath: String) {
        Log.d("!!!", "File is ready, path is $waveFilePath")
        recordingManager.onStopCapturing()
        val mContext = this
        GlobalScope.launch {
            try {
                userRepository.getUser().collect {
                    var gender = "MALE"
                    var year = 2000
                    when (it) {
                        is Result.Success -> {
                            gender = it.data.sex
                            year = Integer.parseInt(it.data.birthYear)
                        }
                    }
                    val intent = newInstance(mContext, waveFilePath, gender, year)
                    enqueueWork(mContext, intent)
                }
            } catch (e: Exception) {
                Log.d("calculateScore!!", "$e")
            }
        }

        sharedPreferenceHelper.setRecordingServiceRunning(false)
        stopForeground(true)
    }

    override fun onWavFileReadyForSegmentScore(waveFilePath: String, segmentNumber:Int) {
        Log.d("RecordingService", "File is ready, path is $waveFilePath")
        val mContext = this
        GlobalScope.launch {
            try {
                userRepository.getUser().collect {
                    var gender = "MALE"
                    var year = 2000
                    when (it) {
                        is Result.Success -> {
                            gender = it.data.sex
                            year = Integer.parseInt(it.data.birthYear)
                        }
                    }
                    val intent = SegmentScoringService.newInstance(mContext, waveFilePath, gender, year,segmentNumber)
                    SegmentScoringService.enqueueWork(mContext, intent)
                }
            } catch (e: Exception) {
                Log.d("calculateScore!!", "$e")
            }
        }
    }

    override fun onWavFileReadyForSegmentScore(waveFilePath: String) {

    }

}