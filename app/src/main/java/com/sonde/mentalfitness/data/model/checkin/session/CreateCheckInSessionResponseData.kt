package com.sonde.mentalfitness.data.model.checkin.session


import com.google.gson.annotations.SerializedName

data class CreateCheckInSessionResponseData(
    @SerializedName("id")
    val id: String,
    @SerializedName("sondePlatformDetail")
    val sondePlatformDetail: SondePlatformDetailData,
    @SerializedName("requestId")
    val requestId: String
)