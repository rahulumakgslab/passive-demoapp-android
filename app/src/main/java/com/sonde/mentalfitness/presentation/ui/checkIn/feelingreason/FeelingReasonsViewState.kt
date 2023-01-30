package com.sonde.mentalfitness.presentation.ui.checkIn.feelingreason

sealed class FeelingReasonsViewState  {

    data class ShowError(val messageResId: Int) : FeelingReasonsViewState()
}