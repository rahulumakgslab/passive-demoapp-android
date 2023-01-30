package com.sonde.mentalfitness.data.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("requestId")
    val requestId: String,
    @SerializedName("message")
    var message: String
)