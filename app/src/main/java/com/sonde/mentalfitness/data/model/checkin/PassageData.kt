package com.sonde.mentalfitness.data.model.checkin


import com.google.gson.annotations.SerializedName

data class PassageData(
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("timerLength")
    val timerLength: Int
)