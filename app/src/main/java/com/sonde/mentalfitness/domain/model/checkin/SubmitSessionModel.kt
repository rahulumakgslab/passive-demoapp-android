package com.sonde.mentalfitness.domain.model.checkin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SubmitSessionModel(
    val passage: String,
    val inferenceScoreList: List<InferenceScoreModel>
) : Parcelable