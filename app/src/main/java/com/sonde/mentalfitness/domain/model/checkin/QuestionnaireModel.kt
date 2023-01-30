package com.sonde.mentalfitness.domain.model.checkin


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class QuestionnaireModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("questions")
    val questions: List<QuestionModel>,
    @SerializedName("requestId")
    val requestId: String
) : Parcelable