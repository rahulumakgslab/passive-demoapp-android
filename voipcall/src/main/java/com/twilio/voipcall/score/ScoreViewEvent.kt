package com.twilio.voipcall.score

sealed class ScoreViewEvent {

    object OnDoneClicked : ScoreViewEvent()
    object OnScoreInfoClicked : ScoreViewEvent()
}