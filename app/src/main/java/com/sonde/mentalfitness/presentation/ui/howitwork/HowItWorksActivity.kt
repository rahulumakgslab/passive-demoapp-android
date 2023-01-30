package com.sonde.mentalfitness.presentation.ui.howitwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sonde.mentalfitness.R

class HowItWorksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_it_works)

        //test
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, HowItWorksFragment())
        transaction.commit()
    }
}