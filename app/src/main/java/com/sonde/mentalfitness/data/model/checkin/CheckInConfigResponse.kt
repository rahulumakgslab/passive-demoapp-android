package com.sonde.mentalfitness.data.model.checkin


import com.google.gson.annotations.SerializedName

data class CheckInConfigResponse(
    @SerializedName("requestId")
    val requestId: String,
    @SerializedName("checkInInstanceId")
    val checkInInstanceId: String,
    @SerializedName("passages")
    val passages: List<PassageData>,
    @SerializedName("healthchecks")
    val healthChecks: List<HealthCheckData>,
    @SerializedName("questionnaire")
    val questionnaire: QuestionnaireData,
    @SerializedName("mainHealthcheckIndex")
    val mainHealthCheckIndex: Int
)