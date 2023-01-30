package com.sonde.mentalfitness.data.model.checkin


import com.google.gson.annotations.SerializedName

data class OptionData(
    @SerializedName("text")
    val text: String,
    @SerializedName("score")
    val score: Int
)