package com.sonde.mentalfitness.presentation.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.presentation.ui.signup.SignupActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        findViewById<Button>(R.id.get_started).setOnClickListener({
            startActivity(Intent(this, SignupActivity::class.java))
        })
    }
}