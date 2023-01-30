package com.sonde.mentalfitness.presentation.utils.util

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import com.sonde.mentalfitness.MentalFitnessApplication

const val DEFAULT_SAMPLE_RATE_HZ = 44100

fun getDeviceSampleRate(): Int {
    val audioManager =
        MentalFitnessApplication.applicationContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return try {
        val rate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        rate.toInt()
    } catch (e: Exception) {
        DEFAULT_SAMPLE_RATE_HZ // Default value
    }
}

fun getDeviceId(): String {
    return Settings.Secure.getString(
        MentalFitnessApplication.applicationContext().getContentResolver(),
        Settings.Secure.ANDROID_ID
    )
}