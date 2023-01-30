package com.sonde.mentalfitness.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.presentation.ui.MainActivity
import com.sonde.mentalfitness.presentation.ui.pin.PinCodeActivity
import com.sonde.mentalfitness.presentation.ui.welcome.WelcomeActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var viewModel: SplashViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        observe(viewModel.event, ::onViewEvent)
    }

    private fun onViewEvent(viewEvent: SplashViewEvent) {
        when (viewEvent) {
            is SplashViewEvent.ShowVerifyPin -> {
                showVerifyPinScreen()
            }
            is SplashViewEvent.NoVerifyPin -> {
                launchCheckInScreen()
            }
            is SplashViewEvent.UserNotLoggedIn -> {
                launchWelComeScreen()
            }
            is SplashViewEvent.ShowSetPin -> {
                showSetPinScreen()
            }
        }
    }

    private fun launchWelComeScreen() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    private fun launchCheckInScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showVerifyPinScreen() {
        startActivity(PinCodeActivity.newInstance(this, false))
        finish()
    }

    private fun showSetPinScreen() {
        startActivity(PinCodeActivity.newInstance(this, true))
        finish()
    }
}