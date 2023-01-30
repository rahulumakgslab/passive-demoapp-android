package com.sonde.mentalfitness.data.model.checkin.session


import com.google.gson.annotations.SerializedName

data class SondePlatformDetailData(
    @SerializedName("platformUrl")
    val platformUrl: String,
    @SerializedName("accessToken")
    val accessToken: String
)