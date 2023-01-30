package com.sonde.mentalfitness.presentation.ui.pin.set

sealed class SetPinViewState {
    object Loading : SetPinViewState()
    data class ShowError(val messageResId: Int) : SetPinViewState()
}
