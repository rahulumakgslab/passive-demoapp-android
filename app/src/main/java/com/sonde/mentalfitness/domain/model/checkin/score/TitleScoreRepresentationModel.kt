package com.sonde.mentalfitness.domain.model.checkin.score

data class TitleScoreRepresentationModel(
    val scoreValue: Int,
    val scoreTitle: String,
    val scoreSummary: String,
    val mainBackgroundResId: Int,
    val scoreValueBackgroundResId: Int,
)