package com.sonde.mentalfitness.domain.model.checkin


import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PassageModel(
    val prompt: String,
    val timerLength: Int
) : Parcelable