package com.sonde.mentalfitness.data.model.checkin.queanswers

import com.google.gson.annotations.SerializedName

data class QuestionnaireAnswersRequest(
    @SerializedName("questionnaire")
    val questionnaireAnswersData: QuestionnaireAnswersData
)