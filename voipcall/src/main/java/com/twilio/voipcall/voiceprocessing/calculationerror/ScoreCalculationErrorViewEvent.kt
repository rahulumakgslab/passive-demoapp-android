package com.twilio.voipcall.voiceprocessing.calculationerror

sealed class ScoreCalculationErrorViewEvent {

    object OnGoBackAndTryAgain : ScoreCalculationErrorViewEvent()
    object OnCancel : ScoreCalculationErrorViewEvent()
}