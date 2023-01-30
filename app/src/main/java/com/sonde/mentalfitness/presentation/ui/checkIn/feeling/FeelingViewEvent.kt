package com.sonde.mentalfitness.presentation.ui.checkIn.feeling

import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel

sealed class FeelingViewEvent {

    data class SelectFeelingOption(val option: OptionModel, val configModel: CheckInConfigModel) : FeelingViewEvent()
    data class NavigateToReadyToRecord(
        val questionnaireAnswer: QuestionnaireAnswersModel,
        val passageModel: PassageModel
    ) :
        FeelingViewEvent()

    object NavigateToDashboard : FeelingViewEvent()

    object SkipQuestion : FeelingViewEvent()

    object HandleBackPress : FeelingViewEvent()
}