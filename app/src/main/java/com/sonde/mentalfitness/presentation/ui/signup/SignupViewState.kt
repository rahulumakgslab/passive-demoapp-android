package com.sonde.mentalfitness.presentation.ui.signup

sealed class SignupViewState {
    object Loading : SignupViewState()
    data class ShowError(val messageResId: Int) : SignupViewState()
    object SignupComplete : SignupViewState()
}
