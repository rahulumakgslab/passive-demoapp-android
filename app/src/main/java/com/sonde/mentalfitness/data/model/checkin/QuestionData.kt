package com.sonde.mentalfitness.data.model.checkin


import com.google.gson.annotations.SerializedName

data class QuestionData(
    @SerializedName("type")
    val type: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("isSkippable")
    val isSkippable: Boolean,
    @SerializedName("options")
    val options: List<OptionData>
)