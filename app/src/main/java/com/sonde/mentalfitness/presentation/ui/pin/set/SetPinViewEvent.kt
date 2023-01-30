package com.sonde.mentalfitness.presentation.ui.pin.set

sealed class SetPinViewEvent {
    object PinSetComplete : SetPinViewEvent()
    object NoPinSet : SetPinViewEvent()
}
