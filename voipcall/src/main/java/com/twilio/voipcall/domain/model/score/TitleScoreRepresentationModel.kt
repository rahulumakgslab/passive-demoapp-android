package com.twilio.voipcall.domain.model.score

data class TitleScoreRepresentationModel(
    val scoreValue: Int,
    val scoreTitle: String,
    val scoreSummary: String,
    val mainBackgroundResId: Int,
    val scoreValueBackgroundResId: Int,
)