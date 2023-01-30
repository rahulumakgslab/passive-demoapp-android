package com.sonde.mentalfitness.presentation.utils.util

import android.media.AudioFormat
import android.media.MediaRecorder
import com.sondeservices.edge.recorder.Configuration

const val AUDIO_SOURCE: Int = MediaRecorder.AudioSource.MIC
const val AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
const val AUDIO_BUFFER_SIZE_BYTES = 1024

fun getSondeRecorderConfig(): Configuration {
    return Configuration(
        AUDIO_SOURCE,
        getDeviceSampleRate(),
        AUDIO_CHANNEL_CONFIG,
        AUDIO_FORMAT,
        AUDIO_BUFFER_SIZE_BYTES
    ).apply {
        activateQoS = true
    }
}