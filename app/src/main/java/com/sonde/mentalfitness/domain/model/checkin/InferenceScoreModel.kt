package com.sonde.mentalfitness.domain.model.checkin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InferenceScoreModel(
    val scoreId: String,
    val score: Int,
    val name: String
) : Parcelable
