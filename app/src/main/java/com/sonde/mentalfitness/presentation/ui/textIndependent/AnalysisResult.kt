package com.sonde.mentalfitness.presentation.ui.textIndependent

import net.idrnd.voicesdk.media.SpeechSummary

/**
 * Class that keeps speech analysis result.
 */
data class AnalysisResult(val bytes: ByteArray, val speechSummary: SpeechSummary, val isSpeechEnded: Boolean) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnalysisResult

        if (!bytes.contentEquals(other.bytes)) return false
        if (speechSummary != other.speechSummary) return false
        if (isSpeechEnded != other.isSpeechEnded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + speechSummary.hashCode()
        result = 31 * result + isSpeechEnded.hashCode()
        return result
    }
}
