package com.sonde.mentalfitness.domain.model.checkin.score

data class ScoreFactorAdapterModel(
    val iconDrawableResId: Int,
    val title: String,
    val type: String,
    val typeTextColorResId: Int,
    val typeTextSizeResId: Int,
    val subTitle: String?
)