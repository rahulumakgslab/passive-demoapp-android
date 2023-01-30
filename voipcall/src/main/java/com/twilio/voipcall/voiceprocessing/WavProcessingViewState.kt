package com.twilio.voipcall.voiceprocessing

sealed class WavProcessingViewState {

    data class ShowError(val messageResId: Int) : WavProcessingViewState()
    object ShowNoInternet : WavProcessingViewState()
}