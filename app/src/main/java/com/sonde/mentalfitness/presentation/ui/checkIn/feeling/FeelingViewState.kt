package com.sonde.mentalfitness.presentation.ui.checkIn.feeling

sealed class FeelingViewState {


    object Loading : FeelingViewState()
    object LoadingComplete : FeelingViewState()

    data class ShowError(val messageResId: Int) : FeelingViewState()
    object ShowNoInternet : FeelingViewState()


    /*fun isLoading() = this is Loading

    fun isError() = this is Error*/
}