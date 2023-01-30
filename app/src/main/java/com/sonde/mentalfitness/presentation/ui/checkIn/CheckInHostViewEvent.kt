package com.sonde.mentalfitness.presentation.ui.checkIn

import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel

sealed class CheckInHostViewEvent {
    object FeelingScreenEvent : CheckInHostViewEvent()

    data class ReadyToRecordEvent(
        val questionnaireAnswer: QuestionnaireAnswersModel,
        val passageModel: PassageModel
    ) : CheckInHostViewEvent()
}