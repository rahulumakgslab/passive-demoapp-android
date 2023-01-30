package com.sonde.mentalfitness.domain.model.checkin.session

data class CheckInCreateSessionRequestModel(
    val deviceIdSha256: String,
    val timezone: String
)