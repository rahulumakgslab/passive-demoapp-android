package com.sonde.mentalfitness.presentation.ui.splash

sealed class SplashViewEvent {
    object ShowVerifyPin : SplashViewEvent()
    object NoVerifyPin : SplashViewEvent()
    object ShowSetPin : SplashViewEvent()
    object UserNotLoggedIn : SplashViewEvent()
}
