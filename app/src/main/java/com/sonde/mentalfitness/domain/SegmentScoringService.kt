package com.sonde.mentalfitness.domain

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.domain.model.SegmentScore
import com.sondeservices.common.HealthCheckType
import com.sondeservices.edge.inference.InferenceCallback
import com.sondeservices.edge.inference.InferenceEngine
import com.sondeservices.edge.ml.model.Gender
import com.sondeservices.edge.ml.model.MetaData
import com.sondeservices.edge.ml.model.Score
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.Date

const val ARG_FILE_PATH = "file_path"
val GENDER = "gender"
val YEAR = "year"
val SEGMENT_NUMBER = "segmentNumber"

const val JOB_ID = 3
class SegmentScoringService : JobIntentService() {

    val TAG="SegmentScoringService"
    companion object {
        fun newInstance(context: Context, filePath: String, gender: String, year: Int, segmentNumber:Int): Intent {
            val intent = Intent(context, SegmentScoringService::class.java)
            intent.putExtra(ARG_FILE_PATH, filePath)
            intent.putExtra(GENDER, gender)
            intent.putExtra(YEAR, year)
            intent.putExtra(SEGMENT_NUMBER, segmentNumber)
            return intent
        }

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, SegmentScoringService::class.java, JOB_ID, intent)
        }
    }
    override fun onHandleWork(intent: Intent) {
        val wavFilePath = intent.getStringExtra(ARG_FILE_PATH)!!
        val gender = intent.getStringExtra(GENDER)
        val year = intent.getIntExtra(YEAR, 2000)
        val segmentNumber = intent.getIntExtra(SEGMENT_NUMBER, 0)
        val inferenceEngine =
            InferenceEngine.createInference(
                applicationContext,
                MetaData(Gender.valueOf(gender!!), year)
            )
        inferenceEngine.inferScore(
            wavFilePath,
            HealthCheckType.MENTAL_FITNESS,
            object : InferenceCallback {
                override fun onError(throwable: Throwable) {
                    Log.e(TAG, "Error : $throwable")
                }

                override fun onScore(score: Score) {
                    Log.d(TAG, "Score is ==>${score.getValue()}")
                    val segmentScore=SegmentScore(score, segmentNumber,0,getTotalTimeElapsed())
                    File(wavFilePath).delete()
                    EventBus.getDefault().post(segmentScore)
                }
            }
        )
    }

    fun getTotalTimeElapsed(): Long {
        val sharedPreferenceHelper =
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        val savedTime = sharedPreferenceHelper.getTotalTimeElapsed()
        val difference = Date().time - savedTime
        return difference / 1000
    }
}