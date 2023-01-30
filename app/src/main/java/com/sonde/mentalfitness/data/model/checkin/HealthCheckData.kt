package com.sonde.mentalfitness.data.model.checkin


import com.google.gson.annotations.SerializedName

data class HealthCheckData(
    @SerializedName("name")
    val name: String,
    @SerializedName("sondeplatformName")
    val sondePlatformName: String
)