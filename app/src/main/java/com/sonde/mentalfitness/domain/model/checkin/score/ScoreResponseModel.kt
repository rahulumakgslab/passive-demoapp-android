package com.sonde.mentalfitness.domain.model.checkin.score

data class ScoreResponseModel(
    val requestId: String,
    val measureId: String,
    val name: String,
    val scoreId: String,
    val score: Int
)