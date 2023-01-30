package com.twilio.voipcall.domain.model.score

data class ScoreFactorAdapterModel(
    val iconDrawableResId: Int,
    val title: String,
    val type: String,
    val typeTextColorResId: Int,
    val typeTextSizeResId: Int,
    val subTitle: String?
)