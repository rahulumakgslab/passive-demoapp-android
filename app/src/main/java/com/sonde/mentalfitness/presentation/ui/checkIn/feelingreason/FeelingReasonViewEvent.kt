package com.sonde.mentalfitness.presentation.ui.checkIn.feelingreason

import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel

sealed class FeelingReasonViewEvent {

    object HandleBackPress : FeelingReasonViewEvent()

    data class NavigateToReadyToRecord(
        val questionnaireAnswersModel: QuestionnaireAnswersModel,
        val passageModel: PassageModel
    ) : FeelingReasonViewEvent()
}