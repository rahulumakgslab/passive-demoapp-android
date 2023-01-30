package com.sonde.mentalfitness.domain.model.checkin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class OptionModel(
    val text: String,
    val emojiText: String,
    val score: Int,
    var isSelected : Boolean
) : Parcelable