package com.sonde.mentalfitness.domain.model.checkin.queanswers

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class QuestionnaireAnswersModel(
    val id : String,
    val language : String,
    val respondedAt : String,
    val questionResponses : List<AnswerModel>
) : Parcelable