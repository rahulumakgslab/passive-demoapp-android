package com.sonde.mentalfitness.presentation.ui.signup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sonde.mentalfitness.R

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, SignupStepFragment.newInstance())
        transaction.commit()
    }
}