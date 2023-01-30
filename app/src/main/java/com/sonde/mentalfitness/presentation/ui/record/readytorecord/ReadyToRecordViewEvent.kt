package com.sonde.mentalfitness.presentation.ui.record.readytorecord

sealed class ReadyToRecordViewEvent {

    object HandleBackPress : ReadyToRecordViewEvent()
    object OnBeginRecordingClicked : ReadyToRecordViewEvent()
    object OnQuestionnaireAnswerSubmitted : ReadyToRecordViewEvent()

}