package com.twilio.voipcall.voiceprocessing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sondeservices.edge.ml.model.Score
import com.twilio.voipcall.R
import com.twilio.voipcall.score.ScoreFragment
import android.app.NotificationManager
import com.sondeservices.edge.ml.model.VFFinalScore
import com.twilio.voipcall.score.VoipMFScoreFragment


private const val ARG_AUDIO_FILE_PATH = "audio_file_path"
private const val ARG_SCORE_DETAILS = "score_details"
private const val ARG_NOTI_ID = "notification_id"

class WavProcessingActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, audioFilePath: String): Intent {
            val intent = Intent(context, WavProcessingActivity::class.java)
            intent.putExtra(ARG_AUDIO_FILE_PATH, audioFilePath)
            return intent
        }

        fun newInstance(context: Context, score: VFFinalScore, notiId : Int): Intent {
            val intent = Intent(context, WavProcessingActivity::class.java)
            intent.putExtra(ARG_SCORE_DETAILS, score)
            intent.putExtra(ARG_NOTI_ID,notiId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_processing)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val transaction = supportFragmentManager.beginTransaction()
        if (intent.hasExtra(ARG_SCORE_DETAILS)) {
            manager.cancel(intent.getIntExtra(ARG_NOTI_ID, 0))
            val score = intent.getParcelableExtra<VFFinalScore>(ARG_SCORE_DETAILS)
            if (score != null) {
                transaction.replace(
                    R.id.container, VoipMFScoreFragment.newInstance(score)
                )
            }
        } else {
            transaction.replace(
                R.id.container, WavProcessingFragment.newInstance(
                    intent.getStringExtra(ARG_AUDIO_FILE_PATH)
                        ?: ""
                )
            )
        }
        transaction.commit()
    }
}