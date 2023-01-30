package com.sonde.mentalfitness.presentation.ui

sealed class MainViewEvent {
    data class PassCallEvent(val userName : String) : MainViewEvent()
}
