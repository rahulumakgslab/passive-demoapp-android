package com.twilio.voipcall.voiceprocessing

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonde.base.presentation.BaseApplication
import com.sondeservices.common.HealthCheckType
import com.sondeservices.edge.inference.InferenceCallback
import com.sondeservices.edge.inference.InferenceEngine
import com.sondeservices.edge.ml.model.Gender
import com.sondeservices.edge.ml.model.MetaData
import com.sondeservices.edge.ml.model.Score
import com.twilio.voipcall.R
import kotlinx.coroutines.launch
import java.io.File

class WavProcessingViewModel : ViewModel() {

    private val ONE_SECOND_IN_MILLIS = 1000L
    private lateinit var timer: CountDownTimer
    private val _state = MutableLiveData<WavProcessingViewState>()
    val state: LiveData<WavProcessingViewState>
        get() = _state

    private val _event = MutableLiveData<WavProcessingViewEvent>()
    val event: LiveData<WavProcessingViewEvent>
        get() = _event

    private val _voiceProcessingStatus = MutableLiveData<Int>()
    val voiceProcessingStatus: LiveData<Int> get() = _voiceProcessingStatus


    init {
        // Simulation of animation
        val stringArray = arrayOf(
            R.string.analyzing_your_voice,
            R.string.understanding_your_mental_fitness,
            R.string.checking_additional_factors,
            R.string.finalizing_your_scores
        )

        animation(stringArray)

    }

    private fun animation(stringArray: Array<Int>) {
        var count = 0
        timer = object :
            CountDownTimer(12 * ONE_SECOND_IN_MILLIS, ONE_SECOND_IN_MILLIS * 3) {
            override fun onTick(millisUntilFinished: Long) {
                _voiceProcessingStatus.value = stringArray[count]
                if (count < stringArray.size - 1) {
                    count++
                }
            }

            override fun onFinish() {

            }
        }
        timer.start()
    }

    fun calculateScore(audioFilePath: String) {
        viewModelScope.launch {
            try {
                val inference = InferenceEngine.createInference(
                    BaseApplication.applicationContext(),
                    MetaData(Gender.MALE, 2000)
                )
                inference.inferScore(
                    audioFilePath,
                    HealthCheckType.MENTAL_FITNESS,
                    object : InferenceCallback {
                        override fun onScore(score: Score) {
                            deleteFile(audioFilePath)
                            _event.value =
                                WavProcessingViewEvent.OnWavProcessSuccess(
                                    score.getValue()
                                )
                        }

                        override fun onError(throwable: Throwable) {
                            Log.e("!!", "throwable==" + throwable.toString())
                            timer.cancel()
                            _event.value = WavProcessingViewEvent.OnWavProcessError("")
                            deleteFile(audioFilePath)

                        }

                    })
            } catch (e: Exception) {

            }
        }
    }


    private fun deleteFile(filePath: String) {
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            Log.d("!!", "Error: " + e)
        }
    }

}