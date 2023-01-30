package com.sonde.mentalfitness.presentation.ui.pin.verify

sealed class VerifyPinViewState {
    object Loading : VerifyPinViewState()
    data class ShowError(val messageResId: Int) : VerifyPinViewState()
}