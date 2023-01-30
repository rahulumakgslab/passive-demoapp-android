package com.twilio.voipcall.voiceprocessing

sealed class WavProcessingViewEvent {

    data class OnWavProcessError(val mainCheckInHealthCheckError: String) :
        WavProcessingViewEvent()

    object OnElckFailed : WavProcessingViewEvent()

    data class OnWavProcessSuccess(
        val score: Int
    ) : WavProcessingViewEvent()
}