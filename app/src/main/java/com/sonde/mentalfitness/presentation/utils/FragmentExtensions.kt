package com.sonde.mentalfitness.presentation.utils

import androidx.fragment.app.Fragment
import com.sonde.mentalfitness.R

/**
 * Replace an existing fragment with new one.
 *
 * @param fragment new fragment.
 * @param addToBackStack add this transaction to the back stack.
 */
fun Fragment.replaceWithFragment(mainActivityContainerId : Int, fragment: Fragment, addToBackStack: Boolean) {

    val transaction = parentFragmentManager
        .beginTransaction()
        .setCustomAnimations(R.anim.fade_out, R.anim.fade_in, R.anim.fade_out, R.anim.fade_in)
        .replace(mainActivityContainerId, fragment)

    if (addToBackStack) {
        transaction.addToBackStack(null)
    }

    transaction.commit()
}
