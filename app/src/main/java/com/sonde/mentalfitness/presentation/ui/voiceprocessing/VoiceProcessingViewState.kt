package com.sonde.mentalfitness.presentation.ui.voiceprocessing

import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.SubmitSessionModel
import java.util.*

sealed class VoiceProcessingViewState {

    data class ShowError(val messageResId: Int) : VoiceProcessingViewState()
    object ShowNoInternet : VoiceProcessingViewState()
}