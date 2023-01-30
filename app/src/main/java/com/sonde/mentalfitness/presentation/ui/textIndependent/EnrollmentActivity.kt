package com.sonde.mentalfitness.presentation.ui.textIndependent

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sonde.mentalfitness.R

class EnrollmentActivity : AppCompatActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Init shared view model before onCreate is called
        if (!sharedViewModel.isInitialized) {
            sharedViewModel.init(this)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrollment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, EnrollmenUserFragment())
            .commit()
    }
}