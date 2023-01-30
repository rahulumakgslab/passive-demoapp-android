package com.sonde.mentalfitness.presentation.ui.record.recording

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.ui.record.countdown.CountDownFragment


class RecordingHostActivity : AppCompatActivity() {

    companion object {

        const val EXTRA_LUNCH_MODE = "lunch_mode"
        const val EXTRA_PASSAGE_DATA = "passage_data"

        const val COUNT_DOWN_LUNCH_MODE = 1

        @JvmStatic
        fun newInstanceCountdownMode(context: Context, passageModel: PassageModel) =
            Intent(context, RecordingHostActivity::class.java).apply {
                putExtra(EXTRA_LUNCH_MODE, COUNT_DOWN_LUNCH_MODE)
                putExtra(EXTRA_PASSAGE_DATA, passageModel)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording_host)

        val extras = intent.extras
        val lunchMode = extras?.getInt(EXTRA_LUNCH_MODE)

        lunchMode?.let {
            when (lunchMode) {
                COUNT_DOWN_LUNCH_MODE -> {
                    val passageModel: PassageModel = extras.getParcelable(EXTRA_PASSAGE_DATA)!!
                    lunchFragment(CountDownFragment.newInstance(passageModel))
                }
            }
        }

    }

    private fun lunchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }
}