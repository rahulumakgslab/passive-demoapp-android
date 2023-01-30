package com.sonde.mentalfitness.presentation.ui.record.readytorecord

sealed class ReadyToRecordViewState {

    object Loading : ReadyToRecordViewState()
    object LoadingComplete : ReadyToRecordViewState()

    data class ShowError(val messageResId: Int) : ReadyToRecordViewState()
    object ShowNoInternet : ReadyToRecordViewState()
}