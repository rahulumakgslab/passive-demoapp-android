package com.sonde.base.presentation.utils.extensions

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.sonde.base.R
import com.sonde.base.presentation.utils.listener.AlertDialogNegativeButtonListener
import com.sonde.base.presentation.utils.listener.AlertDialogPositiveButtonListener


fun View.showKeyboard() {
    (this.context.getSystemService(Service.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    (this.context.getSystemService(Service.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.toVisible() {
    this.visibility = View.VISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.toInvisible() {
    this.visibility = View.GONE
}


var alert: AlertDialog? = null

fun Context.showAlertDialog(
    requestCode: Int? = null,
    titleResId: Int? = null,
    messageResId: Int? = null,
    positiveButtonTextResId: Int = R.string.common_ok,
    alertDialogPositiveButtonListener: AlertDialogPositiveButtonListener? = null,
    negativeButtonTextResId: Int? = null,
    alertDialogNegativeButtonListener: AlertDialogNegativeButtonListener? = null,
    setCanceledOnTouchOutside: Boolean = false
) {
    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
    titleResId?.let {
        alertDialog.setTitle(titleResId)
    }

    messageResId?.let {
        alertDialog.setMessage(messageResId)
    }

    positiveButtonTextResId.let {
        alertDialog.setPositiveButton(
            getString(positiveButtonTextResId)
        ) { _, _ ->
            requestCode?.let {
                alertDialogPositiveButtonListener?.OnPositiveButtonClicked(requestCode)
            }
        }
    }
    negativeButtonTextResId?.let {

        alertDialog.setNegativeButton(
            negativeButtonTextResId
        ) { _, _ ->
            requestCode?.let {
                alertDialogNegativeButtonListener?.OnNegativeButtonClicked(requestCode)
            }
        }
    }

    if (alert == null) {
        alert = alertDialog.create()
    } else {
        alert?.dismiss()
        alert = null
        alert = alertDialog.create()
    }

    alert?.setCanceledOnTouchOutside(setCanceledOnTouchOutside)
    alert?.show()
}