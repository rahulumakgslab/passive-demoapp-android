package com.sonde.mentalfitness.data.model.checkin


import com.google.gson.annotations.SerializedName

data class QuestionnaireData(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("questions")
    val questions: List<QuestionData>,
    @SerializedName("requestId")
    val requestId: String
)