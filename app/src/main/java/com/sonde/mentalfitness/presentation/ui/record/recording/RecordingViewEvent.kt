package com.sonde.mentalfitness.presentation.ui.record.recording

import com.sonde.mentalfitness.domain.model.checkin.PassageModel

sealed class RecordingViewEvent {

    object OnRecordingInterrupted : RecordingViewEvent()
    object OnCancelClicked : RecordingViewEvent()
    data class OnRecordingFinished(val audioFilePath: String, val passageModel: PassageModel) :
        RecordingViewEvent()

}