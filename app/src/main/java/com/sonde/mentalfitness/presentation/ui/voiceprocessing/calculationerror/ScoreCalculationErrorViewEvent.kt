package com.sonde.mentalfitness.presentation.ui.voiceprocessing.calculationerror

sealed class ScoreCalculationErrorViewEvent {

    object OnGoBackAndTryAgain : ScoreCalculationErrorViewEvent()
    object OnCancel : ScoreCalculationErrorViewEvent()
}