package com.sonde.mentalfitness.domain.model.checkin.queanswers

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class AnswerModel : Parcelable {
    var optionIndex: Int? = null

    var optionIndexes: List<Int>? = null

    var textResponse: String? = null

    var isSkipped: Boolean? = null
}