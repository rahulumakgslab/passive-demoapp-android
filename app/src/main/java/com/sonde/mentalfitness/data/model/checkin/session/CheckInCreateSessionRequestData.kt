package com.sonde.mentalfitness.data.model.checkin.session

data class CheckInCreateSessionRequestData(
    private val deviceIdSha256: String,
    private val timezone: String
)