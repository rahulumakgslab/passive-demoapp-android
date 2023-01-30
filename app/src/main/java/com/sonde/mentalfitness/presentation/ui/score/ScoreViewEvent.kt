package com.sonde.mentalfitness.presentation.ui.score

sealed class ScoreViewEvent {

    object OnDoneClicked : ScoreViewEvent()
    object OnScoreInfoClicked : ScoreViewEvent()
}