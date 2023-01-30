package com.sonde.mentalfitness.presentation.ui.record.foregroundrecording

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.ui.textIndependent.SharedViewModel

class PassiveForegroundRecordingActivity : AppCompatActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Init shared view model before onCreate is called
        if (!sharedViewModel.isInitialized) {
            sharedViewModel.init(this)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passive_foreground_recording)
        val passageModel= PassageModel("Your passive session is about to begin",3)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, PassiveForegroundCountDownFragment.newInstance(passageModel))
            .commit()
    }
}