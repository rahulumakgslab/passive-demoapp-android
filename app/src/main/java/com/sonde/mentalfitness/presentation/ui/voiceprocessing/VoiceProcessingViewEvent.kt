package com.sonde.mentalfitness.presentation.ui.voiceprocessing

import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.SubmitSessionModel
import com.sondeservices.edge.ml.model.VFFinalScore
import java.util.*

sealed class VoiceProcessingViewEvent {

    data class OnVoiceProcessError(val mainCheckInHealthCheckError: String) :
        VoiceProcessingViewEvent()

    object OnElckFailed : VoiceProcessingViewEvent()

    data class OnVoiceProcessSuccess(
        val sessionData: SubmitSessionModel,
        val selectedFeelingAnswerOption: OptionModel?,
        val selectedFeelingReasonList: ArrayList<OptionModel>?
    ) : VoiceProcessingViewEvent()

    data class OnMFScoreSuccess(
        val sessionData: SubmitSessionModel,
        val vfFinalScore: VFFinalScore
    ) : VoiceProcessingViewEvent()
}