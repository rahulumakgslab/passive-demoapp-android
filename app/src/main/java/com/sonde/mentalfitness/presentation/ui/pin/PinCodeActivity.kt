package com.sonde.mentalfitness.presentation.ui.pin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.presentation.ui.pin.set.SetPinFragment
import com.sonde.mentalfitness.presentation.ui.pin.verify.VerifyPinFragment

const val ARG_IS_SHOW_SET_PIN = "is_show_set_pin"

class PinCodeActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, isShowSetPin: Boolean): Intent {
            val intent = Intent(context, PinCodeActivity::class.java)
            intent.putExtra(ARG_IS_SHOW_SET_PIN, isShowSetPin)
            if (isShowSetPin) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_code)
        if (intent.getBooleanExtra(ARG_IS_SHOW_SET_PIN, false)) {
            showSetPinScreen()
        } else {
            showVerifyPinScreen()
        }
    }

    private fun showSetPinScreen() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, SetPinFragment.newInstance())
        transaction.commit()
    }

    private fun showVerifyPinScreen() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, VerifyPinFragment.newInstance())
        transaction.commit()
    }
}