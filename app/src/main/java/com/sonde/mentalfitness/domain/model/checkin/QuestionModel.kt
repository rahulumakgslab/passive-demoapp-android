package com.sonde.mentalfitness.domain.model.checkin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionModel(
    val type: String,
    val text: String,
    val isSkippable: Boolean,
    val options: List<OptionModel>
) : Parcelable