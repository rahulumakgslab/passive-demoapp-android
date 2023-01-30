package com.sonde.mentalfitness.data.model.checkin.queanswers

data class QuestionnaireAnswersData(
    val id: String,
    val language: String,
    var userIdentifier: String,
    val respondedAt: String,
    val questionResponses: List<AnswerData>
)
