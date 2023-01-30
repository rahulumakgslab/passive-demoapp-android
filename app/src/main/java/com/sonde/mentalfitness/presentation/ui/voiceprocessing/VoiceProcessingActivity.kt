package com.sonde.mentalfitness.presentation.ui.voiceprocessing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.model.checkin.PassageModel

private const val ARG_AUDIO_FILE_PATH = "audio_file_path"
private const val ARG_PASSAGE_DATA = "passage_data"

class VoiceProcessingActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, audioFilePath: String, passageModel: PassageModel): Intent {
            val intent = Intent(context, VoiceProcessingActivity::class.java)
            intent.putExtra(ARG_AUDIO_FILE_PATH, audioFilePath)
            intent.putExtra(ARG_PASSAGE_DATA, passageModel)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_processing)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.container, VoiceProcessingFragment.newInstance(
                intent.getStringExtra(ARG_AUDIO_FILE_PATH)
                    ?: "",
                intent.getParcelableExtra(ARG_PASSAGE_DATA) ?: PassageModel("", 0)
            )
        )
        transaction.commit()
    }
}