package com.sonde.mentalfitness.presentation.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.sonde.mentalfitness.Constants
import com.sonde.mentalfitness.R
import java.util.*

object Common {
    fun getYears(): List<String> {
        val years = ArrayList<String>()
        val currentYear = Calendar.getInstance()[Calendar.YEAR]
        for (i in (currentYear - Constants.MIN_AGE) downTo Constants.START_YEAR) {
            years.add(i.toString())
        }
        return years
    }
}

fun showSnackbar(view: View, message: String) {
    try {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(Color.parseColor("#52C3FF"))
            .setTextColor(Color.parseColor("#000000"))
            .show()
    } catch (e: IllegalArgumentException) {
        Log.d("showSnackbar", e.message.toString())
    }
}

fun showLongSnackbar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

fun showSnackbar(view: View, @StringRes msgStringResId: Int) {
    Snackbar.make(view, msgStringResId, Snackbar.LENGTH_SHORT).show()
}

fun showDialog(context: Context, title: String, onDialogOkButtonClickedListener: OnDialogOkButtonClickedListener) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.alert_dialog_layout)
    val body = dialog.findViewById(R.id.text_message) as TextView
    body.text = title
    val yesBtn = dialog.findViewById(R.id.btn_ok) as Button

    yesBtn.setOnClickListener {
        onDialogOkButtonClickedListener.onDialogOkButtonClicked()
        dialog.dismiss()
    }
    dialog.show()

}

interface OnDialogOkButtonClickedListener {
    fun onDialogOkButtonClicked()
}